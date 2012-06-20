<!--
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
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.EntityNotFoundException" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="java.util.List" %>
<%@ page import="oauth.signpost.basic.DefaultOAuthConsumer"%>
<%@ page import="oauth.signpost.OAuthConsumer" %>
<%@ page import="oauth.signpost.signature.HmacSha1MessageSigner" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="bluevia.*" %>
<%@ page import="com.google.appengine.api.datastore.Query.SortDirection" %>
   
<html>
 <head>
   <style type="text/css">
       body{
    min-width: 810px;
    max-width: 810px;
    padding-top:5px;
        padding-left: 50px;
        padding-right: 498px;   
      }
      .main{    
    border: 5px double blue;
    font-size: large;
    min-height:140px;
    height:100%;
    padding:5px;  
      }
      .sign-up{ 
    border: 3px solid blue; 
    border-radius:25px; 
    float: right; 
    padding:7px;
    font-size:small;
      }
    .user_info{
        border: 3px solid blue; 
        border-radius:25px;
        padding:7px;
    }
    </style>
  </head>
<body>
<%
    String latitude="-4.700471";
    String longitude="55.521759";   

    User user=null;
    List<Entity> results =null;
    UserService userService = UserServiceFactory.getUserService();
    user = userService.getCurrentUser();
    Entity bvUser=null;
    
    if (user!=null){	
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("BlueViaUser");
        query.addFilter("mail", Query.FilterOperator.EQUAL, user.getEmail());
        results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());                  
        if (!results.isEmpty()){
        	bvUser = results.remove(0);
        	latitude="";
        	longitude="";
        }else
        	bvUser=null;
   }
%>
 <div class="main">
    <img src="images/BV-728x90.png" style="width:100%;"/>
    <div style="padding:2px;">
    <%if (user == null) {%>
        <div class="sign-up">
	    <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
	    </div>	  
	    Welcome to Bluevia @ Google App Engine.<br>
	    Sign up to start playing with BlueVia APIs	
	         
    <% } else { %>        
        <div class="sign-up">
        <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Sign out</a>
        </div>
        <% if (bvUser!=null){ %>             
            <div class="sign-up"> <a href="bluevia/remove-user">Delete Account</a></div>
        <%} %>    
        Hello, <%=user.getNickname() %><br>
        Welcome to Bluevia @ Google App Engine.<br>
        
        <% if (bvUser==null){%>
            Before to start, you should authorize. Just click <a href="/bluevia">here</a>.        
        <% } else { %>                        
            <div style="padding:2px;">
	            <div class="user_info" style="float:left; width:300px;">
		            <h3>Personal Profile</h3>
		            <table><tr> <td>Nickname:</td><td><%=user.getNickname() %></td></tr>
		                   <tr> <td>Mail Address:</td><td><%= user.getEmail()%></td></tr>		                                                
		            </table>
	            </div>
	              <div class="user_info" style="float:right;width:440px; height:440px">
	              <h3><%=user.getNickname() %>'s Messages</h3>
	              <%
	              DatastoreService datastore = Util.getDatastoreServiceInstance();
	              Query query = new Query("BlueViaUser");
                  query.addFilter("mail", Query.FilterOperator.EQUAL, user.getEmail());
                  List<Entity> userQuery = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
                  if (!userQuery.isEmpty()){
                      Entity currentUser = userQuery.remove(0);
                      Key userKey = currentUser.getKey();
                      
                      Query msgQuery= new Query();
                      msgQuery.setAncestor(userKey);
                      msgQuery.addFilter(Entity.KEY_RESERVED_PROPERTY,Query.FilterOperator.GREATER_THAN,userKey);
                                            
                      List<Entity> msgList = datastore.prepare(msgQuery).asList(FetchOptions.Builder.withLimit(5));
                      
                      for (Entity msgItem : msgList) {
                %> 
                      Date: <%= msgItem.getProperty("Date") %><br>
                      Sender: <%= msgItem.getProperty("Sender") %><br>
                      Message:<%= msgItem.getProperty("Message") %><br>
                   <% }
                  }
	              %>
	              </div>
	            <div style="float: left;padding:2px;">
		            <div class="user_info" style="width:297px;">
		            <h3>SMS Service</h3>
		            <form action="/bluevia/send-sms" method="post">
		            Contact:<br />
		            <textarea name="phone-number" rows="1" cols="30"></textarea><br />
		            Text:<br />
		            <textarea name="sms-message" rows="3" cols="30"></textarea><br />
		            <input type="submit" value="Send SMS" />   
		            </form>
		            </div>
	            </div>
            </div>      
       <% } %>               
    <% } %>
    </div>    
  </div>  
</body>
</html>
