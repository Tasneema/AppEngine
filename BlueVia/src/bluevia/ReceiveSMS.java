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
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.me.JSONObject;
import org.json.me.JSONArray;
import org.json.me.JSONException;

@SuppressWarnings("serial")
public class ReceiveSMS extends HttpServlet {
	private static final Logger log = Logger.getLogger(ReceiveSMS.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String contentType = req.getContentType();
		String oauth_consumer_key="";
		
		String htmlAuthHeader=req.getHeader("Authorization");
		if (htmlAuthHeader!=null){
			
			StringTokenizer htmlHeader = new StringTokenizer(htmlAuthHeader," ");
			String szAux = htmlHeader.nextToken();
			if (szAux.compareTo("OAuth")==0){
				szAux = htmlHeader.nextToken(",");
				szAux=szAux.trim();
				while(szAux!=null){
					StringTokenizer headerParam = new StringTokenizer(szAux,"='");
					String param = headerParam.nextToken();
					if (param.compareTo("oauth_consumer_key")==0){
						szAux=headerParam.nextToken();
						szAux = szAux.replace('"', ' ');
						oauth_consumer_key=szAux.trim();		
						break;
					}else{
						szAux = htmlHeader.nextToken(",");
						szAux = szAux.replace('"', ' ');
						szAux=szAux.trim();
					}
				}
			}
			
			if (oauth_consumer_key.compareTo(Util.BlueViaOAuth.consumer_key)==0){
				if (contentType.compareTo("application/json")==0){
					try{
						StringBuffer doc = new StringBuffer();
						BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
						String line = br.readLine();
						while (line != null) {
							doc.append(line);
							line = br.readLine();
						}
						
						JSONObject receivedSMSAsync = new JSONObject(doc.toString());
						JSONObject smsInfo = receivedSMSAsync.getJSONObject("receivedSMSAsync").getJSONObject("message");
						
						String szMessage = smsInfo.getString("message");
	            		String szOrigin = smsInfo.getJSONObject("originAddress").getString("phoneNumber");
	            		Date now= new Date();
	            		
	            		StringTokenizer msgParser = new StringTokenizer(szMessage);
						
						// Removing app id
						msgParser.nextToken();
						
						String userAlias = msgParser.nextToken();
						
						String msg = "";
						while (msgParser.hasMoreTokens())
							msg += " "+ msgParser.nextToken();    						    					    	
						
						String userEmail = (String)Util.getUserWithAlias(userAlias).getProperty("mail");
						if (userEmail!=null){
							Util.addUserMessage(userEmail, szOrigin, msg, Long.toString(now.getTime()));
							
							SendSMS.setTwitterStatus(userEmail,msg);
	
							SendSMS.setFacebookWallPost(userEmail,msg);
							
							resp.setStatus(HttpServletResponse.SC_NO_CONTENT);	
						}else
							resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Error processing receivedSMSAsync: invalid user/alias");
						
					} catch (Exception e) {
						log.severe(e.getMessage());
						resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Error processing receivedSMSAsync: invalid json data structure\n"+e.getMessage());
					}						
				}else
					resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"This version of the server only accepts applicaciont/json");
			}else
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}else
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}
