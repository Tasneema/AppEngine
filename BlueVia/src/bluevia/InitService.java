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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.me.JSONObject;
import org.json.me.JSONArray;
import org.json.me.JSONException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;

import bluevia.SendSMS;

@SuppressWarnings("serial")
public class InitService extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(InitService.class.getName());
	
	private static final String MO_URI_UK = "https://api.bluevia.com/services/REST/SMS/inbound/445480605/messages?version=v1&alt=json";
	private static final String MO_URI_SP = "https://api.bluevia.com/services/REST/SMS/inbound/34217040/messages?version=v1&alt=json";
	private static final String MO_URI_GE = "https://api.bluevia.com/services/REST/SMS/inbound/493000/messages?version=v1&alt=json";
	private static final String MO_URI_BR = "https://api.bluevia.com/services/REST/SMS/inbound/55281/messages?version=v1&alt=json";
	private static final String MO_URI_MX = "https://api.bluevia.com/services/REST/SMS/inbound/524040/messages?version=v1&alt=json";
	private static final String MO_URI_AR = "https://api.bluevia.com/services/REST/SMS/inbound/546780/messages?version=v1&alt=json";
	private static final String MO_URI_CH = "https://api.bluevia.com/services/REST/SMS/inbound/5698765/messages?version=v1&alt=json";
	private static final String MO_URI_CO = "https://api.bluevia.com/services/REST/SMS/inbound/572505/messages?version=v1&alt=json";
		 
	private static final String MO_UK = "445480605";
	private static final String MO_SP = "34217040";
	private static final String MO_GE = "493000";
	private static final String MO_BR = "55281";
	private static final String MO_MX = "524040";
	private static final String MO_AR = "546780";
	private static final String MO_CH = "5698765";
	private static final String MO_CO = "572505";
	
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	if (!subscribeNotifications())
    		doPolling();
    }
    
    boolean subscribeNotifications(){
    	boolean result=true;
    	String [] countryShortNumbers ={MO_UK, MO_SP, MO_GE, MO_BR, MO_MX, MO_AR, MO_CH, MO_CO};
    	int i=0;

    	for (i=0;i<countryShortNumbers.length;i++){
    		try{
    			OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer(Util.BlueViaOAuth.consumer_key, Util.BlueViaOAuth.consumer_secret);
    			consumer.setMessageSigner(new HmacSha1MessageSigner());
    			
    			com.google.appengine.api.urlfetch.FetchOptions.Builder.doNotValidateCertificate();
    			
    			URL apiURI = new URL("https://api.bluevia.com/services/REST/SMS_Sandbox/inbound/subscriptions?version=v1");
    			           
    			HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();
    			
    			Random rand = new Random();
    			Date now = new Date();
    		
    			rand.setSeed(now.getTime());
    			Long correlator = rand.nextLong();
    			if (correlator<0)
    				correlator= -1*correlator;
    			
    			String jsonSubscriptionMsg="{\"smsNotification\":{\"reference\":{\"correlator\": \"%s\",\"endpoint\": \"%s\"},\"criteria\":\"%s\",\"destinationAddress\":{\"phoneNumber\":\"%s\"}}}";
    			String szBody = String.format(jsonSubscriptionMsg,"bv_"+correlator.toString(),Util.getCallbackDomain()+"/notifySmsReception",Util.BlueViaOAuth.app_keyword,countryShortNumbers[i]);
    			
    			request.setRequestProperty("Content-Type", "application/json");
    			request.setRequestProperty("Content-Length", "" + Integer.toString(szBody.getBytes().length));
    			request.setRequestMethod("POST");
    			request.setDoOutput(true);
    			request.setDoInput(true);
    			
    			consumer.sign(request);
    			request.connect();
    			
    			OutputStream os = request.getOutputStream();
    			os.write(szBody.getBytes());
    			os.flush();

    			int rc =request.getResponseCode(); 
    						
    			if (rc==HttpURLConnection.HTTP_CREATED)    				
    				Util.addUnsubscriptionURI(countryShortNumbers[i], request.getHeaderField("Location"),"bv_"+correlator.toString());
    			else
    				logger.severe(String.format("Error %d sending SMS:%s",rc,request.getResponseMessage())); 
    		}catch (Exception e){
    			logger.severe("Exception raised: %s"+e.getMessage());
    		}	
    	}
    	
    	return result;
    }
    
    void doPolling(){
    	Thread threadPoller = ThreadManager.createBackgroundThread(new Runnable() {
    		public void run() {
    			String consumer_key = Util.BlueViaOAuth.consumer_key;
    			String consumer_secret = Util.BlueViaOAuth.consumer_secret;
    			BufferedReader br =null;
    			int countryIndex=0;
    			String [] countryURIs ={MO_URI_UK,MO_URI_SP,MO_URI_GE,MO_URI_BR,MO_URI_MX, MO_URI_AR,MO_URI_CH,MO_URI_CO};
    			while (true){
	    			try{	 		
	    				com.google.appengine.api.urlfetch.FetchOptions.Builder.doNotValidateCertificate();			    	
	    				OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer (consumer_key, consumer_secret);
	    				consumer.setMessageSigner(new HmacSha1MessageSigner());									
	    				URL apiURI = new URL (countryURIs[countryIndex]);
	    				
	    				countryIndex=(countryIndex+1) % countryURIs.length;
	    				
	    				int rc = 0;					
	    										
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
    							JSONObject apiResponse;
    							JSONObject smsPool;
    							JSONArray  smsInfo;

							    apiResponse = new JSONObject(doc.toString());
					            if (apiResponse.getString("receivedSMS")!=null){
					            	String szMessage;
					            	String szOrigin;
					            	String szDate;
					            	
					            	smsPool = apiResponse.getJSONObject("receivedSMS");  
					            	smsInfo = smsPool.optJSONArray("receivedSMS");
					                if (smsInfo!=null){	        
						            	for (int i=0; i<smsInfo.length(); i++){
						            		szMessage = smsInfo.getJSONObject(i).getString("message");
						            		szOrigin = smsInfo.getJSONObject(i).getJSONObject("originAddress").getString("phoneNumber");
						            		szDate = smsInfo.getJSONObject(i).getString("dateTime");
						            		
						            		StringTokenizer msgParser = new StringTokenizer(szMessage);
				    						
			    							// Removing app id
			    							msgParser.nextToken();
			    							
			    							String userAlias = msgParser.nextToken();
			    							
			    							String msg = "";
			    							while (msgParser.hasMoreTokens())
			    								msg += " "+ msgParser.nextToken();    						    					    	
			    							
			    							String userEmail = (String)Util.getUserWithAlias(userAlias).getProperty("mail");
			    							if (userEmail!=null){
			    								Util.addUserMessage(userEmail, szOrigin, msg, szDate);
			    								
			    								SendSMS.setTwitterStatus(userEmail,msg);

			    								SendSMS.setFacebookWallPost(userEmail,msg);
			    							}
						            	}
					                }else{
					                	JSONObject sms = smsPool.getJSONObject("receivedSMS");
										szMessage = sms.getString("message");
										szOrigin = sms.getJSONObject("originAddress").getString("phoneNumber");
										szDate = sms.getString("dateTime");
					            		
					            		StringTokenizer msgParser = new StringTokenizer(szMessage);
			    						
		    							// Removing app id
		    							msgParser.nextToken();
		    							
		    							String userAlias = msgParser.nextToken();
		    							
		    							String msg = "";
		    							while (msgParser.hasMoreTokens())
		    								msg += " "+ msgParser.nextToken();    						    					    	

		    							Util.addUserMessage(userAlias, szOrigin, msg, szDate);	
					                }
					            }else{
					            	logger.warning("No messages");
					            	if (txn.isActive())
	    						        txn.rollback();
					            }
    						    
    						}catch (JSONException e){
    							logger.severe(e.getMessage());
    						}catch(Exception e){
    							logger.severe(e.getMessage());
    						}finally {
    						    if (txn.isActive())
    						        txn.rollback();    						
    						}
    							    				
    					}else if (rc==HttpURLConnection.HTTP_NO_CONTENT) {
    						//log.warning(String.format("No content from: %s", apiURI.getPath()));
    					} else
    						logger.severe(String.format("%d: %s",rc,request.getResponseMessage()));    					    							
    				
	    			}catch(Exception e){
	    				logger.severe(e.getMessage());
	    			}
	    			
	    			Thread.currentThread();
					try {
						Thread.sleep(150000);
					} catch (InterruptedException e) {						
						e.printStackTrace();
						logger.severe(String.format("%s",e.getMessage()));
					}		    			
    		    }
    		}
    	});
    	  	
    	threadPoller.start();
    }
}