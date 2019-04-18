package fr.inria.diverse.ale.repl.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecoretools.ale.ALEInterpreter;
import org.eclipse.emf.ecoretools.ale.core.parser.Dsl;
import org.eclipse.emf.ecoretools.ale.core.parser.DslBuilder;
import org.eclipse.emf.ecoretools.ale.core.parser.visitor.ParseResult;
import org.eclipse.emf.ecoretools.ale.implementation.ExtendedClass;
import org.eclipse.emf.ecoretools.ale.implementation.ModelUnit;
import org.eclipse.emf.ecoretools.ale.implementation.impl.MethodImpl;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

public class AbstractSyntaxGenerator {
	
	private String ecorePath;
	private String alePath;
	
	private ResourceSet resourceSet;
	
	
	public AbstractSyntaxGenerator(String ecorePath, String alePath) {
		this.ecorePath = ecorePath;
		this.alePath = alePath;
	}
	
	
	/**
	 * Create a modeling project with the specified name
	 * 
	 * Remove any existing project with the same name
	 * and open the project in the current workspace after it creation
	 * @param projectName the name of the project
	 * @return the created project
	 */
	public IProject createProject(String projectName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		IProject project = workspaceRoot.getProject(projectName);
		
		// Delete any existing project
		if (project.exists()) {
			try {
				project.delete(true, null);
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		// Create and return the new project
		try {
			project.create(null);
			project.open(null);
			
			IProjectDescription projectDescription = project.getDescription();
			projectDescription.setNatureIds(new String[] {"org.eclipse.sirius.nature.modelingproject",
					"org.eclipse.jdt.core.javanature", "org.eclipse.pde.PluginNature",
					"org.eclipse.gemoc.execution.sequential.javaxdsml.ide.ui.GemocSequentialLanguageNature"});
			project.setDescription(projectDescription, null);
			
			IFolder modelFolder = project.getFolder("model");
			modelFolder.create(false, true, null);	
			IFolder srcgenFolder = project.getFolder("src-gen");
			srcgenFolder.create(false, true, null);
			IFolder binFolder = project.getFolder("bin");
			binFolder.create(false, true, null);
			
			IJavaProject javaProject = JavaCore.create(project);
			
		    List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		    IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		    LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		    for (LibraryLocation element : locations) {
		        entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		    } 
		    entries.add(JavaCore.newSourceEntry(javaProject.getPackageFragmentRoot(srcgenFolder).getPath()));
		    javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			
			javaProject.setOutputLocation(binFolder.getFullPath(), null);
			
			return project;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * Generate a ecore file for REPL execution based on the one referenced by this instance
	 * 
	 * The necessary data is extracted from the ale file referenced in this instance
	 * @param projectName the project in which to create the file
	 * @param languageName the name of the language defined in the file
	 * @return a URI of the generated file
	 */
	public URI generateEcore(String projectName, String languageName) {
		URI ecoreUri = URI.createFileURI(ecorePath);
		
		ALEInterpreter interpreter = new ALEInterpreter();
		Dsl environment = new Dsl(Arrays.asList(ecoreUri.toString()), Arrays.asList(alePath));	
		
		this.resourceSet = new ResourceSetImpl();
		this.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		
		// Load the referenced ecore file
		Resource ecoreResource = this.resourceSet.getResource(ecoreUri, true);
		List<ParseResult<ModelUnit>> parsedSemantics = new DslBuilder(interpreter.getQueryEnvironment(),
				this.resourceSet)
				.parse(environment);
		
		// List methods annotated with the `@repl` tag in the referenced ale file
		List<MethodImpl> steps = parsedSemantics.get(0).getRoot().eContents().stream()
			.flatMap(obj -> obj.eContents().stream())
			.filter(met -> (met instanceof MethodImpl)
					&& ((MethodImpl) met).getTags().stream().anyMatch(t -> t.startsWith("repl")))
			.map(met -> (MethodImpl) met).collect(Collectors.toList());
		
		EPackage rootPackage = (EPackage) ecoreResource.getContents().get(0);
		
		EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;
	
		// Create the InterpretableInstruction interface
		EClass interpretableInstructionClass = ecoreFactory.createEClass();
		interpretableInstructionClass.setName("InterpretableInstruction");
		interpretableInstructionClass.setAbstract(true);
		interpretableInstructionClass.setInterface(true);
		rootPackage.getEClassifiers().add(interpretableInstructionClass);
		
		// Create the Interpreter class
		EClass interpreterClass = ecoreFactory.createEClass();
		interpreterClass.setName("Interpreter");
		EReference currentInstructionReference = ecoreFactory.createEReference();
		currentInstructionReference.setName("instruction");
		currentInstructionReference.setEType(interpretableInstructionClass);
		currentInstructionReference.setContainment(false);
		interpreterClass.getEStructuralFeatures().add(currentInstructionReference);
		rootPackage.getEClassifiers().add(interpreterClass);
		
		// For each repl method, add InterpretableInstruction as super type
		for (MethodImpl step : steps) {
			EClass baseClass = ((ExtendedClass) step.eContainer()).getBaseClass();
			ecoreResource.getAllContents().forEachRemaining(el -> {
				if (el instanceof EClass) {
					EClass actualClass = (EClass) el;
					if (actualClass.getName().equals(baseClass.getName()) &&
							actualClass.getEPackage().getNsPrefix().equals(baseClass.getEPackage()
									.getNsPrefix())) {						
						if (!actualClass.getESuperTypes().contains(interpretableInstructionClass)) {
							actualClass.getESuperTypes().add(interpretableInstructionClass);
						}
					}
				}
			});
		}
		
		// Add `/repl` to the Ns URI of every package
		ecoreResource.getAllContents().forEachRemaining(el -> {
			if (el instanceof EPackage) {
				EPackage pkg = (EPackage) el;
				String splitted[] = pkg.getNsURI().split("/");
				splitted[2] = splitted[2] + "/repl";
				pkg.setNsURI(String.join("/", splitted));
			}
		});
		
		// Save the newly created ecore file
		try {
			ecoreResource.setURI(URI.createURI("platform:/resource/" + projectName + "/model/"
					+ languageName + ".ecore", true));
			ecoreResource.save(null);
			return ecoreResource.getURI();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Create a genmodel file for the specified ecore file
	 * @param languageName the name of the language defined in the file
	 * @param projectName the project in which to create the file
	 * @param ecoreUri the uri of the ecore file
	 * @return the uri of the generated file
	 */
	public URI generateGenmodel(String languageName, String projectName, URI ecoreUri) {
		GenModel genmodel = GenModelFactory.eINSTANCE.createGenModel();
		genmodel.setComplianceLevel(GenJDKLevel.JDK80_LITERAL);
		genmodel.setModelDirectory("/" + projectName + "/src-gen");
		genmodel.getForeignModel().add(ecoreUri.toString());
		genmodel.setModelName(languageName);
		genmodel.setModelPluginID(projectName);
		genmodel.initialize(this.resourceSet.getResource(ecoreUri, true).getContents().stream()
				.map(p -> (EPackage) p).collect(Collectors.toList()));
		genmodel.getGenPackages().stream()
			.forEach(pkg -> pkg.setBasePackage(projectName.replace("." + languageName, "")));
		Resource genmodelRes = this.resourceSet.createResource(URI.createURI("platform:/resource/" + projectName
				+ "/model/" + languageName + ".genmodel"));
		genmodelRes.getContents().add(genmodel);
		try {
			genmodelRes.save(null);
			return genmodelRes.getURI();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Generate model code for the specified genmodel file
	 * @param genmodelUri the uri of the genmodel file
	 */
	public void generateModelCode(URI genmodelUri) {
		GenModel genmodel = (GenModel) this.resourceSet.getResource(genmodelUri, true).getContents().get(0);
		Generator generator = new Generator();
		generator.requestInitialize();
		generator.setInput(genmodel);
		genmodel.setCanGenerate(true);
		generator.generate(genmodel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE,
				new BasicMonitor.Printing(System.out));
	}
	
	
	/**
	 * Register in the package registry the Ns URIs defined in the specified ecore file
	 * @param ecoreUri the uri of the ecore file
	 */
	public void registerNsUris(URI ecoreUri) {
		Resource ecoreResource = this.resourceSet.getResource(ecoreUri, true);
		ecoreResource.getAllContents().forEachRemaining(el -> {
			if (el instanceof EPackage) {
				String nsUri = (String) el.eGet(el.eClass().getEStructuralFeature("nsURI"));
				EPackage.Registry.INSTANCE.put(nsUri, el);
			}
		});
	}
		
}
