package controller;

import spark.*;
import spark.template.velocity.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import sql.*;

/**
 * This class contains the necessary routes for Flash Card functionality
 * @author Zac Migues
 *
 */
public class FlashCardController {
	
	/**
	 * Shows all of the flash card groups a user has
	 */
	public static Route viewFlashCardGroups = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		String uname = req.session().attribute("currentUser");
		
		model.put("uname", uname);
		
		String findUIDquery = "SELECT userid FROM public.\"UserAccount\" WHERE username = '" + uname + "'";
		
		ResultList result = SQL.executeQuery(findUIDquery, 0);
		
		int userid = (Integer)result.getFirst("userid");
		
		String getFlashCardGroupQuery = "SELECT \"group\" FROM public.\"FlashCard\" WHERE userid = " + userid + " GROUP BY \"group\" ORDER BY \"group\" ASC;";
		
		ResultList cardGroups = SQL.executeQuery(getFlashCardGroupQuery, 0);
		
		model.put("groups", cardGroups);
		
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/viewFlashCardGroups.html"));
		
	};
	
	/**
	 * deletes a specified flash card group from the database and
	 * redirects to viewFlashCardGroups
	 */
	public static Route deleteFlashCardGroup = (Request req, Response res) -> {
		
		String uname = req.session().attribute("currentUser");
		String groupName = req.queryParams("group");
		
		String findUIDquery = "SELECT userid FROM public.\"UserAccount\" WHERE username = '" + uname + "'";
		ResultList result = SQL.executeQuery(findUIDquery, 0);
		int userid = (Integer)result.getFirst("userid");
		
		String deleteGroupQuery = "DELETE FROM public.\"FlashCard\" WHERE userid = " + userid + " AND \"group\" = '" + groupName + "';";
		ResultList deleteResult = SQL.executeQuery(deleteGroupQuery, 3);
		
		res.redirect("http://localhost:8080/" + uname + "/home/flashcardwizard/");
		return null;
		
	};
	
	/**
	 * Renders the page for adding one or more flash cards
	 */
	public static Route getAddFlashCardForm = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		ResultList result = SQL.executeQuery("SELECT coursename FROM public.\"Courses\""
				+ "ORDER BY \"coursename\" ASC;", 0);
		
		model.put("courses", result);
		
		String uname = req.session().attribute("currentUser");
		model.put("uname", uname);
		
		Object addResult = req.session().attribute("addFlashCardResult");
		model.put("result", addResult);
		
		return new VelocityTemplateEngine().render(
				new ModelAndView(model, "html/addFlashCardGroup.html")
				);
	};
	
	/**
	 * adds the entered flash cards into the database
	 */
	public static Route addFlashCardGroup = (Request req, Response res) -> {
		//grabbing info
		String uname = req.session().attribute("currentUser");
		String course = req.queryParams("course");
		String group = req.queryParams("group");
		String description = req.queryParams("description");
		
		//setting up for first iteration of loop
		String questionName = "question0";
		String answerName = "answer0";
		//getting the question and answer attributes of first entry
		String question = req.queryParams(questionName);
		String answer = req.queryParams(answerName);
		//loop controllers
		boolean result = false;
		int iterator = 1;
		//loop to get all the entered cards
		do {
			questionName = "question"; //ready to be modified
			answerName = "answer";
			//create FlashCard object with entry
			FlashCard card = new FlashCard(uname, course, group, description, question, answer);
			result = PreparedQueries.addFlashCard(card); //add to db
			String iter = Integer.toString(iterator); 
			questionName = questionName.concat(iter); //move to next entry
			answerName = answerName.concat(iter);
			question = req.queryParams(questionName); //get info for next entry
			answer = req.queryParams(answerName);
			iterator++;
			if(!result) {
				break;
			}
			
		} while(question != null && answer != null);
		
		req.session().attribute("addFlashCardResult", true);
		res.redirect("http://localhost:8080/" + uname + "/home/flashcardwizard/addnewgroup");
		return null;
		
	};
	
	/**
	 * gets info for showing a flash card group
	 */
	public static Route viewFlashCardsPost = (Request req, Response res) -> {
		String groupName = req.queryParams("group");
		req.session().attribute("groupName", groupName);
		String uname = req.session().attribute("currentUser");
		res.redirect("http://localhost:8080/" + uname + "/home/flashcardwizard/view/" + groupName);
		return null;
	};
	
	/**
	 * shows a flash card group
	 */
	public static Route viewFlashCards = (Request req, Response res) -> {
		Map<String, Object> model = new HashMap<>();
		
		String groupName = req.session().attribute("groupName");
		String uname = req.session().attribute("currentUser");
		
		String getCardsQuery = "SELECT question, answer FROM public.\"FlashCard\" AS fc "
				+ "INNER JOIN public.\"UserAccount\" AS ua ON fc.userid = ua.userid WHERE "
				+ "ua.username = '" + uname + "' AND fc.\"group\" = '" + groupName + "';";
		ResultList result = SQL.executeQuery(getCardsQuery, 0);
		
		Collections.shuffle(result);
		model.put("cards", result);
		model.put("uname", uname);
		
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/viewFlashCards.html"));
	};
	
	/**
	 * destination for group data
	 */
	public static Route getGroupName = (Request req, Response res) -> {
		Map<String, Object> model = new HashMap<>();
		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/groupnamepost.html"));
	};

}
