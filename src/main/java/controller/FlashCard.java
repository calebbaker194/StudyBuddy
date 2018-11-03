package controller;

public class FlashCard {
	
	private int userid;
	private String coursename;
	private String group;
	private String description;
	private String question;
	private String answer;
	
	FlashCard(int un, String cn, String grp, String descrip, String ques, String ans) {
		
		userid = un;
		coursename = cn;
		group = grp;
		description = descrip;
		question = ques;
		answer = ans;
		
	}
	
	public int getUserid() {
		
		return userid;
		
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
