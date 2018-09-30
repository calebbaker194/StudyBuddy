package sql;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.FailedLoginException;


/**
 * 
 * @author caleb.baker
 * This is a Java SQL wrapper that makes it a little more simple to handle SQL without 
 * knowing some of the connection and connection closing syntax
 * Combined with Result list this is very handy to pulling data from Postgresql database
 */
public class SQL{
	
	public static final Logger LOGGER = Logger.getLogger(SQL.class.getName());
	
	/*
	 * The flags for an SQL query that determine the type of result.
	 */
	public static HashMap<String,Integer> flagMap = new HashMap<String,Integer>();
	
	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not load postgresql Drivers ", e);
		}
		
		flagMap.put("SELECT", 0);
		flagMap.put("INSERT", 1);
		flagMap.put("UPDATE", 2);
		flagMap.put("DELETE", 3);
		flagMap.put("CREATE", 4);
		flagMap.put("ALTER", 5);
		flagMap.put("DROP", 6);
	}
	
	private static String connectionString = null;
	private static String username;
	private static String password;
	private static String dbTableName = "query";

	/**
	 * This connects you to a database and stores the credential so that it can connect again every time you execute a query
	 * @param db : The database you want to connect to 
	 * @param host : The host that the database is on
	 * @param port : The port that you want to connect to the database on
	 * @param username : The username for the connection
	 * @param password : The password for the connection
	 * @return Returns a String. "0" For connection success
	 */
	public static String Connect(String db,String host,int port,String username,String password)
	{
		connectionString="jdbc:postgresql://"+host+":"+port+"/"+db;
		Connection dbConnection;
		
		try
		{
			dbConnection = DriverManager.getConnection(connectionString,username,password);
			dbConnection.close();
		} catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "UNABLE TO LOG IN TO DATABASE ", e);
		}
		setPassword(password);
		setUsername(username);
		return "0";
	}
	
	/**
	 * This is used to execute querys that you have stored on the database. Specifically by default on the table 
	 * "query" there are 4 fields. query_id, query_group, query_name, query_source. 
	 * the tabel name can be configured through. configDatabase.
	 * @param group The group name for the database query
	 * @param name the name of the database query
	 * @return returns the same as a typical query. 
	 */
	public static ResultList executeDbQuery(String group,String name) {
		String query = (String) SFQ("SELECT query_source FROM "+dbTableName+" WHERE query_group ="+group+" AND query_name="+name);
		return executeQuery(query);
	}
	
	/**
	 * Set the table name for executing database query's
	 * @param tablename : the table that the special data is stored on.
	 */
	public static void configDatabase(String tablename) {
		dbTableName = tablename;
	}
	
	/**
	 * This executes a query to the database. It determines the flag for you to know how to return the results.
	 * @param query : the query that you want to execute
	 * @return Returns a result set. 
	 */
	public static ResultList executeQuery(String query)
	{
		int flag = 0;
		
		String function = query.contains(" ") ? query.split(" ")[0].toUpperCase() : query.toUpperCase();
		
		flag = SQL.flagMap.get(function) != null ? SQL.flagMap.get(function) : 0;
		
		
		return executeQuery(query,flag);
	}
	
	/**
	 * This executes a query to the database. that you are connected to and returns a ResultList 
	 * @param query : The query represented by a string.
	 * @param flag : This represents the type of query. If you don't know what type use executeQuery(String); instead.
	 * @return The result set
	 */
	public static ResultList executeQuery(String query,int flag)
	{
		Connection dbConnection=null;
		
		try 
		{
			dbConnection = DriverManager.getConnection(connectionString,username,password);
		}
		catch (SQLException e) 
		{
			System.out.println("Failed To Connect");
			e.printStackTrace();
		}
		
		try {
			if(dbConnection!=null)
			{
				try {
					Statement st = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
					List<HashMap<String,Object>> resultArray = new ArrayList<HashMap<String,Object>>();
					HashMap<String,Object> row;
					ResultSet results;
					
					if(flag==0)
					{
						results = st.executeQuery(query);
					}	
					else
					{
						st.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
						results = st.getGeneratedKeys();
					}
					
					ResultSetMetaData meta= results.getMetaData();
					
						
					int columns = meta.getColumnCount();
					while(results.next())
					{
						row = new HashMap<String,Object>(columns);
						for(int i=1;i<=columns;i++)
						{
							row.put(meta.getColumnName(i), results.getObject(i));
						}
						resultArray.add(row);
					}
				
					results.close();
					st.close();
					dbConnection.close();
					return new ResultList(resultArray);
				} catch (SQLException e) {
					ResultList r = new ResultList();
					r.addRow();
					r.put("error",e.getMessage());
					LOGGER.log(Level.WARNING,"Query Failed: " + e.getMessage(),e);
					return r;
				}
			}
			else
			{
				throw new FailedLoginException("Failed to connect to the database");
			}
		} catch (FailedLoginException e) {
			LOGGER.log(Level.WARNING,e.toString(),e);
			return null;
		}
	}
	
	/**
	 * this gives a string representing the active password
	 * @return this returns the active password.
	 */
	public static String getPassword() {
		return password;
	}
	
	/**
	 * Sets the password for the database. Using this is best in conjunction with setUsername
	 * @param password : the password for the ative user
	 */
	public static void setPassword(String password) {
	SQL.password = password;
	}
	
	/**
	 * This give you the active username. 
	 * @return
	 * The username as a String
	 */
	public static String getUsername() {
		return username;
	}
	
	/**
	 * Set the username that you want to use to log into the database
	 * @param username : The username for the login
	 */
	public static void setUsername(String username) {
		SQL.username = username;
	}
	/**
	 * Single field query. made for getting one field using an id. 
	 * If you use more then one field. it will only return the value of the first field
	 * @param query The query for the value
	 */
	public static Object SFQ(String query)
	{
		ResultList r = executeQuery(query);
		return r.get(r.get(0).keySet().iterator().next());
	}

	/*
	 * Here I am adding some functions to execute basic select and insert queries,
	 * this will make these types of queries easier to use in our routes
	 * The methods are overloaded so they can be used for small or large queries.
	 * -Zac Migues
	 */
	
	/**
	 * This method executes an Insert query to the database 
	 * @param table - the table name
	 * @param columns - the columns in a CSV strings. like "col1,col2,col3"
	 * @param values - An array of the values
	 * @return - The Results of the SQL statment
	 * @see - Call as insert("table1","col1,col2,col3","val1","val2","val3")
	 */
	public static ResultList insert (String table,String columns,String...values) {
		String q="\'";
		for(String v : values) 
		{
			q+=v+"\', \'";
		}
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + columns + ") VALUES ("+q.substring(0,q.length()-3)+");", 1);
	}
	
	public static ResultList selectAll(String table) {
		
		return executeQuery("SELECT * FROM public.\"" + table + "\";", 0);
		
	}
	
	public static ResultList select(String table, String columns) {
		
		return executeQuery("SELECT " + columns + " FROM public.\"" + table + "\"", 0);
		
	}
	
}