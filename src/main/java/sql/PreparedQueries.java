package sql;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		String query = "SELECT username, userpassword FROM public.\"UserAccount\" "
				+ "WHERE username = ? AND userpassword = ?;";
		
		//try-with-resources block. Closes the Connection automatically when done.
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			//establishing the query as a prepared statement for Postgres
			PreparedStatement pst = con.prepareStatement(query);
			//setting the value of the parameters
			pst.setString(1, uname);
			pst.setString(2, upass);
			//executing the query
			try(ResultSet rs = pst.executeQuery()) {
				/*
				 * next() positions an iterator behind the first row of the result set.
				 * It returns true if there are more rows, false if there are no more.
				 * Therefore, if next() returns false on the first try, the query was
				 * unsuccessful. If next() returns true, then the query found the matching
				 * data, and the client can be logged in.
				 */
				if(!rs.next())
					return false;
				else
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
				+ "userpassword, email, firstname, lastname, classification, school) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?);";
		
		//try-with-resources block. Closes the Connection automatically when done.
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			//establishing the query as a prepared statement for Postgres
			PreparedStatement pst = con.prepareStatement(query);
			//setting the value of the parameters
			pst.setString(1, uname);
			pst.setString(2, upass);
			pst.setString(3, email);
			pst.setString(4, fname);
			pst.setString(5, lname);
			pst.setString(6, classif);
			pst.setString(7, school);
			
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
	public static boolean addFlashCard(String question, String answer, String course, String uname) {
		
		//this query gets the userid. The "?" is a placeholder
		String findUIDquery = "SELECT userid FROM public.\"UserAccount\" WHERE username = ?";
		//this query gets the courseid. The "?" is a placeholder
		String findCIDquery = "SELECT courseid FROM public.\"Courses\" WHERE coursename = ?";
		//this query adds the flash card to the database. the "?" are placeholders
		String addFCquery = "INSERT INTO public.\"FlashCard\"(userid, courseid, question, answer) "
				+ "VALUES (?, ?, ?, ?)";
		
		//wrapping entire insertion process in try-catch block
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			
			//establishing above queries as prepared statements in Postgres
			PreparedStatement findUIDpst = con.prepareStatement(findUIDquery);
			PreparedStatement findCIDpst = con.prepareStatement(findCIDquery);
			PreparedStatement addFCpst = con.prepareStatement(addFCquery);
			
			//declaring variables to hold userid and courseid
			int userid, courseid;
			
			//setting the values of the parameters in both SELECT queries
			findUIDpst.setString(1, uname);
			findCIDpst.setString(1, course);
			
			//attempting to find the userid. Wrapped in try-catch block
			try(ResultSet uidPSTres = findUIDpst.executeQuery()) {
				if(!uidPSTres.next())
					return false;
				else
					userid = uidPSTres.getInt(1);
			}
			catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}
			
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
			
			//setting vlaue of placeholders in INSERT query
			addFCpst.setInt(1, userid);
			addFCpst.setInt(2, courseid);
			addFCpst.setString(3, question);
			addFCpst.setString(4, answer);
			
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
