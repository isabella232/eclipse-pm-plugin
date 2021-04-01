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
import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.models.OpenCredentials;
import com.akamai.ruletreevalidator.utils.FileUtils;

public class CredentialSettingsDialog extends TitleAreaDialog {

    private Text txtFilePath;
    private Text txtAccountSwitchKey;
    private Combo sectionNameCombo;
    private Shell parentShell;

    private String filePath;
    private String sectionName;
    private String accountSwitchKey;

    public CredentialSettingsDialog(Shell parentShell) {
    	super(parentShell);
    	this.parentShell=parentShell;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Set up Property Manager credentials");
        setMessage("Upload the edgerc file and select the EdgeGrid credentials you want to use.", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        layout.numColumns = 2;
        container.setLayout(layout);

        createLayout(container);

        return area;
    }

    private void createLayout(Composite container) {
    	FileUtils fileUtils = new FileUtils();
		OpenCredentials openCredentials = null;
		
        Label filePathLabel = new Label(container, SWT.NONE);
        String homeDirectory = System.getProperty("user.home");
        filePathLabel.setText("Edgerc file");

        GridData filePathTxtGrid = new GridData();
        filePathTxtGrid.grabExcessHorizontalSpace = true;
        filePathTxtGrid.horizontalAlignment = GridData.FILL;
        filePathTxtGrid.horizontalSpan = 2;

        txtFilePath = new Text(container, SWT.BORDER);
        txtFilePath.setLayoutData(filePathTxtGrid);
        final Button browse = new Button(container, SWT.PUSH);
        browse.setText("Browse");

        //open file dialog on click of Browse button
        FileDialog dialog = new FileDialog(container.getShell());
        dialog.setFilterPath(homeDirectory); // Windows specific

        Listener openerListener = new Listener() {
          public void handleEvent(Event event) {
            String filePath = dialog.open();
            if (filePath != null) {
                // Set the text box to the new selection
            	txtFilePath.setText(filePath);
              }
          }
        };

        browse.addListener(SWT.Selection, openerListener);

        GridData sectionNameLabelGrid = new GridData();
        sectionNameLabelGrid.grabExcessHorizontalSpace = true;
        sectionNameLabelGrid.horizontalSpan = 2;
        sectionNameLabelGrid.horizontalAlignment = GridData.FILL;
        Label sectionNameLabel = new Label(container, SWT.NONE);
        sectionNameLabel.setLayoutData(sectionNameLabelGrid);
        sectionNameLabel.setText("EdgeGrid credentials");
        sectionNameCombo = new Combo(container, SWT.DROP_DOWN);
        ModifyListener modifyListener = new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				// TODO Auto-generated method stub
			}
		};
		sectionNameCombo.addModifyListener(modifyListener);
		GridData sectionNameLayout = new GridData();
		sectionNameLayout.grabExcessHorizontalSpace = true;
		sectionNameLayout.horizontalAlignment = GridData.FILL;
		sectionNameCombo.setLayoutData(sectionNameLayout);

		// Account Switch Key Label
		GridData accountSwitchKeyLabelGrid = new GridData();
		accountSwitchKeyLabelGrid.grabExcessHorizontalSpace = true;
		accountSwitchKeyLabelGrid.horizontalSpan = 2;
		accountSwitchKeyLabelGrid.horizontalAlignment = GridData.FILL;
		Label accountSwitchKeyLabel = new Label(container, SWT.NONE);
		accountSwitchKeyLabel.setLayoutData(accountSwitchKeyLabelGrid);
		accountSwitchKeyLabel.setText("Account Switch Key <optional>");

		// Account Switch Key Text Area
		GridData accountSwitchKeyTextGrid = new GridData();
		accountSwitchKeyTextGrid.grabExcessHorizontalSpace = true;
		accountSwitchKeyTextGrid.horizontalAlignment = GridData.FILL;
		txtAccountSwitchKey = new Text(container, SWT.BORDER);
		txtAccountSwitchKey.setLayoutData(accountSwitchKeyTextGrid);
		String sectionNameInUse = null;
		// populate the fields with credential settings in use
		try {
			openCredentials = fileUtils.readCredentialsFile();
			if (openCredentials != null) {
				txtFilePath.setText(openCredentials.getFilePath());
				txtAccountSwitchKey.setText(openCredentials.getAccount_id());
				sectionNameInUse = openCredentials.getSection();
				populateSection(sectionNameInUse);
			}

		} catch (CredentialsMissingException e) {
			// Ignore this exception
		}
		final String sectionInUse = sectionNameInUse;

		// focusListener for Section Name field to populate section names
		FocusListener focusListener = new FocusListener() {

			@Override
			public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
				System.out.println("Focus gained");
				populateSection(sectionInUse);
			}

			@Override
			public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		};
		sectionNameCombo.addFocusListener(focusListener);

		SelectionListener selectionListener = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("selected section: " + sectionNameCombo.getText());
				sectionName = sectionNameCombo.getText();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		};
		sectionNameCombo.addSelectionListener(selectionListener);

    }


    @Override
    protected boolean isResizable() {
        return true;
    }

    private void validateInput() {
        if (txtFilePath.getText() == null || txtFilePath.getText().trim().isEmpty()) {
        	MessageDialog.openWarning(
					parentShell,
					"Invalid file path",
					"Verify the path to the '.edgerc' file is correct.");
        }
        filePath = txtFilePath.getText();
        if (sectionNameCombo.getText() == null || sectionNameCombo.getText().trim().isEmpty()) {
        	MessageDialog.openWarning(
					parentShell,
					"Invalid credentials section",
					"Verify the EdgeGrid credentials are correct.");
        }
        sectionName = sectionNameCombo.getText();
        accountSwitchKey = txtAccountSwitchKey.getText();
    }


    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private void processInput() {
    	validateInput();
		try {
			FileUtils fileUtils = new FileUtils();
			fileUtils.saveCredentailsUnderWorkspace(
					new OpenCredentials(filePath, sectionName, null, null, accountSwitchKey));

		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	@Override
	protected void okPressed() {
		processInput();
		super.okPressed();
	}

	public String getFirstName() {
		return filePath;
	}

	public String getLastName() {
		return sectionName;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Save", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	

	private void populateSection(String sectionInUse) {
		FileUtils fileUtils = new FileUtils();
		ArrayList<String> sectionNames = fileUtils.readSectionsFromEdgeRc(txtFilePath.getText());
		String[] ITEMS = new String[sectionNames.size()];
		ITEMS = sectionNames.toArray(ITEMS);
		sectionNameCombo.setItems(ITEMS);

		// If user has already set a section then, show the section name
		if (sectionInUse != null) {
			Point selection = sectionNameCombo.getSelection();
			sectionNameCombo.setText(sectionInUse);
			sectionNameCombo.setSelection(selection);
			sectionNameCombo.setSelection(selection);
		}
	}


}
