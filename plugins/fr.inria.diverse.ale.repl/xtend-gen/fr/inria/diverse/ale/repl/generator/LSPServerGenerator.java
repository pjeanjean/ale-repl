package fr.inria.diverse.ale.repl.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;
import org.eclipse.pde.internal.ui.wizards.tools.UpdateClasspathJob;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class LSPServerGenerator {
  private String xtextBasePackage = "";
  
  private String languageName = "";
  
  private String languageNamePkg = "";
  
  public LSPServerGenerator(final String xtextBasePackage, final String languageName) {
    this.xtextBasePackage = xtextBasePackage;
    String _upperCase = languageName.substring(0, 1).toUpperCase();
    String _substring = languageName.substring(1);
    String _plus = (_upperCase + _substring);
    this.languageName = _plus;
    String _upperCase_1 = languageName.substring(0, 1).toUpperCase();
    String _lowerCase = languageName.substring(1).toLowerCase();
    String _plus_1 = (_upperCase_1 + _lowerCase);
    this.languageNamePkg = _plus_1;
  }
  
  public IProject generateProject() {
    try {
      final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
      final String projectName = (this.xtextBasePackage + ".server");
      final IProject project = workspaceRoot.getProject(projectName);
      boolean _exists = project.exists();
      if (_exists) {
        try {
          project.delete(true, null);
        } catch (final Throwable _t) {
          if (_t instanceof CoreException) {
            final CoreException e = (CoreException)_t;
            e.printStackTrace();
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
      try {
        final IProjectDescription projectDescription = workspaceRoot.getWorkspace().newProjectDescription(projectName);
        projectDescription.setNatureIds(((String[])Conversions.unwrapArray(Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet(XtextProjectHelper.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature")), String.class)));
        final Set<String> builders = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet(JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder"));
        final ArrayList<ICommand> commands = CollectionLiterals.<ICommand>newArrayList();
        for (final String builder : builders) {
          {
            final ICommand command = projectDescription.newCommand();
            command.setBuilderName(builder);
            commands.add(command);
          }
        }
        projectDescription.setBuildSpec(((ICommand[])Conversions.unwrapArray(commands, ICommand.class)));
        project.create(projectDescription, null);
        boolean _isOpen = project.isOpen();
        boolean _not = (!_isOpen);
        if (_not) {
          project.open(null);
        }
        final IFolder srcFolder = project.getFolder("src");
        srcFolder.create(false, true, null);
        final IJavaProject javaProject = JavaCore.create(project);
        final ArrayList<IClasspathEntry> entries = CollectionLiterals.<IClasspathEntry>newArrayList();
        final IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
        final LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
        for (final LibraryLocation element : locations) {
          entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
        }
        entries.add(JavaCore.newSourceEntry(javaProject.getPackageFragmentRoot(srcFolder).getPath()));
        javaProject.setRawClasspath(((IClasspathEntry[])Conversions.unwrapArray(entries, IClasspathEntry.class)), null);
        this.generateManifest(projectName);
        this.generateProjectXml(projectName);
        IFile _file = project.getFile("META-INF/MANIFEST.MF");
        WorkspacePluginModel _workspacePluginModel = new WorkspacePluginModel(_file, false);
        final UpdateClasspathJob updateClasspath = new UpdateClasspathJob(
          ((IPluginModelBase[])Conversions.unwrapArray(Collections.<WorkspacePluginModel>unmodifiableSet(CollectionLiterals.<WorkspacePluginModel>newHashSet(_workspacePluginModel)), IPluginModelBase.class)));
        updateClasspath.schedule();
        updateClasspath.join();
      } catch (final Throwable _t_1) {
        if (_t_1 instanceof CoreException) {
          final CoreException e_1 = (CoreException)_t_1;
          e_1.printStackTrace();
          return null;
        } else {
          throw Exceptions.sneakyThrow(_t_1);
        }
      }
      return workspaceRoot.getProject(projectName);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void generateManifest(final String projectName) {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Manifest-Version: 1.0");
      _builder.newLine();
      _builder.append("Bundle-ManifestVersion: 2");
      _builder.newLine();
      _builder.append("Bundle-Name: ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".server");
      _builder.newLineIfNotEmpty();
      _builder.append("Bundle-SymbolicName: ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".server; singleton:=true");
      _builder.newLineIfNotEmpty();
      _builder.append("Bundle-Version: 1.0.0");
      _builder.newLine();
      _builder.append("Automatic-Module-Name: ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".server");
      _builder.newLineIfNotEmpty();
      _builder.append("Bundle-RequiredExecutionEnvironment: JavaSE-1.8");
      _builder.newLine();
      _builder.append("Require-Bundle: com.google.inject,");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("fr.inria.diverse.ale.repl,");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("org.eclipse.lsp4j,");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("org.eclipse.lsp4j.jsonrpc,");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("org.eclipse.xtext.ide,");
      _builder.newLine();
      _builder.append(" ");
      _builder.append(this.xtextBasePackage, " ");
      _builder.append(",");
      _builder.newLineIfNotEmpty();
      _builder.append(" ");
      _builder.append(this.xtextBasePackage, " ");
      _builder.append(".ide");
      _builder.newLineIfNotEmpty();
      final String content = _builder.toString();
      final IFolder metaInf = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFolder("META-INF");
      metaInf.create(false, true, null);
      IFile _file = metaInf.getFile("MANIFEST.MF");
      byte[] _bytes = content.getBytes();
      ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_bytes);
      _file.create(_byteArrayInputStream, true, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void generateProjectXml(final String projectName) {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      _builder.newLine();
      _builder.append("<?eclipse version=\"3.4\"?>");
      _builder.newLine();
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("<extension");
      _builder.newLine();
      _builder.append("         ");
      _builder.append("point=\"fr.inria.diverse.ale.repl.lsp\">");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("<server");
      _builder.newLine();
      _builder.append("            ");
      _builder.append("class=\"");
      _builder.append(this.xtextBasePackage, "            ");
      _builder.append(".server.");
      _builder.append(this.languageName, "            ");
      _builder.append("LspServer\"");
      _builder.newLineIfNotEmpty();
      _builder.append("            ");
      _builder.append("languageName=\"");
      _builder.append(this.languageName, "            ");
      _builder.append("\">");
      _builder.newLineIfNotEmpty();
      _builder.append("      ");
      _builder.append("</server>");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("</extension>");
      _builder.newLine();
      _builder.append("</plugin>");
      _builder.newLine();
      final String content = _builder.toString();
      IFile _file = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile("plugin.xml");
      byte[] _bytes = content.getBytes();
      ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_bytes);
      _file.create(_byteArrayInputStream, true, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public String generateImports() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import com.google.inject.Injector;");
    _builder.newLine();
    _builder.append("import fr.inria.diverse.ale.repl.server.ReplLspServer;");
    _builder.newLine();
    _builder.append("import java.net.ServerSocket;");
    _builder.newLine();
    _builder.append("import java.net.Socket;");
    _builder.newLine();
    _builder.append("import org.eclipse.lsp4j.InitializedParams;");
    _builder.newLine();
    _builder.append("import org.eclipse.lsp4j.InitializeParams;");
    _builder.newLine();
    _builder.append("import org.eclipse.lsp4j.jsonrpc.Launcher;");
    _builder.newLine();
    _builder.append("import org.eclipse.lsp4j.launch.LSPLauncher;");
    _builder.newLine();
    _builder.append("import org.eclipse.lsp4j.services.LanguageClient;");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.ide.server.LanguageServerImpl;");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.resource.IResourceServiceProvider;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import ");
    _builder.append(this.xtextBasePackage);
    _builder.append(".");
    String _lowerCase = this.languageName.toLowerCase();
    _builder.append(_lowerCase);
    _builder.append(".");
    _builder.append(this.languageNamePkg);
    _builder.append("Package;");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
  
  public String generateAttributes() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("private static Injector injector = null;");
    _builder.newLine();
    _builder.append("private static ");
    _builder.append(this.languageNamePkg);
    _builder.append("Package pkg = null;");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("private LanguageServerImpl server;");
    _builder.newLine();
    _builder.append("private Socket clientSocket;");
    _builder.newLine();
    _builder.append("private ServerSocket serverSocket;");
    _builder.newLine();
    return _builder.toString();
  }
  
  public String generateStartStop() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void runServer(int port) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("if (pkg == null) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("pkg = ");
    _builder.append(this.languageNamePkg, "\t\t");
    _builder.append("Package.eINSTANCE;");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("if (injector == null) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("injector = new ");
    _builder.append(this.languageName, "\t\t");
    _builder.append("LspServerSetup().createInjectorAndDoEMFRegistration();");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("// Keeping `ecl` leads to a NullPointerException when getting");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("//   the resourceServiceProvider for each language");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("injector.getInstance(IResourceServiceProvider.Registry.class)");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append(".getExtensionToFactoryMap().remove(\"ecl\");");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("this.server = injector.getInstance(LanguageServerImpl.class);");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("try {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.serverSocket = new ServerSocket(port);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.clientSocket = this.serverSocket.accept();");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("server, this.clientSocket.getInputStream(), this.clientSocket.getOutputStream());");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.server.connect(launcher.getRemoteProxy());");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("launcher.startListening();");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.server.initialize(new InitializeParams());");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.server.initialized(new InitializedParams());");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("} catch (Exception e) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("e.printStackTrace();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public void stopServer() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("this.server.shutdown();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("this.server = null;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("try {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.serverSocket.close();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("} catch (Exception e) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("e.printStackTrace();");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }
  
  public void generateSetupClass(final String projectName) {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".server;");
      _builder.newLineIfNotEmpty();
      _builder.newLine();
      _builder.append("import org.eclipse.xtext.ide.server.DefaultProjectDescriptionFactory;");
      _builder.newLine();
      _builder.append("import org.eclipse.xtext.ide.server.IProjectDescriptionFactory;");
      _builder.newLine();
      _builder.append("import org.eclipse.xtext.ide.server.IWorkspaceConfigFactory;");
      _builder.newLine();
      _builder.append("import org.eclipse.xtext.ide.server.ProjectWorkspaceConfigFactory;");
      _builder.newLine();
      _builder.append("import org.eclipse.xtext.util.Modules2;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import com.google.inject.Binder;");
      _builder.newLine();
      _builder.append("import com.google.inject.Guice;");
      _builder.newLine();
      _builder.append("import com.google.inject.Injector;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".");
      _builder.append(this.languageName);
      _builder.append("RuntimeModule;");
      _builder.newLineIfNotEmpty();
      _builder.append("import ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".ide.");
      _builder.append(this.languageName);
      _builder.append("IdeModule;");
      _builder.newLineIfNotEmpty();
      _builder.append("import ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".ide.");
      _builder.append(this.languageName);
      _builder.append("IdeSetup;");
      _builder.newLineIfNotEmpty();
      _builder.newLine();
      _builder.append("public class ");
      _builder.append(this.languageName);
      _builder.append("LspServerSetup extends ");
      _builder.append(this.languageName);
      _builder.append("IdeSetup {");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Override");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public Injector createInjector() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("return Guice.createInjector(Modules2.mixin(new ");
      _builder.append(this.languageName, "\t\t");
      _builder.append("RuntimeModule(),");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t");
      _builder.append("new ");
      _builder.append(this.languageName, "\t\t\t");
      _builder.append("IdeModule(),");
      _builder.newLineIfNotEmpty();
      _builder.append("  \t    \t");
      _builder.append("(Binder b) -> {");
      _builder.newLine();
      _builder.append("  \t    \t\t");
      _builder.append("b.bind(IWorkspaceConfigFactory.class).to(ProjectWorkspaceConfigFactory.class);");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("b.bind(IProjectDescriptionFactory.class).to(DefaultProjectDescriptionFactory.class);");
      _builder.newLine();
      _builder.append("  \t    \t");
      _builder.append("}));");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      String setup = _builder.toString();
      final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      final String projectFileStr = project.getLocation().toOSString();
      String _replace = projectName.replace(".", "/");
      String _plus = (((projectFileStr + "/") + "src/") + _replace);
      String _plus_1 = (_plus + "/");
      String _plus_2 = (_plus_1 + this.languageName);
      String _plus_3 = (_plus_2 + "LspServerSetup.java");
      final File serverFile = new File(_plus_3);
      serverFile.getParentFile().mkdirs();
      serverFile.createNewFile();
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
      final IFile serverIFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(serverFile.getAbsolutePath()));
      byte[] _bytes = setup.getBytes();
      ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_bytes);
      serverIFile.setContents(_byteArrayInputStream, true, false, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void generateServerClass(final String projectName) {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package ");
      _builder.append(this.xtextBasePackage);
      _builder.append(".server;");
      _builder.newLineIfNotEmpty();
      _builder.newLine();
      String _generateImports = this.generateImports();
      _builder.append(_generateImports);
      _builder.newLineIfNotEmpty();
      _builder.newLine();
      _builder.append("public class ");
      _builder.append(this.languageName);
      _builder.append("LspServer implements ReplLspServer {");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      String _generateAttributes = this.generateAttributes();
      _builder.append(_generateAttributes, "\t");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t");
      String _generateStartStop = this.generateStartStop();
      _builder.append(_generateStartStop, "\t");
      _builder.newLineIfNotEmpty();
      _builder.append("}");
      _builder.newLine();
      String server = _builder.toString();
      final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      final String projectFileStr = project.getLocation().toOSString();
      String _replace = projectName.replace(".", "/");
      String _plus = (((projectFileStr + "/") + "src/") + _replace);
      String _plus_1 = (_plus + "/");
      String _plus_2 = (_plus_1 + this.languageName);
      String _plus_3 = (_plus_2 + "LspServer.java");
      final File serverFile = new File(_plus_3);
      serverFile.getParentFile().mkdirs();
      serverFile.createNewFile();
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
      final IFile serverIFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(serverFile.getAbsolutePath()));
      byte[] _bytes = server.getBytes();
      ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_bytes);
      serverIFile.setContents(_byteArrayInputStream, true, false, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
