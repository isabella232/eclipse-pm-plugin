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
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.akamai.ruletreevalidator.exceptions.AuthenticationMethodNotSelectedError;
import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.MissingPropertyDetailsException;
import com.akamai.ruletreevalidator.exceptions.NoExternalResourcesAvailableException;
import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
import com.akamai.ruletreevalidator.models.AvailableExternalResources;
import com.akamai.ruletreevalidator.models.ExternalResourceItem;
import com.akamai.ruletreevalidator.utils.FileUtils;
import com.akamai.ruletreevalidator.utils.JsonUtil;
import com.akamai.ruletreevalidator.utils.RuleTreeUtils;

public class AddExternalResourcesDialog {

	private Shell parentShell;

	public AddExternalResourcesDialog(Shell parentShell) {
		this.parentShell = parentShell;
	}

	protected void createDialogArea() {
		JsonUtil jsonSchemaUtil = new JsonUtil();
		FileUtils fileUtils = new FileUtils();
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		try {
			fileUtils.updateContextFiles();
			AvailableExternalResources showAvailableResourcesForThisConfig = ruleTreeUtils
					.checkLocationForExternalResources();
			if (showAvailableResourcesForThisConfig != null) {
				List<ExternalResourceItem> externalResourceItems = jsonSchemaUtil
						.getAvailableExternalResource(showAvailableResourcesForThisConfig);
				List<String> displayName = new ArrayList<String>();
				if (externalResourceItems.isEmpty()) {
					MessageDialog.openWarning(parentShell, "No External Resources", "There are no external resources available for this behavior. Configure new resources and try again.");
				} else {
					for (ExternalResourceItem et : externalResourceItems) {
						displayName.add(et.getDisplayName());
					}
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(parentShell,
							new LabelProvider());
					dialog.setElements(displayName.toArray());
					dialog.setTitle("Available " + showAvailableResourcesForThisConfig.getConfigName());
					dialog.open();
					Object selectedExternalResource = dialog.getFirstResult();
					for (ExternalResourceItem et : externalResourceItems) {
						if (et.getDisplayName().equalsIgnoreCase((String) selectedExternalResource)) {
							ruleTreeUtils.insertSelectedExternalResourceInRuleTree(et.getValue(),
									showAvailableResourcesForThisConfig);
						}
					}
				}

			}

		} catch (CredentialsMissingException e) {
			MessageDialog.openWarning(parentShell, "EdgeGrid credentials not set",
					"Go to 'Credential Settings' to configure your preferences.");
		} catch (MissingPropertyDetailsException e) {
			MessageDialog.openWarning(parentShell, "Missing Property Details", e.getMessage());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuleTreeDownloadError | NoExternalResourcesAvailableException | AuthenticationMethodNotSelectedError
				| CoreException e) {
		}
	}
}