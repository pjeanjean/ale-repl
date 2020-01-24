package fr.inria.diverse.ale.repl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
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
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gemoc.executionframework.engine.core.CommandExecution;
import org.eclipse.gemoc.trace.commons.model.trace.GenericMSE;
import org.eclipse.gemoc.trace.commons.model.trace.MSE;
import org.eclipse.gemoc.trace.commons.model.trace.MSEModel;
import org.eclipse.gemoc.trace.commons.model.trace.TraceFactory;
import org.eclipse.sirius.common.tools.api.resource.ResourceSetFactory;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.ChunkedResourceDescriptions;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;

public class REPLInterpreter {
	
	private Dsl environment;
	private String xtextExtension;
	private EObject caller;
	
	private ALEInterpreter interpreter;
	
	private LinkedList<String> modelHistory;
	private LinkedList<String> outputHistory;
	
	private String output;
	private String errors;
	
	private ResourceSetFactory resourceSetFactory;
	private ResourceSet mainResourceSet;
	private Resource mainResource;
	private List<ParseResult<ModelUnit>> parsedSemantics;
	
	private Pattern outputHistoryPattern;
	
	
	public REPLInterpreter(Dsl environment, String xtextExtension) {		
		this.environment = environment;
		this.xtextExtension = xtextExtension;
		
		this.output = "";
		this.errors = "";
		
		this.modelHistory = new LinkedList<>();
		this.outputHistory = new LinkedList<>();
		
		this.outputHistoryPattern = Pattern.compile("^([0-9]+).*$");
		
		this.init();
	}
	
	
	public REPLInterpreter(String ecorePath, String alePath, String xtextExtension) {
		this(new Dsl(Arrays.asList(URI.createFileURI(ecorePath).toString()),
				Arrays.asList(alePath)), xtextExtension);	
	}
	
	
	public String getModelHistory(int index) {
		return this.modelHistory.get(index);
	}
	
	
	public int getModelHistorySize() {
		return this.modelHistory.size();
	}
	
	
	public String getOutput() {
		return this.output;
	}
	
	
	public String getErrors() {
		return this.errors;
	}
	
	//private GenericTraceEngineAddon traceAddon;
	
	private void init() {		
		this.interpreter = new ALEInterpreter();
		
		URI modelUri = URI.createURI("dummy:/interpreter." + this.xtextExtension);
		
		// Factory to get resource sets for the models
		this.resourceSetFactory = ResourceSetFactory.createFactory();
		this.mainResourceSet = this.resourceSetFactory.createResourceSet(modelUri);
		this.mainResourceSet.getLoadOptions().put("org.eclipse.xtext.scoping.LIVE_SCOPE", false);
		this.mainResourceSet.eAdapters()
				.add(new ChunkedResourceDescriptions.ChunkedResourceDescriptionsAdapter(
						new ChunkedResourceDescriptions()));

		this.mainResource = this.mainResourceSet.createResource(modelUri);
		try {
			// Load an empty model to initialize the engine
			this.mainResource.load(new ByteArrayInputStream(new byte[] {}), this.mainResourceSet.getLoadOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		this.caller = this.mainResource.getContents().get(0);
		this.parsedSemantics = new DslBuilder(this.interpreter.getQueryEnvironment(), this.mainResourceSet)
				.parse(this.environment);

		TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(this.mainResourceSet);
		
	/*	this.traceAddon = new GenericTraceEngineAddon();
		
		this.traceAddon.setDynamicPartAccessor(new AleDynamicAccessor(interpreter, 
				this.parsedSemantics.stream().map(p -> p.getRoot())
						.filter(elem -> elem != null).collect(Collectors.toList())));
		this.traceAddon.initEngine(this.mainResource);
		
		if (this.interpreter != null && this.parsedSemantics != null) {
			this.interpreter.addListener(new ServiceCallListener() {
				private Deque<Step<?>> steps = new ArrayDeque<>();
				
				@Override
				public void preCall(IService service, Object[] arguments) {
					if(service instanceof EvalBodyService) {
						boolean isStep = ((EvalBodyService)service).getImplem().getTags().contains("step");
						if(isStep) {
							//System.out.println("STEP IN");
							if (arguments[0] instanceof EObject) {
								EObject currentCaller = (EObject) arguments[0];
								String className = currentCaller.eClass().getName();
								String methodName = service.getName();
								MSE mse = findOrCreateMSE(caller, className, methodName);
								Step<?> step = traceAddon.getFactory().createStep(mse, new ArrayList<>(), new ArrayList<>());
								traceAddon.aboutToExecuteStep(null, step);
								this.steps.push(step);
								traceAddon.getTraceConstructor().save();
							}
						}
					}
				}
				
				@Override
				public void postCall(IService service, Object[] arg1, Object arg2) {
					if(service instanceof EvalBodyService) {
						boolean isStep = ((EvalBodyService)service).getImplem().getTags().contains("step");
						if(isStep) {
							traceAddon.stepExecuted(null, this.steps.pop());
						}
					}
				}
			});
		}*/
		
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
		
		this.modelHistory.addFirst(model);
		
		String splittedModel[] = model.split("\\$");
		for (int i = 1; i < splittedModel.length; i++) {
			Matcher matcher = this.outputHistoryPattern.matcher(splittedModel[i]);
			if (matcher.find()) {				
				int wantedOutput = Integer.parseInt(matcher.group(1));
				if (wantedOutput > 0 && wantedOutput <= this.outputHistory.size()) {
					model = model.replace("$" + wantedOutput, this.outputHistory.get(wantedOutput - 1));
				}
			}
		}
		
		// New resource set for the model to interpret
		URI modelUri = URI.createURI("dummy:/instruction." + this.xtextExtension);
		
		Resource resource = this.mainResourceSet.createResource(modelUri);
		try {
			// Load the complete model after adding the model given as parameter
			resource.load(new ByteArrayInputStream(model.getBytes()), this.mainResourceSet.getLoadOptions());
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		
		// Print parsing errors before resolution and exit if any
		if (resource.getErrors().size() > 0) {
			for (Diagnostic error : resource.getErrors()) {				
				this.errors += error;
			}
			this.mainResourceSet.getResources().remove(resource);
			return false;
		}
		
		// Check that the parsed model is actually an instruction
		if (!resource.getContents().get(0).eClass().getEAllSuperTypes()
				.stream().anyMatch(c -> c.getName().equals("InterpretableInstruction"))) {
			this.mainResourceSet.getResources().remove(resource);
			return false;
		}
		
		EObject newInstruction = resource.getContents().get(0);
		
		// Set history before resolving proxies
		EStructuralFeature instructionFeature = this.caller.eClass().getEStructuralFeature("instruction");
		EStructuralFeature previousFeature = newInstruction.eClass().getEStructuralFeature("previous");
		if (newInstruction != null) {
			TransactionalEditingDomain ed = TransactionUtil.getEditingDomain(this.mainResourceSet);
			RecordingCommand command = new RecordingCommand(ed, "") {
				@Override
				protected void doExecute() {
					try {
						newInstruction.eSet(previousFeature, caller.eGet(instructionFeature));
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			};
			CommandExecution.execute(ed, command);
		}
		
		EcoreUtil2.resolveAll(resource);
		
		// Print parsing errors after resolution and exit if any
		if (resource.getErrors().size() > 0) {
			for (Diagnostic error : resource.getErrors()) {				
				this.errors += error;
			}
			this.mainResourceSet.getResources().remove(resource);
			return false;
		}
		
		// Validate the resource
		List<Issue> validationErrors = ((XtextResource) resource).getResourceServiceProvider()
				.getResourceValidator().validate(resource, CheckMode.ALL, null).stream()
				.filter(i -> i.getSeverity().equals(Severity.ERROR)).collect(Collectors.toList());
					
		// Print validation errors if any
		if (!validationErrors.isEmpty()) {
			for (Issue issue : validationErrors) {
				if (!this.errors.contains(issue.getMessage()))
					this.errors += issue.getMessage();
			}
			this.mainResourceSet.getResources().remove(resource);
			return false;
		}
		
		// Get the caller from the main resource
		TransactionalEditingDomain ed = TransactionUtil.getEditingDomain(this.mainResourceSet);
		RecordingCommand command = new RecordingCommand(ed, "") {
			@Override
			protected void doExecute() {
				try {
					caller.eSet(instructionFeature, resource.getContents().get(0));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
		CommandExecution.execute(ed, command);
		
		// Search the root class in the parsed semantics
		List<ExtendedClass> classes = this.parsedSemantics.stream().map(p -> p.getRoot()).filter(e -> e != null)
				.flatMap(unit -> unit.getClassExtensions().stream())
				.filter(ext -> ext.getBaseClass().getName().equals(this.caller.eClass().getName()))
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
		this.interpreter.getCurrentEngine().eval(this.caller, main.get(), Arrays.asList());
		this.output = outputStream.toString().trim();
		this.outputHistory.addFirst(this.output);
		
		this.mainResourceSet.getResources().remove(resource);
		System.out.flush();
		System.setOut(stdOut);
		
		return true;
	}


	private EOperation findOperation(EObject object, String className, String methodName) {
		// We try to find the corresponding EOperation in the execution
		// metamodel
		for (EOperation operation : object.eClass().getEAllOperations()) {
			// TODO !!! this is not super correct yet as overloading allows the
			// definition of 2 methods with the same name !!!
			if (operation.getName().equalsIgnoreCase(methodName)) {
				return operation;
			}
		}

		// If we didn't find it, we try to find the class that should contain
		// this operation
		EClass containingEClass = null;
		if (object.eClass().getName().equalsIgnoreCase(className)) {
			containingEClass = object.eClass();
		} else {
			for (EClass candidate : object.eClass().getEAllSuperTypes()) {
				if (candidate.getName().equalsIgnoreCase(className)) {
					containingEClass = candidate;
				}
			}
		}

		// Then we create the missing operation (VERY approximatively)
		EOperation operation = EcoreFactory.eINSTANCE.createEOperation();
		if (containingEClass != null) {
			containingEClass.getEOperations().add(operation);
		}
		operation.setName(methodName);
		return operation;
	}

	private MSEModel _actionModel;
	
	/**
	 * Find the MSE element for the triplet caller/className/MethodName in the model
	 * of precalculated possible MSE. If it doesn't exist yet, create one and add it
	 * to the model.
	 * 
	 * @param caller
	 *            the caller object
	 * @param className
	 *            the class containing the method
	 * @param methodName
	 *            the name of the method
	 * @return the retrieved or created MSE
	 */
	public final MSE findOrCreateMSE(EObject caller, String className, String methodName) {
		EOperation operation = findOperation(caller, className, methodName);
		// TODO Should be created/loaded before execution by analyzing the
		// model?
		if (_actionModel == null) {
			_actionModel = TraceFactory.eINSTANCE.createMSEModel();
		}

		if (_actionModel != null) {
			for (MSE existingMSE : _actionModel.getOwnedMSEs()) {
				if (existingMSE.getCaller().equals(caller) && ((existingMSE.getAction() != null && existingMSE.getAction().equals(operation)) || (existingMSE.getAction() == null && operation == null))) {
					// no need to create one, we already have it
					return existingMSE;
				}
			}
		}
		// let's create a MSE
		final GenericMSE mse = TraceFactory.eINSTANCE.createGenericMSE();
		mse.setCallerReference(caller);
		mse.setActionReference(operation);
		if (operation != null)
			mse.setName("MSE_" + caller.getClass().getSimpleName() + "_" + operation.getName());
		else
			mse.setName("MSE_" + caller.getClass().getSimpleName() + "_" + methodName);
		// and add it for possible reuse
		if (_actionModel != null) {

			if (_actionModel.eResource() != null) {
				TransactionUtil.getEditingDomain(_actionModel.eResource());
				RecordingCommand command = new RecordingCommand(TransactionUtil.getEditingDomain(_actionModel.eResource()), "Saving new MSE ") {
					@Override
					protected void doExecute() {
						_actionModel.getOwnedMSEs().add(mse);
						try {
							_actionModel.eResource().save(null);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				TransactionUtil.getEditingDomain(_actionModel.eResource()).getCommandStack().execute(command);
			}
		} else {
			_actionModel.getOwnedMSEs().add(mse);
		}
		return mse;
	}
	
}
