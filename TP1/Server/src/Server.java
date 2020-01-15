import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	private static ServerSocket listener;
	
	public static void main(String[] args) throws Exception{
		int clientNumber = 0;
		
		String serverAdress = "127.0.0.1";
		int serverPort = 5000;
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAdress);
		
		listener.bind(new InetSocketAddress(serverIp, serverPort));
		
		System.out.println(serverAdress);
		System.out.println(serverPort);
		
		try {
			Scanner in = new Scanner(System.in);
			String message = "";
			System.out.println("gimme the message: ");
			message = in.next();
			in.close();
			while(true) {
				new ClientHandler(listener.accept(), clientNumber++, message).start();
			}
		} finally {
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread{
		private Socket socket;
		private int clientNumber;
		private String message;
		
		public ClientHandler(Socket socket, int clientNumber, String message) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			this.message = message;
			System.out.println();
			System.out.println(clientNumber);
			System.out.println(socket);
		}
		
		public void run() {
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(message);
			} catch (IOException e){
				System.out.println("Error");
			} finally {
				try {
					socket.close();
				} catch(IOException e) {
					System.out.println("Socket Error");
				}
				System.out.println("Closed " + clientNumber);
			}
		}
	}
}
