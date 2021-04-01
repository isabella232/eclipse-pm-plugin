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
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.akamai.ruletreevalidator.models.ValidationError;
import com.akamai.ruletreevalidator.models.ValidationErrorType;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import net.minidev.json.JSONUtil;


public class ConsoleErrorMatcher implements IPatternMatchListener {
	private TextConsole console;

	@Override
	public void connect(TextConsole console) {
		System.out.println("Connected pattern match listener delegate");
		this.console = console;
	}

	@Override
	public void disconnect() {
		console = null;
	}

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			int offset = event.getOffset();
			int length = event.getLength();
			//System.out.println("Event offset: "+event.getOffset()+"Event Length: "+event.getLength());
			String errorString = console.getDocument().get(event.getOffset(), event.getLength());
			System.out.println("Error String: "+errorString);
			JsonUtil jsonSchemaUtil = new JsonUtil();
			IHyperlink link = makeHyperlink(errorString, jsonSchemaUtil.getFilePathFromConsoleJson(console.getDocument().get()));
			console.addHyperlink(link, offset, length);
		} catch (BadLocationException e) {
			// ignore
		}
	}

	private static IHyperlink makeHyperlink(String errorString, String validationResultFilePath) {
		return new IHyperlink() {
			@Override
			public void linkExited() {
				// ignore
			}

			@Override
			public void linkEntered() {
				// ignore
			}

			@Override
			public void linkActivated() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart editorPart = page.getActiveEditor();
				FileUtils fileUtils = new FileUtils();
				if (!fileUtils.getCurrentFilePath().toString().equals(validationResultFilePath)) {
				    try {
				    	IFileStore fileStore = EFS.getLocalFileSystem().getStore(new URI(validationResultFilePath)); 
				    	IDE.openEditorOnFileStore( page, fileStore );
				        jumpToPosition(editorPart, 5, 5, errorString);
				    } catch ( PartInitException e ) {
				        //Put your exception handler here if you wish to
				    }catch ( URISyntaxException e ) {
				        //Put your exception handler here if you wish to
				    }
				}
				else {
					jumpToPosition(editorPart, 5, 5, errorString);
				}
				
			}
		};
	}

	private static void jumpToPosition(IEditorPart editorPart, int lineNumber, int lineOffset, String errorString) {
		if (editorPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editorPart;
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			RuleTreeUtils ruleTreeUtils = new RuleTreeUtils();
			ValidationError error = ruleTreeUtils.getErrorStringFromRuleTree(document.get(), errorString);
			int errorEndIndex = 0;
			int errorIndex = document.get().indexOf(error.getErrorString());
			if (error.getType().equals(ValidationErrorType.BLOCK)) {
				errorEndIndex = document.get().indexOf('}', errorIndex);
			} else errorEndIndex = errorIndex+50;
			
			if (document != null) {
				IRegion region = null;
				try {
					region = document.getLineInformation(lineNumber - 1);
				} catch (BadLocationException exception) {
					// ignore
				}

				if (region != null)
					System.out.println("File Title: "+textEditor.getTitle());
					textEditor.resetHighlightRange();
					textEditor.setHighlightRange(errorIndex, errorEndIndex-errorIndex, true);
					textEditor.selectAndReveal(errorIndex, errorEndIndex-errorIndex);
			}
		}
	}

	@Override
	public int getCompilerFlags() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLineQualifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPattern() {
		// TODO Auto-generated method stub
		return "\"#.*\"";
	}

}
