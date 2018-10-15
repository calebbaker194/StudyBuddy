package controller;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import sql.*;

public class FlashCardController {
	
	public static Route viewFlashCards = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/flashcard.html")
				);
		
	};

}
