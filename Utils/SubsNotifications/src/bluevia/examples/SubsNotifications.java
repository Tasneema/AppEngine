/*
 Copyright 2010 Andres Leonardo Martinez Ortiz

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
package bluevia.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Date;
import java.util.Random;
import java.util.logging.*;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;
import java.util.Random;

public class SubsNotifications {

	public static String getCallbackDomain(){
		String callbackDomain="https://net-bluevia.appspot.com";
		
		return callbackDomain;
	}
	
	static void doSubscription(Logger log){
		BufferedReader br = null;
		
		log.info("Starting Subscrition to SMS Notification");
		
		try{
			br = new BufferedReader(new FileReader("API-AccessToken.ini"));
		
			String consumer_key = br.readLine();
			String consumer_secret = br.readLine();
			
			br.close();
			
			OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer(consumer_key, consumer_secret);
			consumer.setMessageSigner(new HmacSha1MessageSigner());
			
			URL apiURI = new URL("https://api.bluevia.com/services/REST/SMS_Sandbox/inbound/subscriptions?version=v1&alt=json");
			
			HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();
			
			Random rand = new Random();
			Date now = new Date();
		
			rand.setSeed(now.getTime());
			Long correlator = rand.nextLong();
			if (correlator<0)
				correlator= -1*correlator;
			
			String jsonSubscriptionMsg="{\"smsNotification\":{\"reference\":{\"correlator\": \"%s\",\"endpoint\": \"%s\"},\"destinationAddress\":{\"phoneNumber\":\"%s\"},\"criteria\":\"%s\"}}";
			String szBody = String.format(jsonSubscriptionMsg,"bv"+correlator.toString().substring(0, 10),getCallbackDomain()+"/notifySmsReception","445480605","SANDmallrats");
			
			request.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			request.setRequestMethod("POST");
			request.setDoOutput(true);
			
			consumer.sign(request);
			request.connect();
			
			OutputStream os = request.getOutputStream();
			os.write(szBody.getBytes());
			os.flush();

			log.info("Wrote body: \n"+szBody);
			
			int rc =request.getResponseCode(); 
						
			if (rc==HttpURLConnection.HTTP_CREATED)
				log.info("Subscription call returns\n"+request.getHeaderField("Location"));
			else
				log.severe(String.format("Error %d subscribing Notifications: %s",rc,request.getResponseMessage())); 
		}catch (Exception e){
			log.severe("Exception raised: %s"+e.getMessage());
		}
	}
	
	static void doUnsubscription(Logger log, String uri){
		BufferedReader br = null;
		
		log.info("Starting Unsubscrition of SMS Notification at: " + uri);
		try{
			br = new BufferedReader(new FileReader("API-AccessToken.ini"));
			
			String consumer_key = br.readLine();
			String consumer_secret = br.readLine();
			
			br.close();
			
			OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer(consumer_key, consumer_secret);
			consumer.setMessageSigner(new HmacSha1MessageSigner());
			
			URL apiURI = new URL(uri+"?version=v1");
			HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();
			
			request.setRequestMethod("DELETE");
			consumer.sign(request);
			request.connect();
			int rc=request.getResponseCode(); 
			
			if (rc==HttpURLConnection.HTTP_NO_CONTENT)
				log.info("Notification URI: "+uri+"\nDone!");
			else
				log.severe(String.format("Error %d Unsubscribing Notifications: %s",rc,request.getResponseMessage())); 
			
		}catch(Exception e){
			log.severe("Exception raised: %s"+e.getMessage());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = Logger.getLogger(SubsNotifications.class.getCanonicalName());
		System.setProperty("debug", "1");
		if (args.length!=2)
			doSubscription(log);
		else {
			if (args[0].compareTo("-d")==0)
				doUnsubscription(log,args[1]);
		}		
	}
}
