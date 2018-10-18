package controller;

public class FlashCard {
	
	private static int userid;
	private static int courseid;
	private static String group;
	private static String description;
	private static String question;
	private static String answer;
	
	FlashCard(int uid, int cid, String grp, String descrip, String ques, String ans) {
		
		userid = uid;
		courseid = cid;
		group = grp;
		description = descrip;
		question = ques;
		answer = ans;
		
	}
	
	public static int getUserid() {
		
		return userid;
		
	}
	
	public static int getCourseid() {
		
		return courseid;
		
	}
	
	public static String getGroup() {
		
		return group;
		
	}
	
	public static String getDescription() {
		
		return description;
		
	}
	
	public static String getQuestion() {
		
		return question;
		
	}
	
	public static String getAnswer() {
		
		return answer;
		
	}

}
