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

import java.util.List;
import java.util.Properties;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.utils.SystemProperty;

public class Util {
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public static DatastoreService getDatastoreServiceInstance(){
		  return datastore;
	}
	
	public static void addUser(String userMail, String userAlias, long date){
	    
	    DatastoreService datastore = Util.getDatastoreServiceInstance();
	    Transaction txn = datastore.beginTransaction();
	    
	    Entity user =null;
	    
	    Query query = new Query("BlueViaUser");
    	query.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
    	
    	user = datastore.prepare(query).asSingleEntity();
    	if (user==null){
    				
    		Entity blueviaUser = new Entity("BlueViaUser");
    		blueviaUser.setProperty("mail", userMail);
    		blueviaUser.setProperty("alias",userAlias);
    		blueviaUser.setProperty("date", date);
               
    		datastore.put(blueviaUser);    		    		
    	}    
    	
        txn.commit();
	};
	
	public static void deleteUser(String userMail){
		
		DatastoreService datastore = Util.getDatastoreServiceInstance();
    	Transaction txn = datastore.beginTransaction();
		 	
    	Query query = new Query("BlueViaUser");
    	query.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
    	Entity user= datastore.prepare(query).asSingleEntity();
    	
    	if (user!=null){
    		//Removing Networks Accounts 
    		deleteNetworkAccount(userMail,TwitterOAuth.networkID);
    		deleteNetworkAccount(userMail,BlueViaOAuth.networkID);
    		deleteNetworkAccount(userMail,FaceBookOAuth.networkID);
    		
    		// Removing Messages
    		Query msgQuery= new Query();
            
    		msgQuery.setAncestor(user.getKey());
            msgQuery.setKeysOnly();

            msgQuery.setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY,Query.FilterOperator.GREATER_THAN,user.getKey()));
                                  
            Iterable<Entity> msgList = datastore.prepare(msgQuery).asIterable(FetchOptions.Builder.withDefaults());
            for (Entity e: msgList)
            	datastore.delete(e.getKey());	
            
    		datastore.delete(user.getKey());
    	}
    	txn.commit();
	}
	
	public static Entity getUserWithAlias (String userAlias){
	    
	    DatastoreService datastore = Util.getDatastoreServiceInstance();
	    Query query = new Query("BlueViaUser");
	    query.setFilter(new FilterPredicate("alias", Query.FilterOperator.EQUAL, userAlias));
	    
	    return datastore.prepare(query).asSingleEntity();
	};
	
	public static Entity getUser(String userEmail){
	    
	    DatastoreService datastore = Util.getDatastoreServiceInstance();
	    Query query = new Query("BlueViaUser");
	    query.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userEmail));
	    
	    return datastore.prepare(query).asSingleEntity();
	};
	
	public static void addUserMessage(String userMail, String szSender, String szMessage, String szDate){
		
		DatastoreService datastore = Util.getDatastoreServiceInstance();
		Transaction txn = datastore.beginTransaction();
		
    	Query query = new Query("BlueViaUser");
    	query.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
    	
    	Entity user = datastore.prepare(query).asSingleEntity();
    	
    	if (user!=null){
    			    
		    Key userKey = user.getKey();
		    
		    Entity userMsg = new Entity("Message", userKey);
		    userMsg.setProperty("Sender", szSender);
		    userMsg.setProperty("Message", szMessage);
		    userMsg.setProperty("Date", szDate);
	    	
		    datastore.put(userMsg);			
    	}
    	txn.commit();
	}
	
	// IMPORTANT: if you query messages for non existing user, you got null
	//            but if you query messages for an existing user with no messages
	//            you get empty list
	public static List<Entity> getUserMessages(String userMail){
		
		List<Entity> msgList=null;
				
		DatastoreService datastore = Util.getDatastoreServiceInstance();
        
		Query query = new Query("BlueViaUser");
        query.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
        
        Entity user = datastore.prepare(query).asSingleEntity();
        
        if (user!=null){
            Key userKey = user.getKey();
            
            Query msgQuery= new Query("Message");
            msgQuery.setAncestor(userKey);
            msgQuery.addSort(Entity.KEY_RESERVED_PROPERTY, SortDirection.DESCENDING);
            msgQuery.addSort("Date",SortDirection.DESCENDING);
            msgQuery.setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY,Query.FilterOperator.GREATER_THAN,userKey));
            msgList = datastore.prepare(msgQuery).asList(FetchOptions.Builder.withLimit(4));
        }
		
		return msgList;
	}
	
	public static void addUnsubscriptionURI(String shortNumber, String apiURI, String correlator){
		DatastoreService datastore = Util.getDatastoreServiceInstance();
    	Transaction txn = datastore.beginTransaction();
    	
		Query query = new Query("UnsubscriptionURI");
    	query.setFilter(new FilterPredicate("shortNumber", Query.FilterOperator.EQUAL, shortNumber));
    	
    	Entity unSubscriptionURI = datastore.prepare(query).asSingleEntity();
    	 	
    	if (unSubscriptionURI==null){	
    		
		    unSubscriptionURI = new Entity("UnsubscriptionURI");
		    
		    unSubscriptionURI.setProperty("shortNumber", shortNumber);
		    unSubscriptionURI.setProperty("apiURI", apiURI);
		    unSubscriptionURI.setProperty("correlator", correlator);
	    	
		    datastore.put(unSubscriptionURI);								    
    	}
	    txn.commit();
	}
	
	public static void addNetworkAccount(String userMail, String network,String consumer_key, String consumer_secret, String access_key, String access_secret){
				
		DatastoreService datastore = Util.getDatastoreServiceInstance();
    	Transaction txn = datastore.beginTransaction();
    	
		Query query = new Query("BlueViaUser");
    	query.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
    	
    	Entity user = datastore.prepare(query).asSingleEntity();
    	 	
    	if (user!=null){		    
		    Key userKey = user.getKey();
		    
		    Entity networkAccount = new Entity(network, userKey);
		    networkAccount.setProperty("consumer_key", consumer_key);
		    networkAccount.setProperty("consumer_secret", consumer_secret);
		    networkAccount.setProperty("access_key", access_key);
		    networkAccount.setProperty("access_secret", access_secret);
	    	
		    datastore.put(networkAccount);								    
    	}
	    txn.commit();					    					            	
	}
	
	public static Properties getNetworkAccount(String userMail, String network){
		
		Properties networkAccount = null;
		        
	    DatastoreService datastore = getDatastoreServiceInstance();
	    
        Query userQuery = new Query("BlueViaUser");
        userQuery.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
        
        Entity user = datastore.prepare(userQuery).asSingleEntity();
        
        if (user!=null){            
            Key userKey = user.getKey();
            
            Query networkQuery= new Query(network);
            networkQuery.setAncestor(userKey);
            
            Entity networkInstance = datastore.prepare(networkQuery).asSingleEntity();
            
            if (networkInstance!=null){
            	networkAccount = new Properties();
            	networkAccount.setProperty(network+".consumer_key",(String)networkInstance.getProperty("consumer_key"));
            	networkAccount.setProperty(network+".consumer_secret",(String)networkInstance.getProperty("consumer_secret"));
            	networkAccount.setProperty(network+".access_key",(String)networkInstance.getProperty("access_key"));
            	networkAccount.setProperty(network+".access_secret",(String)networkInstance.getProperty("access_secret"));
            }
        }
        
		return networkAccount;
	}

	public static void deleteNetworkAccount(String userMail, String network){
		
	    DatastoreService datastore = getDatastoreServiceInstance();
	    Transaction txn = datastore.beginTransaction();
	    
        Query userQuery = new Query("BlueViaUser");
        userQuery.setFilter(new FilterPredicate("mail", Query.FilterOperator.EQUAL, userMail));
        
        Entity user = datastore.prepare(userQuery).asSingleEntity();
        
        if (user!=null){
            
            Key userKey = user.getKey();
            
            Query networkQuery= new Query(network);
            networkQuery.setAncestor(userKey);
                        
            Entity networkAccount = datastore.prepare(networkQuery).asSingleEntity();
            
            if (networkAccount!=null)            	          
            	datastore.delete(networkAccount.getKey());            	            
        }
        txn.commit();            	
	}
	
	public static String getCallbackDomain(){
		String callbackDomain="http://localhost:8888";
		
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
			callbackDomain="https://net-bluevia.appspot.com";
		}
		
		return callbackDomain;
	}
            
	public static class TwitterOAuth{
		final public static String networkID = "TwitterAccount";
		final public static String consumer_key="LWjzycvPAAqWrkRJNWGvA";
		final public static String consumer_secret="qEprN6eVBh1JNJSTDOCjHnFaMpL6MUkMxZ1RVK4sQ";
		final public static String url_request_token="https://api.twitter.com/oauth/request_token";
		final public static String url_authorize="https://api.twitter.com/oauth/authorize";
		final public static String url_access_token="https://api.twitter.com/oauth/access_token";
	}	
	
	public static class BlueViaOAuth{
		final public static String networkID = "BlueViaAccount";
		final public static String consumer_key="Ze12091125745565";
		final public static String consumer_secret="vVcY01133464";
		final public static String url_request_token="https://api.bluevia.com/services/REST/Oauth/getRequestToken/";
		
		public static String url_authorize(){
			String url_authorize="https://bluevia.com/test-apps/authorise";
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
				url_authorize="https://connect.bluevia.com/authorise/";
			}
			return url_authorize;
		}
		
		final public static String url_access_token="https://api.bluevia.com/services/REST/Oauth/getAccessToken/";
		
		final public static String app_keyword="mallrats";
	}	
	
	public static class FaceBookOAuth{
		final public static String networkID = "FaceBookAccount";
		final public static String consumer_key = "494065820603715";
		final public static String consumer_secret = "11daacc669b977b123ad342cac767131";
	}
}
