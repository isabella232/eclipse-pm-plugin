//  Copyright 2021. Akamai Technologies, Inc
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package com.akamai.ruletreevalidator.handlers;
/**
 * @author michalka
 */
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.PropertyNotFoundException;
import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
import com.akamai.ruletreevalidator.models.Property;
import com.akamai.ruletreevalidator.papiCalls.PapiOpenCalls;

public class DownloadRuleTreeDialog extends TitleAreaDialog {
	private Text propertyNameText;
	private String propertyId;
	private HashMap<String, Integer> propertyVersions;
    private Integer propertyVersion;

	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public DownloadRuleTreeDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		setTitle("Download Rule Tree");
		setMessage("Enter a property name and select a version to start editing.", IMessageProvider.INFORMATION);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblPropertyName = new Label(container, SWT.NONE);
		lblPropertyName.setBounds(22, 37, 111, 14);
		lblPropertyName.setText("Property name");

		Label lblPropertyVersion = new Label(container, SWT.NONE);
		lblPropertyVersion.setBounds(22, 105, 96, 14);
		lblPropertyVersion.setText("Property version");

		propertyNameText = new Text(container, SWT.BORDER);
		propertyNameText.setBounds(139, 34, 301, 19);

		Combo propertyVersionCombo = new Combo(container, SWT.READ_ONLY);
		propertyVersionCombo.setBounds(139, 102, 301, 22);
		propertyVersionCombo.setEnabled(false);

		Button btnSearch = new Button(container, SWT.TOGGLE);
		btnSearch.setBounds(10, 72, 94, 27);
		btnSearch.setText("Search");

		//enable selection of Search button on modifying text in property name field
		propertyNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				propertyVersionCombo.removeAll();

				btnSearch.setSelection(true);
			}
		});
		btnSearch.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub
				if (!propertyNameText.getText().strip().isEmpty()) {
					try {
						PapiOpenCalls papiOpenCalls = new PapiOpenCalls();
						Property property = papiOpenCalls.getProperty(propertyNameText.getText().strip());
						if (property.getId() != null) {
							System.out.println("PropertyId selected: " + property.getId());
							propertyId = property.getId().strip();
							propertyVersions = property.getPropertyVersionsWithStatus();
							String[] ITEMS = new String[property.getVersions().size()];
							ITEMS = propertyVersions.keySet().toArray(ITEMS);
							propertyVersionCombo.setItems(ITEMS);
							propertyVersionCombo.setEnabled(true);
							propertyVersionCombo.select(property.getVersions().size()-1);
							propertyVersion = propertyVersions.get(propertyVersionCombo.getText());
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						} else {
							MessageDialog.openWarning(getParentShell(), "Property not found", "Verify the property name is correct.");
						}
					} catch (CredentialsMissingException e) {
						MessageDialog.openWarning(getParentShell(), "EdgeGrid credentials not set",
								"Go to 'Credential Settings' to configure your preferences.");
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RuleTreeDownloadError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (PropertyNotFoundException e) {
						MessageDialog.openWarning(getParentShell(), "Property not found", "Verify the property name is correct.");
					}

				}
			}
		});

		propertyVersionCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("selected propversion: "+propertyVersionCombo.getText()+getButton(IDialogConstants.OK_ID).getSelection());
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				System.out.println("Download button selection status: "+getButton(IDialogConstants.OK_ID).getSelection());
				propertyVersion = propertyVersions.get(propertyVersionCombo.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				propertyVersion = null;
			}
		});

		return area;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Download", true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);;
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(493, 300);
	}

	private void processInput() {
    	//validateInput();
		try {
			if (propertyVersion == null) {
				MessageDialog.openWarning(getParentShell(), "Property version not selected", "Please enter a valid property name and select the property version you want to edit.");
				open();
			} else {
				PapiOpenCalls papiOpenCalls = new PapiOpenCalls();
				papiOpenCalls.getRuleTreeForPropertyIdAndVersion(propertyId, propertyVersion);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (e instanceof CredentialsMissingException) {
				MessageDialog.openWarning(
						getParentShell(),
						"Invalid credentials",
						"Verify your EdgeGrid credentials are correct.");
				CredentialSettingsDialog getContextDialog = new CredentialSettingsDialog(getParentShell().getShell());
				getContextDialog.create();
				getContextDialog.open();
			}
		}
	}
	@Override
    protected void okPressed() {
        processInput();
        super.okPressed();
    }

}
