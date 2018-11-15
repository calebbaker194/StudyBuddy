package controller;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import sql.*;

public class AddFriendController {
	
	public static Route viewUsers = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		String userid = req.session().attribute("currentUser");
		
		String query = "SELECT username FROM public.\"UserAccount\" "
				+ "WHERE userid NOT IN (SELECT friend_request_receiver FROM public.\"Friend\" "
				+ "WHERE friend_request_sender = " + Integer.parseInt(userid) + ") "
				+ "AND userid NOT IN (SELECT friend_request_sender FROM public.\"Friend\" "
				+ "WHERE friend_request_receiver = " + Integer.parseInt(userid) + ") "
				+ "AND userid != " + Integer.parseInt(userid) + ";";
		
		ResultList result = SQL.executeQuery(query, 0);
		model.put("users", result);
		
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/viewusers.html"));
		
	};
	
	public static Route addFriendPost = (Request req, Response res) -> {
		
		String username = req.queryParams("username");
		String userid = req.session().attribute("currentUser");
		
		String query = "SELECT userid FROM public.\"UserAccount\" WHERE username = '" + username + "'";
		ResultList result = SQL.executeQuery(query, 0);
		
		String uid = result.getFirst("userid").toString();
		
		String addFriendQuery = "INSERT INTO public.\"Friend\" (friend_request_sender, friend_request_receiver) "
				+ "VALUES (" + Integer.parseInt(userid) + ", " + Integer.parseInt(uid) + ");";
		
		ResultList r = SQL.executeQuery(addFriendQuery, 1);
		
		res.redirect("https://localhost:8080/" + userid + "/home/viewusers");
		return null;
		
	};

}
