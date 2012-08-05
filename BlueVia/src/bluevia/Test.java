package bluevia;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import bluevia.Util;

@SuppressWarnings("serial")
public class Test extends HttpServlet {
	private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.info("Starting the test");
		
		logger.info((testingUser()?"Test addUser/getUser with NO errors":"Test addUser/getUser with errors"));
		
		logger.info((testNetworksAccount()?"Test NetworkAccounts with NO errors":"Test NetworkAccounts with errors"));
		
		logger.info((testUserMessages()?"Test UserMessages with NO errors":"Test UserMessages with errors"));
		
	}
	
	private boolean testUserMessages(){
		int testIndex=0;
		boolean bError=false;
		List<Entity> messageList = null;
		
		++testIndex;
		messageList=Util.getUserMessages("aleonar@gmail.com");
		if (messageList!=null){
			if (!messageList.isEmpty()){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": Database empty but getUserMessages(use1(aleonar)) got them");
				bError=true;
			}
			messageList=null;
		}
		
		Date date = new Date();
		Util.addUser("aleonar@gmail.com", "aleonar", date.getTime());
		++testIndex;
		messageList=Util.getUserMessages("aleonar@gmail.com");
		if (messageList!=null){
			if (!messageList.isEmpty()){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": User1 (aleonar) added without messages but getUserMessages(user1(aleonar)) got them");
				bError=true;
			}
			messageList=null;
		}
		date=null;
		
		++testIndex;
		messageList=Util.getUserMessages("almo@live.co.uk");
		if (messageList!=null){
			if (!messageList.isEmpty()){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": User1 (aleonar) added without messages but getUserMessages(user2 (almo)) got them");
				bError=true;
			}
			messageList=null;
		}
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		date = new Date();
		Random random = new Random(System.currentTimeMillis());
		
		Util.addUserMessage("almo@live.co.uk", "447860518373", "Random message "+String.format("%d", random.nextLong()), df.format(date));
	
		++testIndex;
		messageList=Util.getUserMessages("aleonar@gmail.com");
		if (messageList!=null){
			if (!messageList.isEmpty()){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": Message added to non exist User2 (almo) but getUserMessages(user1(aleonar)) got them");
				bError=true;
			}
			messageList=null;
		}
		
		++testIndex;
		messageList=Util.getUserMessages("almo@live.co.uk");
		if (messageList!=null){
			if (!messageList.isEmpty()){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": Message added to non exist User2 (almo) but getUserMessages(user2(almo)) got them");
				bError=true;
			}
			messageList=null;
		}
		
		Util.addUserMessage("aleonar@gmail.com", "447860518373", "Random message "+String.format("%d", random.nextLong()), df.format(date));
		++testIndex;
		messageList=Util.getUserMessages("aleonar@gmail.com");
		if (messageList!=null){
			if (messageList.size()!=1){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": One message added User1 (aleonar) but getUserMessages(user1(aleonar)) got more than one");
				bError=true;
			}
			messageList=null;
		}else{
			logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": Message added User1 (aleonar) but getUserMessages(user1(aleonar)) got none");
			bError=true;
		}
		
		
		for (int i=0; i<20;i++)
			Util.addUserMessage("aleonar@gmail.com", "447860518373", "Random message "+String.format("%d", random.nextLong()), df.format(date));
		
		messageList=Util.getUserMessages("aleonar@gmail.com");
		if (messageList!=null){
			if (messageList.size()!=21){
				logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": 21 messages added User1 (aleonar) but getUserMessages(user1(aleonar)) got distint number");
				bError=true;
			}
			messageList=null;
		}else{
			logger.severe("Checking UserMessages #"+ String.format("%02d", testIndex)+": 21 messages added User1 (aleonar) but getUserMessages(user1(aleonar)) got distint number");
			bError=true;
		}
		
		Util.deleteUser("aleonar@gmail.com");
		return bError;
	}
	
	
	private boolean testNetworksAccount(){
		int testIndex=0;
		boolean bError=false;
		Properties twitterAccount= null;
		Properties blueviaAccount=null;
		Properties facebookAccount=null;
				
		++testIndex;
		twitterAccount = Util.getNetworkAccount("aleonar@gmail.com", "TwitterAccount");
		if (twitterAccount!=null){
			logger.severe("Checking NetworkAccounts #"+ String.format("%02d", testIndex)+": Database empty but getNetworkAccount got one (twitterAccount)");
			bError=true;
		}
		twitterAccount=null;
		
		Date date = new Date();		
		Util.addUser("aleonar@gmail.com","aleonar",date.getTime());

		++testIndex;
		twitterAccount = Util.getNetworkAccount("aleonar@gmail.com", "TwitterAccount");
		if (twitterAccount!=null){
			logger.severe("Checking NetworkAccounts #"+ String.format("%02d", testIndex)+": user1 (aleonar) create but with no NA but getNetworkAccount got one (twitterAccount)");
			bError=true;
		}
		twitterAccount=null;

		++testIndex;
		Util.addNetworkAccount("almo@live.co.uk","TwitterAccount","consume_key","consume_secret","access_key","access_secret");
		twitterAccount = Util.getNetworkAccount("aleonar@gmail.com", "TwitterAccount");
		if (twitterAccount!=null){
			logger.severe("Checking NetworkAccounts #"+ String.format("%02d", testIndex)+": user1 (aleonar) create but NA added to other (almo) and getNetworkAccount got one (twitterAccount)");
			bError=true;
		}
		twitterAccount=null;
		
		++testIndex;
		Util.addNetworkAccount("aleonar@gmail.com","TwitterAccount","consume_key","consume_secret","access_key","access_secret");
		twitterAccount = Util.getNetworkAccount("aleonar@gmail.com", "TwitterAccount");
		if (twitterAccount==null){
			logger.severe("Checking NetworkAccounts #"+ String.format("%02d", testIndex)+": user1 (aleonar) created, NA added but getNetworkAccount didn't get (twitterAccount)");
			bError=true;
		}
		twitterAccount=null;

		++testIndex;
		blueviaAccount = Util.getNetworkAccount("aleonar@gmail.com", "BlueViaAccount");
		if (blueviaAccount!=null){
			logger.severe("Checking NetworkAccounts #"+ String.format("%02d", testIndex)+": user1 (aleonar) created, NA added (twitter) but getNetworkAccount got other (bluevia)");
			bError=true;
		}
		blueviaAccount=null;
		
		++testIndex;
		Util.addNetworkAccount("aleonar@gmail.com","BlueViaAccount","consume_key","consume_secret","access_key","access_secret");
		Util.addNetworkAccount("aleonar@gmail.com","FaceBookAccount","consume_key","consume_secret","access_key","access_secret");
		
		twitterAccount = Util.getNetworkAccount("aleonar@gmail.com", "TwitterAccount");
		blueviaAccount = Util.getNetworkAccount("aleonar@gmail.com", "BlueViaAccount");
		facebookAccount = Util.getNetworkAccount("aleonar@gmail.com", "FaceBookAccount");
		
		if ((twitterAccount==null)||(blueviaAccount==null)||(facebookAccount==null)){
			logger.severe("Checking NetworkAccounts #"+ String.format("%02d", testIndex)+": added twitter, bluevia and facebook NA but they all aren not there");
			bError=true;
		}

		Util.deleteUser("aleonar@gmail.com");
		return bError;
	}
	
	private boolean testingUser(){
		
		int testIndex=0;
		boolean bError=false;
		// Testing DataStore functions
		logger.info("Database empty");
		
		++testIndex;
		Entity user1 = Util.getUser("aleonar@gmail.com");
		if (user1!=null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database empty but getUser function got an entitny (user)");
			bError=true;
		}
		user1 = null;
		
		++testIndex;
		Entity user2 = Util.getUser("almo@live.co.uk");
		if (user2!=null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database empty but getUser function got an entitny (user)");
			bError=true;
		}
		user2=null;
		
		logger.info("Adding some users");
		logger.info("Adding user1 (aleonar)");
		
		
		Date date = new Date();
		Util.addUser("aleonar@gmail.com", "aleonar", date.getTime());
		
		++testIndex;
		user1 = Util.getUser("aleonar@gmail.com");
		if (user1==null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database must have user1 (aleonar) but getUser(user1) got no one");
			bError=true;
		}
		user1=null;
		
		++testIndex;
		user2 = Util.getUser("almo@live.co.uk");
		if (user2!=null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database with user1 (aleonar) but getUser function got an user2 (almo)");
			bError=true;
		}
		user2=null;
		
		logger.info("Adding user2 (almo)");
		date = new Date();
		Util.addUser("almo@live.co.uk", "almo", date.getTime());
		
		++testIndex;
		user1 = Util.getUser("aleonar@gmail.com");
		if (user1==null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database must have user1 (aleonar) & user2 (almo) but getUser(user1) got no one");
			bError=true;
		}
		user1=null;
		
		++testIndex;
		user2 = Util.getUser("almo@live.co.uk");
		if (user2==null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database must have user1 (aleonar) & user2 (almo) but getUser(user2) got no one");
			bError=true;
		}
		user2=null;
		
		++testIndex;
		Entity user3 = Util.getUser("davila.grau@computer.org");
		if (user3!=null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": Database must only have user1 (aleonar) & user2 (almo) but getUser function got an user3 (davila.grau)");
			bError=true;
		}	
		user3=null;
		
		++testIndex;
		Util.deleteUser("aleonar@gmail.com");
		user1=Util.getUser("aleonar@gmail.com");
		if (user1!=null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": user1 deleted but seem to be still there");
			bError=true;
		}
		user1=null;
		
		++testIndex;
		user2=Util.getUser("almo@live.co.uk");
		if (user2==null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": after user1 deleted I can't find user2");
			bError=true;
		}
		user2=null;
		
		++testIndex;
		Util.deleteUser("almo@live.co.uk");
		user2=Util.getUser("almo@live.co.uk");
		if (user2!=null){
			logger.severe("Checking addUser/getUser #"+ String.format("%02d", testIndex)+": user2 deleted but seem to be still there");
			bError=true;
		}
		user2=null;		
		
		return bError;
	}
	
}
