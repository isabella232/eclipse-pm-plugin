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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.core.runtime.CoreException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.models.AvailableExternalResources;
import com.akamai.ruletreevalidator.models.Context;
import com.akamai.ruletreevalidator.models.ExternalResource;
import com.akamai.ruletreevalidator.models.ExternalResourceItem;
import com.akamai.ruletreevalidator.models.OpenCredentials;
import com.akamai.ruletreevalidator.models.SnippetType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;

public class JsonUtil {
	
	public JSONObject externalResourcesJson;

	public JSONObject readSchema(String productId, String ruleFormat) throws IOException, CredentialsMissingException {
		FileUtils fileUtils = new FileUtils();
		JSONParser parser = new JSONParser();
		System.out.println("Reading schema file for productid: "+productId);
		String jsonSchema = fileUtils.readSchemaFile(productId, ruleFormat);
		JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(jsonSchema);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	public JSONArray readExternalResources(String propertyId, String version) throws IOException, CredentialsMissingException, ConfigurationException, CoreException {
		FileUtils fileUtils = new FileUtils();
		JSONParser parser = new JSONParser();
		String externalResources = fileUtils.readExternalResourcesFile(propertyId, version);
		JSONArray json = null;
		try {
			json = (JSONArray) parser.parse(externalResources);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	

	public ArrayList<String> getAllowedBehaviors() throws IOException, CredentialsMissingException {
		FileUtils fileUtils = new FileUtils();
		Context context = fileUtils.readContextFile();
		System.out.println("Reading schema file for behaviors");
		JSONObject schemaJson = readSchema(context.getProductId(), context.getRuleFormat());
		JSONObject jsonChildObject = (JSONObject) schemaJson.get("definitions");
		JSONObject allOf = (JSONObject) ((JSONArray) ((JSONObject) jsonChildObject.get("behavior")).get("allOf"))
				.get(0);
		JSONArray allowedBehaviors = (JSONArray) ((JSONObject) ((JSONObject) allOf.get("properties")).get("name"))
				.get("enum");
		return allowedBehaviors;

	}

	public ArrayList<String> getAllowedCriterias() throws IOException, CredentialsMissingException {
		FileUtils fileUtils = new FileUtils();
		Context context = fileUtils.readContextFile();
		JSONObject schemaJson = readSchema(context.getProductId(), context.getRuleFormat());
		JSONObject jsonChildObject = (JSONObject) schemaJson.get("definitions");
		JSONObject allOf = (JSONObject) ((JSONArray) ((JSONObject) jsonChildObject.get("criteria")).get("allOf"))
				.get(0);
		JSONArray allowedCriterias = (JSONArray) ((JSONObject) ((JSONObject) allOf.get("properties")).get("name"))
				.get("enum");
		return allowedCriterias;

	}

	public ArrayList<String> getAllowedVariables() {
		ArrayList<String> allowedVariables = new ArrayList<String>();
		System.out.println("Reading rule tree for variables");
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		JSONObject ruleTree = ruleTreeUtils.readRuleTreeOpenInEditor();
		JSONArray variables = (JSONArray) ((JSONObject) ruleTree.get("rules")).get("variables");
		if (variables != null) {
			for (int i = 0; i < variables.size(); i++) {
				JSONObject jo = (JSONObject) variables.get(i);
				allowedVariables.add(jo.get("name").toString());
			}
		}
		System.out.println("Adding builtin variables");
		allowedVariables.addAll(getBuilinVariables());
		return allowedVariables;

	}

	public ArrayList<String> getBuilinVariables() {
		FileUtils fileUtils = new FileUtils();
		String vars = fileUtils.readBuiltinVariables();
		JSONParser parser = new JSONParser();
		ArrayList<String> builinVariables = new ArrayList<String>();
		JSONObject variablesJson = null;
		try {
			variablesJson = (JSONObject) parser.parse(vars);
			JSONObject variables = (JSONObject)variablesJson.get("internal-variables");
			System.out.println("Variables: "+variables);
			System.out.println("Variables Size: "+variables.size());
			HashMap varsMap = variables;
			Iterator hmIterator = varsMap.entrySet().iterator(); 
	        while (hmIterator.hasNext()) { 
	            Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
	            builinVariables.add(mapElement.getValue().toString());
	        }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builinVariables;
		
	}

	// create a body for provided name of type (behavior/criteria)
	public String getSnippets(String name, String type) throws IOException, CredentialsMissingException {
		if (type == SnippetType.CHILDREN.type) {
			String childrenSnippet = "{\n" + 
					"				\"name\" : \"\",\n" + 
					"				\"children\": [],\n" + 
					"				\"behaviors\": [],\n" + 
					"				\"criteria\": [],\n" + 
					"				\"criteriaMustSatisfy\": \"all\"\n" + 
					"			}";
			return childrenSnippet;
		}
		FileUtils fileUtils = new FileUtils();
		Context context = fileUtils.readContextFile();
		JSONObject schemaJson = readSchema(context.getProductId(), context.getRuleFormat());
		System.out.println("Reading " + type + ":" + name);
		JSONObject definitions = (JSONObject) schemaJson.get("definitions");
		JSONObject snippetType = (JSONObject) (((JSONObject) definitions.get("catalog")).get(type));
		JSONObject optionProperties = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) snippetType.get(name))
				.get("properties")).get("options")).get("properties");
		JSONObject snippet = new JSONObject();
		snippet.put("name", name);
		JSONObject options = new JSONObject();
		if (!optionProperties.isEmpty()) {
			for (Object option : optionProperties.keySet()) {
				String optionName = option.toString();
				if (((JSONObject) (optionProperties.get(optionName))).containsKey("default")) {
					Object optionDefaultValue = (Object) ((JSONObject) (optionProperties.get(optionName)))
							.get("default");
					options.put(optionName, optionDefaultValue);
				} else
					options.put(optionName, null);
			}
		}
		snippet.put("options", options);
		System.out.println("Generated Snippet: " + snippet.toString());
		return snippet.toString();
	}

	public JSONObject getJsonFromString(String str) {
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	public String getVariableSnippet() {
		String variableSnippet = "{\n" + "\"name\": \"PMUSER_var\",\n" + "\"value\": \"\",\n"
				+ "\"description\": \"\",\n" + "\"hidden\": false,\n" + "\"sensitive\": false\n" + "}";
		return variableSnippet;
	}
	
	public String getFilePathFromConsoleJson(String validationResult) {
		System.out.println("Console Output: "+validationResult);
		JSONObject consoleJson = getJsonFromString(validationResult);
		String filePath = (String) consoleJson.get("FilePath");
		return filePath;
	}
	
	public String generateFileNameFromPropertyDetails(String propertyDetails) {
		JSONObject json = getJsonFromString(propertyDetails);
		String propertyName = (String) json.get("propertyName");
		System.out.println("property version items:"+((JSONArray)((JSONObject) json.get("versions")).get("items")).get(0));
		String propertyVersionNum = ((JSONObject)((JSONArray)((JSONObject) json.get("versions")).get("items")).get(0)).get("propertyVersion").toString();
		return propertyName+"_"+propertyVersionNum+".json";
	}	
	
	@SuppressWarnings("unchecked")
	public List<ExternalResource> getExternalResourcesFromJson(JSONObject json) {
		List<ExternalResource> externalResources = new ArrayList<ExternalResource>();
		ExternalResource ext = null;
		
		for (AvailableExternalResources resource: AvailableExternalResources.values()) {
			externalResourcesJson = (JSONObject)json.get("externalResources");
			try {
				System.out.println("Calling method: "+"get"+resource.getConfigName()+"FromJson");
				Method method = this.getClass().getMethod("get"+resource.getConfigName()+"FromJson");
				Object externalResourceItemObject = method.invoke(this);
				ext = new ExternalResource(resource.getConfigName(), resource.getBehaviorName(),(List<ExternalResourceItem>) externalResourceItemObject);
				
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			externalResources.add(ext);
		}
		return externalResources;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getCpCodesFromJson() {
		List<ExternalResourceItem> cpCodes = new ArrayList<ExternalResourceItem>();
		JSONArray availableCpCodes = (JSONArray)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.AVAILABLE_CP_CODES.toString()));
		if (availableCpCodes != null) {
			for (int i=0;i<availableCpCodes.size();i++) {
				JSONObject cpCodeItem = (JSONObject) availableCpCodes.get(i);
				JSONObject cpCode = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("value",cpCodeItem);
				cpCode.put("name","cpCode");
				cpCode.put("options",value);
				String name = cpCodeItem.get("name").toString() +" - "+ cpCodeItem.get("id").toString();
				cpCodes.add(new ExternalResourceItem(name, cpCode.toString()));
			}
		}
		return cpCodes;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getNetStorageGroupsFromJson() {
		List<ExternalResourceItem> netStorageGroups = new ArrayList<ExternalResourceItem>();
		JSONArray availableNetStorageGroups = (JSONArray)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.AVAILABLE_NET_STORAGE_GROUPS.toString()));
		if (availableNetStorageGroups != null) {
			for (int i=0;i<availableNetStorageGroups.size();i++) {
				JSONObject netStorageItem = (JSONObject)availableNetStorageGroups.get(i);
				String downloadDomainName = netStorageItem.get("downloadDomainName").toString();
				String cpCode = ((JSONObject)((JSONArray)netStorageItem.get("cpCodeList")).get(0)).get("cpCode").toString();
				Object g2oToken = ((JSONObject)((JSONArray)netStorageItem.get("cpCodeList")).get(0)).get("g2oToken");
				if (g2oToken == null) {
					g2oToken = "null";
				}
				JSONObject origin = new JSONObject();
				origin.put("name","origin");
				String name = netStorageItem.get("name").toString()+" - "+downloadDomainName+"/"+cpCode;
				JSONObject netStorage = new JSONObject();
				JSONObject options = new JSONObject();
				netStorage.put("downloadDomainName", downloadDomainName);
				netStorage.put("cpCode",Integer.parseInt(cpCode));
				netStorage.put("g2oToken", g2oToken);
				options.put("originType", "NET_STORAGE");
				options.put("netStorage", netStorage);
				origin.put("options", options);
				netStorageGroups.add(new ExternalResourceItem(name,origin.toString()));
			}
		}
		return netStorageGroups;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getCustomBehaviorsFromJson() {
		List<ExternalResourceItem> customBehaviors = new ArrayList<ExternalResourceItem>();
		JSONObject customBehaviorsJson = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.CUSTOM_BEHAVIORS.toString()));
		if (customBehaviorsJson != null) {
			for (Object customBehaviorKey : customBehaviorsJson.keySet()) {
				JSONObject item = (JSONObject)customBehaviorsJson.get(customBehaviorKey);
				System.out.println("Custom Behavior Item: "+item.toString());
				String name = item.get("name").toString();
				String behaviorId = item.get("behaviorId").toString();
				System.out.println("Behavior name: "+name+"Behavior id: "+behaviorId);
				JSONObject customBehavior = new JSONObject();
				JSONObject options = new JSONObject();
				options.put("behaviorId", behaviorId);
				customBehavior.put("name", AvailableExternalResources.CUSTOM_BEHAVIORS.getBehaviorName());
				customBehavior.put("options", options);
				customBehaviors.add(new ExternalResourceItem(name, customBehavior.toString()));
			}
		}

		return customBehaviors;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getAWSAccessKeyFromJson() {
		List<ExternalResourceItem> awsAccessKeys = new ArrayList<ExternalResourceItem>();
		System.out.println("KEY FOR AWS: "+CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.AWS_ACCESS_KEYS.toString()));
		JSONObject awsAccessKeysJson = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.AWS_ACCESS_KEYS.toString()));
		for (Object awsAccessKey : awsAccessKeysJson.keySet()) {
			JSONObject item = (JSONObject)awsAccessKeysJson.get(awsAccessKey);
			//System.out.println("AWS Access Key Item: "+item.toString());
			String name = item.get("displayName").toString();
			String awsAccessKeyVersionGuid = item.get("guid").toString();
			String authenticationMethod = item.get("authenticationMethod").toString();
			String country = "UNKNOWN";
			String awsRegion = "UNKNOWN";
			Boolean accessKeyEncryptedStorage = true;
			String awsService = "s3";
			JSONObject options = new JSONObject();
			JSONObject originCharacteristics = new JSONObject();
			options.put("awsAccessKeyVersionGuid", awsAccessKeyVersionGuid);
			options.put("authenticationMethod", authenticationMethod);
			options.put("country", country);
			options.put("accessKeyEncryptedStorage", accessKeyEncryptedStorage);
			options.put("awsService", awsService);
			options.put("awsRegion", awsRegion);
			options.put("awsHost", "");
			originCharacteristics.put("name", "originCharacteristics");
			originCharacteristics.put("options", options);
			awsAccessKeys.add(new ExternalResourceItem(name, originCharacteristics.toString()));
		}
		return awsAccessKeys;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getGCSAccessKeyFromJson() {
		List<ExternalResourceItem> gcsAccessKeys = new ArrayList<ExternalResourceItem>();
		JSONObject awsAccessKeysJson = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.GCS_ACCESS_KEYS.toString()));
		if (awsAccessKeysJson != null) {
			for (Object awsAccessKey : awsAccessKeysJson.keySet()) {
				JSONObject item = (JSONObject)awsAccessKeysJson.get(awsAccessKey);
				String name = item.get("displayName").toString();
				String gwsAccessKeyVersionGuid = item.get("guid").toString();
				String authenticationMethod = item.get("authenticationMethod").toString();
				String country = "UNKNOWN";
				Boolean accessKeyEncryptedStorage = true;
				JSONObject options = new JSONObject();
				JSONObject originCharacteristics = new JSONObject();
				options.put("gwsAccessKeyVersionGuid", gwsAccessKeyVersionGuid);
				options.put("authenticationMethod", authenticationMethod);
				options.put("country", country);
				options.put("accessKeyEncryptedStorage", accessKeyEncryptedStorage);
				originCharacteristics.put("name", "originCharacteristics");
				originCharacteristics.put("options", options);
				gcsAccessKeys.add(new ExternalResourceItem(name, originCharacteristics.toString()));
			}
		}
		return gcsAccessKeys;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getAdaptiveAccelerationFromJson() throws IOException, CredentialsMissingException, ParseException {
		List<ExternalResourceItem> adaptiveAcceleration = new ArrayList<ExternalResourceItem>();
		JSONObject adaptiveAccelerationJson = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.ADAPTIVE_ACCELERATION.toString()));
		if (adaptiveAccelerationJson != null) {
			for (Object adaptiveAccelerationKey : adaptiveAccelerationJson.keySet()) {
				JSONObject item = (JSONObject)adaptiveAccelerationJson.get(adaptiveAccelerationKey);
				System.out.println("adaptiveAccelerationKey Item: "+item.toString());
				String name = item.get("name").toString();
				String source = item.get("id").toString();
				JSONParser jsonParser = new JSONParser();
				String adaptiveAccelerationSnippet = getSnippets("adaptiveAcceleration", SnippetType.BEHAVIORS.type);
				JSONObject adaptiveAccelerationSnippetObject = (JSONObject) jsonParser.parse(adaptiveAccelerationSnippet);
				JSONObject adaptiveAccelerationJsonObject = new JSONObject();
				JSONObject options = (JSONObject) adaptiveAccelerationSnippetObject.get("options");
				options.remove("source");
				options.put("source", source);
				System.out.println("Options created: "+options.toJSONString());
				adaptiveAccelerationJsonObject.put("name", "adaptiveAcceleration");
				adaptiveAccelerationJsonObject.put("options", options);
				adaptiveAcceleration.add(new ExternalResourceItem(name, adaptiveAccelerationJsonObject.toString()));
			}
		}
		
		return adaptiveAcceleration;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getTokenRevocationBlacklistFromJson() throws IOException, CredentialsMissingException, ParseException {
		List<ExternalResourceItem> tokenRevocationBlacklist = new ArrayList<ExternalResourceItem>();
		JSONObject tokenRevocationBlacklistJson = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.TOKEN_REVOCATION_BLACKLIST.toString()));
		if (tokenRevocationBlacklistJson != null) {
			for (Object tokenRevocationBlacklistKey : tokenRevocationBlacklistJson.keySet()) {
				JSONObject item = (JSONObject)tokenRevocationBlacklistJson.get(tokenRevocationBlacklistKey);
				String name = item.get("name").toString();
				int revokedListId = Integer.parseInt(item.get("id").toString());
				JSONParser jsonParser = new JSONParser();
				String segmentedContentProtectionSnippet = getSnippets(AvailableExternalResources.TOKEN_REVOCATION_BLACKLIST.getBehaviorName(), SnippetType.BEHAVIORS.type);
				System.out.println("Found Snippet: "+segmentedContentProtectionSnippet);
				JSONObject segmentedContentProtectionSnippetObject = (JSONObject) jsonParser.parse(segmentedContentProtectionSnippet);
				JSONObject segmentedContentProtection = new JSONObject();
				JSONObject options = (JSONObject) segmentedContentProtectionSnippetObject.get("options");
				options.remove("tokenRevocationEnabled");
				options.put("tokenRevocationEnabled",true);
				options.remove("revokedListId");
				options.put("revokedListId", revokedListId);
				segmentedContentProtection.put("name", AvailableExternalResources.TOKEN_REVOCATION_BLACKLIST.getBehaviorName());
				segmentedContentProtection.put("options", options);
				tokenRevocationBlacklist.add(new ExternalResourceItem(name, segmentedContentProtection.toString()));
			}
		}
		return tokenRevocationBlacklist;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getEdgeWorkerFromJson() throws IOException, CredentialsMissingException, ParseException {
		List<ExternalResourceItem> edgeWorkerList = new ArrayList<ExternalResourceItem>();
		JSONObject edgeWorkerJSON = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.EDGE_WORKERS.toString()));
		if (edgeWorkerJSON != null) {
			for (Object edgeWorkerKey : edgeWorkerJSON.keySet()) {
				JSONObject item = (JSONObject)edgeWorkerJSON.get(edgeWorkerKey);
				String name = item.get("name").toString();
				String edgeWorkerId = item.get("id").toString();
				JSONParser jsonParser = new JSONParser();
				String edgeWorkerBehaviorSnippet = getSnippets(AvailableExternalResources.EDGE_WORKERS.getBehaviorName(), SnippetType.BEHAVIORS.type);
				JSONObject edgeWorkerBehaviorSnippetObject = (JSONObject) jsonParser.parse(edgeWorkerBehaviorSnippet);
				JSONObject edgeWorkersList = new JSONObject();
				JSONObject options = (JSONObject) edgeWorkerBehaviorSnippetObject.get("options");
				options.remove("edgeWorkerId");
				options.put("edgeWorkerId", edgeWorkerId);
				edgeWorkersList.put("name", AvailableExternalResources.EDGE_WORKERS.getBehaviorName());
				edgeWorkersList.put("options", options);
				edgeWorkerList.add(new ExternalResourceItem(name, edgeWorkersList.toString()));
			}
		}
		return edgeWorkerList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ExternalResourceItem> getLogStreamFromJson() throws IOException, CredentialsMissingException, ParseException {
		List<ExternalResourceItem> logStreamsList = new ArrayList<ExternalResourceItem>();
		JSONObject logStreamsJSON = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.LOG_STREAM.toString()));
		if (logStreamsJSON != null) {
			for (Object logStreamKey : logStreamsJSON.keySet()) {
				JSONObject item = (JSONObject)logStreamsJSON.get(logStreamKey);
				System.out.println("logStreamKey Item: "+item.toString());
				String name = item.get("name").toString();
				int logStreamId = Integer.parseInt(item.get("id").toString());
				JSONObject dataStreamObject = new JSONObject();
				JSONObject options = new JSONObject();
				options.put("streamType", "LOG");
				options.put("logStreamTitle", "");
				options.put("logEnabled", true);
				options.put("logStreamName", logStreamId);
				dataStreamObject.put("name", AvailableExternalResources.LOG_STREAM.getBehaviorName());
				dataStreamObject.put("options", options);
				logStreamsList.add(new ExternalResourceItem(name, dataStreamObject.toString()));
			}
		}
		
		return logStreamsList;
	}
	
	public List<ExternalResourceItem> getJwtKeyWithAlgFromJson() throws IOException, CredentialsMissingException, ParseException {
		List<ExternalResourceItem> jwtKeysList = new ArrayList<ExternalResourceItem>();
		JSONObject jwtKeyWithAlgJSON = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.JWT_KEY_WITH_ALG.toString()));
 
		if (jwtKeyWithAlgJSON != null) {
			for (Object jwtKeys : jwtKeyWithAlgJSON.keySet()) {
				JSONObject item = (JSONObject)jwtKeyWithAlgJSON.get(jwtKeys);
				String name = item.get("name").toString();
				String jwt = item.get("jwt").toString();
				JSONParser jsonParser = new JSONParser();
				String edgeWorkerBehaviorSnippet = getSnippets(AvailableExternalResources.JWT_KEY_WITH_ALG.getBehaviorName(), SnippetType.BEHAVIORS.type);
				JSONObject edgeWorkerBehaviorSnippetObject = (JSONObject) jsonParser.parse(edgeWorkerBehaviorSnippet);
				JSONObject edgeWorkersList = new JSONObject();
				JSONObject options = (JSONObject) edgeWorkerBehaviorSnippetObject.get("options");
				options.remove("jwt");
				options.put("jwt",jwt);
				JSONObject jwtKeyObject = new JSONObject();
				jwtKeyObject.put("name", AvailableExternalResources.JWT_KEY_WITH_ALG.getBehaviorName());
				jwtKeyObject.put("options", options);
				jwtKeysList.add(new ExternalResourceItem(name, jwtKeyObject.toString()));
			}
		}
		
		return jwtKeysList;
	}
	
	public List<ExternalResourceItem> getCloudWrapperLocationFromJson() throws IOException, CredentialsMissingException, ParseException {
		List<ExternalResourceItem> clousWrapperLocationList = new ArrayList<ExternalResourceItem>();
		JSONObject cloudWrapperLocationJSON = (JSONObject)externalResourcesJson.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, AvailableExternalResources.CLOUD_WRAPPER_LOCATION.toString()));
		if (cloudWrapperLocationJSON != null) {
			for (Object cloudWrapperLocationKey : cloudWrapperLocationJSON.keySet()) {
				JSONObject item = (JSONObject)cloudWrapperLocationJSON.get(cloudWrapperLocationKey);
				System.out.println("cloudWrapperLocationKey Item: "+item.toString());
				String name = item.get("location").toString();
				String location = item.get("sroMapName").toString();
				JSONParser jsonParser = new JSONParser();
				String cloudWrapperSnippet = getSnippets(AvailableExternalResources.CLOUD_WRAPPER_LOCATION.getBehaviorName(), SnippetType.BEHAVIORS.type);
				JSONObject cloudWrapperSnippetObject = (JSONObject) jsonParser.parse(cloudWrapperSnippet);
				JSONObject options = (JSONObject) cloudWrapperSnippetObject.get("options");
				options.remove("location");
				options.put("location",location);
				JSONObject cloudWrapperObject = new JSONObject();
				cloudWrapperObject.put("name", AvailableExternalResources.CLOUD_WRAPPER_LOCATION.getBehaviorName());
				cloudWrapperObject.put("options", options);
				clousWrapperLocationList.add(new ExternalResourceItem(name, cloudWrapperObject.toString()));
			}
		}
		
		return clousWrapperLocationList;
	}
	

	public List<ExternalResourceItem> getAvailableExternalResource(AvailableExternalResources availableExternalResources) throws ConfigurationException, CoreException {
		FileUtils fileUtils = new FileUtils();
		Context context = fileUtils.readContextFile();
		List<ExternalResourceItem> externalResourceItems = new ArrayList<ExternalResourceItem>();
		try {
			JSONArray externalResourcesJson = (JSONArray)readExternalResources(context.getPropertyId(), context.getVersion());
			for (int i=0;i<externalResourcesJson.size();i++) {
				JSONObject externalResource = (JSONObject) externalResourcesJson.get(i);
				String name = externalResource.get("name").toString();
				if (name.equals(availableExternalResources.getConfigName())) {
					JSONArray externalResourceItemArray = (JSONArray)externalResource.get("externalResourceItem");
					for (int j=0;j<externalResourceItemArray.size();j++) {
						ObjectMapper objectMapper = new ObjectMapper();
						ExternalResourceItem externalResourceItem = objectMapper.readValue(externalResourceItemArray.get(j).toString(), ExternalResourceItem.class);
						externalResourceItems.add(externalResourceItem);
					}
					
				}
			}
		} catch (IOException | CredentialsMissingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return externalResourceItems;
	}
}
