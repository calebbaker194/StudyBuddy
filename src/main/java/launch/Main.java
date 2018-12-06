package launch;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;
import controller.*;
import singnaling.Signaler;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import sql.*;


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
		SQL.Connect("StudyBuddy", "localhost", 5432, "studybuddy", "studybuddypass");
		
		/*
		 * -- Role: studybuddy
		   -- Password: studybuddypass
		   -- DROP ROLE studybuddy;
			
			CREATE ROLE studybuddy LOGIN
  			ENCRYPTED PASSWORD 'md5d714909662b90e419e13956575e18913'
  			SUPERUSER INHERIT CREATEDB CREATEROLE REPLICATION;
		 */	
	
		staticFiles.externalLocation("web");
		
		secure("web/cert/certificate.pfx", "password", null, null);
		
		//This starts up the signaling server
		Signaler sig = new Signaler();
		sig.start();
		
		//to go to a page go to https://127.0.0.1/[page path here]
		port(443);
		
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
			
			path("/home", () -> {
				
				get("/", UserPageController.serveUserPage);
				
				path("/flashcardwizard", () -> {
					
					get("/", FlashCardController.viewFlashCardGroups);
					post("/", FlashCardController.deleteFlashCardGroup);
					get("/groupnamepost", FlashCardController.getGroupName);
					post("/groupnamepost", FlashCardController.viewFlashCardsPost);
					get("/view/:group", FlashCardController.viewFlashCards);
					get("/addnewgroup", FlashCardController.getAddFlashCardForm);
					post("/addnewgroup", FlashCardController.addFlashCardGroup);
					
				});
				
				get("/viewusers", AddFriendController.viewUsers);
				post("/viewusers", AddFriendController.addFriendPost);
				get("/deny/", UserPageController.denyFriendRequest);
				post("/deny/", UserPageController.denyFreindRequestPost);
				get("/confirm/", UserPageController.confirmFriendRequest);
				post("/confirm/", UserPageController.confirmFriendRequestPost);
				
			});
			
		});
		
				
	}
}
