package launch;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

public class Main {
	public static void main(String args[]) throws Exception{
		
		/**
		 * This Allows Us to serve static files like .js and .scc files.
		 * If a file is located at foldername/theme.css
		 * Then we can link it in the html doc with <link rel="stylesheet" href="theme.css">
		 * folder name is located at the root of the project so StudyBuddy>foldername
		 */
		staticFiles.externalLocation("foldername");
		
		//to go to a page go to http://127.0.0.1:8080/[page path here]
		port(8080);
		
		/**
		 * GET: HTTP Request type
		 * "/" Is the path after https://www.example.com
		 * req is the request
		 * res is the response that you will send back.
		 */
		get("/", (req,res)->{
			return "RAW HTML";
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
		// NOTE: I am trying to find a way to put the html file in a location that is not it the claspath so that we can modify them post release with a recompile
	}
}
