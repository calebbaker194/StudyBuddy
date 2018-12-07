package sql;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

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
	 * @return : returns true if the entered password matches the hash
	 * 			 in the db
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
	
	/**
	 * This method retrieves a user's id number given their username.
	 * 
	 * @param uname : client's username
	 * @return : A string with the client's user id number.
	 */
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
	 * Adds a new flash card for a user. Retrieves flash card info
	 * from the card object and constructs an insertion query.
	 * 
	 * @param card : a FlashCard object with all the necessary info
	 * @return : returns true if the query was successful, false otherwise.
	 */
	public static boolean addFlashCard(FlashCard card) {
		
		//this query adds the flash card to the database. the "?" are placeholders
		String addFCquery = "INSERT INTO public.\"FlashCard\"(userid, question, answer, \"group\", description, datecreated) "
				+ "VALUES (?, ?, ?, ?, ?, DEFAULT)";
		
		//wrapping entire insertion process in try-catch block
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			
			//establishing above queries as prepared statements in Postgres
			//PreparedStatement findCIDpst = con.prepareStatement(findCIDquery);
			PreparedStatement addFCpst = con.prepareStatement(addFCquery);
			
			//setting value of placeholders in INSERT query
			addFCpst.setInt(1, card.getUserid());
			addFCpst.setString(2, card.getQuestion());
			addFCpst.setString(3, card.getAnswer());
			addFCpst.setString(4, card.getGroup());
			addFCpst.setString(5, card.getDescription());
			
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
	
	/**
	 * Deletes a group of flash cards. Prevents secondary SQL Injection
	 * 
	 * @param group : group name of flash cards
	 * @param userid : user who is deleting flash cards
	 * @return : true if successful, false otherwise
	 */
	public static boolean deleteCardGroup(String group, int userid) {
		
		String deleteGroupQuery = "DELETE FROM public.\"FlashCard\" WHERE userid = ? AND \"group\" = ?;";
		
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			
			PreparedStatement pst = con.prepareStatement(deleteGroupQuery);
			pst.setInt(1, userid);
			pst.setString(2, group);
			
			try {
				
				pst.executeUpdate();
				return true;
				
			} catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}
			
		} catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Retrieves a users's flash cards using their id and a group name
	 * 
	 * @param userid : client's id number
	 * @param group : group name of flash cards desired
	 * @return : a ResultList object constructed from the query results
	 * 			 if successful, null otherwise.
	 */
	public static ResultList getFlashCards(int userid, String group) {
		
		String getCardsQuery = "SELECT question, answer FROM public.\"FlashCard\" WHERE userid = ? AND \"group\" = ?;";
		
		try(Connection con = DriverManager.getConnection(connectionString, username, password)) {
			
			PreparedStatement pst = con.prepareStatement(getCardsQuery);
			pst.setInt(1, userid);
			pst.setString(2, group);
			
			try(ResultSet rs = pst.executeQuery()) {
				
				List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
				HashMap<String, Object> row;
				
				ResultSetMetaData rm = rs.getMetaData();
				int columns = rm.getColumnCount();
				
				while(rs.next()) {
					row = new HashMap<String, Object>(columns);
					for(int i = 1; i <= columns; i++) {
						row.put(rm.getColumnName(i), rs.getObject(i));
					}
					results.add(row);
				}
				
				return new ResultList(results);
			} catch(SQLException e) {
				Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
				lgr.log(Level.SEVERE, e.getMessage(), e);
				return null;
			}
			
		} catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

}
