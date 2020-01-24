package fr.inria.diverse.ale.repl.generator

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.jdt.core.JavaCore
import java.io.File
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.Path
import java.io.ByteArrayInputStream
import org.eclipse.jdt.launching.JavaRuntime
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel
import java.util.Random
import org.eclipse.pde.internal.core.ClasspathComputer

class NotebookKernelGenerator {
	
	var basePackage = ""
	var languageName = ""
	
	var gemoc_port = 0
	var lsp_port = 0
	
	
	new(String basePackage, String languageName) {
		val rand = new Random;
		this.basePackage = basePackage
		this.languageName = languageName.substring(0, 1).toUpperCase + languageName.substring(1)
		this.gemoc_port = rand.nextInt(55535) + 10000
		this.lsp_port = rand.nextInt(55535) + 10000
	}
	
	
	def generateProject() {	
		val workspaceRoot = ResourcesPlugin.getWorkspace().getRoot()
		val projectName = basePackage + ".notebook"
		
		val project = workspaceRoot.getProject(projectName)
		if (project.exists()) {
			try {
				project.delete(true, null)
			} catch (CoreException e) {
				e.printStackTrace()
			}
		}
		
		try {
			val projectDescription = workspaceRoot.workspace.newProjectDescription(projectName)
			projectDescription.setNatureIds(#{JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature"})
			val builders = #{JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder"}			
			val commands = newArrayList;
			for (builder : builders) {
				val command = projectDescription.newCommand();
				command.setBuilderName(builder);
				commands.add(command);
			}
			projectDescription.setBuildSpec(commands);
			
			project.create(projectDescription, null)
			
			if (!project.open) {
				project.open(null)
			}
			
			val srcFolder = project.getFolder("src");
			srcFolder.create(false, true, null);
			
			val javaProject = JavaCore.create(project)
			
		    val entries = newArrayList
		    val vmInstall = JavaRuntime.getDefaultVMInstall()
		    val locations = JavaRuntime.getLibraryLocations(vmInstall)
		    for (element : locations) {
		        entries.add(JavaCore.newLibraryEntry(element.systemLibraryPath, null, null))
		    }
		    entries.add(JavaCore.newSourceEntry(javaProject.getPackageFragmentRoot(srcFolder).getPath()))
			javaProject.setRawClasspath(entries, null)
			
			generateManifest(projectName)
			generateProjectXml(projectName)
			generateBuildProperties(projectName)
			
			ClasspathComputer.setClasspath(project,
					new WorkspacePluginModel(project.getFile("META-INF/MANIFEST.MF"), false));
		} catch (CoreException e) {
			e.printStackTrace
			return null
		}
		
		return workspaceRoot.getProject(projectName)
	}
	
	
	def generateManifest(String projectName) {
		val content = '''
			Manifest-Version: 1.0
			Bundle-ManifestVersion: 2
			Bundle-Name: «basePackage».notebook
			Bundle-SymbolicName: «basePackage».notebook; singleton:=true
			Bundle-Version: 1.0.0
			Automatic-Module-Name: «basePackage».notebook
			Bundle-RequiredExecutionEnvironment: JavaSE-1.8
			Require-Bundle: fr.inria.diverse.ale.repl.notebook,
			 org.eclipse.gemoc.ale.interpreted.engine;bundle-version="1.0.1",
			 org.eclipse.gemoc.executionframework.engine;bundle-version="4.0.0",
			 org.eclipse.gemoc.dsl;bundle-version="3.0.0",
			 org.eclipse.emf.ecoretools.ale.core,
			 «basePackage».xtext.server
		'''
		
		val metaInf = ResourcesPlugin.workspace.root.getProject(projectName).getFolder("META-INF")
		metaInf.create(false, true, null)
		metaInf.getFile("MANIFEST.MF").create(new ByteArrayInputStream(content.bytes), true, null)
	}
	
	
	def generateProjectXml(String projectName) {
		val content = '''
			<?xml version="1.0" encoding="UTF-8"?>
			<?eclipse version="3.4"?>
			<plugin>
			   <extension
			         point="fr.inria.diverse.ale.repl.kernel">
			      <kernel
			            class="«basePackage».notebook.«languageName»Kernel"
			            languageName="«languageName»">
			      </kernel>
			   </extension>
			</plugin>
		'''
		
		ResourcesPlugin.workspace.root.getProject(projectName).getFile("plugin.xml")
				.create(new ByteArrayInputStream(content.bytes), true, null)
	}
	
	
	def generateBuildProperties(String projectName) {
		val content = '''
			source.. = src/
			bin.includes = .,\
			               META-INF/,\
			               plugin.xml
		'''
		
		ResourcesPlugin.workspace.root.getProject(projectName).getFile("build.properties")
				.create(new ByteArrayInputStream(content.bytes), true, null)
	}
	
	
	def generateImports() {
		return '''
			import java.io.File;
			import java.io.IOException;
			import java.nio.file.Files;
			import java.nio.file.StandardCopyOption;
			
			import org.eclipse.gemoc.ale.interpreted.engine.Helper;
			import org.eclipse.gemoc.executionframework.engine.commons.DslHelper;
			
			import fr.inria.diverse.ale.repl.notebook.KernelServer;
			import «basePackage».xtext.server.«languageName»LspServer;
		'''
	}
	
	
	def generateStartStop() {
		return '''
			public «languageName»Kernel() {
				super(Helper.gemocDslToAleDsl(DslHelper.load("«languageName»")),
						"«languageName.toLowerCase»", «gemoc_port», «lsp_port»);
			}
			
			private «languageName»LspServer lspServer;

			public void start() {
				lspServer = new «languageName»LspServer();
				new Thread(() -> lspServer.runServer(«lsp_port»)).start();
				super.start();
			}
			
			public void stop() {
				lspServer.stopServer();
				super.stop();
			}
		'''
	}
	
	
	def generateInstallUninstall() {
		return '''
			public void install(String kernelsLocation) {
				File kernelDir = new File(kernelsLocation + File.separator + "«languageName.toLowerCase»«gemoc_port»");
				kernelDir.mkdir();
				try {
					File kernelFile = new File("«ResourcesPlugin.getWorkspace().getRoot()
							.getProject(basePackage + ".notebook")
							.getFile(new Path("kernels/" + languageName.toLowerCase + "/kernel.json"))
							.getLocation().toOSString»");
					Files.copy(kernelFile.toPath(), kernelDir.toPath().resolve("kernel.json"), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			public void uninstall(String kernelsLocation) {
				File kernelDir = new File(kernelsLocation + File.separator + "«languageName.toLowerCase»«gemoc_port»");
				File kernelFile = new File(kernelsLocation + File.separator + "«languageName.toLowerCase»«gemoc_port»"
						+ File.separator + "kernel.json");
				kernelFile.delete();
				kernelDir.delete();
			}
		'''
	}
	
	
	def generateServerClass(String projectName) {
		var server = '''
			package «basePackage».notebook;
			
			«generateImports»
			
			public class «languageName»Kernel extends KernelServer {
				«generateStartStop»
				
				«generateInstallUninstall»
			}
		'''
		
		val project = ResourcesPlugin.workspace.root.getProject(projectName)
		val projectFileStr = project.location.toOSString
		val serverFile = new File(projectFileStr + "/"
				+ "src/" + projectName.replace(".", "/") + "/" + languageName + "Kernel.java")
		serverFile.parentFile.mkdirs
		serverFile.createNewFile
		project.refreshLocal(IResource.DEPTH_INFINITE, null)
		val serverIFile = ResourcesPlugin.workspace.root
				.getFileForLocation(Path.fromOSString(serverFile.absolutePath))
				
		serverIFile.setContents(new ByteArrayInputStream(server.bytes), true, false, null)
	}
	
	
	def generateKernelFile(String projectName) {
		val actualLanguageName = languageName.replaceAll("_repl$", "")
		var kernel = '''
			{
			    "display_name": "«actualLanguageName»",
			    "argv": [
			        "/opt/anaconda/bin/gemoc_kernel",
			        "-f",
			        "{connection_file}",
			        "«gemoc_port»"
			    ],
			    "language": "«actualLanguageName»"
			}
		'''
		
		val project = ResourcesPlugin.workspace.root.getProject(projectName)
		val projectFileStr = project.location.toOSString
		val kernelFile = new File(projectFileStr + "/"
				+ "/kernels/" + languageName.toLowerCase + "/kernel.json")
		kernelFile.parentFile.mkdirs
		kernelFile.createNewFile
		project.refreshLocal(IResource.DEPTH_INFINITE, null)
		val serverIFile = ResourcesPlugin.workspace.root
				.getFileForLocation(Path.fromOSString(kernelFile.absolutePath))
				
		serverIFile.setContents(new ByteArrayInputStream(kernel.bytes), true, false, null)
	}
	
}