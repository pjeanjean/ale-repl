package fr.inria.diverse.ale.repl.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.mwe2.launch.ui.shortcut.Mwe2LaunchShortcut;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;
import org.eclipse.pde.internal.ui.wizards.tools.UpdateClasspathJob;
import org.eclipse.xtext.AbstractMetamodelDeclaration;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.ReferencedMetamodel;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.TypeRef;
import org.eclipse.xtext.XtextFactory;
import org.eclipse.xtext.XtextRuntimeModule;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor.FileExtensions;
import org.eclipse.xtext.xtext.wizard.ProjectLayout;
import org.eclipse.xtext.xtext.wizard.WizardConfiguration;
import org.eclipse.xtext.xtext.wizard.cli.CliProjectsCreator;

import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConcreteSyntaxGenerator {
	
	private String xtextPath;

	
	public ConcreteSyntaxGenerator(String xtextPath) {
		this.xtextPath = xtextPath;
	}
	
	
	/**
	 * Create a Xtext project and its subprojects for the specified language
	 * 
	 * Delete any existing project and open all the newly created ones
	 * @param modelProjectName the model project that defines the language
	 * @param projectName the name of the newly created project
	 * @param languageName the name of the language
	 * @return the newly created main xtext project
	 */
	public IProject createProject(String modelProjectName, String projectName, String languageName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String projectNames[] = new String[] {projectName, projectName + ".ide", projectName + ".ui"};
		
		// Delete any existing project
		IProject project;
		try {
			for (String currProjectName : projectNames) {
				project = workspaceRoot.getProject(currProjectName);
				if (project.exists()) {
					project.delete(true, null);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		
		File targetLocation = new File(workspaceRoot.getLocation().toString());
		
		// Create the projects using Xtext projects creator
		WizardConfiguration conf = new WizardConfiguration();
		conf.setRootLocation(targetLocation.getPath());
		conf.getLanguage().setName(projectName + "." + languageName.substring(0, 1).toUpperCase()
				+ languageName.substring(1));
		conf.getLanguage().setFileExtensions(FileExtensions.fromString(languageName));
		conf.setXtextVersion(XtextVersion.getCurrent());
		conf.setEncoding(Charsets.UTF_8);
		conf.setBaseName(projectName);
		conf.setProjectLayout(ProjectLayout.FLAT);
		conf.getIdeProject().setEnabled(true);
		conf.getUiProject().setEnabled(true);
		
		CliProjectsCreator projectsCreator = new CliProjectsCreator();
		projectsCreator.setLineDelimiter("\n");
		projectsCreator.createProjects(conf);
		
		List<WorkspacePluginModel> pluginModels = new ArrayList<WorkspacePluginModel>();
		
		try {
			// Convert the projects to Eclipse projects and open them
			for (String currProjectName : projectNames) {				
				project = workspaceRoot.getProject(currProjectName);
				if (!project.exists()) {
					IProjectDescription projectDescription = workspaceRoot.getWorkspace()
							.newProjectDescription(currProjectName);
					projectDescription.setLocation(workspaceRoot.getLocation().append("/" + currProjectName));
					projectDescription.setNatureIds(new String[] {XtextProjectHelper.NATURE_ID,
							JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature"});
					String[] builders = new String[]{JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder",
							"org.eclipse.pde.SchemaBuilder", XtextProjectHelper.BUILDER_ID};
					ICommand commands[] = new ICommand[builders.length];
					for (int i = 0; i < builders.length; i++) {
						ICommand command = projectDescription.newCommand();
						command.setBuilderName(builders[i]);
						commands[i] = command;
					}
					projectDescription.setBuildSpec(commands);
				
					project.create(projectDescription, null);
				}
				if (!project.isOpen()) {
					project.open(null);
					JavaCore.create(project).setRawClasspath(new ClasspathEntry[0], null);
					pluginModels.add(new WorkspacePluginModel(project.getFile("META-INF/MANIFEST.MF"), false));
				}
			}
			
			// Add dependency to model project
			try {
				IFile manifestFile = pluginModels.get(0).getFile();
				Manifest manifest = new Manifest(manifestFile.getContents());
				Attributes.Name requireBundle = new Attributes.Name("Require-Bundle");
				manifest.getMainAttributes().put(requireBundle,
						manifest.getMainAttributes().get(requireBundle) + "," + modelProjectName);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				manifest.write(outputStream);
				manifestFile.setContents(new ByteArrayInputStream(outputStream.toByteArray()), true, false, null);
			} catch (IOException e) {
				e.printStackTrace();
			}	
			
			// Update the classpath for all the projects
			Job updateClasspath = new UpdateClasspathJob(pluginModels.toArray(new WorkspacePluginModel[0]));
			updateClasspath.schedule();
			try {
				updateClasspath.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return workspaceRoot.getProject(projectName);
	}
	
	
	/**
	 * Generate a xtext file for REPL execution based on the one referenced by this instance
	 * @param projectName the project in which to create the file
	 * @param ecoreUri the URI to the ecore file defining the language
	 * @param languageName the name of the language
	 * @return the URI of the newly created file
	 */
	public URI createGrammar(String projectName, URI ecoreUri, String languageName) {
		Injector injector = Guice.createInjector(new XtextRuntimeModule());
		XtextResourceSet rs = injector.getInstance(XtextResourceSet.class);
		
		if (!rs.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey("ecore")) {
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("ecore", new EcoreResourceFactoryImpl());
		}
		if (!rs.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey("xtext")) {
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("xtext", injector.getInstance(IResourceFactory.class));
		}
		if (!IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("xtext")) {
			IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap()
					.put("xtext", injector.getInstance(IResourceServiceProvider.class));
		}
		
		Resource ecoreResource = rs.getResource(ecoreUri, true);
		Resource resource = rs.getResource(URI.createFileURI(this.xtextPath), true);
		
		Grammar eRoot = (Grammar) resource.getContents().get(0);
		EList<AbstractRule> rules = eRoot.getRules();
		
		EPackage ecoreRoot = (EPackage) ecoreResource.getContents().get(0);
		String rootPackagePrefix = (String) ecoreRoot.eGet(ecoreRoot.eClass().getEStructuralFeature("nsPrefix"));
		
		AbstractMetamodelDeclaration rootPackageXText = null;
		EList<AbstractMetamodelDeclaration> metamodelDeclarations = eRoot.getMetamodelDeclarations();
		
		// Change the name of the grammar
		eRoot.setName(projectName + "." + languageName.substring(0, 1).toUpperCase() + languageName.substring(1));
		
		// Replace (if possible) the packages referenced in the xtext file by the corresponding ones from
		//   the ecore file and also do it for the classifiers in these packages
		ecoreResource.getAllContents().forEachRemaining(el -> {
			if (el instanceof EPackage) {
				EPackage pkg = (EPackage) el;
				for (AbstractMetamodelDeclaration rm : metamodelDeclarations) {
					EPackage pkg2 = rm.getEPackage();
					if (pkg.getNsPrefix().equals(pkg2.getNsPrefix())) {
						rm.setEPackage(pkg);
						resource.getAllContents().forEachRemaining(el2 -> {
							if (el2 instanceof TypeRef) {
								TypeRef type = (TypeRef) el2;
								if (type.getMetamodel() == rm) {
									EClassifier oldClassifier = type.getClassifier();
									String classifierName = oldClassifier.getName();
									type.setClassifier(pkg.getEClassifier(classifierName));
								}
							}
						});
					}
				}
			}
		});
		
		// Find the root package from the ones defined in the xtext file
		for (AbstractMetamodelDeclaration rm : metamodelDeclarations) {
			EPackage pkg = rm.getEPackage();
			if (pkg.getNsPrefix().equals(rootPackagePrefix)) {
				rootPackageXText = rm;
				break;
			}
		}
		
		Optional<AbstractMetamodelDeclaration> oEcoreMetamodel = eRoot.getMetamodelDeclarations().stream()
				.filter(m -> (m.getAlias() != null) && m.getAlias().equals("ecore")).findFirst();
		AbstractMetamodelDeclaration ecoreMetamodel;
		if (oEcoreMetamodel.isPresent()) {
			ecoreMetamodel = oEcoreMetamodel.get();
		} else {
			ecoreMetamodel = XtextFactory.eINSTANCE.createReferencedMetamodel();
			ecoreMetamodel.setEPackage(EcorePackage.eINSTANCE);
			ecoreMetamodel.setAlias("ecore");
			eRoot.getMetamodelDeclarations().add(ecoreMetamodel);
		}
		EClass eObjectClass = (EClass) ecoreMetamodel.getEPackage().getEClassifier("EObject");
		
		// EntryPoint rule
		TypeRef entryPointType = XtextFactory.eINSTANCE.createTypeRef();
		entryPointType.setMetamodel(ecoreMetamodel);
		entryPointType.setClassifier(eObjectClass);
		
		ParserRule entryPointRule = XtextFactory.eINSTANCE.createParserRule();
		entryPointRule.setName("EntryPoint");
		entryPointRule.setType(entryPointType);
		entryPointRule.getHiddenTokens();
		rules.add(0, entryPointRule);
		
		Alternatives entryPointAlternatives = XtextFactory.eINSTANCE.createAlternatives();
		entryPointRule.setAlternatives(entryPointAlternatives);
		
		// InterpretableInstruction rule
		EClass interpretableInstructionClass = (EClass) ecoreRoot.getEClassifier("InterpretableInstruction");
		
		TypeRef interpretableInstructionType = XtextFactory.eINSTANCE.createTypeRef();
		interpretableInstructionType.setMetamodel(rootPackageXText);
		interpretableInstructionType.setClassifier(interpretableInstructionClass);
		
		ParserRule interpretableInstructionRule = XtextFactory.eINSTANCE.createParserRule();
		interpretableInstructionRule.setName("InterpretableInstruction");
		interpretableInstructionRule.setType(interpretableInstructionType);
		interpretableInstructionRule.getHiddenTokens();
		
		Alternatives interpretableInstructionAlternatives = XtextFactory.eINSTANCE.createAlternatives();
		rules.stream().forEach(rule -> {
			// Find all the rules for interpretable instructions and set priority using `->` operator
			TypeRef type = rule.getType();
			if (type != null && type.getClassifier() instanceof EClass
					&& ((EClass) type.getClassifier()).getESuperTypes() != null
				    && ((EClass) type.getClassifier()).getESuperTypes().contains(interpretableInstructionClass)) {
				RuleCall ruleCall = XtextFactory.eINSTANCE.createRuleCall();
				ruleCall.eSet(ruleCall.eClass().getEStructuralFeature("rule"), rule);
				ruleCall.setFirstSetPredicated(true);
				interpretableInstructionAlternatives.getElements().add(ruleCall);
			}
		});
		
		// Delete rules from the alternatives if they are children of other rules
		this.deleteChildren(interpretableInstructionAlternatives);
		
		interpretableInstructionRule.setAlternatives(interpretableInstructionAlternatives);
		rules.add(1, interpretableInstructionRule);
		
		RuleCall interpretableInstructionRuleCall = XtextFactory.eINSTANCE.createRuleCall();
		interpretableInstructionRuleCall.setRule(interpretableInstructionRule);
		entryPointAlternatives.getElements().add(interpretableInstructionRuleCall);
		
		// Interpreter rule
		EClass interpreterClass = (EClass) ecoreRoot.getEClassifier("Interpreter");
		
		TypeRef interpreterType = XtextFactory.eINSTANCE.createTypeRef();
		interpreterType.setMetamodel(rootPackageXText);
		interpreterType.setClassifier(interpreterClass);
				
		ParserRule interpreterRule = XtextFactory.eINSTANCE.createParserRule();
		interpreterRule.setName("Interpreter");
		interpreterRule.setType(interpreterType);
		interpreterRule.getHiddenTokens();
		
		Action interpreterAction = XtextFactory.eINSTANCE.createAction();
		interpreterAction.setType(EcoreUtil.copy(interpreterType));
		interpreterRule.setAlternatives(interpreterAction);
		
		rules.add(2, interpreterRule);
		
		RuleCall interpreterRuleCall = XtextFactory.eINSTANCE.createRuleCall();
		interpreterRuleCall.setRule(interpreterRule);
		entryPointAlternatives.getElements().add(interpreterRuleCall);
		
		// Create resource for new xtext file
		Resource newResource = rs.createResource(URI.createURI("platform:/resource/" + projectName
				+ "/src/" + projectName.replace(".", "/")
				+ "/" + languageName.substring(0, 1).toUpperCase() + languageName.substring(1) + ".xtext"));
		Grammar newRoot = (Grammar) EcoreUtil.copy(resource.getContents().get(0));
		newResource.getContents().add(newRoot);
		
		// Replace the URI of referenced packages to file URIs
		List<String> referencedResources = new ArrayList<>();
		for (AbstractMetamodelDeclaration rm : newRoot.getMetamodelDeclarations()) {
			EPackage pkg = (EPackage) rm.getEPackage();
			String pkgPath = "";
			EPackage pkgTemp = pkg;
			while (pkgTemp.eContainer() != null) {
				pkgPath = "/" + pkgTemp.getName() + pkgPath;
				pkgTemp = (EPackage) pkgTemp.eContainer();
			}
			String filePath = rs.getResource(URI.createURI(pkg.getNsURI()), true).getURI().toPlatformString(true);
			if (filePath != null) {
				filePath = ResourcesPlugin.getWorkspace().getRoot().getLocation() + filePath;
				if (!referencedResources.contains(filePath)) {
					referencedResources.add(filePath);
				}
				pkg.setNsURI("file:" + filePath + "#/" + pkgPath);
			}
		}
		
		// Add referencedResource to mwe2 file
		try {
			File mweFile = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()
					+ "/" + projectName + "/src/" + projectName.replace(".", "/") + "/Generate"
					+ languageName.substring(0, 1).toUpperCase() + languageName.substring(1) + ".mwe2");
			List<String> mweLines = Files.readAllLines(mweFile.toPath());
			for (String referencedResource : referencedResources) {
				mweLines.add(mweLines.size() - 3, "\t\t\treferencedResource = \"file:"
						+ referencedResource.replace(".ecore", ".genmodel") + "\"");
			}
			Files.write(mweFile.toPath(), mweLines);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Save the xtext resource
		try {
			newResource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newResource.getURI();
	}
			
	
	/**
	 * Generate a grammar by running a MWE2 workflow
	 * 
	 * Also set global scoping to ResourceSetGlobalScoping
	 * @param projectName the project in which the grammar is defined
	 * @param languageName the name of the language that the grammar defines
	 */
	public void generateGrammar(String projectName, String languageName) {
		// Launch the MWE2 workflow
		new Mwe2LaunchShortcut().launch(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(projectName + "/src/" + projectName.replace(".", "/") + "/Generate"
						+ languageName.substring(0, 1).toUpperCase() + languageName.substring(1) + ".mwe2"))),
				"run");
		ILaunchConfiguration launchConf = DebugUITools.getLastLaunch("org.eclipse.debug.ui.launchGroup.run");
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new ILaunchesListener2() {
			@Override
			public void launchesRemoved(ILaunch[] launches) {}
			
			@Override
			public void launchesChanged(ILaunch[] launches) {}
			
			@Override
			public void launchesAdded(ILaunch[] launches) {}
			
			@Override
			public void launchesTerminated(ILaunch[] launches) {
				// Set the global scoping after the workflow ends
				for (ILaunch launch : launches) {
					if (launch.getLaunchConfiguration().equals(launchConf)) {
						try {
							File moduleFile = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()
									+ "/" + projectName + "/src/" + projectName.replace(".", "/")
									+ "/" + languageName.substring(0, 1).toUpperCase() + languageName.substring(1)
									+ "RuntimeModule.xtend");
							List<String> moduleLines = Files.readAllLines(moduleFile.toPath());
							moduleLines.add(moduleLines.size() - 1, "\toverride bindIGlobalScopeProvider() { " + 
									"return org.eclipse.xtext.scoping.impl.ResourceSetGlobalScopeProvider " + 
									"}");
							Files.write(moduleFile.toPath(), moduleLines);
						} catch (IOException e) {
							e.printStackTrace();
						}
						DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
					}
				}
			}
		});
	}

		
	private void deleteChildren(Alternatives alternatives) {
		for (int i = 0; i < alternatives.getElements().size(); i++) {
			RuleCall rc1 = (RuleCall) alternatives.getElements().get(i);
			for (int j = 0; j < alternatives.getElements().size(); j++) {
				if (i != j) {
					RuleCall rc2 = (RuleCall) alternatives.getElements().get(j);
					if (rc1.getRule().getType().getClassifier().equals(rc2.getRule().getType().getClassifier())
							&& this.isChild(rc1, rc2, new ArrayList<AbstractRule>())) {
						alternatives.getElements().remove(j);
						j--;
					}
				}
			}
		}
	}
	
	
	private boolean isChild(RuleCall rc1, RuleCall rc2, List<AbstractRule> visited) {
		boolean child = false;
		if (rc1.getRule().equals(rc2.getRule())) {
			return true;
		} else if (rc1.getRule().getAlternatives() instanceof Alternatives) {
			Alternatives rc1Alternatives = (Alternatives) rc1.getRule().getAlternatives();
			for (int i = 0; i < rc1Alternatives.getElements().size(); i++) {
				if (rc1Alternatives.getElements().get(i) instanceof RuleCall) {
					if (!visited.contains(((RuleCall) rc1Alternatives.getElements().get(i)).getRule())) {
						visited.add(((RuleCall) rc1Alternatives.getElements().get(i)).getRule());
						child |= this.isChild((RuleCall) rc1Alternatives.getElements().get(i), rc2, visited);
					}
				} else if (rc1Alternatives.getElements().get(i) instanceof Group) {
					child |= this.isChild((Group) rc1Alternatives.getElements().get(i), rc2, visited);
				}
			}
		} else if (rc1.getRule().getAlternatives() instanceof RuleCall) {
			if (!visited.contains(((RuleCall) rc1.getRule().getAlternatives()).getRule())) {
				visited.add(((RuleCall) rc1.getRule().getAlternatives()).getRule());
				return this.isChild((RuleCall) rc1.getRule().getAlternatives(), rc2, visited);
			}
		} else if (rc1.getRule().getAlternatives() instanceof Group) {
			return this.isChild((Group) rc1.getRule().getAlternatives(), rc2, visited);
		}
		return child;
	}
	
	
	private boolean isChild(Group group, RuleCall rc, List<AbstractRule> visited) {
		boolean child = false;
		for (int i = 0; i < group.getElements().size(); i++) {
			if (group.getElements().get(i) instanceof RuleCall) {
				if (!visited.contains(((RuleCall) group.getElements().get(i)).getRule())) {
					visited.add(((RuleCall) group.getElements().get(i)).getRule());
					child |= this.isChild((RuleCall) group.getElements().get(i), rc, visited);
				}
			} else if (group.getElements().get(i) instanceof Group) {
				child |= this.isChild((Group) group.getElements().get(i), rc, visited);
			}
		}
		return child;
	}

}
