package sql;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import org.mindrot.jbcrypt.BCrypt;

import controller.FlashCard;

/**
 * 
 * @author Zac Migues
 * This class will contain all the parameterized queries
 * that we need. This class contains methods to authenticate a user's credentials on a login attempt,
 * to register a new user account with client-submitted information, and for a client with a user account
 * to create a flash card.
 *
 * These queries are paramterized because they handle information submitted by a client,
 * who has the potential to be malicious. The placeholder values in the queries prevent
 * SQL injection by using all text entered by the client as the value for the placeholder, rather
 * concatenating the client's information into the query.
 */
public class PreparedQueries extends SQL {
	
	/**
	 * This method checks a user's entered account info against the database
	 * with a parameterized query to prevent SQL injection.
	 * 
	 * @param uname : the client's username
	 * @param upass : the client's password
	 * @return : returns true if the query returns a result, returns false
	 * 			 if the query returns nothing or if there is an error
	 */
	public static boolean authenticate(String uname, String upass) {
		
		//this is the parameterized query. The ? are placeholders that will be filled
		String query = "SELECT userpassword FROM public.\"UserAccount\" "
				+ "WHERE username = ?";
		
		//try-with-resources block. Closes the Connection automatically when done.
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			//establishing the query as a prepared statement for Postgres
			PreparedStatement pst = con.prepareStatement(query);
			
			//setting the value of the parameters
			pst.setString(1, uname);
			//executing the query
			try(ResultSet rs = pst.executeQuery()) {
				
				if(!rs.next())
					return false;
				else {
					String dbhash = rs.getString(1);
					
					if(BCrypt.checkpw(upass, dbhash))
						return true;
					else 
						return false;
				}
			}
			catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}
			
		}
		catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		
	}
	
	public static String getUserID(String uname) {
		
		String query = "SELECT userid FROM public.\"UserAccount\" WHERE username = ?";
		
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			PreparedStatement pst = con.prepareStatement(query);
			
			pst.setString(1, uname);
			
			try(ResultSet rs = pst.executeQuery()) {
				int uid = 0;
				if(rs.next()) {
					uid = rs.getInt("userid");
				}
				else {
					return null;
				}
				String userid = Integer.toString(uid);
				return userid;
			}
			catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return null;
			}
		} 
		catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
		
	}
	
	/**
	 * 
	 * This function inserts client submitted info into the UserAccount table,
	 * creating an account for the user.
	 * 
	 * @param uname : client's desired username
	 * @param upass : client's desired password
	 * @param email : client's email address
	 * @param school : university attended by client
	 * @param fname : client's given name
	 * @param lname : client's christian name
	 * @param classif : client's school year
	 * @return : returns true if the query was successful, false otherwise
	 */
	public static boolean registerAccount(String uname, String upass, String email, 
			String school, String fname, String lname, String classif) 
	{
		//this is the insertion query. the ? are placeholders that will be filled w/ data
		String query = "INSERT INTO public.\"UserAccount\"(username, "
				+ "userpassword, email, firstname, lastname, classification, school, loggedin) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
		
		//try-with-resources block. Closes the Connection automatically when done.
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			//establishing the query as a prepared statement for Postgres
			PreparedStatement pst = con.prepareStatement(query);
			
			//encrypt password
			upass = BCrypt.hashpw(upass, BCrypt.gensalt());
			
			//setting the value of the parameters
			pst.setString(1, uname);
			pst.setString(2, upass);
			pst.setString(3, email);
			pst.setString(4, fname);
			pst.setString(5, lname);
			pst.setString(6, classif);
			pst.setString(7, school);
			pst.setBoolean(8, true);
			
			try {
				pst.executeUpdate(); //method used when no return data is expected
				return true;
			}
			catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}
			
		}
		catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Adds a new flash card for a user. This method first queries the database
	 * to retrieve the user's userid and the courseid of the course the flashcard
	 * is for. When those have been retrieved, a new flash card will be added to the
	 * database for the user.
	 * 
	 * @param question : the question the flash card asks
	 * @param answer : the answer to the question
	 * @param course : the name of the course for the flash card
	 * @param uname : the username of the client
	 * @return : returns true if the query was successful, false otherwise.
	 */
	public static boolean addFlashCard(FlashCard card) {
		
		//this query gets the courseid. The "?" is a placeholder
		String findCIDquery = "SELECT courseid FROM public.\"Courses\" WHERE coursename = ?";
		//this query adds the flash card to the database. the "?" are placeholders
		String addFCquery = "INSERT INTO public.\"FlashCard\"(userid, courseid, question, answer, \"group\", description) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		
		//wrapping entire insertion process in try-catch block
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			
			//establishing above queries as prepared statements in Postgres
			PreparedStatement findCIDpst = con.prepareStatement(findCIDquery);
			PreparedStatement addFCpst = con.prepareStatement(addFCquery);
			
			//declaring variables to hold userid and courseid
			int courseid;
			
			//setting the values of the parameters in both SELECT queries
			findCIDpst.setString(1, card.getCoursename());
			
			//attempting to find courseid. Wrapped in try-catch block
			try(ResultSet cidPSTres = findCIDpst.executeQuery()) {
				if(!cidPSTres.next())
					return false;
				else
					courseid = cidPSTres.getInt(1);
			}
			catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}
			
			//setting value of placeholders in INSERT query
			addFCpst.setInt(1, card.getUserid());
			addFCpst.setInt(2, courseid);
			addFCpst.setString(3, card.getQuestion());
			addFCpst.setString(4, card.getAnswer());
			addFCpst.setString(5, card.getGroup());
			addFCpst.setString(6, card.getDescription());
			
			//attempting to update database. Wrapped in try-catch block
			try {
				addFCpst.executeUpdate();
				return true;
			}
			catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}
			
		}
		catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		
	}

}
