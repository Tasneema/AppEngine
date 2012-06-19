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

import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;


public class Util {
	private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private static final String consumer_key ="aA12052765241060";
	private static final String consumer_secret="qHQf10665719";
	private static final String adSpace = "BV18721";
	
	public static String getConsumerKey(){
		return consumer_key;
	}
	
	public static String getCosumerSecret(){
		return consumer_secret;
	}
	
	public static DatastoreService getDatastoreServiceInstance(){
		  return datastore;
	}
	
	public static String getAdSpace(){
		return adSpace;
	}
}
