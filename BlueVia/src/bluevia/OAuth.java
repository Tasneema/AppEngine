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
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

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
		// Fixing Facebook oAuth 
		if (step==null)
			step = req.getParameter("state");
			
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
		
		if (step.compareTo("1")==0){
			oAuthUrl="https://www.facebook.com/dialog/oauth/?client_id="+Util.FaceBookOAuth.consumer_key +
					 "&redirect_uri="+ Util.getCallbackDomain() +"/oauth?network=facebook&step=2" +
					 "&scope=user_status,user_location,email,publish_checkins,publish_actions"+
					 "&state=2";
		}else{
			String code = req.getParameter("code");
			
			oAuthUrl="https://graph.facebook.com/oauth/access_token?"+
					 "client_id="+Util.FaceBookOAuth.consumer_key+
					 "&redirect_uri="+ Util.getCallbackDomain() +"/oauth?network=facebook&step=3"+
					 "&client_secret="+Util.FaceBookOAuth.consumer_secret+
					 "&code="+ code;
			try{
				URL apiURI = new URL (oAuthUrl);
				HttpURLConnection request = (HttpURLConnection)apiURI.openConnection();			
				request.setRequestMethod("GET");
				
				int rc = request.getResponseCode();

				if (rc==HttpURLConnection.HTTP_OK){	
					BufferedReader br = new BufferedReader (new InputStreamReader (request.getInputStream()));
					StringBuffer doc = new StringBuffer();
					String line;

					do{
						line = br.readLine();
						if (line!=null)
							doc.append(line);
					}while (line!=null);
					
					StringTokenizer paramParser = new StringTokenizer(doc.toString());
					String access_token;
					do{
						String param = paramParser.nextToken("&");
						StringTokenizer valueParser = new StringTokenizer(param);
						String name = valueParser.nextToken("=");
						if (name.compareTo("access_token")==0){
							access_token = valueParser.nextToken("=");
							break;
						}
					}while (true);
					
					UserService userService = UserServiceFactory.getUserService();
					User user = userService.getCurrentUser();
					
					Util.addNetworkAccount(user.getEmail(),Util.FaceBookOAuth.networkID,
							Util.FaceBookOAuth.consumer_key,
							Util.FaceBookOAuth.consumer_secret,
							access_token,"");
				}
				
			}catch(Exception e){
				logger.severe("Error: "+e.getMessage());
			}
			
			oAuthUrl="/initialize.jsp";
		}
		
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
				BlueViaOAuth.url_authorize());
		
		apiConsumer.setMessageSigner(new HmacSha1MessageSigner()); 
		
		try{
			if (step.compareTo("1")==0){   

				oAuthUrl= apiProvider.retrieveRequestToken(apiConsumer,Util.getCallbackDomain() +"/oauth?network=bluevia&step=2");
		
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
				
				 UserService userService = UserServiceFactory.getUserService();
				 User user = userService.getCurrentUser();

				Util.addNetworkAccount(user.getEmail(),"BlueViaAccount",
						BlueViaOAuth.consumer_key,
						BlueViaOAuth.consumer_secret,
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
				
				RequestToken requestToken = twitter.getOAuthRequestToken(Util.getCallbackDomain()+"/oauth?network=twitter&step=2");
				
				req.getSession().setAttribute("request_key", requestToken.getToken());
				req.getSession().setAttribute("request_secret",requestToken.getTokenSecret());				
				
				oAuthUrl=requestToken.getAuthenticationURL();
				
			}else{
				
				String request_key = (String) req.getSession().getAttribute("request_key");
				String request_secret=(String) req.getSession().getAttribute("request_secret");				
				
				String oauth_verifier = req.getParameter("oauth_verifier");
				
				RequestToken requestToken = new RequestToken(request_key,request_secret);
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,oauth_verifier);
				 
				UserService userService = UserServiceFactory.getUserService();
				User user = userService.getCurrentUser();
				 
				Util.addNetworkAccount(user.getEmail(),"TwitterAccount",
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