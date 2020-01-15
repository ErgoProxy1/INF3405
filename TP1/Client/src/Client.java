import java.io.DataInputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Provide port number (5000-5050) : ");
		Scanner input = new Scanner(System.in);
		int port = input.nextInt();
		

		System.out.println("Provide IP Address : ");
		String server = input.next();
		

		System.out.println("Provide Username : ");
		String username = input.next();
		
		System.out.println("Provide Password : ");
		String password = input.next();
		
		//Verify
		server = "127.0.0.1";
		port = 5000;
	
		socket = new Socket(server, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String messageFromServer = in.readUTF();
		System.out.println(messageFromServer);
				
		input.close();
		socket.close();
	}
}
