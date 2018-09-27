package launch;

import spark.*;
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
public class UserRegistrationController {
	
	/**
	 * Simple route to render the registration form, used with get()
	 */
	public static Route serveRegistrationPage = (Request request, Response response) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		return new VelocityTemplateEngine().render(
				(new ModelAndView(model, "html/registration.html"))
				);
		
	};
	
	/**
	 * pulls info entered on serveRegistrationPage and stores in the db.
	 * Used with post()
	 */
	public static Route handleRegistrationPost = (Request request, Response response) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		//messages to be output
		String successMsg = "Your account has been successfully registered!";
		String errorMsg = "There was an error :(";
		
		//getting user-entered information
		String uname = request.queryParams("username");
		String email = request.queryParams("email");
		String pword = request.queryParams("pword");
		String school = request.queryParams("school");
		String fname = request.queryParams("fname");
		String lname = request.queryParams("lname");
		String classif = request.queryParams("class");
		
		//inserting info in the database
		ResultList result = SQL.insert("username", "email", "userpassword", "school", 
				"firstname", "lastname", "classification", uname, email, pword, 
				school, fname, lname, classif, "UserAccount");
		
		//check to see if new record was added successfully
		if(result.get(0).get("username") != null)
			model.put("msg", successMsg); //allows successMsg object to be rendered by Velocity by typing $msg in the html
		
		else
			model.put("msg", errorMsg); //allows errorMsg object to be rendered by Velocity by typing $msg in the html
		
		return new VelocityTemplateEngine().render(
				(new ModelAndView(model, "html/registration_confirm.html"))
				);
		
	};

}
