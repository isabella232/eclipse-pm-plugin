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
package com.akamai.ruletreevalidator.models;
import java.util.List;
/**
 * @author michalka
 */

public class ExternalResource {
	
	String name;
	
	String behaviorName;
	
	List<ExternalResourceItem> externalResourceItem;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * @param externalResourceItem
	 */
	public ExternalResource(String name, List<ExternalResourceItem> externalResourceItem) {
		super();
		this.name = name;
		this.externalResourceItem = externalResourceItem;
	}

	/**
	 * @param name
	 * @param behaviorName
	 * @param externalResourceItem
	 */
	public ExternalResource(String name, String behaviorName, List<ExternalResourceItem> externalResourceItem) {
		super();
		this.name = name;
		this.behaviorName = behaviorName;
		this.externalResourceItem = externalResourceItem;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the externalResourceItem
	 */
	public List<ExternalResourceItem> getExternalResourceItem() {
		return externalResourceItem;
	}

	/**
	 * @param externalResourceItem the externalResourceItem to set
	 */
	public void setExternalResourceItem(List<ExternalResourceItem> externalResourceItem) {
		this.externalResourceItem = externalResourceItem;
	}

	/**
	 * @return the behaviorName
	 */
	public String getBehaviorName() {
		return behaviorName;
	}

	/**
	 * @param behaviorName the behaviorName to set
	 */
	public void setBehaviorName(String behaviorName) {
		this.behaviorName = behaviorName;
	}


}
