package sql;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author caleb.baker
 * This is designed for taking results of the SQL.executeQuery. its main purpose is to 
 * allow an easily accesable way to get the results from an SQL query and using them without keeping the connection open.
 */
public class ResultList extends ArrayList<HashMap<String,Object>>{
	
	private String errorMessage = null;
	private static final long serialVersionUID = 5821956676968273233L;
	private int lastRow;
	
	/**
	 * This is a way of creating a result list using an arraylist of HashMaps
	 * @param list The list of Hashmaps to create teh result set out of
	 */
	public ResultList(java.util.List<HashMap<String, Object>> list)
	{
		addAll(list);
	}

	/**
	 * Creates an empty result list. that we can build up
	 * Mainly used for json
	 */
	public ResultList()
	{
		
	}

	/**
	 * Add a row to the ResultList.
	 * @see DO NOT USE if you are executing a query. this will not keep consistant column headings of the whole result set.  
	 */
	public int addRow()
	{
		add(new HashMap<String,Object>());
		lastRow = size()-1;
		return size()-1;
	}
	
	/**
	 * gets the value of the field specified by prop in the first elemet of the result set
	 * @param field the column that you want the value from.
	 */
	public Object get(String field)
	{
		if(size()>0)
			return get(0).get(field);
		else 
			return null;
	}
	
	/**
	 * Gets the column names of the result set. Honestly not very usefull. Becuase you make the column names with the query you write
	 * @return An Arraylist of Strings representing column names.
	 */
	public ArrayList<String> getColumnNames() {
		Object[] it =  get(0).keySet().toArray();
		ArrayList<String> rt = new ArrayList<String>();
		for(Object o : it)
			rt.add(o.toString());
		return rt;
	}
	
	/**
	 * Checks to see if there are any columns in the result
	 * @return True if there are results 
	 * False if nothing was returned
	 */
	public boolean first(){
		return size()>0;
	}

	/**
	 * This alllows you to add json items into every item with a query.
	 * The nested group will be accessed as items. 
	 * @param query This is the query that it will use. 
	 * @param linker This is the field you will use to link the items. make sure that it is the same name in both querys
	 * @see NOTE: make sure that you have a link, to link the second set to the first set.
	 */
	public void addLevel(String query,String field)
	{
		ResultList r = SQL.executeQuery(query);
		
		for(HashMap<String, Object> row: this)
		{
			row.put("items",new ResultList());
			for(HashMap<String, Object> indRow:r)
			{
				if(indRow.get(field).equals(row.get(field)))
				{
					int rid = ((ResultList) row.get("items")).addRow();
					((ResultList) row.get("items")).set(rid, indRow);
				}
			}
		}
	}

	/**
	 * Places a value in last row that was added. 
	 * @param column The name of the value
	 * @param value The value you want to put in the row in 
	 * @see This method is normally only used of your trying to add rows manually
	 */
	public void put(String column, Object value)
	{
		get(lastRow).put(column, value);
	}
	
	/**
	 * Gets the message of the sql error.
	 * @return the human readable error message from the SQL server
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * This indicates weather or not an error was thrown by the query that was executed
	 * @return true if there is an error or false if there was not
	 */
	public boolean hasError() {
		return errorMessage != null;
	}
}