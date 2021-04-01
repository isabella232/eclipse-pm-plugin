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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ValueSuggestionHandler implements IObjectActionDelegate {
	private Shell shell;
	 
	public ValueSuggestionHandler() {
		super();
	}
 
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}
 
 
	@Override
	public void run(IAction action) {
		try {
			//get editor
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			   IEditorPart editorPart = page.getActiveEditor();
 
			if (editorPart instanceof AbstractTextEditor) {
				int offset = 0;
				int length = 0;
				String selectedText = null;
				IEditorSite iEditorSite = editorPart.getEditorSite();
				if (iEditorSite != null) {
					//get selection provider
					ISelectionProvider selectionProvider = iEditorSite
							.getSelectionProvider();
					if (selectionProvider != null) {
						ISelection iSelection = selectionProvider
								.getSelection();
						//offset
						offset = ((ITextSelection) iSelection).getOffset();
						if (!iSelection.isEmpty()) {
							selectedText = ((ITextSelection) iSelection)
									.getText();
							//length
							length = ((ITextSelection) iSelection).getLength();
							System.out.println("length: " + length);
							 MessageDialog.openInformation(
							         shell,
							         "Do Something Menu",
							         "Length: " + length + "    Offset: " + offset);
						}
					}
				}
 
			}
		} catch (Exception e) {		}
	}
 
 
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
