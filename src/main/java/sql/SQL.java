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
		String q="";
		for(String v : values) 
		{
			q+=v+", ";
		}
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + columns + ") VALUES ("+q.substring(0,q.length()-2)+");", 1);
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST methods in HTTP
	 * @param column1 - name of the column where you want the value to go
	 * @param value1 - the value you want to insert
	 * @param table - the table containing column1
	 * @return - Returns the result set
	 */
	public static ResultList insert(String column1, String value1, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\"(" + column1 + ") VALUES (\'" + value1 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST methods in HTTP
	 * @param column1-2 : columns receiving data
	 * @param value1-2 : info to be inserted
	 * @param table : table containing columns
	 * @return - returns the result set
	 */
	public static ResultList insert(String column1, String column2, String value1, String value2, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ") VALUES (\'" + value1 + "\', \'" + value2 + "\');", 1);
		
	}
	
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-3 : columns receiving data
	 * @param value1-3 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String value1, String value2, String value3, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-4 : columns receiving data
	 * @param value1-4 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String value1, String value2, String value3, String value4, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-5 : columns receiving data
	 * @param value1-5 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String column5, String value1, String value2, String value3, String value4, String value5, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\', \'" + value5 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-6 : columns receiving data
	 * @param value1-6 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String column5, String column6, String value1, String value2, String value3, String value4, String value5, String value6, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\', \'" + value5 + "\', \'" + value6 +"\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-7 : columns receiving data
	 * @param value1-7 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + ") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\', \'" + value5 + "\', \'" + value6 + "\', \'" + value7 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-8 : columns receiving data
	 * @param value1-8 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String column8, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + ", " + column8 +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\', \'" + value5 + "\', \'" + value6 + "\', \'" + value7 + "\', \'" + value8 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-9 : columns receiving data
	 * @param value1-9 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String column8, String column9, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + ", " + column8 + ", " + column9 + ", " +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\', \'" + value5 + "\', \'" + value6 + "\', \'" + value7 + "\', \'" + value8 + "\', \'" + value9 + "\');", 1);
		
	}
	
	/**
	 * This method executes an INSERT query, used with PUT/POST method in HTTP
	 * @param column1-10 : columns receiving data
	 * @param value1-10 : info to be inserted
	 * @param table : table containing columns
	 * @return : returns result set
	 */
	public static ResultList insert(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String column8, String column9, String column10, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9, String value10, String table) {
		
		return executeQuery("INSERT INTO public.\"" + table + "\" (" + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + ", " + column8 + ", " + column9 + ", " + column10 + ", " +") VALUES (\'" + value1 + "\', \'" + value2 + "\', \'" + value3 + "\', \'" + value4 + "\', \'" + value5 + "\', \'" + value6 + "\', \'" + value7 + "\', \'" + value8 + "\', \'" + value9 + "\', \'" + value10 + "\');", 1);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP.
	 * This is for selecting all the columns in a table only.
	 * @param table - name of the table containing desired data
	 * @return - returns the result set
	 */
	public static ResultList selectAll(String table) {
		
		return executeQuery("SELECT * FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String table) {
		
		return executeQuery("SELECT " + column1 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-2 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-3 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-4 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String column4, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + ", " + column4 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-5 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String column4, String column5, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-6 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String column4, String column5, String column6, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-7 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-8 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String column8, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + ", " + column8 + " FROM public.\"" + table + "\";", 0);
		
	}
	
	/**
	 * This method executes a SELECT query, used with GET methods in HTTP
	 * This is for selecting only certain columns in a table
	 * @param column1-9 - name of column containing desired data
	 * @param table - name of table containing column1
	 * @return - returns the result set
	 */
	public static ResultList select(String column1, String column2, String column3, String column4, String column5, String column6, String column7, String column8, String column9, String table) {
		
		return executeQuery("SELECT " + column1 + ", " + column2 + ", " + column3 + ", " + column4 + ", " + column5 + ", " + column6 + ", " + column7 + ", " + column8 + ", " + column9 + " FROM public.\"" + table + "\";", 0);
		
	}
	
}