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
public class ProductRuleFormat {
	public ProductRuleFormat() {
		super();
	}
	String productId;
	String ruleFormat;
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getRuleFormat() {
		return ruleFormat;
	}
	public void setRuleFormat(String ruleFormat) {
		this.ruleFormat = ruleFormat;
	}
	public ProductRuleFormat(String productId, String ruleFormat) {
		super();
		this.productId = productId;
		this.ruleFormat = ruleFormat;
	}
	
	
}
