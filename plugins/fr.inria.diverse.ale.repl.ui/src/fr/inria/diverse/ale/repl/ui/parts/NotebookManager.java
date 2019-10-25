package fr.inria.diverse.ale.repl.ui.parts;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fr.inria.diverse.ale.repl.notebook.KernelServer;

public class NotebookManager {
	@PostConstruct
	public void createGui(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		
		Composite kernelLocationComposite = new Composite(parent, SWT.NONE);
		kernelLocationComposite.setLayout(new GridLayout(2, false));
		Label kernelLocationLabel = new Label(kernelLocationComposite, SWT.LEFT);
		kernelLocationLabel.setText("Kernels location: ");
		Text kernelLocation = new Text(kernelLocationComposite, SWT.BORDER);
		kernelLocation.setText("/opt/anaconda/share/jupyter/kernels");
		
		Composite kernels = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		
		kernels.setLayout(gridLayout);
		
		IConfigurationElement[] notebookKernels = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("fr.inria.diverse.ale.repl.kernel");
		for (IConfigurationElement notebookKernel : notebookKernels) {
			try {
				KernelServer kernelInstance =
						(KernelServer) notebookKernel.createExecutableExtension("class");
				
				Label kernelLabel = new Label(kernels, SWT.LEFT);
				Button installButton = new Button(kernels, SWT.PUSH);
				Button startButton = new Button(kernels, SWT.PUSH);
				Button stopButton = new Button(kernels, SWT.PUSH);
				Button uninstallButton = new Button(kernels, SWT.PUSH);
				
				startButton.setEnabled(false);
				stopButton.setEnabled(false);
				uninstallButton.setEnabled(false);
				
				kernelLabel.setText(notebookKernel.getAttribute("languageName"));
				installButton.setText("Install");
				installButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						kernelInstance.install(kernelLocation.getText());
						kernelLocation.setEnabled(false);
						startButton.setEnabled(true);
						installButton.setEnabled(false);
						uninstallButton.setEnabled(true);
					}
				});
				startButton.setText("Start");
				startButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						kernelInstance.start();
						startButton.setEnabled(false);
						stopButton.setEnabled(true);
						uninstallButton.setEnabled(false);
					}
				});
				stopButton.setText("Stop");
				stopButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						kernelInstance.stop();
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
						uninstallButton.setEnabled(true);
					}
				});
				uninstallButton.setText("Uninstall");
				uninstallButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						kernelInstance.uninstall(kernelLocation.getText());
						kernelLocation.setEnabled(true);
						startButton.setEnabled(false);
						uninstallButton.setEnabled(false);
						installButton.setEnabled(true);
					}
				});

			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}

		kernels.pack();
		parent.pack();
	}
}
