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
<%@ page import="java.util.Date" %>
<%@ page import="bluevia.Util" %>

<!DOCTYPE html>
<html>
  <head>
  <title>Initializating user account</title>
  </head>
  <body>
  <%
  UserService userService = UserServiceFactory.getUserService();
  User user = userService.getCurrentUser();
  
  if (Util.getUser(user.getEmail())==null){
    Date date = new Date();
    Util.addUser(user.getEmail(),user.getNickname(),date.getTime());
  }
  %>
  <div>
    <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Sign out</a>
  </div>
  Before to start, you should authorize. 
  <table> 
      <tr>
      <td><img src="images/bluevia-small.png"/></td>
      <td>Click <a href="/oauth?network=bluevia&step=1">here</a> to authorize with BlueVia.</td>
      </tr>
      <tr>
      <td><img src="images/twitter-small.png"/></td>
      <td>Click <a href="/oauth?network=twitter&step=1">here</a> to authorize with Twitter.</td>               
      </tr>
      <tr>
      <td><img src="images/facebook-small.png"/></td>
      <td>Click <a href="/oauth?network=facebook&step=1">here</a> to authorize with Facebook.</td>               
      </tr>               
  </table>    
  <div>
    <a href="index.jsp">Back</a>
  </div>                
  </body>
</html>