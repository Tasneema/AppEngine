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
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bluevia.Util.BlueViaOAuth;
import bluevia.Util.TwitterOAuth;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

@SuppressWarnings("serial")
public class OAuth extends HttpServlet {
	private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String network = req.getParameter("network");
		String step    = req.getParameter("step");
		
		if (network.compareTo("twitter")==0){
			resp.sendRedirect(oAuthTwitter(req,step));			
		}else if (network.compareTo("bluevia")==0){
			resp.sendRedirect(oAuthBlueVia(req,step));
		}else if (network.compareTo("facebook")==0){
			resp.sendRedirect(oAuthFacebook(req,step));
		}else
			resp.sendRedirect("index.jsp");		
	}

	// FIXME
	private String oAuthFacebook(HttpServletRequest req,String step){
		String oAuthUrl="/index.jsp";
		
		return oAuthUrl;
	}
	
	private String oAuthBlueVia(HttpServletRequest req,String step){
		String oAuthUrl="/index.jsp";

		String consumer_key = BlueViaOAuth.consumer_key;
		String consumer_secret=BlueViaOAuth.consumer_secret;
	
		OAuthConsumer apiConsumer = new DefaultOAuthConsumer(consumer_key,consumer_secret);
		OAuthProvider apiProvider = new DefaultOAuthProvider(
				BlueViaOAuth.url_request_token,
				BlueViaOAuth.url_access_token,
				BlueViaOAuth.url_authorize);
		
		apiConsumer.setMessageSigner(new HmacSha1MessageSigner()); 
		
		try{
			if (step.compareTo("1")==0){   

				oAuthUrl= apiProvider.retrieveRequestToken(apiConsumer,"http://localhost:8888/oauth?network=bluevia&step=2");
		
				req.getSession().setAttribute("request_key", apiConsumer.getToken());
				req.getSession().setAttribute("request_secret", apiConsumer.getTokenSecret());

			}else {
				String request_key = (String) req.getSession().getAttribute("request_key");
				String request_secret=(String) req.getSession().getAttribute("request_secret");

				apiConsumer.setTokenWithSecret(request_key, request_secret);
				apiProvider.setOAuth10a(true);
				
				String oauth_verifier = req.getParameter("oauth_verifier");
				
				//BlueVia bug fix: step is equal to 2?oauth_verifier=<pin>
				if (oauth_verifier==null){
					StringTokenizer parser = new StringTokenizer(step,"=");
					parser.nextToken();					
					oauth_verifier=parser.nextToken();					
				}
				// end bug fix
				
				apiProvider.retrieveAccessToken(apiConsumer, oauth_verifier);

				Util.setNetworkAccount("BlueViaAccount",
						Util.TwitterOAuth.consumer_key,
						Util.TwitterOAuth.consumer_secret,
						apiConsumer.getToken(),
						apiConsumer.getTokenSecret());

				req.getSession().removeAttribute("request_key");
				req.getSession().removeAttribute("request_secret");

				oAuthUrl="/initialize.jsp";
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		
		return oAuthUrl;
	}
	
	private String oAuthTwitter(HttpServletRequest req,String step){
		String oAuthUrl="/index.jsp";
		Properties prop = new Properties();
		
        prop.setProperty("oauth.consumerKey", TwitterOAuth.consumer_key);
        prop.setProperty("oauth.consumerSecret", TwitterOAuth.consumer_secret);
        
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(TwitterOAuth.consumer_key, TwitterOAuth.consumer_secret);
        try {
        	
			if (step.compareTo("1")==0){
				
				RequestToken requestToken = twitter.getOAuthRequestToken("http://localhost:8888/oauth?network=twitter&step=2");
				
				req.getSession().setAttribute("request_key", requestToken.getToken());
				req.getSession().setAttribute("request_secret",requestToken.getTokenSecret());				
				
				oAuthUrl=requestToken.getAuthenticationURL();
				
			}else{
				
				String request_key = (String) req.getSession().getAttribute("request_key");
				String request_secret=(String) req.getSession().getAttribute("request_secret");				
				
				String oauth_verifier = req.getParameter("oauth_verifier");
				
				RequestToken requestToken = new RequestToken(request_key,request_secret);
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,oauth_verifier);
				
				Util.setNetworkAccount("TwitterAccount",
						Util.TwitterOAuth.consumer_key,
						Util.TwitterOAuth.consumer_secret,
						accessToken.getToken(),
						accessToken.getTokenSecret());
				
				req.getSession().removeAttribute("request_key");
		        req.getSession().removeAttribute("request_secret");
		        
		        oAuthUrl="/initialize.jsp";
			}						
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.severe(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			logger.severe(e.getMessage());			
		}
        
        return oAuthUrl;
	}
}
