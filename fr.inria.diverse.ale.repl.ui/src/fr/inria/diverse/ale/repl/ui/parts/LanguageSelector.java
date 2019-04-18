package fr.inria.diverse.ale.repl.ui.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gemoc.execution.sequential.javaxdsml.api.extensions.languages.SequentialLanguageDefinitionExtensionPoint;
import org.eclipse.gemoc.executionframework.engine.commons.DslHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class LanguageSelector {
	
	@PostConstruct
	public void createGui(Composite parent) {
		ToolBar toolBar = new ToolBar(parent, SWT.NONE);
		
		Text port = new Text(parent, SWT.SINGLE);
		port.setMessage("LSP Port");
		
		ToolItem languagesMenu = new ToolItem(toolBar, SWT.DROP_DOWN);
		languagesMenu.setText("Languages");
		
		Menu menu = new Menu(parent);
		for (String language : this.getAllLanguages()) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(language);
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (REPLView.getInstance() != null) {
						int portInteger;
						try {
							portInteger = Integer.parseInt(port.getText());
						} catch (NumberFormatException e1) {
							portInteger = -1;
						}
						REPLView.getInstance().loadLanguage(
								DslHelper.load(menuItem.getText()),
								language.toLowerCase().replaceAll(" ", "_"),
								portInteger
						);
					}
					super.widgetSelected(e);
				}
			});
		}
		
		languagesMenu.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rectangle = languagesMenu.getBounds();
					Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
					point = toolBar.toDisplay(point);
					menu.setLocation(point.x, point.y);
					menu.setVisible(true);
				}
			}
		});
	}
	
	public List<String> getAllLanguages(){
		List<String> languagesNames = new ArrayList<String>();
		IConfigurationElement[] languages = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						SequentialLanguageDefinitionExtensionPoint.GEMOC_SEQUENTIAL_LANGUAGE_EXTENSION_POINT);
		for (IConfigurationElement lang : languages) {
			String xdsmlPath = lang.getAttribute("xdsmlFilePath");
			String xdsmlName = lang.getAttribute("name");
			if(xdsmlPath.endsWith(".dsl")) {
				languagesNames.add(xdsmlName);
			}
		}
		return languagesNames;
	}
}