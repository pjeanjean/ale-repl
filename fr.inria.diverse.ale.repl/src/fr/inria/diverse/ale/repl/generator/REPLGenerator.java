package fr.inria.diverse.ale.repl.generator;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;

public class REPLGenerator {
	
	private String ecorePath;
	private String alePath;
	private String xtextPath; 

	
	public REPLGenerator(String ecorePath, String alePath, String xtextPath) {
		this.ecorePath = ecorePath;
		this.alePath = alePath;
		this.xtextPath = xtextPath;
	}
	
	
	public void generate(String baseName) {
		String languageName = this.ecorePath.substring(this.ecorePath.lastIndexOf("/") + 1)
				.replace(".ecore", "_repl");
		baseName += "." + languageName;
		
		AbstractSyntaxGenerator asGenerator = new AbstractSyntaxGenerator(this.ecorePath, this.alePath);
		IProject asProject = asGenerator.createProject(baseName + ".model");
		URI ecoreUri = asGenerator.generateEcore(asProject.getName(), languageName);
		URI genmodelUri = asGenerator.generateGenmodel(languageName, asProject.getName(), ecoreUri);
		asGenerator.generateModelCode(genmodelUri);
		asGenerator.registerNsUris(ecoreUri);
		
		ConcreteSyntaxGenerator csGenerator = new ConcreteSyntaxGenerator(this.xtextPath);
		IProject csProject = csGenerator.createProject(asProject.getName(), baseName + ".xtext", languageName);
		csGenerator.createGrammar(csProject.getName(), ecoreUri, languageName);
		csGenerator.generateGrammar(csProject.getName(), languageName);		

		SemanticGenerator semGenerator = new SemanticGenerator(this.alePath);
		semGenerator.createDsl(asProject.getName(), languageName, ecoreUri);
		semGenerator.generateAle(ecoreUri);
	}
	
}
