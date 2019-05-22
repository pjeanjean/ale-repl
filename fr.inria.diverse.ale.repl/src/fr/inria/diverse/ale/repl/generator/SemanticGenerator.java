package fr.inria.diverse.ale.repl.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecoretools.AleRuntimeModule;
import org.eclipse.emf.ecoretools.ale.AleFactory;
import org.eclipse.emf.ecoretools.ale.BehavioredClass;
import org.eclipse.emf.ecoretools.ale.Block;
import org.eclipse.emf.ecoretools.ale.Call;
import org.eclipse.emf.ecoretools.ale.ClassifierType;
import org.eclipse.emf.ecoretools.ale.Comp;
import org.eclipse.emf.ecoretools.ale.Expression;
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
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.inria.diverse.ale.repl.Visitor2replRuntimeModule;
import fr.inria.diverse.ale.repl.visitor2repl.InterpreterAttributeRef;
import fr.inria.diverse.ale.repl.visitor2repl.OutputRef;
import fr.inria.diverse.ale.repl.visitor2repl.SelfRef;
import fr.inria.diverse.ale.repl.visitor2repl.Transformation;

public class SemanticGenerator {
	
	private String alePath;
	private String v2rPath;
	
	
	public SemanticGenerator(String alePath, String v2rPath) {
		this.alePath = alePath;
		this.v2rPath = v2rPath;
	}
	
	
	/**
	 * Create a dsl file for the specified language
	 * 
	 * The ale file is supposed to be in the same place and have the same name as the first ecore file
	 * @param projectName the project in which to create the file
	 * @param languageName the name of the language
	 * @param ecoreUri the URI to the ecore files
	 * 
	 */
	public void createDsl(String projectName, String languageName, URI... ecoreUri) {
		String dslContent =
				"name=" + languageName.substring(0, 1).toUpperCase() + languageName.substring(1) + "\n"
				+ "ecore=" + ecoreUri[0];
		for (int i = 1; i < ecoreUri.length; i++) {
			dslContent += ("," + ecoreUri[i]);
		}
		dslContent += "\n"
				+ "ale=" + URI.createURI(ecoreUri.toString().replaceAll("ecore$", "ale"));
		
		IFile dslFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(ecoreUri[0].toPlatformString(true).replaceAll("ecore$", "dsl")));
		try {
			Files.write(new File(dslFile.getLocation().toOSString()).toPath(), dslContent.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			dslFile.refreshLocal(IProject.DEPTH_INFINITE, null);
			IProjectDescription description = dslFile.getProject().getDescription();
			String oldNatures[] = description.getNatureIds();
			String newNatures[] = new String[oldNatures.length + 1];
			System.arraycopy(oldNatures, 0, newNatures, 0, oldNatures.length);
			newNatures[oldNatures.length] = 
					"org.eclipse.gemoc.execution.sequential.javaxdsml.ide.ui.GemocSequentialLanguageNature";
			description.setNatureIds(newNatures);
			dslFile.getProject().setDescription(description, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean needsOutput;
	
	/**
	 * Generate a ale file for REPL execution based on the one referenced by this instance
	 * 
	 * The file will be created in the same folder and with the same name as the first ecore file
	 * @param ecoreUri the ecore files that defines the language
	 * @return the URI of the newly created ale file
	 */
	public URI generateAle(URI... ecoreUri) {
		XtextResourceSet rs = Guice.createInjector(new AleRuntimeModule()).getInstance(XtextResourceSet.class);
		rs.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		
		Injector v2rInjector = Guice.createInjector(new Visitor2replRuntimeModule());
		if (!rs.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey("v2r")) {
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("v2r", v2rInjector.getInstance(IResourceFactory.class));
		}
		if (!IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("v2r")) {
			IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap()
					.put("v2r", v2rInjector.getInstance(IResourceServiceProvider.class));
		}
		
		// Copy the existing ale file
		try {
			Files.copy(new File(this.alePath).toPath(),
					new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
							+ ecoreUri[0].toPlatformString(true).replaceAll("ecore$", "ale")).toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for (URI uri : ecoreUri) {
			rs.getResource(uri, true);
		}
		
		Resource aleResource = rs.getResource(URI.createURI(ecoreUri.toString().replaceAll("ecore$", "ale")),
				true);
		Unit unit = (Unit) aleResource.getContents().get(0);
		
		// Remove main and init tags for all existing operations
		unit.eAllContents().forEachRemaining(e -> {
			if (e instanceof Operation) {
				Operation o = (Operation) e;
				for (int i = 0; i < o.getTag().size(); i++) {
					Tag t = o.getTag().get(i);
					if (t.getName().equals("init") || t.getName().equals("main")) {
						o.getTag().remove(i);
					}
				}
			}
		});
		
		Resource v2rResource = rs.getResource(URI.createURI("platform:/resource" + this.v2rPath), true);
		EcoreUtil.resolveAll(v2rResource);
		Transformation transformation = (Transformation) v2rResource.getContents().get(0);
		
		// Interpreter class
		ExtendedClass interpreterClass = AleFactory.eINSTANCE.createExtendedClass();
		interpreterClass.getAttributes()
				.addAll(EcoreUtil.copyAll(transformation.getInterpreter().getAttributes()));
		
		Operation initOperation = EcoreUtil.copy(transformation.getInterpreter().getInitMethod());
		Tag initTag = AleFactory.eINSTANCE.createTag();
		initTag.setName("init");
		initOperation.getTag().add(initTag);
		interpreterClass.getOperations().add(initOperation);
		
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
	
		interpreterClass.setName("Interpreter");
		unit.getXtendedClasses().add(0, interpreterClass);
		
		transformation.getInstructions().stream().forEach(i -> {
			BehavioredClass bc = AleFactory.eINSTANCE.createExtendedClass();
			bc.setName(i.getClassifier().getName() + "_Instruction");
			
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
			interpreterVar.setName(transformation.getInterpreter().getName());
			interpreterVar.setType(interpreterType);
			replOperation.getParams().add(interpreterVar);
			Block block = AleFactory.eINSTANCE.createBlock();
			replOperation.setBody(block);
			Call call = AleFactory.eINSTANCE.createCall();
			call.setName(i.getEvalMethod());
			VarRef selfVar = AleFactory.eINSTANCE.createVarRef();
			selfVar.setID("self");
			Feature originalFeature = AleFactory.eINSTANCE.createFeature();
			originalFeature.setTarget(selfVar);
			originalFeature.setFeature("original");
			call.setTarget(originalFeature);
			ExpressionStmt statement = AleFactory.eINSTANCE.createExpressionStmt();
			statement.setExp(call);
			
			// Create parameters for interpret method
			for (Expression parameter : i.getEvalParams()) {
				call.getParams().add(this.toAleExpression(EcoreUtil.copy(parameter)));
			}
			
			// Manage the output of the instruction
			if (i.getEvalResult() == null) {
				block.getStatements().add(statement);
			} else {
				this.needsOutput = false;
				i.getEvalResult().eAllContents().forEachRemaining(c -> this.needsOutput |= c instanceof OutputRef);
				if (this.needsOutput) {
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
				ifStatementComparison.setLeft(this.toAleExpression(EcoreUtil.copy(i.getEvalResult())));
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
				ifElseCall.setTarget(this.toAleExpression(EcoreUtil.copy(i.getEvalResult())));
			}
			bc.getOperations().add(replOperation);
			unit.getXtendedClasses().add(1, bc);
		});
		
		// Save the created resource
		try {
			aleResource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return aleResource.getURI();
	}
	
	
	private Expression toAleExpression(Expression expression) {
		Expression target = expression;
		while (target != null) {
			if (target instanceof SelfRef) {
				VarRef newTarget = AleFactory.eINSTANCE.createVarRef();
				newTarget.setID("self");
				EcoreUtil.replace(target, newTarget);
			} else if (target instanceof OutputRef) { 
				VarRef newTarget = AleFactory.eINSTANCE.createVarRef();
				newTarget.setID("output");
				EcoreUtil.replace(target, newTarget);				
			} else if (target instanceof InterpreterAttributeRef) {
				Feature newTarget = AleFactory.eINSTANCE.createFeature();
				VarRef interpreterVarRef = AleFactory.eINSTANCE.createVarRef();
				interpreterVarRef.setID(((InterpreterAttributeRef) target).getTarget().getName());
				newTarget.setTarget(interpreterVarRef);
				newTarget.setFeature(((InterpreterAttributeRef) target).getFeature().getName());
				EcoreUtil.replace(target, newTarget);
			}
			
			if (target.eClass().getEStructuralFeature("target") != null) {
				target = (Expression) target.eGet(target.eClass().getEStructuralFeature("target"));	
			} else {
				target = null;
			}
		}
		return expression;
	}

}
