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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.MissingPropertyDetailsException;
import com.akamai.ruletreevalidator.utils.FileUtils;
import com.akamai.ruletreevalidator.utils.JsonUtil;
import com.akamai.ruletreevalidator.utils.RuleTreeUtils;

public class CreateVariableHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		//AddVariableDialog addVariableDialog = new AddVariableDialog(window.getShell());
		//addVariableDialog.createDialogArea();
		JsonUtil jsonSchemaUtil = new JsonUtil();
		FileUtils fileUtils = new FileUtils();
        try {
			fileUtils.updateContextFiles();
		} catch (CredentialsMissingException e) {
			MessageDialog.openWarning(window.getShell(), "EdgeGrid Credentials not set",
					"Go to 'Credential Settings' to configure your preferences.");
		} catch (MissingPropertyDetailsException e) {
			MessageDialog.openWarning(
					window.getShell(),
					"Missing Property Details",
					e.getMessage());
		}
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		if (!ruleTreeUtils.createVariable(jsonSchemaUtil.getVariableSnippet())) {
			MessageDialog.openWarning(
					window.getShell(),
					"Invalid location for variable",
					"You can only insert a variable into a valid string value.");
		}
		return null;
	}
}
