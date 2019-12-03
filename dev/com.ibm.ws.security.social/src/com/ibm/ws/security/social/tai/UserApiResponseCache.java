/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.security.social.tai;

import com.ibm.ws.security.social.SocialLoginConfig;
import com.ibm.ws.security.common.structures.BoundedHashMap;

public class UserApiResponseCache {
	private static BoundedHashMap cachedResponses = new BoundedHashMap(500);
	private static Object lock = new Object();
	private static UserApiResponseCache self = new UserApiResponseCache();
	
	class MapEntry{
		long timestamp;
		String userApiResponse;
		MapEntry(long time, String response){
			timestamp = time;
			userApiResponse = response;
		}
	}
	
    static String getCachedResponse(SocialLoginConfig socialConfig, String accessToken) {
    	String key = computeCacheKey(socialConfig, accessToken);
    	MapEntry candidate = null;
    	synchronized (lock) {
    		candidate = (MapEntry) cachedResponses.get(key);
    	}
    	if (candidate != null) {
    		long now = System.currentTimeMillis();
    		long maxage = socialConfig.getUserApiResponseCacheDurationMsec();
    		if (candidate.timestamp + maxage > now) {
    			return candidate.userApiResponse;
    		}
    		// it's expired, purge it
    		synchronized(lock) {
    			cachedResponses.remove(key);
    		}
    	}
    	return null;
    }
    
    static void putCachedResponse(SocialLoginConfig socialConfig, String accessToken, String response) {
    	String key = computeCacheKey(socialConfig, accessToken);
    	MapEntry entry = self.new MapEntry(System.currentTimeMillis(), response);
    	synchronized(lock) {
    		cachedResponses.put(key, entry);
    	}
    }
    
    // called on any config update
    public void clear() {
    	synchronized(lock) {
    		cachedResponses.clear();
    	}
    }
    
    //TODO: fixme
    private static String computeCacheKey(SocialLoginConfig socialConfig, String accessToken) {
    	return "fixme";
    }
    
}
