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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.MissingPropertyDetailsException;
import com.akamai.ruletreevalidator.models.Context;
import com.akamai.ruletreevalidator.models.PropertyVersion;
import com.akamai.ruletreevalidator.models.SnippetType;
import com.akamai.ruletreevalidator.models.ValidationError;
import com.akamai.ruletreevalidator.models.ValidationErrorType;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class RuleTreeUtils {

	public boolean insertSnippet(String snippet, String type) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = page.getActiveEditor();
		if (part instanceof AbstractTextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());
			try {
				ITextSelection textSelection = (ITextSelection) editor.getSite().getSelectionProvider().getSelection();
				int offset = textSelection.getOffset();
				String text = doc.get(0, offset).replaceAll("\\s", "");
				System.out.println("text selection end line"+doc.get(offset, 10).contains("{"));
				if (isValidPlaceforSnippet(text, type)) {
					if (type == SnippetType.VARIABLE.type) {
						if (doc.getChar(offset-1) == '}') {
							doc.replace(offset, 0, "," + snippet);
						} else if (doc.getChar(offset-1) == '[' && doc.get(offset, 10).contains("{")) {
							doc.replace(offset, 0, snippet+",");
						} else doc.replace(offset, 0, snippet);
						beautifyJson();
						return true;
					} else {
						System.out.println("Is Character offset }: "+doc.getChar(offset-1));
						if (doc.getChar(offset-1) == '}') {
							doc.replace(offset, 0, ",\n" + snippet + "\n");
						} else if (doc.getChar(offset-1) == '[' && doc.get(offset, 10).contains("{")) {
							doc.replace(offset, 0, "\n" + snippet + ",\n");
						} else doc.replace(offset, 0, "\n" + snippet + "\n");
						beautifyJson();
						return true;
					}
					
				}

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;
	}

	public boolean createVariable(String snippet) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = page.getActiveEditor();
		if (part instanceof AbstractTextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());
			JsonUtil jsonSchemaUtil = new JsonUtil();
			String variables = "\"variables\": [";
			if (doc.get().indexOf(variables) != -1 || doc.get().indexOf(variables.replaceAll("\\s", "")) != -1) {
				int varStartIndex = doc.get().lastIndexOf(variables);
				int varEndIndex = varStartIndex + variables.length();
				try {
					doc.replace(varEndIndex, 0, "\n" + snippet + ",\n");
					editor.resetHighlightRange();
					editor.setHighlightRange(varEndIndex+22, 0, true);
					editor.setFocus();
					beautifyJson();
					return true;

				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				// Rule tree is missing variables block. So, first insert the block after
				// "rules": {
				try {
					String rules = "\"rules\": {";
					int rulesStartIndex = doc.get().lastIndexOf(rules);
					int rulesEndIndex = rulesStartIndex + rules.length();
					String variableBlock = "\"variables\": [\n"+ snippet +"\n],";
					doc.replace(rulesEndIndex, 0, "\n" + variableBlock + "\n");
					beautifyJson();
					return true;
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				beautifyJson();
			}

		}
		return true;
	}
	
	public void beautifyJson() {
		System.out.println("Beautifying the rule tree");
		String commandId = "org.eclipse.wst.sse.ui.format.document";
	    IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
	    try {
	        System.out.println("Executing beautify");
	    	handlerService.executeCommand(commandId, null);
	    } catch (Exception e1) {
	    	System.out.println("Exception while beautifying: "+e1.getMessage());
	    }
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = page.getActiveEditor();
		if (part != null) {
			ITextOperationTarget target = (ITextOperationTarget) part.getAdapter(ITextOperationTarget.class);
			if (target instanceof ISourceViewer) {
				target.doOperation(ISourceViewer.FORMAT);
			}
		}
	}

	public boolean isValidPlaceforSnippet(String text, String type) {
		int snippetOffset = 0;
		int countOpenBraces = 0;
		int countClosedBraces = 0;
		int countOpenCurlyBraces = 0;
		int countClosedCurlyBraces = 0;
		if (text.contains("\"" + type + "\":[")) {
			//If blank rule template is being inserted in nesting]
			if (type == SnippetType.CHILDREN.type && text.contains("\"" + type + "\":[{") && text.endsWith("}")) {
				snippetOffset = text.indexOf("\"" + type + "\":[{");
				countOpenBraces= -1;
				countOpenCurlyBraces = -1;
			} else snippetOffset = text.lastIndexOf("\"" + type + "\":[");
			
			for (int i = snippetOffset; i < text.length(); i++) {
				if (text.charAt(i) == '[') {
					countOpenBraces++;
				}
				if (text.charAt(i) == ']') {
					countClosedBraces++;
				}
				if (text.charAt(i) == '{') {
					countOpenCurlyBraces++;
				}
				if (text.charAt(i) == '}') {
					countClosedCurlyBraces++;
				}
			}
			if (type == SnippetType.VARIABLE.type) {
				//check whether variable is being inserted within options {} block
				if (text.lastIndexOf("\"options\":{") > -1 && text.length() > text.lastIndexOf("}")) {
					if (countOpenBraces > countClosedBraces && countOpenCurlyBraces > countClosedCurlyBraces) {
						String valueOfField = text.substring(text.lastIndexOf("\":"), text.length());
						if (valueOfField.matches("\".*$")) {
							// valid location for inserting variable e.g. "options" : { "name" : " or  "options" : { "name" : "xyz
							return true;
						}
					}
				} else return false;
			}
			if (countOpenBraces == countClosedBraces + 1 && countOpenCurlyBraces == countClosedCurlyBraces) {
				if (text.endsWith("}") || text.endsWith("[") || text.endsWith(",")) {
					// valid location for inserting snippet e.g. "behaviors" : [ OR "criteria": [
					// {}
					System.out.println("Returning true");
					return true;
				}
			}
		}
		return false;
	}

	public String createRuleTreeTemplate(String ruleTree) throws CredentialsMissingException {
		JSONParser parser = new JSONParser();
		JSONObject ruleTreeJson = new JSONObject();
		FileUtils fileUtils = new FileUtils();
		try {
			ruleTreeJson = (JSONObject) parser.parse(ruleTree);
			ruleTreeJson.remove("etag");
			ruleTreeJson.remove("warnings");
			ruleTreeJson.remove("errors");
			ruleTreeJson.remove("warnings");
			ruleTreeJson.put("$schema", fileUtils.getSchemaFilePath());

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Rule Tree after trimming: " + ruleTreeJson.toString());
		return ruleTreeJson.toString();
	}

	public String readRuleTreeFromEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String ruleTree = null;
		IEditorPart part = page.getActiveEditor();
		try {
			if (part instanceof AbstractTextEditor) {
				ITextEditor editor = (ITextEditor) part;
				IDocumentProvider dp = editor.getDocumentProvider();
				IDocument doc = dp.getDocument(editor.getEditorInput());
				ruleTree = doc.get();
				//Append productId and ruleFormat in the rule tree for validation
				JSONParser parser = new JSONParser();
				JSONObject ruleTreeJson = (JSONObject) parser.parse(ruleTree);
				FileUtils fileUtils = new FileUtils();
				Context context = fileUtils.readContextFile();
				ruleTreeJson.put("productId", context.getProductId());
				ruleTreeJson.put("ruleFormat", context.getRuleFormat());
				ruleTree = ruleTreeJson.toString();
			}
		} catch(ParseException e) {
			System.out.println("Error caused while pasing the object: "+e.getMessage());
		}
		System.out.println("Rule tree read before validating: " +ruleTree);
		
		return ruleTree;
	}
	
	public void refreshRuleTree() throws CredentialsMissingException {
		System.out.println("Refreshing rule tree");
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String ruleTree = null;
		IEditorPart part = page.getActiveEditor();
		try {
			if (part instanceof AbstractTextEditor) {
				ITextEditor editor = (ITextEditor) part;
				IDocumentProvider dp = editor.getDocumentProvider();
				IDocument doc = dp.getDocument(editor.getEditorInput());
				ruleTree = doc.get();
				doc.replace(0, doc.getLength(), createRuleTreeTemplate(ruleTree));
				beautifyJson();
				editor.doSave(new NullProgressMonitor());
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String readRuleTreeToValidate() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String ruleTree = null;
		IEditorPart part = page.getActiveEditor();
		if (part instanceof AbstractTextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());
			ruleTree = doc.get();
			FileUtils fileUtils = new FileUtils();
		}
		return ruleTree;
	}
	
	public ValidationError getErrorStringFromRuleTree(String jsonString, String path) {
		ValidationError validationError = new ValidationError();
		DocumentContext docCtx = JsonPath.parse(jsonString);
		path = path.replace("#", "$");
		path = path.replaceAll("/", ".");
		path = path.replaceAll("\"", "");
		Pattern pattern = Pattern.compile("\\d+");
	    Matcher matcher = pattern.matcher(path);
	    String error = null;
	    Object errorValue = new Object();
	    // convert the errorpath in jsonpath format e.g: #/rules/behaviors/0 -> $.rules.behaviors[0]
	    while (matcher.find()) {
	    	StringBuilder string = new StringBuilder(path);
	    	System.out.println("matcher start: "+matcher.start());
	        string.setCharAt(matcher.start()-1, '[');
	        System.out.println("step 1: "+string);
	        if (string.length() <= matcher.end()) {
	        	string.append("]");
	        } else {
	        	string.setCharAt(matcher.end(), ']');
	        }
	        System.out.println("step 2: "+string);
	        path = string.toString();
	    }
	    System.out.println("Path: "+path);
	    try {
	    	errorValue = docCtx.read(path);
	    	System.out.println("Path found");
	    } catch (PathNotFoundException pe) {
	    	System.out.println("Path not found");
	    	path = path.substring(0,path.lastIndexOf('.'));
	    	errorValue = docCtx.read(path);
	    }
	    
	    System.out.println("Object value: "+errorValue.toString());
	    
	    String err = errorValue.toString();
	    if (err.startsWith("{") && !err.startsWith("{{") ) {
	    	validationError.setType(ValidationErrorType.BLOCK);
	    	if (err.contains(",")) {
	    		error = err.substring(1, err.indexOf(","));
	    	}
	    	else {
	    		if (!(err.contains("{{user.")||err.contains("{{builtin."))) {
	    			error = err.replace("{", "");
		    		error = error.replace("}", "");
	    		}
	    		
	    	}
	    	String val = error.substring(error.lastIndexOf("=")+1, error.length());
	    	try {
	    		//check if the value is string or integer
	    		if (val != null && (val.matches("\\d+(\\.\\d+)?") || val.matches("^[-+]?[0-9]*\\.[0-9]+[aE]?[0-9]+$"))) {
	    			//if integer, the value won't be in quotes
		    		error = error.replaceAll("=", "\": ");
			    	error = "\""+error;
	    		} else {
	    			System.out.println("Value is non numeric");
		    		//if value is boolean then skip quotes
		    		System.out.println("Value : "+val);
		    		if(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false") || val.equalsIgnoreCase("null")) {
		    			error = error.replaceAll("=", "\": ");
				    	error = "\""+error;
		    		} else {
		    			//if value is string then add quotes
		    			error = error.replaceAll("=", "\": \"");
				    	error = "\""+error+"\"";
		    		}
	    		}
	    		
	    	} catch (NumberFormatException e) {
	    		
	    	}
	    	validationError.setErrorString(error);
	    	System.out.println("Error String in Block: "+error);
	    	
	    } else {
	    	validationError.setType(ValidationErrorType.VALUE);
	    	error = "\""+path.substring(path.lastIndexOf('.')+1,path.length())+"\": "+"\""+errorValue.toString()+"\"";
	    	validationError.setErrorString(error);
	    	System.out.println("Error String: "+error);
	    }
	    return validationError;
	}

	public void addListner() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String ruleTree = null;
		IEditorPart part = page.getActiveEditor();
		if (part instanceof AbstractTextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());
			// int offset = textSelection.getOffset();
			doc.addDocumentListener(new IDocumentListener() // **this is line 45**

			{
				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					ITextSelection textSelection = (ITextSelection) editor.getSite().getSelectionProvider()
							.getSelection();
					// TODO Auto-generated method stub
					System.out.println("Hello offset: " + textSelection.getText());
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					// TODO Auto-generated method stub
					ITextSelection textSelection = (ITextSelection) editor.getSite().getSelectionProvider()
							.getSelection();
					System.out.println("Hello second" + textSelection.getText());
					beautifyJson();

				}
			});
		}
	}
	
	public JSONObject readRuleTreeOpenInEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = page.getActiveEditor();
		JSONObject json = null;
		if (part instanceof AbstractTextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());
			JSONParser parser = new JSONParser();
			try {
				json = (JSONObject) parser.parse(doc.get());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return json;
	}

	public boolean productIdPresentInRuleTree() {
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		JSONObject ruleTreeInEditor = ruleTreeUtils.readRuleTreeOpenInEditor();
		if (!ruleTreeInEditor.containsKey("productId")) {
			return false;
		} return true;
	}
	
	public PropertyVersion getPropertyVersionFromRuleTree() throws MissingPropertyDetailsException {
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		JSONObject ruleTreeInEditor = ruleTreeUtils.readRuleTreeOpenInEditor();
		PropertyVersion propertyVersion = new PropertyVersion();
		try {

			propertyVersion.setId(ruleTreeInEditor.get("propertyId").toString());
			propertyVersion.setVersion(Integer.parseInt(ruleTreeInEditor.get("propertyVersion").toString()));
		} catch (NullPointerException ne) {
			throw new MissingPropertyDetailsException("Missing Property Details in the Rule Tree - propertyId, propertyVersion");
		}
		return propertyVersion;
	}
	
	public void updateRuleTree() throws CredentialsMissingException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String ruleTree = readRuleTreeFromEditor();
		IEditorPart part = page.getActiveEditor();
		if (part instanceof AbstractTextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider dp = editor.getDocumentProvider();
			IDocument doc = dp.getDocument(editor.getEditorInput());
			try {
				doc.replace(0, doc.getLength(), createRuleTreeTemplate(ruleTree));
				beautifyJson();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
