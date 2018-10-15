package controller;

import spark.*;
import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

import java.util.*;
import spark.template.velocity.*;
import sql.*;

/**
 * This class provides the routes for creating a new user account.
 * The first route, serveRegistrationPage presents the registration form to the client
 * The second route, handleRegistrationPost adds the information from the form to the database
 * and returns a success or failure message to a new page.
 * @author Zac Migues
 *
 */
public class UserRegistrationController implements SparkApplication {
	
	/**
	 * Simple route to render the registration form, used with get()
	 */
	public static Route serveRegistrationPage = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		return new VelocityTemplateEngine().render(
				(new ModelAndView(model, "html/registration.html"))
				);
		
	};
	
	/**
	 * pulls info entered on serveRegistrationPage and stores in the db.
	 * Used with post()
	 */
	public static Route handleRegistrationPost = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		//getting user-entered information
		String uname = req.queryParams("username");
		String email = req.queryParams("email");
		String pword = req.queryParams("pword");
		String school = req.queryParams("school");
		String fname = req.queryParams("fname");
		String lname = req.queryParams("lname");
		String classif = req.queryParams("class");
		
		//inserting info in the database
		boolean result = PreparedQueries.registerAccount(uname, pword, email, school, fname, lname, classif);
		
		if(!result)
			model.put("fail", result);
		else
			model.put("success", result);
		
		return new VelocityTemplateEngine().render(
				(new ModelAndView(model, "html/registration.html"))
				);
		
	};

	@Override
	public void init() {
		System.out.println("inint now");
	}

}
