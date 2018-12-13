package controller;

import spark.*;
import spark.template.velocity.*;
import sql.*;

import java.util.*;
/**
 * 
 * @author Zac Migues
 * 
 */
public class UserPageController {
	
	public static Route serveUserPage = (Request req, Response res) -> {
		
		LoginController.userLoggedIn(req, res);
		String userid = req.session().attribute("currentUser");
		int uid = Integer.parseInt(userid);
		String username = PreparedQueries.getUsername(uid);
		req.session().attribute("addFlashCardResult", null);
		
		Map<String, Object> model = new HashMap<>();
		
		model.put("username", username);
		model.put("uid", userid);
		
		String getFriendRequestsQuery = "SELECT username FROM public.\"UserAccount\" AS ua " + 
				"INNER JOIN public.\"Friend\" AS f ON ua.userid = f.friend_request_sender " + 
				"WHERE f.friend_request_receiver =" + userid + " AND f.friend_request_confirmed = false";
		ResultList result = SQL.executeQuery(getFriendRequestsQuery, 0);
		
		String getPendingRequestsQuery = "SELECT username FROM public.\"UserAccount\" AS ua " + 
				"INNER JOIN public.\"Friend\" AS f ON ua.userid = f.friend_request_receiver " + 
				"WHERE f.friend_request_sender =" + userid + " AND f.friend_request_confirmed = false";
		ResultList pendingResult = SQL.executeQuery(getPendingRequestsQuery, 0);
		
		String getFriendsQueryReceiver = "SELECT username FROM public.\"UserAccount\" AS ua "
				+ "INNER JOIN public.\"Friend\" AS f ON ua.userid = f.friend_request_sender "
				+ "WHERE f.friend_request_receiver = " + userid + " AND f.friend_request_confirmed = true";
		ResultList friendsResult = SQL.executeQuery(getFriendsQueryReceiver, 0);
		
		String getFriendsQuerySender = "SELECT username FROM public.\"UserAccount\" AS ua "
				+ "INNER JOIN public.\"Friend\" AS f ON ua.userid = f.friend_request_receiver "
				+ "WHERE f.friend_request_sender = " + userid + " AND f.friend_request_confirmed = true";
		ResultList friendsSenderResult = SQL.executeQuery(getFriendsQuerySender, 0);
		
		friendsResult.addAll(friendsSenderResult);
		
		if(!result.isEmpty()) {
			model.put("hasFriendRequests", true);
			model.put("friendRequests", result);	
		}
		else
			model.put("noFriendRequests", true);
		
		if(!friendsResult.isEmpty()) {
			model.put("hasFriends", true);
			model.put("friends", friendsResult);
		}
		else
			model.put("noFriends", true);
		
		if(!pendingResult.isEmpty()) {
			model.put("hasPending", true);
			model.put("pending", pendingResult);
		}
		else
			model.put("noPending", true);
				
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/userpage.html")
				);
		
	};
	
	public static Route confirmFriendRequestPost = (Request req, Response res) -> {
		
		String uid = PreparedQueries.getUserID(req.queryParams("username"));
		String userid = req.session().attribute("currentUser");
		
		String confirmRequestQuery = "UPDATE public.\"Friend\" SET friend_request_confirmed = true "
				+ "WHERE friend_request_sender = " + Integer.parseInt(uid) + "AND friend_request_receiver = " + Integer.parseInt(userid) + ";";
		
		ResultList result = SQL.executeQuery(confirmRequestQuery, 2);
		
		res.redirect(userid + "/home/");
		return res;
	};
	
	public static Route confirmFriendRequest = (Request req, Response res) -> {
		Map<String, Object> model = new HashMap<>();
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/confirmFriendRequest.html"));
	};
	
	public static Route denyFriendRequest = (Request req, Response res) -> {
		Map<String, Object> model = new HashMap<>();
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/denyFriendRequest.html"));
	};
	
	public static Route denyFreindRequestPost = (Request req, Response res) -> {
		
		String userid = req.session().attribute("currentUser");
		String uid = PreparedQueries.getUserID(req.queryParams("username"));

		String denyQuery = "DELETE FROM public.\"Friend\" " + 
				"WHERE friend_request_sender = " + Integer.parseInt(uid) + ";";
		
		ResultList result = SQL.executeQuery(denyQuery, 3);
		
		res.redirect(userid + "/home/");
		return res;
	};

}
