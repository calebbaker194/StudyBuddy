package launch;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import sql.ResultList;
import sql.SQL;


public class Main {
	public static void main(String args[]) throws Exception{
		
		
		/**
		 * This is how you connect to the database.
		 * Zach. Go ahead and change the fields to whatever you have them as on your computer.
		 * everyone else will set them up like you have yours. The fields are as follows
		 * Database: "dbname"
		 * Host: "localhost"
		 * Port: "5432"
		 * Username: "uname"
		 * Password: "passwd"
		 */
		SQL.Connect("StudyBuddy", "localhost", 5432, "postgres", "Lenin.Lover.69_420.");
		
		
		/**
		 * Execute the sql statement and store the results in a ResultList
		 */
		ResultList results = SQL.executeQuery("SELECT 'Its Working' AS test");
		
		/*
		 * Get the value of test in row 0. And store it into String. 
		 * All values in a result set are stored as objects, So you will have to cast them as 
		 * whatever you know them to be. 
		 * 
		 * if you want the fast way to get the first row then use results.get("test");
		 */
		
		String s = (String) results.get(0).get("test");
		System.out.println(s);
		
		/**
		 * This Allows Us to serve static files like .js and .scc files.
		 * If a file is located at foldername/theme.css
		 * Then we can link it in the html doc with <link rel="stylesheet" href="theme.css">
		 * folder name is located at the root of the project so StudyBuddy>foldername
		 */
		staticFiles.externalLocation("web/");
		
		//to go to a page go to http://127.0.0.1:8080/[page path here]
		port(8080);
		
		/**
		 * GET: HTTP Request type
		 * "/" Is the path after https://www.example.com
		 * req is the request
		 * res is the response that you will send back.
		 *
		get("/", (req,res)->{
			req.cookie("name");
			return "RAW HTML";
		});*/
		
		get("/about", (req,res)->{
			Map<String, Object> model = new HashMap<String, Object>();
			//This is where the rendering actually happens
			return new VelocityTemplateEngine().render(
					
					//model is the map from above
					//package name will be under src/main/java/packagename
					new ModelAndView(model, "html/about.html")
			);
		});
		/*
		 * Some common Methods for the res
		 * res.redirect("/");
		 */
		
		//This example shows how to render an html page
		get("/render", (req,res)->{
			
			// Create a hashmap. you can leave it empty if you choose
			Map<String, Object> model = new HashMap<String, Object>();
			// Add fields to the hashmap if you want them to replace values in the html
			model.put("test", "This is a test");
			//This is where the rendering actually happens
			return new VelocityTemplateEngine().render(
					
					//model is the map from above
					//package name will be under src/main/java/packagename
					new ModelAndView(model, "html/main.html")
			);
		});
		// NOTE: I am trying to find a way to put the html file in a location 
		//that is not in the classpath so that we can modify 
		//them post release with a recompile
		
		get("/", HomePageController.serveHomePage);
		
		//Defining paths for account registration
		get("/register", UserRegistrationController.serveRegistrationPage);
		post("/register", UserRegistrationController.handleRegistrationPost);
		
		//defining paths for login-logout procedures
		get("/login", LoginController.serveLoginPage);
		post("/login", LoginController.handleLoginPost);
		get("/logout", LoginController.serveLogoutPage);
		post("/logout", LoginController.handleLogoutPost);
		
		path("/:user", () -> {
			
			get("/home", UserPageController.serveUserPage);
			
		});
		
				
	}
}
