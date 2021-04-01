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

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.MissingPropertyDetailsException;
import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
import com.akamai.ruletreevalidator.papiCalls.PapiOpenCalls;
import com.akamai.ruletreevalidator.utils.FileUtils;

public class ValidateRuleTreeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		FileUtils fileUtils = new FileUtils();
        try {
			fileUtils.updateContextFiles();
			PapiOpenCalls papiOpenCalls;
			papiOpenCalls = new PapiOpenCalls();
			papiOpenCalls.validateRuleTree();
		} catch (CredentialsMissingException e) {
			MessageDialog.openWarning(window.getShell(), "EdgeGrid credentials not set",
					"Go to 'Credential Settings' to configure your preferences.");
		} catch (ConfigurationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuleTreeDownloadError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingPropertyDetailsException e) {
			MessageDialog.openWarning(
					window.getShell(),
					"Missing property details.",
					e.getMessage());
		}


		return null;
	}
}
