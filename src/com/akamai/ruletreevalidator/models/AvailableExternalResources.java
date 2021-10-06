/**
 * 
 */
package com.akamai.ruletreevalidator.models;

/**
 * @author michalka
 *
 */
public enum AvailableExternalResources {
	AVAILABLE_CP_CODES("CpCodes", "cpCode", "{\"name\":\"cpCode\"", "value"),
	AVAILABLE_NET_STORAGE_GROUPS("NetStorageGroups", "origin", "{\"name\":\"origin\"", "value"),
	CUSTOM_BEHAVIORS("CustomBehaviors", "customBehavior", "{\"name\":\"customBehavior\"", ""),
	AWS_ACCESS_KEYS("AWSAccessKey", "originCharacteristics", "{\"name\":\"originCharacteristics\"", "options"),
	GCS_ACCESS_KEYS("GCSAccessKey", "originCharacteristics", "{\"name\":\"originCharacteristics\"", "options"),
	ADAPTIVE_ACCELERATION("AdaptiveAcceleration", "adaptiveAcceleration", "{\"name\":\"adaptiveAcceleration\"",
			"options"),
	TOKEN_REVOCATION_BLACKLIST("TokenRevocationBlacklist", "segmentedContentProtection",
			"{\"name\":\"segmentedContentProtection\"", "options"),
	EDGE_WORKERS("EdgeWorker", "edgeWorker", "{\"name\":\"edgeWorker\"", "options"),
	LOG_STREAM("LogStream", "datastream", "{\"name\":\"datastream\"", "options"),
	JWT_KEY_WITH_ALG("JwtKeyWithAlg", "verifyJsonWebTokenForDcp", "{\"name\":\"verifyJsonWebTokenForDcp\"", "options"),
	CLOUD_WRAPPER_LOCATION("CloudWrapperLocation", "cloudWrapper", "{\"name\":\"cloudWrapper\"", "options");

	private final String configName;
	private final String behaviorName;
	private final String snippetInRuleTree;
	private final String externalResourcesFieldToBeInserted;

	private AvailableExternalResources(String configName, String behaviorName, String snippetInRuleTree,
			String externalResourcesFieldToBeInserted) {
		this.configName = configName;
		this.behaviorName = behaviorName;
		this.snippetInRuleTree = snippetInRuleTree;
		this.externalResourcesFieldToBeInserted = externalResourcesFieldToBeInserted;
	}

	public static AvailableExternalResources getByConfigName(String configName) {
		if (configName == null) {
			return null;
		}

		for (AvailableExternalResources availableExternalResources : AvailableExternalResources.values()) {
			if (availableExternalResources.configName.equals(configName)) {
				return availableExternalResources;
			}
		}
		throw new IllegalArgumentException(
				AvailableExternalResources.class.getName() + " does not support enum const: " + configName);
	}

	public String getConfigName() {
		return configName;
	}

	public String getBehaviorName() {
		return behaviorName;
	}

	/**
	 * @return the snippetInRuleTree
	 */
	public String getSnippetInRuleTree() {
		return snippetInRuleTree;
	}

	/**
	 * @return the externalResourcesFieldToBeInserted
	 */
	public String getExternalResourcesFieldToBeInserted() {
		return externalResourcesFieldToBeInserted;
	}

}
