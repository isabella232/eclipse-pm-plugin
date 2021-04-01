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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Property {
	String id;
	List<PropertyVersion> versions;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<PropertyVersion> getVersions() {
		return versions;
	}
	public void setVersions(List<PropertyVersion> versions) {
		this.versions = versions;
	}
	public Property(String id, List<PropertyVersion> versions) {
		super();
		this.id = id;
		this.versions = versions;
	}
	public Property() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public HashMap<String, Integer> getPropertyVersionsWithStatus() {
		HashMap<String, Integer> propertyVersions =  new HashMap<String, Integer>();
		for(PropertyVersion pv: this.versions) {
			propertyVersions.put(pv.getVersion().toString()+pv.getProductionStatus()+pv.getStagingStatus(), pv.getVersion());
		}
		
		return sortByValue(propertyVersions);
	}
	
	//Method to sort the property versions HashMap by versionId
	public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Integer> > list = 
               new LinkedList<Map.Entry<String, Integer> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
            public int compare(Map.Entry<String, Integer> o1,  
                               Map.Entry<String, Integer> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>(); 
        for (Map.Entry<String, Integer> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
}
