package controller;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import sql.*;

public class FlashCardController {
	
	public static Route viewFlashCards = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		ResultList result = SQL.executeQuery("SELECT coursename FROM public.\"Courses\""
				+ "ORDER BY \"coursename\" ASC;", 0);
		
		model.put("courses", result);
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/flashcard.html")
				);
		
	};
	
	public static Route addFlashCard = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		String uname = req.session().attribute("currentUser");
		String question = req.queryParams("question");
		String answer = req.queryParams("answer");
		String course = req.queryParams("course");
		
		boolean result = PreparedQueries.addFlashCard(question, answer, course, uname);
		
		if(!result)
			model.put("fail", result);
		else
			model.put("success", result);
		
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/flashcard.html"));
		
	};

}
