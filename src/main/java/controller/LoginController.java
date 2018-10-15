package controller;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import sql.*;

/**
 * 
 * @author Zac Migues
 * This class contains the routes for a user to log in and log out
 * it contains four routes, two boolean functions, one string and one void function.
 */
public class LoginController {
	
	/**
	 * This routes the client to the main login page,
	 * and adds the variable loggedOut to the model.
	 * This route is used with get()
	 */
	public static Route serveLoginPage = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		
		model.put("loggedOut", removeSessionLogOut(req));
		model.put("loginRedirect", removeSessionLoginRedirect(req));
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/login.html")
				);
		
	};
	
	/**
	 * This route performs the the login process.
	 * It pulls the username and password from the serveLoginPage,
	 * checks them against the database, and adds two variables,
	 * authFail and authSuccess, to the model. Used with post()
	 */
	public static Route handleLoginPost = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		//getting the username and password entered by client
		String uname = req.queryParams("username");
		String pword = req.queryParams("password");
		
		//checks client info against the database
		if(!PreparedQueries.authenticate(uname, pword))
			model.put("authFail", true); //used to check for login failure in html
		else
			model.put("authSuccess", true); //used to check for login success in html
		
		//adds the attribute currentUser to the session, with its value being the clients username
		req.session().attribute("currentUser", uname);
		model.put("uname", req.session().attribute("currentUser"));
		if(req.queryParams("loginRedirect") != null)
			res.redirect(req.queryParams("loginRedirect"));
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/login.html")
				);
		
	};
	
	/**
	 * This routes the client to the logout page
	 */
	public static Route serveLogoutPage = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		model.put("user", req.session().attribute("currentUser"));
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/logout.html")
				);
		
	};
	
	/**
	 * This route performs the logout process. The session attribute currentUser
	 * is removed, and the attribute loggedOut is added and given the value of true.
	 * The client will then be redirected to the login page.
	 */
	public static Route handleLogoutPost = (Request req, Response res) -> {
		
		req.session().removeAttribute("currentUser");
		req.session().attribute("loggedOut", true);
		res.redirect("/login");
		return null;
		
	};
	
	/**
	 * This function creates a loggedOut Object which will be used
	 * in serveLoginPage
	 * @param req : request
	 * @return : returns the loggedOut Object
	 */
	public static boolean removeSessionLogOut(Request req) {
		
		Object loggedOut = req.session().attribute("loggedOut");
		req.session().removeAttribute("loggedOut");
		return loggedOut != null;
		
	}

	/**
	 * makes removing the loginRedirect session attribute easier
	 * @param req - request
	 * @return - returns value of loginRedirect
	 */
	public static String removeSessionLoginRedirect(Request req) {
		
		String loginRedirect = req.session().attribute("loginRedirect");
		req.session().removeAttribute("loginRedirect");
		return loginRedirect;
		
	}
	
	/**
	 * Makes sure that the user is logged in. If they aren't,
	 * they are redirected to the login page
	 * @param req - request
	 * @param res - response
	 */
	public static void userLoggedIn(Request req, Response res) {
		
		if(req.session().attribute("currentUser") == null) {
			req.session().attribute("loginRedirect", req.pathInfo());
			res.redirect("/login");
		}
		
	}

}
