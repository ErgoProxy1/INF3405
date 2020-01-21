import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class Server {
	private static ServerSocket listener;
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
		int port = 5000;
		String prompt = "Provide port number (5000-5050) : ";
		while(!isValid) {
			System.out.println(prompt);
			port = input.nextInt();
			isValid = (port >= 5000) && (port <= 5050);
			prompt = "Port number not in range\nProvide port number (5000-5050) : ";
		}
		return port;
	}
	
	public static void main(String[] args) throws Exception{
		int clientNumber = 0;
		
		String serverAdress = inputAndValidateIP();
		
		int serverPort = inputAndValidatePort();
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAdress);
		
		listener.bind(new InetSocketAddress(serverIp, serverPort));
		
		try {
			while(true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		} finally {
			input.close();
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread{
		private Socket socket;
		private int clientNumber;
		private BufferedImage image;
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
		}
		
		public void run() {
			try {
				boolean done = false;
				while(!done) {
					DataInputStream in = new DataInputStream(socket.getInputStream());	
					int userAction = in.read();
					if(userAction == 1) {
						getClientImage();
					} else if (userAction == 2) {
						sendClientImage();
					} else if (userAction == 3) {
						System.out.println("Client " + clientNumber + " has disconnected.");
						done = true;
					}
				}
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
		
		public void getClientImage() throws IOException {
			image = ImageIO.read(socket.getInputStream());
			System.out.print("hi");
			ImageIO.write(Sobel.process(image), "jpg", new File("test.jpg"));
		}
		
		public void sendClientImage() throws IOException {
			ImageOutputStream output = ImageIO.createImageOutputStream(socket.getOutputStream());
			ImageIO.write(image, "jpg", output);
            output.close();
		}
	}
}
