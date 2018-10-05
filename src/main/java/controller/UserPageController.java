package controller;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
/**
 * 
 * @author Zac Migues
 * 
 */
public class UserPageController {
	
	public static Route serveUserPage = (Request req, Response res) -> {
		
		LoginController.userLoggedIn(req, res);
		String uname = req.session().attribute("currentUser");
		
		Map<String, Object> model = new HashMap<>();
		
		model.put("uname", uname);
				
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/userpage.html")
				);
		
	};

}
