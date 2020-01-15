import java.io.DataInputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static Scanner input = new Scanner(System.in);
	
	public static String inputAndValidateIP() {
		boolean isValid = false;
		String ip = "";
		String prompt = "Provide IP Address : ";
		while(!isValid) {
			System.out.println(prompt);
			ip = input.next().strip();
			isValid = ip.matches(
			"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
			);
			prompt = "IP Address format is incorrect!\nProvide IP Address : ";
		}
		return ip;
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Provide port number (5000-5050) : ");
		int port = input.nextInt();
		
		String server = inputAndValidateIP();
		
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
