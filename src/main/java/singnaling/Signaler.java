package singnaling;
	
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import controller.ChatController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Signaler extends WebSocketServer {

    private Map<Integer,Set<WebSocket>> Rooms = new HashMap<>();
    private Map<Integer,Integer> hash = new HashMap<>();
    private static Signaler instance = null;
    public static Signaler getInstance()
    {
    	if(instance == null)
    	{
    		instance = new Signaler();
    	}
    	return instance;
    }
    
    private Signaler() {
        super(new InetSocketAddress(30001));
        setWebSocketFactory(null);
     // load up the key store
     		String STORETYPE = "PKCS12";
     		String KEYSTORE = "web/cert/certificate.pfx";
     		String STOREPASSWORD = "password";
     		String KEYPASSWORD = "password";

     		try
     		{
	     		KeyStore ks = KeyStore.getInstance( STORETYPE );
	     		File kf = new File( KEYSTORE );
	     		ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );
	
	     		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
	     		kmf.init( ks, KEYPASSWORD.toCharArray() );
	     		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
	     		tmf.init( ks );
	
	     		SSLContext sslContext = null;
	     		sslContext = SSLContext.getInstance( "TLS" );
	     		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
	
	     		setWebSocketFactory( new DefaultSSLWebSocketServerFactory( sslContext ) );
     		}
     		catch(IOException e) {
     			e.printStackTrace();
     		} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New client connected: " + conn.getRemoteSocketAddress() + " hash " + conn.getRemoteSocketAddress().hashCode());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        Set<WebSocket> s;
        try {
            JSONObject obj = new JSONObject(message);
            String msgtype = obj.getString("type");
            int roomNumber = -1;
            try {
            	roomNumber = Integer.parseInt(obj.getString("room"));
            }catch(JSONException e) {}
            switch (msgtype) {
                case "ENTERQUEUE":
                	ChatController.addToQueue(conn);
                	break;
                default:
                    sendToAll(conn,roomNumber, message);
                    break;
            }
        } catch (JSONException e) {
        	sendToAll(conn,hash.get(conn.getRemoteSocketAddress().hashCode()),message);
        }
        System.out.println();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + reason);
    }

    @Override
    public void onError(WebSocket conn, Exception exc) {
        exc.printStackTrace();
    }

    public int generateRoomNumber() {
        return new Random(System.currentTimeMillis()).nextInt();
    }

    public void createRoom(int roomNumber) {
        Set<WebSocket>  s = new HashSet<>();
        Rooms.put(roomNumber, s);
    }
    
    public void addToRoom(int roomNumber, WebSocket connection)
    {
    	System.out.println("Adding "+ connection +" To Room:"+ roomNumber);
    	Set<WebSocket> s = Rooms.get(roomNumber);
    	hash.put(connection.getRemoteSocketAddress().hashCode(), roomNumber);
    	s.add(connection);
    	Rooms.put(roomNumber, s);
    }
    
    private void sendToAll(WebSocket conn,int roomNumber, String message) {
    	System.out.println(message);
        Iterator it = Rooms.get(roomNumber).iterator();
        while (it.hasNext()) {
            WebSocket c = (WebSocket)it.next();
            if (c != conn) c.send(message);
        }
    }
}

