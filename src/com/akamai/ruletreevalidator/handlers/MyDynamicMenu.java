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
import java.util.Date;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MyDynamicMenu extends ContributionItem {
	public MyDynamicMenu() {
	}

	public MyDynamicMenu(String id) {
		super(id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		//Here you could get selection and decide what to do
		//You can also simply return if you do not want to show a menu
		
		//create the menu item
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		menuItem.setText("My menu item (" + new Date() + ")");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//what to do when menu is subsequently selected.
				System.err.println("Dynamic menu selected");
			}
		});
	}
}
