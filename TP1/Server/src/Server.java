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
		
		Scanner input = new Scanner(System.in);

		System.out.println("Provide port number (5000-5050) : ");
		int serverPort = input.nextInt();
		
		System.out.println("Provide IP Address : ");
		String serverAdress = input.next();
		
		//Verify
		serverAdress = "127.0.0.1";
		serverPort = 5000;
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAdress);
		
		listener.bind(new InetSocketAddress(serverIp, serverPort));
		
		System.out.println(serverAdress);
		System.out.println(serverPort);
		
		try {
			while(true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		} finally {
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread{
		private Socket socket;
		private int clientNumber;
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println();
			System.out.println(clientNumber);
			System.out.println(socket);
		}
		
		public void run() {
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("message");
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
