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
import java.util.Properties;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


public class Util {
	private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public static DatastoreService getDatastoreServiceInstance(){
		  return datastore;
	}
	
	public static void addUser(){
		UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    
	    DatastoreService datastore = Util.getDatastoreServiceInstance();
	    Transaction txn = datastore.beginTransaction();
	    
		Date date = new Date();
		
        Entity blueviaUser = new Entity("BlueViaUser");
        blueviaUser.setProperty("mail", user.getEmail());
        blueviaUser.setProperty("alias",user.getNickname());
        blueviaUser.setProperty("date", date);
        //FIXME 
        //blueviaUser.setProperty("phone", szPhoneNumber)
               
        datastore.put(blueviaUser);
        
        txn.commit();
	};
	
	public static Entity getUser(String userMail){
		Entity bvUser=null;	    
	    List<Entity> results =null;
	    
	    DatastoreService datastore = Util.getDatastoreServiceInstance();
	    Query query = new Query("BlueViaUser");
	    query.addFilter("mail", Query.FilterOperator.EQUAL, userMail);
	    
	    results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());                  
	    if (!results.isEmpty())
	      bvUser = results.remove(0);	        
	   
	    return bvUser;
	};
	
	public static void addUserMessage(String userMail, String szSender, String szMessage, String szDate){
		
		DatastoreService datastore = Util.getDatastoreServiceInstance();
    	Transaction txn = datastore.beginTransaction();
    	
    	Query query = new Query("BlueViaUser");
    	query.addFilter("mail", Query.FilterOperator.EQUAL, userMail);
    	
    	List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    	if (!results.isEmpty()){
		    Entity bvUser = results.remove(0);
		    Key userKey = bvUser.getKey();
		    
		    Entity userMsg = new Entity("Message", userKey);
		    userMsg.setProperty("Sender", szSender);
		    userMsg.setProperty("Message", szMessage);
		    userMsg.setProperty("Date", szDate);
	    	
		    datastore.put(userMsg);	
		    
    	}
    	txn.commit();
	}
	
	public static List<Entity> getUserMessages(){
		
		List<Entity> msgList=null;
		
		UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
		
		DatastoreService datastore = Util.getDatastoreServiceInstance();
        
		Query query = new Query("BlueViaUser");
        query.addFilter("mail", Query.FilterOperator.EQUAL, user.getEmail());
        
        List<Entity> userQuery = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        if (!userQuery.isEmpty()){
            Entity currentUser = userQuery.remove(0);
            Key userKey = currentUser.getKey();
            
            Query msgQuery= new Query();
            msgQuery.setAncestor(userKey);
            msgQuery.addFilter(Entity.KEY_RESERVED_PROPERTY,Query.FilterOperator.GREATER_THAN,userKey);
                                  
            msgList = datastore.prepare(msgQuery).asList(FetchOptions.Builder.withLimit(5));
        }
		
		return msgList;
	}
	
	public static void setNetworkAccount(String network,String consumer_key, String consumer_secret, String access_key, String access_secret){
		
		UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
		
		DatastoreService datastore = Util.getDatastoreServiceInstance();
    	Transaction txn = datastore.beginTransaction();
    	
		Query query = new Query("BlueViaUser");
    	query.addFilter("mail", Query.FilterOperator.EQUAL, user.getEmail());
    	
    	List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    	if (!results.isEmpty()){
		    Entity bvUser = results.remove(0);
		    Key userKey = bvUser.getKey();
		    
		    Entity networkAccount = new Entity(network, userKey);
		    networkAccount.setProperty("consumer_key", consumer_key);
		    networkAccount.setProperty("consumer_secret", consumer_secret);
		    networkAccount.setProperty("access_key", access_key);
		    networkAccount.setProperty("access_secret", access_secret);
	    	
		    datastore.put(networkAccount);								    
    	}
	    txn.commit();					    					            	
	}
	
	public static Properties getNetworkAccount(String network){
		
		Properties networkAccount = new Properties();
		
		UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
        
	    DatastoreService datastore = getDatastoreServiceInstance();
	    Transaction txn = datastore.beginTransaction();
	    
        Query userQuery = new Query("BlueViaUser");
        userQuery.addFilter("mail", Query.FilterOperator.EQUAL, user.getEmail());
        
        List<Entity> usersList = datastore.prepare(userQuery).asList(FetchOptions.Builder.withDefaults());
        
        if (!usersList.isEmpty()){
            Entity currentUser = usersList.remove(0);
            Key userKey = currentUser.getKey();
            
            Query networkQuery= new Query(network);
            networkQuery.setAncestor(userKey);
                                              
            List<Entity> networkList = datastore.prepare(networkQuery).asList(FetchOptions.Builder.withLimit(5));
            if (!networkList.isEmpty()){
            	Entity networkInstance = networkList.remove(0);
            	networkAccount.setProperty(network+".consumer_key",(String)networkInstance.getProperty("consumer_key"));
            	networkAccount.setProperty(network+".consumer_secret",(String)networkInstance.getProperty("consumer_secret"));
            	networkAccount.setProperty(network+".access_key",(String)networkInstance.getProperty("access_key"));
            	networkAccount.setProperty(network+".access_secret",(String)networkInstance.getProperty("access_secret"));
            }
        }
        
        txn.commit();
        
		return networkAccount;
	}
	
	public static class TwitterOAuth{
		final public static String consumer_key="LWjzycvPAAqWrkRJNWGvA";
		final public static String consumer_secret="qEprN6eVBh1JNJSTDOCjHnFaMpL6MUkMxZ1RVK4sQ";
		final public static String url_request_token="https://api.twitter.com/oauth/request_token";
		final public static String url_authorize="https://api.twitter.com/oauth/authorize";
		final public static String url_access_token="https://api.twitter.com/oauth/access_token";
	}	
	
	public static class BlueViaOAuth{
		final public static String consumer_key="vD12072833882323";
		final public static String consumer_secret="XTla79065935";
		final public static String url_request_token="https://api.bluevia.com/services/REST/Oauth/getRequestToken/";
		final public static String url_authorize="https://connect.bluevia.com/authorise/";
		final public static String url_access_token="https://api.bluevia.com/services/REST/Oauth/getAccessToken/";
	}	
}
