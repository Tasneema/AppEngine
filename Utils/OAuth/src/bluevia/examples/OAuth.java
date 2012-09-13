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

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;


/**
 * @author almo
 * This application shows how to get an access token on behalf of a user. It uses an out-of-band process 
 * to get the authentication code. As interesting features, the application reads the data needed for the
 * connection of a file named "API-Data.ini". The file should be in the same directory of the application 
 * and it should contain the following data, one for each line: consumer_key, consumer_secret, requestTokenURL
 * accessTokenURL and authorizeURL.
 * IMPORTANTE: if you use this application to get you accessToken, don't forget to save it once obtained.   
 * You can get more examples at anonymous svn:
 * svn checkout https://svn.forge.morfeo-project.org/insomne/trunk/Mobile-Clients/examples
 */
public class OAuth {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader iReader = null;
		
		String apiDataFile = "API-Data.ini";
		String requestTokenURL;
		String accessTokenURL;
		String authorizeURL;

		String consumer_key;
		String consumer_secret;
		
		System.out.println("Starting oAuthDemo....");

        try {
        	// Get API access data from an external file just to hide connection data
        	iReader = new BufferedReader (new FileReader(apiDataFile));        	
        	consumer_key = iReader.readLine();
        	consumer_secret= iReader.readLine();
        	
        	requestTokenURL = iReader.readLine(); 
        	accessTokenURL = iReader.readLine();
        	authorizeURL = iReader.readLine();
        	
        	// Get the requestToken
     		OAuthConsumer apiConsumer = new DefaultOAuthConsumer(consumer_key,consumer_secret);
	    	OAuthProvider apiProvider = new DefaultOAuthProvider(requestTokenURL,accessTokenURL,authorizeURL);

	    	apiConsumer.setMessageSigner(new HmacSha1MessageSigner ());     
	    	
	    	String oAuthUrl= apiProvider.retrieveRequestToken(apiConsumer,oauth.signpost.OAuth.OUT_OF_BAND);
	    	System.out.printf("Request Token (KEY: %s, SECRET: %s)\n",apiConsumer.getToken(),apiConsumer.getTokenSecret());

	    	// Get the verification code out of band.
		    System.out.printf("oAuth URL:%s\n",oAuthUrl);
	        System.out.println("Please visit:\n" + oAuthUrl + "\n... and get the authorization code for this app.\nPlease, enter your verification code: ");

	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        String verification_code = br.readLine();
	        
	        System.out.printf("The verification code is:%s\n",verification_code);
	        
	        // Get the access token
	        apiProvider.retrieveAccessToken(apiConsumer, verification_code.trim());
	        System.out.printf("Access Token (KEY: %s, SECRET: %s)\n",apiConsumer.getToken(),apiConsumer.getTokenSecret());	        
        
		} catch (Exception e){
			System.out.println(e.getMessage());
		} finally {
			if (iReader != null)
				iReader.close();
		}

		System.out.println("Finishing oAuthDemo....");
	}
}
