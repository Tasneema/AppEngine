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
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.*;

@SuppressWarnings("serial")
public class RemoveUser extends HttpServlet {
	private static final Logger log = Logger.getLogger(RemoveUser.class.getName());
	 
	 public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    	DatastoreService datastore = Util.getDatastoreServiceInstance();
    	Transaction txn = datastore.beginTransaction();

		 try{
			 UserService userService = UserServiceFactory.getUserService();
			 User user = userService.getCurrentUser();
			 if (user!=null){		
				 log.info("Removing data user");	 	
				 	
		    	Query query = new Query("BlueViaUser");
		    	query.addFilter("mail", Query.FilterOperator.EQUAL, user.getEmail());
		    	List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());			    					    
			    Entity bvUser = results.remove(0);
		    	datastore.delete(bvUser.getKey());
		    	txn.commit();
		    	resp.sendRedirect("/index.jsp");
			 }else
				 log.info("Not current user");
		 }catch (Exception e){
			 log.info(e.getMessage());
		 }finally {
		    if (txn.isActive()) {
		        txn.rollback();
		    }
		}
	 }
}
