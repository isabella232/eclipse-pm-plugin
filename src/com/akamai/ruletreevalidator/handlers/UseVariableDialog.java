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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.akamai.ruletreevalidator.models.SnippetType;
import com.akamai.ruletreevalidator.utils.JsonUtil;
import com.akamai.ruletreevalidator.utils.RuleTreeUtils;

public class UseVariableDialog {

	private Shell parentShell;

	public UseVariableDialog(Shell parentShell) {
		this.parentShell = parentShell;
	}

	protected void createDialogArea() {
		JsonUtil jsonSchemaUtil = new JsonUtil();
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		ArrayList<String> variables = jsonSchemaUtil.getAllowedVariables();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(parentShell, new LabelProvider());
		dialog.setMultipleSelection(false);
		dialog.setElements(variables.toArray());
		dialog.setTitle("Select Variable");
		dialog.open();
		for (Object r : dialog.getResult()) {
			if(r.toString().startsWith("PMUSER_")) {
				if (!ruleTreeUtils.insertSnippet("{{user." + r.toString() + "}}", SnippetType.VARIABLE.type)) {
					MessageDialog.openWarning(parentShell, "Invalid location for variables",
							"You can only insert a variable into a string value in the 'options' object.");
				}
			} else {
				if (!ruleTreeUtils.insertSnippet("{{builtin." + r.toString() + "}}", SnippetType.VARIABLE.type)) {
					MessageDialog.openWarning(parentShell, "Invalid location for variables",
							"You can only insert a variable into a string value in the 'options' object.");
				}
			}
			
		}
	}

}