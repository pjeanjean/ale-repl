package fr.inria.diverse.ale.repl.generator;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;

public class REPLGenerator {
	
	private String v2rPath;
	private String ecorePath;
	private String alePaths[];
	private String xtextPath; 

	
	public REPLGenerator(String v2rPath, String ecorePath, String alePaths[], String xtextPath) {
		this.v2rPath = v2rPath;
		this.ecorePath = ecorePath;
		this.alePaths = alePaths;
		this.xtextPath = xtextPath;
	}
	
	
	public void generate(String baseName) {
		String languageName = this.ecorePath.substring(this.ecorePath.lastIndexOf("/") + 1)
				.replace(".ecore", "_repl");
		baseName += "." + languageName.toLowerCase();
		URI ecoreBaseUri = URI.createURI("platform:/resource" + ResourcesPlugin.getWorkspace()
				.getRoot().getFileForLocation(new Path(this.ecorePath)).getFullPath().toString());
		URI aleBaseUris[] = Arrays.stream(this.alePaths)
				.map(p -> URI.createURI("platform:/resource" + ResourcesPlugin.getWorkspace()
						.getRoot().getFileForLocation(new Path(p)).getFullPath().toString()))
				.toArray(URI[]::new);
		AbstractSyntaxGenerator asGenerator = new AbstractSyntaxGenerator(this.ecorePath, this.v2rPath);
		IProject asProject = asGenerator.createProject(baseName + ".model");
		URI ecoreUri = asGenerator.generateEcore(asProject.getName(), languageName);
		asGenerator.registerNsUris(ecoreBaseUri);
		asGenerator.registerNsUris(ecoreUri);
		
		ConcreteSyntaxGenerator csGenerator = new ConcreteSyntaxGenerator(this.xtextPath, this.ecorePath);
		IProject csProject = csGenerator.createProject(asProject.getName(), baseName + ".xtext", languageName);
		csGenerator.createGrammar(csProject.getName(), ecoreUri, languageName);
		URI generatedEcoreUri = csGenerator.generateGrammar(csProject.getName(), languageName);
		asGenerator.registerNsUris(generatedEcoreUri);
		asGenerator.alterEcore(generatedEcoreUri);
		asGenerator.generateModelCode(URI.createURI(generatedEcoreUri.toString()
				.replaceAll("ecore$", "genmodel")));
		csGenerator.createScope(csProject.getName(), languageName);

		SemanticGenerator semGenerator = new SemanticGenerator(this.alePaths, this.v2rPath);
		URI generatedAleUri = URI.createURI(generatedEcoreUri.toString().replaceAll("ecore$", "ale"));
		URI aleUris[] = new URI[aleBaseUris.length + 1];
		aleUris[0] = generatedAleUri;
		for (int i = 0; i < aleBaseUris.length; i++) {
			aleUris[i+1] = aleBaseUris[i];
		}
		semGenerator.createDsl(csProject.getName(), languageName, new URI[] {generatedEcoreUri, ecoreBaseUri},
				aleUris);
		semGenerator.generateAle(generatedEcoreUri, ecoreBaseUri);
		
		LSPServerGenerator lspServerGenerator = new LSPServerGenerator(csProject.getName(), languageName);
		String lspProjectName = lspServerGenerator.generateProject().getName();
		lspServerGenerator.generateSetupClass(lspProjectName);
		lspServerGenerator.generateServerClass(lspProjectName);
		
		NotebookKernelGenerator kernelGenerator = new NotebookKernelGenerator(baseName, languageName);
		String kernelProjectName = kernelGenerator.generateProject().getName();
		kernelGenerator.generateServerClass(kernelProjectName);
		kernelGenerator.generateKernelFile(kernelProjectName);
	}
	
}
