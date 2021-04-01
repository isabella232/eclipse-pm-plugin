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
package com.akamai.ruletreevalidator.papiCalls;
/**
 * @author michalka
 */
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.Request;
import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridRequestSigner;
import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.PropertyNotFoundException;
import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
import com.akamai.ruletreevalidator.models.Context;
import com.akamai.ruletreevalidator.models.OpenCredentials;
import com.akamai.ruletreevalidator.models.Property;
import com.akamai.ruletreevalidator.models.PropertyVersion;
import com.akamai.ruletreevalidator.models.ResponseType;
import com.akamai.ruletreevalidator.utils.ConsoleErrorMatcher;
import com.akamai.ruletreevalidator.utils.EdgeRcClientCredentialProvider;
import com.akamai.ruletreevalidator.utils.FileUtils;
import com.akamai.ruletreevalidator.utils.HttpResponseHandlerUtil;
import com.akamai.ruletreevalidator.utils.JsonUtil;
import com.akamai.ruletreevalidator.utils.RuleTreeUtils;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.Json;
import com.google.gson.JsonObject;


public class PapiOpenCalls {
	private EdgeRcClientCredentialProvider edgeRcClientCredentialProvider;
	private GoogleHttpClientEdgeGridRequestSigner googleHttpSigner;
	private HttpTransport httpTransport;
	private ClientCredential clientCredential;
	private HttpRequestFactory requestFactory;
	private OpenCredentials openCredentials;
	private String host;
	private String requestUrl;
	private boolean retry;
	private FileUtils fileUtils;
	private RuleTreeUtils ruleTreeUtils;
	private HttpResponseHandlerUtil httpResponseHandlerUtil;

	public PapiOpenCalls() throws ConfigurationException, IOException, CredentialsMissingException {
		this.fileUtils = new FileUtils();
		OpenCredentials openCredentials = fileUtils.readCredentialsFile();
		this.edgeRcClientCredentialProvider = EdgeRcClientCredentialProvider.fromEdgeRc(openCredentials.getFilePath(), openCredentials.getSection());
		this.googleHttpSigner = new GoogleHttpClientEdgeGridRequestSigner(edgeRcClientCredentialProvider);
		this.httpTransport = new ApacheHttpTransport();
		Request request = null;
		this.clientCredential = edgeRcClientCredentialProvider.getClientCredential(request);
		this.requestFactory = httpTransport.createRequestFactory();
		this.host = clientCredential.getHost().toString();
		this.requestUrl = "";
		this.retry = false;
		this.openCredentials = openCredentials;
		
		this.ruleTreeUtils = new RuleTreeUtils();
		this.httpResponseHandlerUtil = new HttpResponseHandlerUtil();
	}
	
	public Property getProperty(String propertyName)
			throws RuleTreeDownloadError, IOException, PropertyNotFoundException {
		//Get the propertyId for given propertyName using find-by-value api
		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/search/find-by-value";
			} else {
				requestUrl = "https://" + host + "/papi/v0/search/find-by-value?accountSwitchKey="+openCredentials.getAccount_id();
			}
		}
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String propertySearchResponse = null;
		String requestBody = "{\"propertyName\":\""+propertyName+"\"}";
		Property property = new Property();
		try {
			request = requestFactory.buildPostRequest(new GenericUrl(uri), ByteArrayContent.fromString("application/json", requestBody));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.getIOExceptionHandler();
			request.setThrowExceptionOnExecuteError(false);
			System.out.println("Making get properties request");
			response = request.execute();
			System.out.println("Got response: "+response.getStatusMessage());
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getProperty(propertyName);
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				propertySearchResponse = response.parseAsString();
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}
		
		try {
			//Extract propertyId from the find-by-value response
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(propertySearchResponse);
			JSONArray versions = (JSONArray)((JSONObject) json.get("versions")).get("items");
			if (versions.isEmpty()) {
				throw new PropertyNotFoundException("Property not found");
			}
			String propertyId = ((JSONObject)versions.get(0)).get("propertyId").toString();
			//Now that we get the propertyId, get all the propertyVersions associated with it using getPropertyVersions() method
			property = new Property(propertyId,getPropertyVersions(propertyId));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CredentialsMissingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return property;

	}
	
	public List<PropertyVersion> getPropertyVersions(String propertyId)
			throws RuleTreeDownloadError, IOException, CredentialsMissingException {

		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/properties/" + propertyId + "/versions/";
			} else {
				requestUrl = "https://" + host + "/papi/v0/properties/" + propertyId + "/versions/" + "?accountSwitchKey="+openCredentials.getAccount_id();
			}
			
		}
		
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String propertyVersions = null;
		List<PropertyVersion> ver = new ArrayList<PropertyVersion>();
		try {
			request = requestFactory.buildGetRequest(new GenericUrl(uri));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.getIOExceptionHandler();
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getPropertyVersions(propertyId);
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				propertyVersions = response.parseAsString();
			 System.out.println("Response status: "+response.getStatusCode());
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			// TODO Auto-generated catch block
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(propertyVersions);
			JSONArray versions = (JSONArray)((JSONObject) json.get("versions")).get("items");
			for (int i=0;i<versions.size();i++) {
				PropertyVersion pv = new PropertyVersion();
				pv.setVersion(Integer.parseInt(((JSONObject)versions.get(i)).get("propertyVersion").toString()));
				pv.setProductionStatus(" Production:"+((JSONObject)versions.get(i)).get("productionStatus").toString());
				pv.setStagingStatus(" Staging:"+((JSONObject)versions.get(i)).get("stagingStatus").toString());
				ver.add(pv);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ver;

	}
	

	public String getRuleTreeForPropertyIdAndVersion(String propertyId, Integer version)
			throws RuleTreeDownloadError, IOException, CredentialsMissingException {

		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/properties/" + propertyId + "/versions/" + version + "/rules";
			} else {
				requestUrl = "https://" + host + "/papi/v0/properties/" + propertyId + "/versions/" + version + "/rules?accountSwitchKey="+openCredentials.getAccount_id();
			}
			
		}
		
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String ruleTree = null;
		try {
			request = requestFactory.buildGetRequest(new GenericUrl(uri));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.getIOExceptionHandler();
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getRuleTreeForPropertyIdAndVersion(propertyId, version);
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				ruleTree = response.parseAsString();
			 System.out.println("Response status: "+response.getStatusCode());
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			// TODO Auto-generated catch block
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}
		//getPropertyVersion will download details like propertyId, accountId, assetId and save it in context file
		String propertyDetails = getPropertyVersionDetails(propertyId, version.toString());
		System.out.println("Property Versions Details:"+propertyDetails);
		JsonUtil jsonSchemaUtil = new JsonUtil();
		String fileName = jsonSchemaUtil.generateFileNameFromPropertyDetails(propertyDetails);
		fileUtils.openFile(ruleTree, fileName);
		ruleTreeUtils.refreshRuleTree();
		return ruleTree;

	}
	
	public String getRuleTreeSchemaForProductId(String productId, String ruleFormat) throws RuleTreeDownloadError, IOException {
		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/schemas/products/" + productId +"/"+ ruleFormat;
			} else {
				requestUrl = "https://" + host + "/papi/v0/schemas/products/" + productId +"/"+ ruleFormat+"?accountSwitchKey="+openCredentials.getAccount_id();
			}
			
		}
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String ruleTreeSchema = null;
		try {
			request = requestFactory.buildGetRequest(new GenericUrl(uri));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getRuleTreeSchemaForProductId(productId, ruleFormat);
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				ruleTreeSchema= response.parseAsString();
				fileUtils.saveSchemaFileUnderWorkspace(ruleTreeSchema, productId, ruleFormat);
				return ruleTreeSchema;
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RuleTreeDownloadError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ruleTreeSchema;
	}
	
	public String getPropertyVersionDetails(String propertyId, String string) throws RuleTreeDownloadError, IOException {
		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/properties/" + propertyId + "/versions/" + string;
			} else {
				requestUrl = "https://" + host + "/papi/v0/properties/" + propertyId + "/versions/" + string+"?accountSwitchKey="+openCredentials.getAccount_id();
			}
			
		}
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String propertyVersionRules = null;
		try {
			request = requestFactory.buildGetRequest(new GenericUrl(uri));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getPropertyVersionDetails(propertyId, string);
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				propertyVersionRules = response.parseAsString();
			 System.out.println("Rules response: "+propertyVersionRules);
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}
		fileUtils.saveContextFileUnderWorkspace(propertyVersionRules);
		
		//Download schema file for productId and ruleFormat present in propertyVersions response
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(propertyVersionRules);
			JSONObject versions = (JSONObject) json.get("versions");
			JSONArray items = (JSONArray) versions.get("items");
			JSONObject s = (JSONObject) items.get(0);
			String productId = s.get("productId").toString();
			String ruleFormat = s.get("ruleFormat").toString();
			System.out.println("Fetching Schema for productID "+productId+" and ruleFormat "+ruleFormat);
			if (!fileUtils.schemaFileExist(productId, ruleFormat)) {
				System.out.println("Fetching Schema for productID "+productId+" and ruleFormat "+ruleFormat);
				getRuleTreeSchemaForProductId(productId, ruleFormat);
			} else System.out.println("Schema file already exists for productId "+productId+" and ruleFormat "+ruleFormat);
			
		} catch (ParseException pe) {
			System.out.println("Exception caught while parsing propertyVersionRules : "+pe.getMessage());
		}
		return propertyVersionRules;
	}
	
	public String validateRuleTree()
			throws RuleTreeDownloadError, IOException, CredentialsMissingException {
		Context context = fileUtils.readContextFile();
		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v1/validate/rules?contractId=" + context.getContractId() + "&groupId=" + context.getGroupId();
			} else {
				requestUrl = "https://" + host + "/papi/v1/validate/rules?contractId=" + context.getContractId() + "&groupId=" + context.getGroupId()+"&accountSwitchKey="+openCredentials.getAccount_id();
			}
		}
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String validateResponse = null;
		try {
			HttpContent content;
			String ruleTreeFromEditor = ruleTreeUtils.readRuleTreeFromEditor();
			request = requestFactory.buildPostRequest(new GenericUrl(uri), ByteArrayContent.fromString("application/json", ruleTreeFromEditor));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.getIOExceptionHandler();
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return validateRuleTree();
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				if (response.getStatusCode() == 400) {
					System.out.println("Bad Response Message: "+response.getContent());
					ruleTreeUtils.refreshRuleTree();
					validateResponse = "\u26A0 Invalid rule tree. Please resolve errors in rule tree.";
				} else {
					validateResponse = response.parseAsString();
				}
			 System.out.println("Response status: "+response.getStatusMessage().toString());
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		}
		ruleTreeUtils.refreshRuleTree();
		//print the validation response on console
		MessageConsole console = new MessageConsole("Validation Result", null);
		console.activate();
		ConsoleErrorMatcher consoleErrorMatcher = new ConsoleErrorMatcher();
		consoleErrorMatcher.connect(console);
		console.addPatternMatchListener(consoleErrorMatcher);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
		console.newOutputStream().write("{\"FilePath\": "+"\""+fileUtils.getCurrentFilePath().toString()+"\",\n \"result\":");
		console.newOutputStream().write(validateResponse.getBytes());
		console.newOutputStream().write("\n}");
		return validateResponse;

	}
	
	public ArrayList<String> getContracts() throws RuleTreeDownloadError, IOException {
		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/contracts";
			} else {
				requestUrl = "https://" + host + "/papi/v0/contracts?accountSwitchKey="+openCredentials.getAccount_id();
			}
		}
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String contractsJson = null;
		ArrayList<String> contracts = new ArrayList<String>();
		try {
			request = requestFactory.buildGetRequest(new GenericUrl(uri));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getContracts();
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				contractsJson = response.parseAsString();
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(contractsJson);
				JSONArray items = (JSONArray)((JSONObject)json.get("contracts")).get("items");
				for (int i=0;i<items.size();i++) {
					contracts.add((((JSONObject)items.get(i)).get("contractId")).toString());
				}
				
			 //System.out.println("Rules response: "+response.parseAsString());
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			// TODO Auto-generated catch block
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contracts;
	}
	
	public ArrayList<String> getProductsForContractId(String contractId) throws RuleTreeDownloadError, IOException {
		if (retry == false) {
			if (openCredentials.getAccount_id() == null || openCredentials.getAccount_id().isEmpty()) {
				requestUrl = "https://" + host + "/papi/v0/products?contractId="+contractId;
			} else {
				requestUrl = "https://" + host + "/papi/v0/products?contractId="+contractId+"?accountSwitchKey="+openCredentials.getAccount_id();
			}
		}
		System.out.println("URL: " + requestUrl);
		URI uri = URI.create(requestUrl);
		HttpRequest request;
		HttpResponse response;
		String contractsJson = null;
		ArrayList<String> products = new ArrayList<String>();
		try {
			request = requestFactory.buildGetRequest(new GenericUrl(uri));
			HttpHeaders headers = new HttpHeaders();
			headers.setUserAgent("Eclipse-Plugin v0.0.1");
			request.setHeaders(headers);
			googleHttpSigner.sign(request);
			request.setFollowRedirects(false);
			request.setThrowExceptionOnExecuteError(false);
			response = request.execute();
			if (response.getStatusCode() == 302) {
				retry = true;
				requestUrl = response.getHeaders().getLocation();
				return getContracts();
			} else
				retry = false;
				httpResponseHandlerUtil.handleResponse(response);
				contractsJson = response.parseAsString();
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(contractsJson);
				JSONArray items = (JSONArray)((JSONObject)json.get("products")).get("items");
				for (int i=0;i<items.size();i++) {
					products.add((((JSONObject)items.get(i)).get("productId")).toString());
				}
				
			 //System.out.println("Rules response: "+response.parseAsString());
		} catch (IOException e) {
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (RequestSigningException e) {
			// TODO Auto-generated catch block
			throw new RuleTreeDownloadError(e.getMessage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return products;
	}

}
