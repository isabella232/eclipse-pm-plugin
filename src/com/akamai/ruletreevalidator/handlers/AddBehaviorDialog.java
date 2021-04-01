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
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.MissingPropertyDetailsException;
import com.akamai.ruletreevalidator.models.SnippetType;
import com.akamai.ruletreevalidator.utils.FileUtils;
import com.akamai.ruletreevalidator.utils.JsonUtil;
import com.akamai.ruletreevalidator.utils.RuleTreeUtils;

public class AddBehaviorDialog {

    private Shell parentShell;
    

    public AddBehaviorDialog(Shell parentShell) {
    	this.parentShell=parentShell;
    }

    protected void createDialogArea() {
        JsonUtil jsonSchemaUtil = new JsonUtil();
        RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
        FileUtils fileUtils = new FileUtils();
        try {
			fileUtils.updateContextFiles();
			ArrayList<String> behaviors = jsonSchemaUtil.getAllowedBehaviors();
			ListSelectionDialog dialog =
					   new ListSelectionDialog(parentShell, behaviors, ArrayContentProvider.getInstance(),
					            new LabelProvider(), "Select one or more behaviors:");

					dialog.setTitle("Add Behavior");

					dialog.open();
					Object[] result = dialog.getResult();
					for (Object r : result) {
						if (!ruleTreeUtils.insertSnippet(jsonSchemaUtil.getSnippets(r.toString(),SnippetType.BEHAVIORS.type), SnippetType.BEHAVIORS.type)) {
							MessageDialog.openWarning(
									parentShell,
									"Invalid location for behaviors",
									"You can only add behaviors to the 'behaviors' array.");
						}
					}
		} catch (CredentialsMissingException e) {
			MessageDialog.openWarning(parentShell, "EdgeGrid credentials not set",
					"Go to 'Credential Settings' to configure your preferences.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingPropertyDetailsException e) {
			MessageDialog.openWarning(
					parentShell,
					"Missing Property Details",
					e.getMessage());
		}
		
    }
    
    
}