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

import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.*;

import org.json.me.JSONObject;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;


@SuppressWarnings("serial")
public class SendSMS extends HttpServlet {
	private static final Logger log = Logger.getLogger(SendSMS.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String phone_number = req.getParameter("phone-number");
		String sms_message = req.getParameter("sms-message");

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		
		if ((phone_number!=null)&&(sms_message!=null)){

			setTwitterStatus(user.getEmail(),sms_message);

			setFacebookWallPost(user.getEmail(),sms_message);

			try{
				
				Properties blueviaAccount = Util.getNetworkAccount(user.getEmail(),"BlueViaAccount");	
				if (blueviaAccount!=null){
					String consumer_key = blueviaAccount.getProperty("BlueViaAccount.consumer_key");
					String consumer_secret = blueviaAccount.getProperty("BlueViaAccount.consumer_secret");
					String access_key = blueviaAccount.getProperty("BlueViaAccount.access_key");
					String access_secret = blueviaAccount.getProperty("BlueViaAccount.access_secret");

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

					int rc =request.getResponseCode(); 

					if (rc==HttpURLConnection.HTTP_CREATED)
						log.info(String.format("SMS sent to %s. Text: %s",phone_number,sms_message));
					else
						log.severe(String.format("Error %d sending SMS:%s\n",rc,request.getResponseMessage()));									
				}else
					log.warning("BlueVia Account seems to be not configured!");

			}catch (Exception e){
				log.severe(String.format("Exception sending SMS: %s", e.getMessage()));
			}
		}

		resp.sendRedirect("/index.jsp");
	}

	public static void setFacebookWallPost(String userEmail, String post){
		try{

			Properties facebookAccount = Util.getNetworkAccount(userEmail,Util.FaceBookOAuth.networkID);	

			if (facebookAccount!=null){
				String access_key =facebookAccount.getProperty(Util.FaceBookOAuth.networkID+".access_key");

				com.google.appengine.api.urlfetch.FetchOptions.Builder.doNotValidateCertificate();
				
				Entity blueviaUser = Util.getUser(userEmail);
				
				//FIXME
				URL fbAPI = new URL("https://graph.facebook.com/"+ (String)blueviaUser.getProperty("alias") +"/feed");
				HttpURLConnection request = (HttpURLConnection)fbAPI.openConnection();

				String content = String.format("access_token=%s&message=%s",access_key, URLEncoder.encode(post,"UTF-8"));

				request.setRequestMethod("POST");
				request.setRequestProperty("Content-Type","javascript/text");
				request.setRequestProperty("Content-Length", "" + Integer.toString(content.getBytes().length));
				request.setDoOutput(true);
				request.setDoInput(true);

				OutputStream os = request.getOutputStream();
				os.write(content.getBytes());
				os.flush();
				os.close();

				BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
				int rc = request.getResponseCode();
				if (rc==HttpURLConnection.HTTP_OK){
					StringBuffer body = new StringBuffer();
					String line;

					do{
						line = br.readLine();
						if (line!=null)
							body.append(line);
					}while (line!=null);

					JSONObject id = new JSONObject(body.toString());
				}else
					log.severe(String.format("Error %d posting FaceBook wall:%s\n",rc,request.getResponseMessage()));
			}
		}catch (Exception e){
			log.severe(String.format("Exception posting FaceBook wall: %s", e.getMessage()));
		}
	}

	public static void setTwitterStatus(String userEmail, String tweet){
		if (tweet!=null){
			try{
				Properties twitterAccount = Util.getNetworkAccount(userEmail,"TwitterAccount");	

				if (twitterAccount!=null){
					String consumer_key = twitterAccount.getProperty("TwitterAccount.consumer_key");
					String consumer_secret = twitterAccount.getProperty("TwitterAccount.consumer_secret");
					String access_key = twitterAccount.getProperty("TwitterAccount.access_key");
					String access_secret = twitterAccount.getProperty("TwitterAccount.access_secret");

					Twitter twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(consumer_key, consumer_secret);
					twitter.setOAuthAccessToken(new AccessToken(access_key,access_secret));

					StatusUpdate status = new StatusUpdate(tweet);
					twitter.updateStatus(status);				 
				}
			}catch (TwitterException te) {
				te.printStackTrace();
				log.severe(te.getMessage());
			}catch (Exception e){
				log.severe(String.format("Error sending SMS: %s", e.getMessage()));
			}
		}
	}
}