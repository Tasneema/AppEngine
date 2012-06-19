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
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

import static com.google.appengine.api.urlfetch.FetchOptions.Builder.*;

import java.io.IOException;
import java.io.OutputStream;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("serial")
public class SendSMS extends HttpServlet {
	 private static final Logger log = Logger.getLogger(SendSMS.class.getName());
	 
	 public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		 String phone_number = req.getParameter("phone-number");
		 String sms_message = req.getParameter("sms-message");

		 if ((phone_number!=null)&&(sms_message!=null)){
			
			UserService userService = UserServiceFactory.getUserService();
		    User user = userService.getCurrentUser();
		    
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
		    Key userKey = KeyFactory.createKey("BlueViaUser",user.getEmail());
		    
		    Query query = new Query("BlueViaUser", userKey);
		    List<Entity> BlueViaUsers = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
		    
		    if (!BlueViaUsers.isEmpty()){
		    	try{
		    			
			    	Entity blueviaUser = BlueViaUsers.get(0);
			    			
					String consumer_key = (String) blueviaUser.getProperty("consumer_key");
					String consumer_secret = (String) blueviaUser.getProperty("consumer_secret");
					String access_key = (String) blueviaUser.getProperty("access_key");
					String access_secret = (String) blueviaUser.getProperty("access_secret");	
					
					com.google.appengine.api.urlfetch.FetchOptions.Builder.doNotValidateCertificate();
					
					OAuthConsumer consumer = (OAuthConsumer) new DefaultOAuthConsumer(consumer_key, consumer_secret);
					consumer.setMessageSigner(new HmacSha1MessageSigner());
					consumer.setTokenWithSecret(access_key, access_secret);
					
					URL apiURI = new URL("https://api.bluevia.com/services/REST/SMS/outbound/requests?version=v1");
					HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();
					
					request.setRequestProperty("Content-Type", "application/json");
					request.setRequestMethod("POST");
					request.setDoOutput(true);

					consumer.sign(request);
					request.connect();
					
					String smsTemplate = "{\"smsText\": {\n  \"address\": {\"phoneNumber\": \"%s\"},\n  \"message\": \"%s\",\n  \"originAddress\": {\"alias\": \"%s\"},\n}}";
					String smsMsg = String.format(smsTemplate, phone_number, sms_message, access_key);
					
					OutputStream os = request.getOutputStream();
					os.write(smsMsg.getBytes());
					os.flush();
					
					if (request.getResponseCode()== HttpURLConnection.HTTP_CREATED)
						log.info(String.format("SMS sent to %d. Text: %s",phone_number,sms_message));
					else
						log.info(String.format("SMS sent to %d. Text: %s",phone_number,sms_message));
														
				}catch (Exception e){
					log.info(String.format("Error sending SMS: %s", e.getMessage()));
				}
			}
		 }

		 resp.sendRedirect("/index.jsp");
	 }

}
