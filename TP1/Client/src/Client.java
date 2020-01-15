import java.io.DataInputStream;
import java.net.Socket;
import java.util.InputMismatchException;
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
	
	public static int inputAndValidatePort() {
		boolean isValid = false;
		int port = 0;
		String prompt = "Provide port number (5000-5050) : ";
		while(!isValid) {
			System.out.println(prompt);
			try {
				input.nextLine();
				port = input.nextInt();
			} catch (InputMismatchException e) {
				isValid = false;
				port = 0;
				prompt = "Input was invalid\nProvide port number (5000-5050) : ";
				continue;
			}
			isValid = (port >= 5000) && (port <= 5050);
			prompt = "Port number not in range\nProvide port number (5000-5050) : ";
		}
		return port;
	}
	
	public static void main(String[] args) throws Exception {
		
		String server = inputAndValidateIP();
		
		int port = inputAndValidatePort();
		
		System.out.println("Provide Username : ");
		String username = input.next();
		
		System.out.println("Provide Password : ");
		String password = input.next();
	
		socket = new Socket(server, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String messageFromServer = in.readUTF();
		System.out.println(messageFromServer);
				
		input.close();
		socket.close();
	}
}
