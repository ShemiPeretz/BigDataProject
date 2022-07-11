/**
 * 
 */
package org.bgu.ise.ddb.registration;



import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController{
	
	MongoClient mongoClient = MongoClients.create("mongodb+srv://shemiperetz:gfahu3554@cluster0.giyqjds.mongodb.net");
	MongoDatabase db = mongoClient.getDatabase("BigDataProjectDB");
	/**
	 * The function checks if the username exist,
	 * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
	 * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param response
	 */
	@RequestMapping(value = "register_new_customer", method={RequestMethod.POST})
	public void registerNewUser(@RequestParam("username") String username,
			@RequestParam("password")    String password,
			@RequestParam("firstName")   String firstName,
			@RequestParam("lastName")  String lastName,
			HttpServletResponse response){
		System.out.println(username+" "+password+" "+lastName+" "+firstName);
		
		try {
			if(this.isExistUser(username)) {
				HttpStatus status = HttpStatus.CONFLICT;
				response.setStatus(status.value());
			} else {
//				MongoClient mongoClient = new MongoClient("localhost", 27017);
//				MongoClient mongoClient = MongoClients.create("mongodb+srv://shemiperetz:gfahu3554@cluster0.giyqjds.mongodb.net");
//				MongoDatabase db = mongoClient.getDatabase("BigDataProjectDB");
				MongoCollection<Document> collection = this.db.getCollection("users");
								
				Document document = new Document();
			    document.put("username", username);
			    document.put("password", password);
			    document.put("firstName", firstName);
			    document.put("lastName", lastName);
			    
		        Long date = System.currentTimeMillis();
			    document.put("date", date);
			    //Inserting the document into the collection
			    collection.insertOne(document);
			    
				HttpStatus status = HttpStatus.OK;
				response.setStatus(status.value());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The function returns true if the received username exist in the system otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_user", method={RequestMethod.GET})
	public boolean isExistUser(@RequestParam("username") String username) throws IOException{
		System.out.println(username);
		boolean result = false;
			
		MongoCollection<Document> collection = this.db.getCollection("users");
        Document doc = collection.find(eq("username",username)).first();
        if (doc != null) {
            result  = true;
	}
        return result;
	}
	
	/**
	 * The function returns true if the received username and password match a system storage entry, otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "validate_user", method={RequestMethod.POST})
	public boolean validateUser(@RequestParam("username") String username,
			@RequestParam("password") String password) throws IOException{
		System.out.println(username+" "+password);
		boolean result = false;
		
		MongoCollection<Document> collection = this.db.getCollection("users");
        Document doc = collection.find(eq("username",username)).first();// based on structure 
        
        if (doc != null && doc.get("password").equals(password)) {
            result  = true;
        }
	
		return result;
		
	}
	/**
	 * The function retrieves number of the registered users in the past n days
	 * @param days
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "get_number_of_registred_users", method={RequestMethod.GET})
	public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException{
		System.out.println(days+"");
		int result = 0;
	
		
		MongoCollection<Document> collection = this.db.getCollection("users");
		
		Long targetDate = System.currentTimeMillis() - days*24*60*60*1000;
		
		User[] allUsers = this.getAllUsers();
		for (User user : allUsers) {
			if(user.getDate() >= targetDate) {
				result++;
			}
		}
		
		return result;
		
	}
	
	/**
	 * The function retrieves all the users
	 * @return
	 */
	@RequestMapping(value = "get_all_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(User.class)
	public  User[] getAllUsers(){
		ArrayList<User> u = new ArrayList<User>();
		
		  //Creating a MongoDB client
//		  MongoClient mongo = new MongoClient( "localhost" , 27017 );
		  //Connecting to the database
//		  MongoDatabase database = mongo.getDatabase("Bigdata");
		  //Creating a collection object
		  MongoCollection<Document> collection = this.db.getCollection("users");
		  //Retrieving the documents
		      FindIterable<Document> iterDoc = collection.find();
		      MongoCursor<Document> it = iterDoc.iterator();
		      do { 
		    	  if (iterDoc != null) {
		    		  it.next();
//			    	  User newUser = new User(doc.getString("username"),
//			    			  doc.getString("password"),
//			    			  doc.getString("firstname"),
//			    			  doc.getString("lastname"),
//			    			  doc.getLong("date"));
//			         u.add(newUser);
		    	  }
		      } while (iterDoc.hasNext()); 
		System.out.println(u);
		return (User[]) u.toArray();
	}

}
