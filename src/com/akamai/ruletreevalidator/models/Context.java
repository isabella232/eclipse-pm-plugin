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
/**
 * @author michalka
 */
public class Context {
	
	String accountId;
	String propertyName;
	String propertyId;
	String version;
	String assetId;
	String groupId;
	String contractId;
	String productId;
	String ruleFormat;
	
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}
	public String getRuleFormat() {
		return ruleFormat;
	}
	public void setRuleFormat(String ruleFormat) {
		this.ruleFormat = ruleFormat;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getAssetId() {
		return assetId;
	}
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public Context(String accountId, String propertyName, String propertyId, String version, String assetId,
			String groupId, String contractId, String productId, String ruleFormat) {
		super();
		this.accountId = accountId;
		this.propertyName = propertyName;
		this.propertyId = propertyId;
		this.version = version;
		this.assetId = assetId;
		this.groupId = groupId;
		this.contractId = contractId;
		this.productId = productId;
		this.ruleFormat = ruleFormat;
	}
	public Context() {
		super();
	}
	
	

}
