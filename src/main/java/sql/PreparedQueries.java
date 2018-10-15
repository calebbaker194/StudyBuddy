package sql;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Zac Migues
 * This class will contain all the parameterized queries
 * that we need.
 *
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
			ResultSet rs = pst.executeQuery();
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
			pst.executeUpdate(); //method used when no return data is expected
			return true;
		}
		catch(SQLException e) {
			Logger lgr = Logger.getLogger(PreparedQueries.class.getName());
			lgr.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

}
