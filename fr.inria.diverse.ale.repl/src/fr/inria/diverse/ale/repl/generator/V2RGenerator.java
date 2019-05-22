package fr.inria.diverse.ale.repl.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecoretools.AleRuntimeModule;
import org.eclipse.emf.ecoretools.ale.ALEInterpreter;
import org.eclipse.emf.ecoretools.ale.AleFactory;
import org.eclipse.emf.ecoretools.ale.Attribute;
import org.eclipse.emf.ecoretools.ale.BehavioredClass;
import org.eclipse.emf.ecoretools.ale.Call;
import org.eclipse.emf.ecoretools.ale.ClassifierType;
import org.eclipse.emf.ecoretools.ale.Operation;
import org.eclipse.emf.ecoretools.ale.Unit;
import org.eclipse.emf.ecoretools.ale.core.parser.Dsl;
import org.eclipse.emf.ecoretools.ale.core.parser.DslBuilder;
import org.eclipse.emf.ecoretools.ale.core.parser.visitor.ParseResult;
import org.eclipse.emf.ecoretools.ale.implementation.ExtendedClass;
import org.eclipse.emf.ecoretools.ale.implementation.ModelUnit;
import org.eclipse.emf.ecoretools.ale.implementation.impl.MethodImpl;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.inria.diverse.ale.repl.Visitor2replRuntimeModule;
import fr.inria.diverse.ale.repl.visitor2repl.Instruction;
import fr.inria.diverse.ale.repl.visitor2repl.Interpreter;
import fr.inria.diverse.ale.repl.visitor2repl.InterpreterAttributeRef;
import fr.inria.diverse.ale.repl.visitor2repl.Model;
import fr.inria.diverse.ale.repl.visitor2repl.OutputRef;
import fr.inria.diverse.ale.repl.visitor2repl.Transformation;
import fr.inria.diverse.ale.repl.visitor2repl.Visitor2replFactory;

public class V2RGenerator {

	private String languageName;
	private String ecorePath;
	private String alePath;
	
	
	public V2RGenerator(String languageName, String ecorePath, String alePath) {
		this.languageName = languageName;
		this.ecorePath = ecorePath;
		this.alePath = alePath;
	}
	
	public URI generateV2R(String v2rPath) {
		URI ecoreUri = URI.createFileURI(ecorePath);
		URI aleUri = URI.createFileURI(alePath);
		
		ALEInterpreter interpreter = new ALEInterpreter();
		Dsl environment = new Dsl(Arrays.asList(ecoreUri.toString()), Arrays.asList(alePath));	
		
		XtextResourceSet resourceSet =
				Guice.createInjector(new AleRuntimeModule()).getInstance(XtextResourceSet.class);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		
		// Load the referenced ecore and ale files
		List<ParseResult<ModelUnit>> parsedSemantics = new DslBuilder(interpreter.getQueryEnvironment(),
				resourceSet).parse(environment);
		Resource ecoreResource = resourceSet.getResource(ecoreUri, true);
		Resource aleResource = resourceSet.getResource(aleUri, true);
		
		Unit unit = (Unit) aleResource.getContents().get(0);
		
		// List methods annotated with the `@repl` tag in the referenced ale file
		List<MethodImpl> steps = parsedSemantics.get(0).getRoot().eContents().stream()
			.flatMap(obj -> obj.eContents().stream())
			.filter(met -> (met instanceof MethodImpl)
					&& ((MethodImpl) met).getTags().stream().anyMatch(t -> t.startsWith("repl")))
			.map(met -> (MethodImpl) met).collect(Collectors.toList());
		
		// Store all ecore packages
		List<EPackage> ecorePackages = new ArrayList<>();
		ecoreResource.getAllContents().forEachRemaining(el -> {
			if (el instanceof EPackage) {
				ecorePackages.add((EPackage) el);
			}
		});
		Model v2rModel = Visitor2replFactory.eINSTANCE.createModel();
		v2rModel.getPackages().addAll(ecorePackages);
			
		// Find the class that contains the init method
		BehavioredClass entryPointClass = unit.getXtendedClasses().stream()
				.filter(c -> c.getOperations().stream()
						.anyMatch(o -> o.getTag().stream()
								.anyMatch(t -> t.getName().equals("init")))).findFirst().get();
		
		// Store attributes and init method of entrypoint
		Operation initMethod = entryPointClass.getOperations().stream()
				.filter(m -> m.getTag().stream().anyMatch(t -> t.getName().equals("init")))
				.findFirst().get();
		Operation initMethodCopy = EcoreUtil.copy(initMethod);
		initMethodCopy.getTag().clear();
		List<Attribute> attributes = entryPointClass.getAttributes();
		Interpreter v2rInterpreter = Visitor2replFactory.eINSTANCE.createInterpreter();
		v2rInterpreter.setName(this.languageName + "_repl");
		v2rInterpreter.getAttributes().addAll(EcoreUtil.copyAll(attributes));
		v2rInterpreter.setInitMethod(initMethodCopy);
		
		List<Instruction> instructions = new ArrayList<>();
		steps.stream().forEach(m -> {
			Instruction v2rInstruction = Visitor2replFactory.eINSTANCE.createInstruction();
			EClassifier aleEcoreClass = ((ExtendedClass) m.eContainer()).getBaseClass();
			EClassifier actualEcoreClass = ecorePackages.stream()
					.filter(p -> p.getNsPrefix().equals(aleEcoreClass.getEPackage().getNsPrefix())).findFirst()
					.get().getEClassifier(aleEcoreClass.getName());
			v2rInstruction.setClassifier(actualEcoreClass);
			v2rInstruction.setEvalMethod(m.getOperationRef().getName());
			Map<String, InterpreterAttributeRef> actualParameters = new HashMap<>();
			// Find parameters from the attributes of Interpreter
			for (EParameter formalParameter : m.getOperationRef().getEParameters()) {
				Attribute attribute = v2rInterpreter.getAttributes().stream()
						.filter(a -> ((ClassifierType) a.getType()).getClassName()
								.equals(formalParameter.getEType().getName()))
						.findFirst().get();
				InterpreterAttributeRef v2rAttributeRef =
						Visitor2replFactory.eINSTANCE.createInterpreterAttributeRef();
				v2rAttributeRef.setTarget(v2rInterpreter);
				v2rAttributeRef.setFeature(attribute);
				v2rInstruction.getEvalParams().add(v2rAttributeRef);
				actualParameters.put(formalParameter.getName(), v2rAttributeRef);
			}
			instructions.add(v2rInstruction);
			
			String splittedTag[] = m.getTags().stream()
					.filter(t -> t.startsWith("repl")).findFirst().get().split("_");
			
			if (splittedTag.length > 1) {
				Call call = AleFactory.eINSTANCE.createCall();
				call.setName(splittedTag[2]);
				if (splittedTag[1].equals("output")) {
					OutputRef outputRef = Visitor2replFactory.eINSTANCE.createOutputRef();
					call.setTarget(outputRef);
				} else if (actualParameters.containsKey(splittedTag[1])) {
					call.setTarget(EcoreUtil.copy(actualParameters.get(splittedTag[1])));
				}
				v2rInstruction.setEvalResult(call);
			}
		});
		
		Injector injector = Guice.createInjector(new Visitor2replRuntimeModule());
		if (!resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey("v2r")) {
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("v2r",
					injector.getInstance(IResourceFactory.class));
		}
		if (!IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("v2r")) {
			IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put("v2r",
					injector.getInstance(IResourceServiceProvider.class));
		}
		
		Resource v2rResource =
				resourceSet.createResource(URI.createFileURI(v2rPath));
		
		Transformation v2rTransformation = Visitor2replFactory.eINSTANCE.createTransformation();
		v2rTransformation.setInterpreter(v2rInterpreter);
		v2rTransformation.setModel(v2rModel);
		v2rTransformation.getInstructions().addAll(instructions);
		v2rResource.getContents().add(v2rTransformation);
		
		try {
			v2rResource.save(null);
			ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(v2rPath))
					.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (IOException | CoreException e) {
			e.printStackTrace();
		}
		
		return v2rResource.getURI();
	}
	
}
