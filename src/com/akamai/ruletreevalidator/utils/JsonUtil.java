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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.models.Context;
import com.akamai.ruletreevalidator.models.SnippetType;
import com.google.gson.JsonObject;

public class JsonUtil {

	public JSONObject readSchema(String productId, String ruleFormat) throws IOException, CredentialsMissingException {
		FileUtils fileUtils = new FileUtils();
		JSONParser parser = new JSONParser();
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

}
