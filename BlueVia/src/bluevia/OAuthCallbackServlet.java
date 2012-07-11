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
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

@SuppressWarnings("serial")
public class OAuthCallbackServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(SMSPolling.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {			
		
		String consumer_key = Util.getBvConsumerKey();
		String consumer_secret=Util.getBvConsumerSecret();
		
		String access_key = (String) req.getSession().getAttribute("access_key");
		String access_secret=(String) req.getSession().getAttribute("access_secret");
		
		if ((access_key==null)&&(access_secret==null)){
			OAuthConsumer apiConsumer = new DefaultOAuthConsumer(consumer_key,consumer_secret);
	    	OAuthProvider apiProvider = new DefaultOAuthProvider(
	    			"https://api.bluevia.com/services/REST/Oauth/getRequestToken/",
	    			"https://api.bluevia.com/services/REST/Oauth/getAccessToken/",
	    			"https://connect.bluevia.com/authorise/");
			
	    	String request_key = (String) req.getSession().getAttribute("request_key");
			String request_secret=(String) req.getSession().getAttribute("request_secret");
			
			if ((request_key!=null)&&(request_secret!=null)){
				apiConsumer.setTokenWithSecret(request_key, request_secret);
			
				String oauth_verifier = req.getParameter("oauth_verifier");
				apiProvider.setOAuth10a(true);
				try {
					apiProvider.retrieveAccessToken(apiConsumer, oauth_verifier);
					req.getSession().setAttribute("access_key",apiConsumer.getToken());
					req.getSession().setAttribute("access_secret",apiConsumer.getTokenSecret());
				} catch (Exception e) {
					e.printStackTrace();
				}
				req.getSession().removeAttribute("request_key");
				req.getSession().removeAttribute("request_secret");
				resp.sendRedirect("/bluevia");
			}
		}
	}
}