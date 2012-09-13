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

package bluevia.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.util.logging.*;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import java.net.HttpURLConnection;
import java.net.URL;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * @author almo 
 */
public class MODemo {

	static String test1="{\"receivedSMS\":{\"receivedSMS\":{\"message\":\"mallrats testing #5\",\"originAddress\":{\"phoneNumber\":\"447860518373\"},\"destinationAddress\":{\"phoneNumber\":\"445480605\"},\"dateTime\":\"2012-09-12T15:56:23.433Z\"}}}";
	static String test2="{\"receivedSMS\":{\"receivedSMS\":[{\"message\":\"mallrats testing #10\",\"originAddress\":{\"phoneNumber\":\"447860518373\"},\"destinationAddress\":{\"phoneNumber\":\"445480605\"},\"dateTime\":\"2012-09-12T15:59:33.662Z\"},{\"message\":\"mallrats testing #11\",\"originAddress\":{\"phoneNumber\":\"447860518373\"},\"destinationAddress\":{\"phoneNumber\":\"445480605\"},\"dateTime\":\"2012-09-12T15:59:37.202Z\"}]}}";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		BufferedReader iReader = null;
		String apiDataFile = "API-AccessToken.ini";

		String consumer_key;
		String consumer_secret;
		String registrationId;

		OAuthConsumer apiConsumer = null;
		HttpURLConnection request = null;
		URL moAPIurl = null;

		Logger logger = Logger.getLogger("moSMSDemo.class");
		int i = 0;
		int rc = 0;

		Thread mThread = Thread.currentThread();

		try {
			System.setProperty("debug", "1");

			iReader = new BufferedReader(new FileReader(apiDataFile));

			// Private data: consumer info + access token info + phone info
			consumer_key = iReader.readLine();
			consumer_secret = iReader.readLine();
			registrationId = iReader.readLine();

			// Set up the oAuthConsumer
			while (true) {
				try {

					logger.log(Level.INFO, String.format("#%d: %s\n", ++i,"Requesting messages..."));

					apiConsumer = new DefaultOAuthConsumer(consumer_key,
							consumer_secret);

					apiConsumer.setMessageSigner(new HmacSha1MessageSigner());

					moAPIurl = new URL("https://api.bluevia.com/services/REST/SMS/inbound/"
									+ registrationId
									+ "/messages?version=v1&alt=json");

					request = (HttpURLConnection) moAPIurl.openConnection();
					request.setRequestMethod("GET");

					apiConsumer.sign(request);

					StringBuffer doc = new StringBuffer();
					BufferedReader br = null;

					rc = request.getResponseCode();
					if (rc == HttpURLConnection.HTTP_OK) {
						br = new BufferedReader(new InputStreamReader(
								request.getInputStream()));

						String line = br.readLine();
						while (line != null) {
							doc.append(line);
							line = br.readLine();
						}

						System.out.printf("Output message: %s\n",doc.toString());
						try{
							JSONObject apiResponse1 = new JSONObject(doc.toString());
							String aux =apiResponse1.getString("receivedSMS");
							if (aux!=null){
								String szMessage;
								String szOrigin;
								String szDate;
								
								JSONObject smsPool = apiResponse1.getJSONObject("receivedSMS"); 
								JSONArray smsInfo = smsPool.optJSONArray("receivedSMS");
								if (smsInfo!=null){	        
									for (i=0; i<smsInfo.length(); i++){
										szMessage = smsInfo.getJSONObject(i).getString("message");
										szOrigin = smsInfo.getJSONObject(i).getJSONObject("originAddress").getString("phoneNumber");
										szDate = smsInfo.getJSONObject(i).getString("dateTime");
										System.out.printf("#%d %s\n - from %s\n - message:%s\n",i,szDate,szOrigin,szMessage);
									}
								}else{
									JSONObject sms = smsPool.getJSONObject("receivedSMS");
									szMessage = sms.getString("message");
									szOrigin = sms.getJSONObject("originAddress").getString("phoneNumber");
									szDate = sms.getString("dateTime");
									System.out.printf("#%d %s\n - from %s\n - message:%s\n",i,szDate,szOrigin,szMessage);
								}
							}
						}catch (JSONException e){
							System.err.println("JSON error: " + e.getMessage());
						}

					} else if (rc == HttpURLConnection.HTTP_NO_CONTENT)
						System.out.printf("No content\n");
					else
						System.err.printf("Error: %d:%s\n", rc,
								request.getResponseMessage());

					request.disconnect();

				} catch (Exception e) {
					System.err.println("Exception: " + e.getMessage());
				}
				mThread.sleep(15000);
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}
}
