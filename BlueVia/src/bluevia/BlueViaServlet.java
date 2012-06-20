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

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.*;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class BlueViaServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String consumer_key = Util.getConsumerKey();
		String consumer_secret=Util.getCosumerSecret();
		
		String access_key=(String)req.getSession().getAttribute("access_key");
		String access_secret=(String) req.getSession().getAttribute("access_secret");		

		if ((access_key==null)&&(access_secret==null)){			
			OAuthConsumer apiConsumer = new DefaultOAuthConsumer(consumer_key,consumer_secret);
		   	OAuthProvider apiProvider = new DefaultOAuthProvider(
		   			"https://api.bluevia.com/services/REST/Oauth/getRequestToken/",
		   			"https://api.bluevia.com/services/REST/Oauth/getAccessToken/",
		   			"https://connect.bluevia.com/authorise/");

		   	apiConsumer.setMessageSigner(new HmacSha1MessageSigner());   
		  	
		   	try {
		   		String oAuthUrl= apiProvider.retrieveRequestToken(apiConsumer,"http://localhost:8888/bluevia/oauth-callback");
				req.getSession().setAttribute("request_key", apiConsumer.getToken());
				req.getSession().setAttribute("request_secret", apiConsumer.getTokenSecret());
				resp.sendRedirect(oAuthUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}								    
		} else{	
			UserService userService = UserServiceFactory.getUserService();
		    User user = userService.getCurrentUser();
		    
			Key userKey = KeyFactory.createKey("BlueViaUser", user.getEmail());
			
			Date date = new Date();
			
	        Entity blueviaUser = new Entity("BlueViaUser", userKey);
	        blueviaUser.setProperty("mail", user.getEmail());
	        blueviaUser.setProperty("alias",user.getNickname());
	        blueviaUser.setProperty("date", date);
	        blueviaUser.setProperty("consumer_key", consumer_key);
	        blueviaUser.setProperty("consumer_secret", consumer_secret);
	        blueviaUser.setProperty("access_key", access_key);
	        blueviaUser.setProperty("access_secret", access_secret);
	        blueviaUser.setProperty("Key", KeyFactory.keyToString(userKey));

	        DatastoreService datastore = Util.getDatastoreServiceInstance();
	        
	        datastore.put(blueviaUser);
	        
	        req.getSession().removeAttribute("request_key");
	        req.getSession().removeAttribute("request_secret");
	        req.getSession().removeAttribute("access_key");
	        req.getSession().removeAttribute("access_secret");

	        resp.sendRedirect("/index.jsp?user="+user.getNickname());	        
		}
	}
}