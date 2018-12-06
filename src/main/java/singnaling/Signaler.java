package singnaling;
	
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

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

    private static Map<Integer,Set<WebSocket>> Rooms = new HashMap<>();
    private int myroom;

    public Signaler() {
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
            switch (msgtype) {
                case "GETROOM":
                    myroom = generateRoomNumber();
                    s = new HashSet<>();
                    s.add(conn);
                    Rooms.put(myroom, s);
                    System.out.println("Generated new room: " + myroom);
                    conn.send("{\"type\":\"GETROOM\",\"value\":" + myroom + "}");
                    break;
                case "ENTERROOM":
                    myroom = obj.getInt("value");
                    System.out.println("New client entered room " + myroom);
                    s = Rooms.get(myroom);
                    s.add(conn);
                    Rooms.put(myroom, s);
                    break;
                default:
                    sendToAll(conn, message);
                    break;
            }
        } catch (JSONException e) {
            sendToAll(conn, message);
        }
        System.out.println();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + reason);
    }

    @Override
    public void onError(WebSocket conn, Exception exc) {
        System.out.println("Error happened: " + exc);
    }

    private int generateRoomNumber() {
        return new Random(System.currentTimeMillis()).nextInt();
    }

    private void sendToAll(WebSocket conn, String message) {
        Iterator it = Rooms.get(myroom).iterator();
        while (it.hasNext()) {
            WebSocket c = (WebSocket)it.next();
            if (c != conn) c.send(message);
        }
    }
}

