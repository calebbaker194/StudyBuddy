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
		
		String uname = req.session().attribute("currentUser");
		model.put("uname", uname);
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/flashcard.html")
				);
		
	};
	
	public static Route addFlashCard = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		String uname = req.session().attribute("currentUser");
		String course = req.queryParams("course");
		String group = req.queryParams("group");
		String description = req.queryParams("description");
		
		String questionName = "question0";
		String answerName = "answer0";
		
		String question = req.queryParams(questionName);
		String answer = req.queryParams(answerName);
		boolean result = false;
		int iterator = 1;
		
		do {
			FlashCard card = new FlashCard(uname, course, group, description, question, answer);
			result = PreparedQueries.addFlashCard(card);
			String iter = Integer.toString(iterator);
			questionName = questionName.replaceAll("[0-9]", iter);
			answerName = answerName.replaceAll("[0-9]", iter);
			question = req.queryParams(questionName);
			answer = req.queryParams(answerName);
			iterator++;
			if(!result) {
				break;
			}
			
		} while(question != null && answer != null);
		
		
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/flashcard.html"));
		
	};
	
	public static Route newCardField = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/newCard.html"));
		
	};

}
