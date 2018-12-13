package controller;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import sql.*;

public class AddFriendController {
	
	public static Route addFriendPost = (Request req, Response res) -> {
		
		String username = req.queryParams("username");
		String userid = req.session().attribute("currentUser");
		
		String uid = PreparedQueries.getUserID(username);
		
		String addFriendQuery = "INSERT INTO public.\"Friend\" (friend_request_sender, friend_request_receiver) "
				+ "VALUES (" + Integer.parseInt(userid) + ", " + Integer.parseInt(uid) + ");";
		
		ResultList r = SQL.executeQuery(addFriendQuery, 1);
		
		res.redirect(userid + "/home/viewusers");
		return res;
		
	};

}
