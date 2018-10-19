package controller;

public class FlashCard {
	
	private String username;
	private String coursename;
	private String group;
	private String description;
	private String question;
	private String answer;
	
	FlashCard(String un, String cn, String grp, String descrip, String ques, String ans) {
		
		username = un;
		coursename = cn;
		group = grp;
		description = descrip;
		question = ques;
		answer = ans;
		
	}
	
	public String getUsername() {
		
		return username;
		
	}
	
	public String getCoursename() {
		
		return coursename;
		
	}
	
	public String getGroup() {
		
		return group;
		
	}
	
	public String getDescription() {
		
		return description;
		
	}
	
	public String getQuestion() {
		
		return question;
		
	}
	
	public String getAnswer() {
		
		return answer;
		
	}

}
