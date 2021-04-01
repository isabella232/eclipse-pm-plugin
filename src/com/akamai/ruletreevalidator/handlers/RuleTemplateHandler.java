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

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.models.SnippetType;
import com.akamai.ruletreevalidator.utils.JsonUtil;
import com.akamai.ruletreevalidator.utils.RuleTreeUtils;

public class RuleTemplateHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		JsonUtil jsonSchemaUtil = new JsonUtil();
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		try {
			if (!ruleTreeUtils.insertSnippet(jsonSchemaUtil.getSnippets("children",SnippetType.CHILDREN.type), SnippetType.CHILDREN.type)) {
				MessageDialog.openWarning(
						window.getShell(),
						"Invalid location for Rule Template",
						"You can only add a rule template to the 'children' array.");
			}
		} catch (CredentialsMissingException e) {
			MessageDialog.openWarning(window.getShell(), "EdgeGrid credentials not set",
					"Go to 'Credential Settings' to configure your preferences.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
