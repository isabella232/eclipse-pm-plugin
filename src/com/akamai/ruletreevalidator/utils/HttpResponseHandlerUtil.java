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
package com.akamai.ruletreevalidator.utils;
/**
 * @author michalka
 */
import java.io.IOException;

import org.eclipse.ui.PlatformUI;

import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
import com.google.api.client.http.HttpResponse;

public class HttpResponseHandlerUtil {
	public void handleResponse(HttpResponse response) throws RuleTreeDownloadError, IOException {
		if (response.getStatusCode() == 403 || response.getStatusCode() == 401) {
			System.out.println("Response: " + response.parseAsString());
			throw new RuleTreeDownloadError("The requested resource does not exist or is not accessible",
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} else if (response.getStatusCode() >= 500) {
			throw new RuleTreeDownloadError("The server is unable to process the request at this time",
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}

	}
}
