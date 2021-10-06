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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.akamai.ruletreevalidator.exceptions.CredentialsMissingException;
import com.akamai.ruletreevalidator.exceptions.MissingPropertyDetailsException;
import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
import com.akamai.ruletreevalidator.models.Context;
import com.akamai.ruletreevalidator.models.OpenCredentials;
import com.akamai.ruletreevalidator.models.ProductRuleFormat;
import com.akamai.ruletreevalidator.models.PropertyVersion;
import com.akamai.ruletreevalidator.papiCalls.PapiOpenCalls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;


public class FileUtils {
	
	public void saveSchemaFileUnderWorkspace(String fileContent, String productId, String ruleFormat) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFolder schemaFolder = resourcesFolder.getFolder("schemas");
		IFile file = schemaFolder.getFile(productId+"_"+ruleFormat+".json");
		try {
			//at this point, no resources have been created
			if (!project.exists()) project.create(null);
			//project.setTeamPrivateMember(true);
			if (!resourcesFolder.exists())
				resourcesFolder.create(IResource.NONE, true, null);
			    resourcesFolder.setTeamPrivateMember(true);
			if (!schemaFolder.exists())
				schemaFolder.create(IResource.NONE, true, null);
				schemaFolder.setTeamPrivateMember(true);
			if (!file.exists()) {
			    byte[] bytes = fileContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			} else {
				file.delete(true, null);
				byte[] bytes = fileContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveExternalResourcesUnderWorkspace(String fileContent, String propertyId, String version) {
		System.out.println("Saving external resources");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFolder externalResourcesFolder = resourcesFolder.getFolder("externalResources");
		IFile file = externalResourcesFolder.getFile(propertyId+"_v"+version+".json");
		try {
			//at this point, no resources have been created
			if (!project.exists()) project.create(null);
			//project.setTeamPrivateMember(true);
			if (!resourcesFolder.exists()) {
				resourcesFolder.create(IResource.NONE, true, null);
			    resourcesFolder.setTeamPrivateMember(true);
			}
				
			if (!externalResourcesFolder.exists()) {
				System.out.println("Creating external resources folder");
				externalResourcesFolder.create(IResource.NONE, true, null);
				externalResourcesFolder.setTeamPrivateMember(true);
			}
				
			if (!file.exists()) {
			    byte[] bytes = fileContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			} else {
				file.delete(true, null);
				byte[] bytes = fileContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean schemaFileExist(String productID, String ruleFormat) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFolder scehmaFolder = resourcesFolder.getFolder("schemas");
		IFile file = scehmaFolder.getFile(productID+"_"+ruleFormat+".json");
		if (!file.exists()) {
			return false;
		}
		return true;
	}
	
	public void saveContextFileUnderWorkspace(String propertyVersionRules) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder schemaTreefolder = project.getFolder("resources");
		IFile file = schemaTreefolder.getFile("context.json");
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(propertyVersionRules);
		    JSONObject contextInfo = new JSONObject();
		    contextInfo.put("propertyId", json.get("propertyId").toString());
		    contextInfo.put("propertyName", json.get("propertyName").toString());
		    contextInfo.put("accountId", json.get("accountId").toString());
		    contextInfo.put("contractId", json.get("contractId").toString());
		    contextInfo.put("groupId", json.get("groupId").toString());
		    contextInfo.put("assetId", json.get("assetId").toString());
		    JSONObject versionInfo = (JSONObject)((JSONArray)((JSONObject)json.get("versions")).get("items")).get(0);
		    contextInfo.put("productId", (versionInfo.get("productId").toString()));
		    contextInfo.put("ruleFormat", (versionInfo.get("ruleFormat").toString()));
		    contextInfo.put("version", (versionInfo.get("propertyVersion").toString()));
		    String contextInfoContent = contextInfo.toString();
		  //at this point, no resources have been created
			if (!project.exists()) project.create(null);
			if (!project.isOpen()) project.open(null);
			if (!schemaTreefolder.exists()) 
				schemaTreefolder.create(IResource.NONE, true, null);
			if (!file.exists()) {
			    byte[] bytes = contextInfoContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			} else {
				file.delete(true, null);
				byte[] bytes = contextInfoContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			}
			
		} catch (ParseException pe) {
			System.out.println("Exception caught while parsing response: "+pe.getMessage());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateProductRuleFormatInContextFile(ProductRuleFormat productRuleFormat) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder schemaTreefolder = project.getFolder("resources");
		IFile file = schemaTreefolder.getFile("context.json");
		try {
			JSONParser parser = new JSONParser();
			InputStream contextFileConent = file.getContents();
			InputStreamReader isReader = new InputStreamReader(contextFileConent);
		      //Creating a BufferedReader object
		      BufferedReader reader = new BufferedReader(isReader);
		      StringBuffer sb = new StringBuffer();
		      String str;
		      while((str = reader.readLine())!= null){
		         sb.append(str);
		      }
			//System.out.println("Schema File InputStream: "+sb.toString());
			String context = sb.toString();
			JSONObject contextInfo = (JSONObject) parser.parse(context);
			contextInfo.put("productId", productRuleFormat.getProductId());
		    contextInfo.put("ruleFormat", productRuleFormat.getRuleFormat());
		    String contextInfoContent = contextInfo.toString();
		    if (!project.exists()) project.create(null);
			if (!project.isOpen()) project.open(null);
			if (!schemaTreefolder.exists()) 
				schemaTreefolder.create(IResource.NONE, true, null);
			if (!file.exists()) {
			    byte[] bytes = contextInfoContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			} else {
				file.delete(true, null);
				byte[] bytes = contextInfoContent.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			}
			
		} catch (ParseException pe) {
			System.out.println("Exception caught while parsing response: "+pe.getMessage());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveCredentailsUnderWorkspace(OpenCredentials openCredentials) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFile file = resourcesFolder.getFile("creds.json");
		try {
			ObjectMapper mapper = new ObjectMapper();
		      //Converting the Object to JSONString
		      String jsonString = mapper.writeValueAsString(openCredentials);
		    System.out.println("Open Credentials Info: "+jsonString);
		  //at this point, no resources have been created
			if (!project.exists()) project.create(null);
			if (!project.isOpen()) project.open(null);
			if (!resourcesFolder.exists()) 
				resourcesFolder.create(IResource.NONE, true, null);
				//resourcesFolder.setTeamPrivateMember(true);
			if (!file.exists()) {
			    byte[] bytes = jsonString.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			} else {
				file.delete(true, null);
				byte[] bytes = jsonString.getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
			    file.create(source, IResource.NONE, null);
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void openFile(String result, String fileName) {
		try {
			File newFile = new File(fileName);
			newFile.createNewFile();
			FileWriter myWriter = new FileWriter(fileName);
		    myWriter.write(result);
		    myWriter.close();
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(newFile.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditorOnFileStore(page, fileStore );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public String readSchemaFile(String productId, String ruleFormat) throws IOException, CredentialsMissingException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFolder schemaFolder = resourcesFolder.getFolder("schemas");
		IFile file = schemaFolder.getFile(productId+"_"+ruleFormat+".json");
		String schema = null;
		//If schema file doesn't exist then download
		if (!file.exists()) {
			System.out.println("Schema file not present. Downloading it now");
			PapiOpenCalls papiOpenCalls;
			try {
				papiOpenCalls = new PapiOpenCalls();
				schema = papiOpenCalls.getRuleTreeSchemaForProductId(productId, ruleFormat);
				return schema;
			} catch (RuleTreeDownloadError e) {
				e.printStackTrace();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		try {
			//System.out.println("Schema file present at this location: "+file.getLocationURI().toString());
			InputStream schemaFileContents = file.getContents();
			InputStreamReader isReader = new InputStreamReader(schemaFileContents);
		      //Creating a BufferedReader object
		      BufferedReader reader = new BufferedReader(isReader);
		      StringBuffer sb = new StringBuffer();
		      String str;
		      while((str = reader.readLine())!= null){
		         sb.append(str);
		      }
			schema = sb.toString();
			System.out.println("Schema file present at this location: "+file.getLocationURI().toString());
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return schema;
	}
	
	public Context readContextFile() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder schemaTreefolder = project.getFolder("resources");
		IFile file = schemaTreefolder.getFile("context.json");
		String contextJson = null;
		Context context = null;
		System.out.println("Reading Context File");
		try {
			InputStream contextFileContents = file.getContents();
			InputStreamReader isReader = new InputStreamReader(contextFileContents);
		      //Creating a BufferedReader object
		      BufferedReader reader = new BufferedReader(isReader);
		      StringBuffer sb = new StringBuffer();
		      String str;
		      while((str = reader.readLine())!= null){
		         sb.append(str);
		      }
			//System.out.println("Context File InputStream: "+sb.toString());
			contextJson = sb.toString();
			ObjectMapper mapper = new ObjectMapper();
			context = mapper.readValue(contextJson, Context.class);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return context;
	}
	
	public String readExternalResourcesFile(String propertyId, String version) throws ConfigurationException, IOException, CredentialsMissingException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFolder externalResourcesFolder = resourcesFolder.getFolder("externalResources");
		String externalResourcesJson = null;
		if (externalResourcesFolder.exists()) {
			IFile file = externalResourcesFolder.getFile(propertyId+"_v"+version+".json");
			if (file.exists()) {
				System.out.println("Reading External Resources File");
				try {
					InputStream externalResoucesFileContent = file.getContents();
					InputStreamReader isReader = new InputStreamReader(externalResoucesFileContent);
				      //Creating a BufferedReader object
				      BufferedReader reader = new BufferedReader(isReader);
				      StringBuffer sb = new StringBuffer();
				      String str;
				      while((str = reader.readLine())!= null){
				         sb.append(str);
				      }
					externalResourcesJson = sb.toString();
					return externalResourcesJson;
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		PapiOpenCalls papiOpenCalls = new PapiOpenCalls();
		try {
			papiOpenCalls.getExternalResourcesForPropertyVersion(propertyId, version);
			externalResourcesJson = readExternalResourcesFile(propertyId, version);
		} catch (RuleTreeDownloadError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return externalResourcesJson;
	}
	
	
	public OpenCredentials readCredentialsFile() throws CredentialsMissingException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder schemaTreefolder = project.getFolder("resources");
		IFile file = schemaTreefolder.getFile("creds.json");
		String credsJson = null;
		OpenCredentials openCredentials = null;
		System.out.println("Reading Credentials File");
		try {
			if (!file.exists()) {
				throw new CredentialsMissingException("missing credentials");
			}
			InputStream contextFileContents = file.getContents();
			InputStreamReader isReader = new InputStreamReader(contextFileContents);
		      //Creating a BufferedReader object
		      BufferedReader reader = new BufferedReader(isReader);
		      StringBuffer sb = new StringBuffer();
		      String str;
		      while((str = reader.readLine())!= null){
		         sb.append(str);
		      }
			System.out.println("Credentials File InputStream: "+sb.toString());
			credsJson = sb.toString();
			ObjectMapper mapper = new ObjectMapper();
			openCredentials = mapper.readValue(credsJson, OpenCredentials.class);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return openCredentials;
	}
	
	public ArrayList<String> readSectionsFromEdgeRc(String filePath) {
		BufferedReader reader;
		System.out.println("Reading section from edgerc with filePath:" +filePath);
		ArrayList<String> sectionNames = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(
					filePath));
			String line = reader.readLine();
			while (line != null) {
				if (line != null && line.matches("\\[([^]]+)\\]")) {
					String matchedName = line.substring(line.indexOf('[')+1, line.indexOf(']'));
					//System.out.println("matched: "+matchedName);
					sectionNames.add(matchedName);
				}
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sectionNames;
		
	}
	
	public String readBuiltinVariables() {
		System.out.println("Reading builtin variables file");
		InputStream is = getClass().getClassLoader().getResourceAsStream("variable_support.json");
		String text = null;
		
	    try (Reader reader = new InputStreamReader(is)) {
	        try {
				text = CharStreams.toString(reader);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return text;
	}
	
	public void updateContextFiles() throws CredentialsMissingException, MissingPropertyDetailsException, RuleTreeDownloadError {
		RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
		Context context = readContextFile();
		System.out.println("Getting property version details");
		try {
			PropertyVersion propertyVersion = ruleTreeUtils.getPropertyVersionFromRuleTree();
			System.out.println("Property Version details: "+propertyVersion.getId());
			if (context.getPropertyId()== null || context.getVersion() == null || !context.getPropertyId().equalsIgnoreCase(propertyVersion.getId()) || !context.getVersion().equalsIgnoreCase(propertyVersion.getVersion().toString())) {
	        	PapiOpenCalls papiOpenCalls = new PapiOpenCalls();
	        	papiOpenCalls.getPropertyVersionDetails(propertyVersion.getId(),propertyVersion.getVersion().toString());
			} else {
				System.out.println("Context file is up to date");
			}
        } catch (IOException e) {
        	
        } catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public String getSchemaFilePath() throws CredentialsMissingException {
		Context context = readContextFile();
		String productId = context.getProductId();
		String ruleFormat = context.getRuleFormat();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject(".papiCreds");
		IFolder resourcesFolder = project.getFolder("resources");
		IFolder scehmaFolder = resourcesFolder.getFolder("schemas");
		IFile file = scehmaFolder.getFile(productId+"_"+ruleFormat+".json");
		if (!file.exists()) {
			System.out.println("Schema file missing. Downloading it now");
			try {
				PapiOpenCalls papiOpenCalls = new PapiOpenCalls();
	        	papiOpenCalls.getRuleTreeSchemaForProductId(productId, ruleFormat);
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RuleTreeDownloadError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Schema file exists : "+file.exists()+" location - "+file.getLocationURI().toString());
		return file.getLocationURI().toString();
	}

	public URI getCurrentFilePath() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorInput fileInEditor = page.getActiveEditor().getEditorInput();
		FileStoreEditorInput file = (FileStoreEditorInput)fileInEditor;
		return file.getURI();
	}
}
