package launch;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import sql.*;

/**
 * 
 * @author Zac Migues
 * This class contains the routes associated with the homepage
 */
public class HomePageController {
	
	/*
	 * serves client the home page. gets courses from db and orders them in ascending alphabet order
	 * Puts the result map into the model so Velocity can use it
	 */
	public static Route serveHomePage = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		ResultList result = SQL.executeQuery("SELECT coursename FROM public.\"Courses\""
				+ "ORDER BY \"coursename\" ASC;", 0);
		
		model.put("courses", result);
		
		return new VelocityTemplateEngine().render(
					new ModelAndView(model, "html/home.html")
				);
		
	};
	
}
