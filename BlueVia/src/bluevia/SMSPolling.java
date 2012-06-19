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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.me.JSONObject;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import com.google.appengine.api.ThreadManager;
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
import bluevia.*;

@SuppressWarnings("serial")
public class SMSPolling extends HttpServlet {
	private static final Logger log = Logger.getLogger(SMSPolling.class.getName());
	 
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    	Thread threadUK = ThreadManager.createBackgroundThread(new Runnable() {
    		public void run() {
    			String consumer_key = Util.getConsumerKey();
    			String consumer_secret = Util.getCosumerSecret();
    			BufferedReader br =null;

    			try{	 		
    				com.google.appengine.api.urlfetch.FetchOptions.Builder.doNotValidateCertificate();			    	
    				OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer (consumer_key, consumer_secret);
    				consumer.setMessageSigner(new HmacSha1MessageSigner());									
    				URL apiURI = new URL ("https://api.bluevia.com/services/REST/SMS/inbound/445480605/messages?version=v1&alt=json");

    				int rc = 0;					
    				do{						
    					HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();			
    					request.setRequestMethod("GET");

    					consumer.sign(request);

    					rc = request.getResponseCode();

    					if (rc==HttpURLConnection.HTTP_OK){					
    						br = new BufferedReader (new InputStreamReader (request.getInputStream()));
    						StringBuffer doc = new StringBuffer();
    						String line;

    						do{
    							line = br.readLine();
    							if (line!=null)
    								doc.append(line);
    						}while (line!=null);

    						DatastoreService datastore = Util.getDatastoreServiceInstance();
					    	Transaction txn = datastore.beginTransaction();
    						try{
    							JSONObject parser = new JSONObject(doc.toString());
    							parser = (JSONObject) parser.get("receivedSMS");						
    							JSONObject sms = (JSONObject)parser.get("receivedSMS");
    							String message = sms.getString("message");
    							String originAddress= ((JSONObject)sms.get("originAddress")).getString("phoneNumber");
    							String dateTime = sms.getString("dateTime"); 
    									
    							StringTokenizer msgParser = new StringTokenizer(message);
    							// Removing app id
    							msgParser.nextToken();
    							String userAlias = msgParser.nextToken();
    							
    							String msg = "";
    							while (msgParser.hasMoreTokens())
    								msg += " "+ msgParser.nextToken();    						    					    	

    					    	Query query = new Query("BlueViaUser");
    					    	query.addFilter("alias", Query.FilterOperator.EQUAL, userAlias);
    					    	List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    					    	if (!results.isEmpty()){
								    Entity bvUser = results.remove(0);
								    Key userKey = bvUser.getKey();
								    
								    Entity userMsg = new Entity("Message", userKey);
								    userMsg.setProperty("Sender", originAddress);
								    userMsg.setProperty("Message", msg);
								    userMsg.setProperty("Date", dateTime);
							    	
								    datastore.put(userMsg);								    
    					    	}
    						    txn.commit();
    						}catch(Exception e){
    							log.info(e.getMessage());
    						}finally {
    						    if (txn.isActive()) {
    						        txn.rollback();
    						    }
    						}
    						
    						Thread.currentThread();
    						Thread.sleep(1500);					

    					}else if (rc==HttpURLConnection.HTTP_NO_CONTENT) {
    						Thread.currentThread();
    						Thread.sleep(1500);						
    					} else
    						log.info(String.format("%d: %s",rc,request.getResponseMessage()));
    				}while ((rc==HttpURLConnection.HTTP_OK) || (rc==HttpURLConnection.HTTP_NO_CONTENT));

    			}catch(Exception e){
    				log.info(e.getMessage());
    			}	
    		}
    	});
    	
    	Thread threadBrasil = ThreadManager.createBackgroundThread(new Runnable() {
    		public void run() {
    			String consumer_key = Util.getConsumerKey();
    			String consumer_secret = Util.getCosumerSecret();
    			BufferedReader br =null;

    			try{	 		
    				com.google.appengine.api.urlfetch.FetchOptions.Builder.doNotValidateCertificate();			    	
    				OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer (consumer_key, consumer_secret);
    				consumer.setMessageSigner(new HmacSha1MessageSigner());									
    				URL apiURI = new URL ("https://api.bluevia.com/services/REST/SMS/inbound/55281/messages?version=v1&alt=json");

    				int rc = 0;					
    				do{						
    					HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();			
    					request.setRequestMethod("GET");

    					consumer.sign(request);

    					rc = request.getResponseCode();

    					if (rc==HttpURLConnection.HTTP_OK){					
    						br = new BufferedReader (new InputStreamReader (request.getInputStream()));
    						StringBuffer doc = new StringBuffer();
    						String line;

    						do{
    							line = br.readLine();
    							if (line!=null)
    								doc.append(line);
    						}while (line!=null);

    						DatastoreService datastore = Util.getDatastoreServiceInstance();
					    	Transaction txn = datastore.beginTransaction();
    						try{
    							JSONObject parser = new JSONObject(doc.toString());
    							parser = (JSONObject) parser.get("receivedSMS");						
    							JSONObject sms = (JSONObject)parser.get("receivedSMS");
    							String message = sms.getString("message");
    							String originAddress= ((JSONObject)sms.get("originAddress")).getString("phoneNumber");
    							String dateTime = sms.getString("dateTime"); 
    									
    							StringTokenizer msgParser = new StringTokenizer(message);
    							// Removing app id
    							msgParser.nextToken();
    							String userAlias = msgParser.nextToken();
    							
    							String msg = "";
    							while (msgParser.hasMoreTokens())
    								msg += " "+ msgParser.nextToken();    						    					    	

    					    	Query query = new Query("BlueViaUser");
    					    	query.addFilter("alias", Query.FilterOperator.EQUAL, userAlias);
    					    	List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    					    	if (!results.isEmpty()){
								    Entity bvUser = results.remove(0);
								    Key userKey = bvUser.getKey();
								    
								    Entity userMsg = new Entity("Message", userKey);
								    userMsg.setProperty("Sender", originAddress);
								    userMsg.setProperty("Message", msg);
								    userMsg.setProperty("Date", dateTime);
							    	
								    datastore.put(userMsg);								    
    					    	}
    						    txn.commit();
    						}catch(Exception e){
    							log.info(e.getMessage());
    						}finally {
    						    if (txn.isActive()) {
    						        txn.rollback();
    						    }
    						}
    						
    						Thread.currentThread();
    						Thread.sleep(1500);					

    					}else if (rc==HttpURLConnection.HTTP_NO_CONTENT) {
    						Thread.currentThread();
    						Thread.sleep(1500);						
    					} else
    						log.info(String.format("%d: %s",rc,request.getResponseMessage()));
    				}while ((rc==HttpURLConnection.HTTP_OK) || (rc==HttpURLConnection.HTTP_NO_CONTENT));

    			}catch(Exception e){
    				log.info(e.getMessage());
    			}	
    		}
    	});
    	
    	threadBrasil.start();    
    	threadUK.start();
    }
}
