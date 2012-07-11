/*
 Copyright 2012 Andres Leonardo Martinez Ortiz

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package bluevia;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


public class Util {
	private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private static final String bv_consumer_key ="Vq12061936302630";
	private static final String bv_consumer_secret="uvzb20497527";
	private static final String adSpace = "BV23720";
	
	public static String getBvConsumerKey(){
		return bv_consumer_key;
	}
	
	public static String getBvConsumerSecret(){
		return bv_consumer_secret;
	}
	
	public static DatastoreService getDatastoreServiceInstance(){
		  return datastore;
	}
	
	public static String getAdSpace(){
		return adSpace;
	}
	
	public static void addUser(){
		UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    
		Key userKey = KeyFactory.createKey("BlueViaUser", user.getEmail());
		
		Date date = new Date();
		
        Entity blueviaUser = new Entity("BlueViaUser", userKey);
        blueviaUser.setProperty("mail", user.getEmail());
        blueviaUser.setProperty("alias",user.getNickname());
        blueviaUser.setProperty("date", date);
        blueviaUser.setProperty("Key", KeyFactory.keyToString(userKey));
        
        DatastoreService datastore = Util.getDatastoreServiceInstance();
        
        datastore.put(blueviaUser);
	};
	
	public static Entity getUser(String userAlias){
		Entity bvUser=null;	    
	    List<Entity> results =null;
	    
	    DatastoreService datastore = Util.getDatastoreServiceInstance();
	    Query query = new Query("BlueViaUser");
	    query.addFilter("mail", Query.FilterOperator.EQUAL, userAlias);
	    
	    results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());                  
	    if (!results.isEmpty())
	      bvUser = results.remove(0);	        
	   
	    return bvUser;
	};
}
