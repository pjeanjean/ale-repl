package fr.inria.diverse.ale.repl.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecoretools.AleRuntimeModule;
import org.eclipse.emf.ecoretools.ale.AleFactory;
import org.eclipse.emf.ecoretools.ale.Attribute;
import org.eclipse.emf.ecoretools.ale.BehavioredClass;
import org.eclipse.emf.ecoretools.ale.Block;
import org.eclipse.emf.ecoretools.ale.Call;
import org.eclipse.emf.ecoretools.ale.ClassifierType;
import org.eclipse.emf.ecoretools.ale.Comp;
import org.eclipse.emf.ecoretools.ale.ExpressionStmt;
import org.eclipse.emf.ecoretools.ale.ExtendedClass;
import org.eclipse.emf.ecoretools.ale.Feature;
import org.eclipse.emf.ecoretools.ale.If;
import org.eclipse.emf.ecoretools.ale.Lit;
import org.eclipse.emf.ecoretools.ale.Operation;
import org.eclipse.emf.ecoretools.ale.Tag;
import org.eclipse.emf.ecoretools.ale.Unit;
import org.eclipse.emf.ecoretools.ale.VarDecl;
import org.eclipse.emf.ecoretools.ale.VarRef;
import org.eclipse.emf.ecoretools.ale.Variable;
import org.eclipse.emf.ecoretools.ale.rType;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Guice;

public class SemanticGenerator {
	
	private String alePath;
	
	
	public SemanticGenerator(String alePath) {
		this.alePath = alePath;
	}
	
	
	/**
	 * Create a dsl file for the specified language
	 * 
	 * The ale file is supposed to be in the same place and have the same name as the ecore file
	 * @param projectName the project in which to create the file
	 * @param languageName the name of the language
	 * @param ecoreUri the URI to the ecore file
	 */
	public void createDsl(String projectName, String languageName, URI ecoreUri) {
		String dslContent =
				"name=" + languageName.substring(0, 1).toUpperCase() + languageName.substring(1) + "\n"
				+ "ecore=" + ecoreUri + "\n"
				+ "ale=" + URI.createURI(ecoreUri.toString().replaceAll("ecore$", "ale"));
		
		try {
			Files.write(new File(ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/" + projectName
					+ "/model/" + languageName + ".dsl").toPath(), dslContent.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ResourcesPlugin.getWorkspace().getRoot().getProject(projectName)
					.refreshLocal(IProject.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Generate a ale file for REPL execution based on the one referenced by this instance
	 * 
	 * The file will be created in the same folder and with the same name as the ecore file
	 * @param ecoreUri the ecore file that defines the language
	 * @return the URI of the newly created ale file
	 */
	public URI generateAle(URI ecoreUri) {
		XtextResourceSet rs = Guice.createInjector(new AleRuntimeModule()).getInstance(XtextResourceSet.class);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		// Copy the existing ale file
		try {
			Files.copy(new File(this.alePath).toPath(),
					new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
							+ ecoreUri.toPlatformString(true).replaceAll("ecore$", "ale")).toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Resource aleResource = rs.getResource(URI.createURI(ecoreUri.toString().replaceAll("ecore$", "ale")),
				true);
		
		Unit unit = (Unit) aleResource.getContents().get(0);
		
		// Find the class that contains the init method
		BehavioredClass entryPointClass = unit.getXtendedClasses().stream()
				.filter(c -> c.getOperations().stream()
						.anyMatch(o -> o.getTag().stream()
								.anyMatch(t -> t.getName().equals("init")))).findFirst().get();
		
		// Interpreter class
		ExtendedClass interpreterClass = AleFactory.eINSTANCE.createExtendedClass();
		
		Operation mainOperation = AleFactory.eINSTANCE.createOperation();
		mainOperation.setName("run");
		interpreterClass.getOperations().add(mainOperation);
		
		Tag mainOperationTag = AleFactory.eINSTANCE.createTag();
		mainOperationTag.setName("main");
		mainOperation.getTag().add(mainOperationTag);
		
		rType mainOperationType = AleFactory.eINSTANCE.createrType();
		mainOperationType.setName("void");
		mainOperation.setType(mainOperationType);
		
		Block mainOperationBlock = AleFactory.eINSTANCE.createBlock();
		mainOperation.setBody(mainOperationBlock);
		
		ExpressionStmt mainOperationStatement = AleFactory.eINSTANCE.createExpressionStmt();
		mainOperationBlock.getStatements().add(mainOperationStatement);
		
		Call mainOperationStatementCall = AleFactory.eINSTANCE.createCall();
		mainOperationStatementCall.setName("interpret");
		mainOperationStatement.setExp(mainOperationStatementCall);
		
		Feature mainOperationStatementCallFeature = AleFactory.eINSTANCE.createFeature();
		mainOperationStatementCallFeature.setFeature("instruction");
		mainOperationStatementCall.setTarget(mainOperationStatementCallFeature);
		
		VarRef mainOperationStatementCallFeatureVar = AleFactory.eINSTANCE.createVarRef();
		mainOperationStatementCallFeatureVar.setID("self");
		mainOperationStatementCallFeature.setTarget(mainOperationStatementCallFeatureVar);
		
		VarRef mainOperationStatementCallVar = AleFactory.eINSTANCE.createVarRef();
		mainOperationStatementCallVar.setID("self");
		mainOperationStatementCall.getParams().add(mainOperationStatementCallVar);
	
		// Copy attributes and init method from the entry point to Interpreter
		if (entryPointClass != null) {
			interpreterClass.getAttributes().addAll(EcoreUtil.copyAll(entryPointClass.getAttributes()));
			interpreterClass.getOperations().add(EcoreUtil.copy(entryPointClass.getOperations().stream()
					.filter(op -> op.getTag().stream()
							.anyMatch(t -> t.getName().equals("init"))).findFirst().get()));
		}
		
		interpreterClass.setName("Interpreter");
		
		unit.getXtendedClasses().add(0, interpreterClass);
		
		Map<Operation, BehavioredClass> replOperations = new HashMap<>();
		
		// Create a interpret method for every class that has a @repl annotated method 
		unit.getXtendedClasses().stream()
				.flatMap(c -> c.getOperations().stream())
				.filter(o -> o.getTag().stream().anyMatch(t -> t.getName().startsWith("repl")))
				.forEach(o -> {
					Tag replTag = o.getTag().stream()
							.filter(t -> t.getName().startsWith("repl")).findFirst().get();
					String splittedTag[] = replTag.getName().split("_");
					Operation replOperation = AleFactory.eINSTANCE.createOperation();
					replOperation.setName("interpret");
					Tag stepTag = AleFactory.eINSTANCE.createTag();
					stepTag.setName("step");
					replOperation.getTag().add(stepTag);
					rType voidType = AleFactory.eINSTANCE.createrType();
					voidType.setName("void");
					replOperation.setType(voidType);
					rType interpreterType = AleFactory.eINSTANCE.createrType();
					interpreterType.setName("Interpreter");
					Variable interpreterVar = AleFactory.eINSTANCE.createVariable();
					interpreterVar.setName("interpreter");
					interpreterVar.setType(interpreterType);
					replOperation.getParams().add(interpreterVar);
					Block block = AleFactory.eINSTANCE.createBlock();
					replOperation.setBody(block);
					replOperations.put(replOperation, (BehavioredClass) o.eContainer());
					Call call = AleFactory.eINSTANCE.createCall();
					call.setName(o.getName());
					VarRef selfVar = AleFactory.eINSTANCE.createVarRef();
					selfVar.setID("self");
					call.setTarget(selfVar);
					ExpressionStmt statement = AleFactory.eINSTANCE.createExpressionStmt();
					statement.setExp(call);
					
					Map<String, Feature> actualParameters = new HashMap<>();
					// Find parameters from the attributes of Interpreter
					for (Variable formalParameter : o.getParams()) {
						Feature feature = AleFactory.eINSTANCE.createFeature();
						Attribute attribute = interpreterClass.getAttributes().stream()
								.filter(a -> {
									rType t = a.getType();
									if (t instanceof ClassifierType) {
										return ((ClassifierType) t).getClassName()
												.equals(formalParameter.getType().getName());
									} else {
										return t.getName().equals(formalParameter.getName());
									}
								}).findFirst().get();
						feature.setFeature(attribute.getName());
						VarRef interpreterVarRef = AleFactory.eINSTANCE.createVarRef();
						interpreterVarRef.setID("interpreter");
						feature.setTarget(interpreterVarRef);
						call.getParams().add(feature);
						actualParameters.put(formalParameter.getName(), feature);
					}
					
					// Manage the output of the instructions
					if (splittedTag.length == 1) {
						block.getStatements().add(statement);
					} else {
						if (splittedTag[1].equals("output")) {
							VarDecl outputVarDecl = AleFactory.eINSTANCE.createVarDecl();
							ClassifierType outputVarDeclType = AleFactory.eINSTANCE.createClassifierType();
							outputVarDeclType.setClassName("EObject");
							outputVarDeclType.setPackageName("ecore");
							outputVarDecl.setType(outputVarDeclType);
							outputVarDecl.setName("output");
							outputVarDecl.setExp(statement);
							block.getStatements().add(outputVarDecl);
						} else {
							block.getStatements().add(statement);
						}
						If ifStatement = AleFactory.eINSTANCE.createIf();
						block.getStatements().add(ifStatement);
						ExpressionStmt ifStatementCondition = AleFactory.eINSTANCE.createExpressionStmt();
						ifStatement.setCond(ifStatementCondition);
						Comp ifStatementComparison = AleFactory.eINSTANCE.createComp();
						ifStatementCondition.setExp(ifStatementComparison);
						ifStatementComparison.setOp("=");
						if (actualParameters.containsKey(splittedTag[1])) {
							ifStatementComparison.setLeft(
									EcoreUtil.copy(actualParameters.get(splittedTag[1])));
						} else {
							VarRef ifStatementComparisonLeft = AleFactory.eINSTANCE.createVarRef();
							ifStatementComparisonLeft.setID(splittedTag[1]);
							ifStatementComparison.setLeft(ifStatementComparisonLeft);
						}
						Lit ifStatementComparisonRight = AleFactory.eINSTANCE.createLit();
						ifStatementComparisonRight.setLiteral(AleFactory.eINSTANCE.createNull());
						ifStatementComparison.setRight(ifStatementComparisonRight);
						
						Block ifThenBlock = AleFactory.eINSTANCE.createBlock();
						ifStatement.setThen(ifThenBlock);
						// TODO: Fix `'null'.log();` not serializing
					/*	ExpressionStmt ifThenStatement = AleFactory.eINSTANCE.createExpressionStmt();
						ifThenBlock.getStatements().add(ifThenStatement);
						Call ifThenCall = AleFactory.eINSTANCE.createCall();
						ifThenStatement.setExp(ifThenCall);
						ifThenCall.setName("log");
						Lit ifThenCallTarget = AleFactory.eINSTANCE.createLit();
						ifThenCall.setTarget(ifThenCallTarget);
						org.eclipse.emf.ecoretools.ale.String ifThenCallTargetLitteral =
								AleFactory.eINSTANCE.createString();
						ifThenCallTarget.setLiteral(ifThenCallTargetLitteral);
						ifThenCallTargetLitteral.setValue("null");
					*/
						Block ifElseBlock = AleFactory.eINSTANCE.createBlock();
						ifStatement.setElse(ifElseBlock);
						ExpressionStmt ifElseStatement = AleFactory.eINSTANCE.createExpressionStmt();
						ifElseBlock.getStatements().add(ifElseStatement);
						Call ifElseCall = AleFactory.eINSTANCE.createCall();
						ifElseStatement.setExp(ifElseCall);
						ifElseCall.setName("log");
						Call ifElseCallTarget = AleFactory.eINSTANCE.createCall();
						ifElseCall.setTarget(ifElseCallTarget);
						ifElseCallTarget.setName(splittedTag[2]);
						if (actualParameters.containsKey(splittedTag[1])) {
							ifElseCallTarget.setTarget(
									EcoreUtil.copy(actualParameters.get(splittedTag[1])));
						} else {
							VarRef ifElseCallTargetTarget = AleFactory.eINSTANCE.createVarRef();
							ifElseCallTarget.setTarget(ifElseCallTargetTarget);
							ifElseCallTargetTarget.setID(splittedTag[1]);
						}
					}
				});
		
		// Add the created interpret methods
		for (Map.Entry<Operation, BehavioredClass> replOperation : replOperations.entrySet()) {
			replOperation.getValue().getOperations().add(replOperation.getKey());
		}
		
		// Save the created resource
		try {
			aleResource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return aleResource.getURI();
	}

}
