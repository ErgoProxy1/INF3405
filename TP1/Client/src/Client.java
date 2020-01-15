import java.io.DataInputStream;
import java.net.Socket;

public class Client {
	private static Socket socket;
	
	public static void main(String[] args) throws Exception {
		
		String server = "127.0.0.1";
		int port = 5000;
		
		socket = new Socket(server, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String messageFromServer = in.readUTF();
		System.out.println(messageFromServer);
				
		socket.close();
	}
}
