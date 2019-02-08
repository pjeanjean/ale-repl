package fr.inria.diverse.ale.repl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecoretools.ale.ALEInterpreter;
import org.eclipse.emf.ecoretools.ale.core.parser.Dsl;
import org.eclipse.emf.ecoretools.ale.core.parser.DslBuilder;
import org.eclipse.emf.ecoretools.ale.core.parser.visitor.ParseResult;
import org.eclipse.emf.ecoretools.ale.implementation.ExtendedClass;
import org.eclipse.emf.ecoretools.ale.implementation.Method;
import org.eclipse.emf.ecoretools.ale.implementation.ModelUnit;
import org.eclipse.sirius.common.tools.api.resource.ResourceSetFactory;

public class REPLInterpreter {
	
	private String ecorePath;
	private String alePath;
	private URI modelUri;
	
	private ALEInterpreter interpreter;
	
	private String output;
	private String errors;
	
	private String completeModel;
	
	private ResourceSetFactory resourceSetFactory;
	private ResourceSet mainResourceSet;
	private Resource mainResource;
	private List<ParseResult<ModelUnit>> parsedSemantics;
	
	
	public REPLInterpreter(String ecorePath, String alePath, String xtextExtension) {
		this.ecorePath = ecorePath;
		this.alePath = alePath;
		this.modelUri = URI.createURI("dummy:/read." + xtextExtension);
		
		this.output = "";
		this.errors = "";
		
		this.completeModel = "";
		
		this.init();
	}
	
	
	public String getOutput() {
		return this.output;
	}
	
	
	public String getErrors() {
		return this.errors;
	}
	
	
	private void init() {		
		this.interpreter = new ALEInterpreter();
		
		Dsl environment = new Dsl(Arrays.asList(URI.createFileURI(ecorePath).toString()), Arrays.asList(alePath));
		
		// Factory to get resource sets for the models
		this.resourceSetFactory = ResourceSetFactory.createFactory();
		this.mainResourceSet = this.resourceSetFactory.createResourceSet(this.modelUri);
		
		this.mainResource = this.mainResourceSet.createResource(this.modelUri);
		try {
			// Load an empty model to initialize the engine
			this.mainResource.load(new ByteArrayInputStream(new byte[] {}), this.mainResourceSet.getLoadOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		EObject caller = this.mainResource.getContents().get(0);
		
		this.parsedSemantics = new DslBuilder(this.interpreter.getQueryEnvironment(), this.mainResourceSet)
				.parse(environment);
		
		// Search the root class in the parsed semantics
		List<ExtendedClass> classes = this.parsedSemantics.stream().map(p -> p.getRoot()).filter(e -> e != null)
				.flatMap(unit -> unit.getClassExtensions().stream())
				.filter(ext -> ext.getBaseClass().getName().equals(caller.eClass().getName()))
				.collect(Collectors.toList());
		
		// Get the 'init' tagged method
		Optional<Method> init = classes.stream().flatMap(cls -> cls.getMethods().stream())
				.filter(mtd -> mtd.getTags().contains("init")).findFirst();

		// If not found, get the 'init' tagged method from parents
		List<ExtendedClass> classExtends = classes;
		while (!init.isPresent() && classExtends.size() > 0) {
			classExtends = classExtends.stream().flatMap(cls -> cls.getExtends().stream())
					.collect(Collectors.toList());
			if (!init.isPresent()) {
				init = classExtends.stream().flatMap(cls -> cls.getMethods().stream())
						.filter(mtd -> mtd.getTags().contains("init")).findFirst();
			}
		}
		
		// Eval the init method
		this.interpreter.eval(caller, init.get(), Arrays.asList(), this.parsedSemantics);
	}
	
	
	public boolean interpret(String model) {
		this.output = "";
		this.errors = "";
		
		// New resource set for the model to interpret
		ResourceSet resourceSet = this.resourceSetFactory.createResourceSet(this.modelUri);
		
		this.completeModel += "~ " + model + "\n";
		Resource resource = resourceSet.createResource(this.modelUri);
		try {
			// Load the complete model after adding the model given as parameter
			resource.load(new ByteArrayInputStream((this.completeModel).getBytes()), resourceSet.getLoadOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		
		// Print parsing errors and exit if any
		if (resource.getErrors().size() > 0) {
			for (Diagnostic error : resource.getErrors()) {				
				this.errors += error;
			}
			return false;
		}
			
		IComparisonScope scope = new DefaultComparisonScope(this.mainResourceSet, resourceSet, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		
		List<Diff> differences = comparison.getDifferences();
		
		IMerger.Registry mergerRegistry = IMerger.RegistryImpl.createStandaloneInstance();
		IBatchMerger merger = new BatchMerger(mergerRegistry);
		
		// Merge the diff between the two models in the main resource set
		merger.copyAllRightToLeft(differences, new BasicMonitor());
				
		// Add the new instruction to the main resource
		EObject caller = this.mainResource.getContents().get(0);
		
		// Search the root class in the parsed semantics
		List<ExtendedClass> classes = this.parsedSemantics.stream().map(p -> p.getRoot()).filter(e -> e != null)
				.flatMap(unit -> unit.getClassExtensions().stream())
				.filter(ext -> ext.getBaseClass().getName().equals(caller.eClass().getName()))
				.collect(Collectors.toList());
				
		// Get the 'main' tagged method
		Optional<Method> main = classes.stream().flatMap(cls -> cls.getMethods().stream())
				.filter(mtd -> mtd.getTags().contains("main")).findFirst();
	
		// If not found, get the 'main' tagged method from parents
		List<ExtendedClass> classExtends = classes;
		while (!main.isPresent() && classExtends.size() > 0) {
			classExtends = classExtends.stream().flatMap(cls -> cls.getExtends().stream())
					.collect(Collectors.toList());
			if (!main.isPresent()) {
			main = classExtends.stream().flatMap(cls -> cls.getMethods().stream())
					.filter(mtd -> mtd.getTags().contains("main")).findFirst();
			}
		}
		
		PrintStream stdOut = System.out;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));
		
		// Eval the main method
		this.interpreter.getCurrentEngine().eval(caller, main.get(), Arrays.asList());
		this.output = outputStream.toString().trim();
		
		System.out.flush();
		System.setOut(stdOut);
		
		return true;
	}

}
