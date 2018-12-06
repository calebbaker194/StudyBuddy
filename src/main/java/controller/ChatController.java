package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.WebSocket;

import singnaling.Signaler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class ChatController{
	
	private static ArrayList<WebSocket> queue = new ArrayList<WebSocket>();
	private static Timer queueDelay = new Timer(true);
	
	public static Route openChat = (Request req, Response res) -> {
		
		Map<String, Object> model = new HashMap<>();
		
		String userid = req.session().attribute("currentUser");
		
		model.put("uid", userid);

		return new VelocityTemplateEngine().render(new ModelAndView(model, "html/chat.html"));
		
	};
	
	public static void addToQueue(WebSocket conn) 
	{
		queue.add(conn);
	}
	
	public static void startQueue()
	{
		queueDelay.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				Signaler sig = Signaler.getInstance();
				
				if(queue==null)
					return;
					
				
				// If we dont have 2 people just end the search and wait
				if(queue.size() < 2)
					return;
				
				//Copy the queue to keep from race cases caused by multi-thread
				ArrayList<WebSocket> queuec = new ArrayList<WebSocket>();
				queuec.addAll(queue);
				
				//Loop through the entire list minus the last person or the odd man out
				for(int x=0;x<queuec.size()-(queuec.size()%2);x+=2)
				{
					int room = sig.generateRoomNumber();
					sig.createRoom(room);
					sig.addToRoom(room, queuec.get(x));
					sig.addToRoom(room, queuec.get(x+1));
					queuec.get(x).send("SETROOM "+room);
					queuec.get(x+1).send("SETROOM "+room);
					queuec.get(x).send("INITCALL");
					queuec.get(x+1).send("CALLREADY");
				}
				if(queuec.size()%2==1)
					queuec.remove(queuec.size()-1);
				
				queue.removeAll(queuec);
			}
		}, 0, 1000);
	}
}
