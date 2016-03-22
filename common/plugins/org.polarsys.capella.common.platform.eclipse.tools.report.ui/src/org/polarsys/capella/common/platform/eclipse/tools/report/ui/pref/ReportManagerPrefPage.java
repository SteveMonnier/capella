/*******************************************************************************
 * Copyright (c) 2006, 2016 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/

package org.polarsys.capella.common.platform.eclipse.tools.report.ui.pref;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.polarsys.capella.common.tools.report.ReportManagerActivator;
import org.polarsys.capella.common.tools.report.config.ReportManagerConstants;
import org.polarsys.capella.common.tools.report.config.registry.ReportManagerRegistry;
import org.polarsys.capella.common.tools.report.ui.pref.CreateBaseComponentTable;
import org.polarsys.capella.common.tools.report.ui.pref.IReportManagerPrefPage;
import org.polarsys.capella.common.tools.report.util.IReportManagerDefaultComponents;


public class ReportManagerPrefPage extends PreferencePage implements
		IWorkbenchPreferencePage, IReportManagerPrefPage {
	private Combo _componentCombo;

	public static Properties _preferenceStore = new Properties();
	public static Properties _tempStore = new Properties();
	public static Map<String, String> _comboItems = new HashMap<String, String>();

	private String[] _levelsName = new String[] {
			ReportManagerConstants.LOG_LEVEL_DEBUG,
			ReportManagerConstants.LOG_LEVEL_INFO,
			ReportManagerConstants.LOG_LEVEL_WARN,
			ReportManagerConstants.LOG_LEVEL_ERROR,
			ReportManagerConstants.LOG_LEVEL_FATAL };

	private ReportManagerRegistry _registry;
	private SelectionListener _componentHandler;
	CreateBaseComponentTable _componentTable;


	@Override
	protected void performDefaults() {
		super.performDefaults();
		_componentTable.defaultValues ();
		
	}

	public ReportManagerPrefPage() {
		_registry = ReportManagerRegistry.getInstance();
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createContents(Composite parent) {
		// Creates the root composite.
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(1, false));

		// The component selection combo & label.
		Label comboLabel = new Label(root, SWT.NONE);
		comboLabel.setText("Select Category :"); //$NON-NLS-1$
		_componentCombo = createComponentCombo(root);
		// Creates the logger viewer.
	   _componentTable = new CreateBaseComponentTable(root, SWT.NONE, _registry, this, _levelsName);
	   _componentCombo.notifyListeners(SWT.Selection, null);

		return root;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// do nothing.
	}

	public Combo createComponentCombo(Composite parent) {

	  // force loading of default loggers, even if they haven't been used yet
	  Field[] fields = IReportManagerDefaultComponents.class.getDeclaredFields();
	  for (Field f : fields){
	    try {
	      String componentName = (String) f.get(null);
	      _registry.subscribe(componentName);
	    } catch (Exception exception) {
	      ReportManagerActivator.getDefault().getLog().log(new Status(IStatus.ERROR, ReportManagerActivator.getDefault().getBundle().getSymbolicName(), exception.getMessage(), exception));
	    }
	  }

		Combo combo = new Combo(parent, SWT.READ_ONLY);
		// Load combo box items.
		Object[] componentList = _registry.getComponentsList();
		String[] items = new String[componentList.length];
		for (int i = 0; i < componentList.length; i++) {
			items[i] = componentList[i].toString();
		}

		// Check combo box content.
		if (0 == items.length) {
			combo.setEnabled(false);
			setErrorMessage("No Application Component available."); //$NON-NLS-1$
		} else {
			if (!combo.isEnabled()) {
				combo.setEnabled(true);
			}
			combo.setItems(items);
			combo.select(0);
		}

		// Add the selection listener.
		_componentHandler = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.widget instanceof Combo) {
					Combo combo_l = (Combo) event.widget;
//					_componentTable.loadPreferences(combo_l.getText());
					_componentTable.selectPage (combo_l.getText());
				} 
			}
		};
		combo.addSelectionListener(_componentHandler);
		return combo;
	}

	/**
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		_componentTable.updateConfigurationHashMap(_registry.getConfigurations());
		_registry.saveConfiguration();
		return super.performOk();
	}

	public Properties get_preferenceStore() {
		return _preferenceStore;
	}
}
