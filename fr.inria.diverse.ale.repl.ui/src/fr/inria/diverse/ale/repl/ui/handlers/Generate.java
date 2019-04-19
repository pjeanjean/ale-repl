package fr.inria.diverse.ale.repl.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.gemoc.DslRuntimeModule;
import org.eclipse.gemoc.commons.eclipse.ui.dialogs.SelectAnyIFileDialog;
import org.eclipse.gemoc.dsl.Dsl;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Guice;

import fr.inria.diverse.ale.repl.generator.REPLGenerator;

public class Generate extends AbstractHandler {
		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelectionService service = window.getSelectionService();
		IStructuredSelection selection = (IStructuredSelection) service.getSelection();
		
		XtextResourceSet rs = Guice.createInjector(new DslRuntimeModule()).getInstance(XtextResourceSet.class);
		
		if (selection.getFirstElement() instanceof IFile) {
			IFile dslFile = (IFile) selection.getFirstElement();
			Dsl dsl = (Dsl) rs.getResource(URI.createFileURI(dslFile.getLocation().toString()), true)
					.getContents().get(0);
			URI ecoreUri = URI.createURI(dsl.getEntries().stream().filter(e -> e.getKey().equals("ecore"))
					.findFirst().get().getValue());
			URI aleUri = URI.createURI(dsl.getEntries().stream().filter(e -> e.getKey().equals("ale"))
					.findFirst().get().getValue());
			
			SelectAnyIFileDialog dialog = new SelectAnyIFileDialog();
			dialog.setPattern("*.xtext");
			if (dialog.open() == Dialog.OK) {
				String xtextPath = ((IResource) dialog.getResult()[0]).getLocation().toOSString();
				new REPLGenerator(
						ResourcesPlugin.getWorkspace().getRoot()
								.getFile(new Path(ecoreUri.toPlatformString(true))).getRawLocation().toOSString(),
						ResourcesPlugin.getWorkspace().getRoot()
								.getFile(new Path(aleUri.toPlatformString(true))).getRawLocation().toOSString(),
						xtextPath).generate("fr.inria.diverse");
			}	
		}
		
		return null;
	}

}