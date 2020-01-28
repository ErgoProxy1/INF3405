import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Server {
	private static ServerSocket listener;
	private static Scanner input = new Scanner(System.in);
	
	public static String inputAndValidateIP() {
		boolean isValid = false;
		String ip = "";
		String prompt = "Provide IP Address : ";
		while(!isValid) {
			System.out.print(prompt);
			ip = input.next().strip();
			isValid = ip.matches(
			"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
			);
			prompt = "IP Address format is incorrect!\nProvide IP Address : \n";
		}
		return ip;
	}
	
	public static int inputAndValidatePort() {
		boolean isValid = false;
		int port = 5000;
		String prompt = "Provide port number (5000-5050) : ";
		while(!isValid) {
			System.out.print(prompt);
			port = input.nextInt();
			isValid = (port >= 5000) && (port <= 5050);
			prompt = "Port number not in range\nProvide port number (5000-5050) : \n";
		}
		return port;
	}
	
	// Lire le fichier de bd 
	public static boolean readFile(String name, String password) {
		try {
			FileReader reader = new FileReader("Database.txt");
			BufferedReader bufferedReader = new BufferedReader(reader);
	
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals(name)) {
					if (bufferedReader.readLine().equals(password)){
						bufferedReader.close();
						return true;
					} else {
						bufferedReader.close();
						return false;
					}
				}
			}
			reader.close();
			addNewUser(name, password);
			return true;
	
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An error occured while accessing the database");
			return false;
		}
	}
		
	public static void addNewUser(String name, String password) {
		try {
			FileWriter writer = new FileWriter("Database.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write(name);
			bufferedWriter.newLine();
			bufferedWriter.write(password);
			bufferedWriter.newLine();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		String serverAdress = inputAndValidateIP();
		
		int serverPort = inputAndValidatePort();
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAdress);
		
		listener.bind(new InetSocketAddress(serverIp, serverPort));
		
		try {
			while(true) {
				new ClientHandler(listener.accept()).start();
			}
		} finally {
			input.close();
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread{
		private Socket socket;
		private BufferedImage image;
		private String username;
		private String password;
		
		public ClientHandler(Socket socket) {
			this.socket = socket;
			this.username = "default";
			this.password = "defaultpass";
		}
		
		public void run() {
			try {
				boolean connected = false;
				boolean done = false;
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				while(!connected) {
					username = in.readUTF();
					password = in.readUTF();
					connected = readFile(username, password);
					out.writeBoolean(connected);
					out.flush();
				}
				
				while(!done) {
					int userAction = in.read();
					if(userAction == 1) {
						String imageName = in.readUTF();
						this.image = this.readImage(in, imageName);
					} else if (userAction == 2) {
						this.sendImage(out);
					} else if (userAction == 3) {
						System.out.println("\nClient " + username + " has disconnected.");
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
				System.out.println("Closed " + username);
			}
		}
		
		public BufferedImage readImage(DataInputStream in, String imageName) throws IOException {
			int length = in.readInt();
			byte[] fileContent = new byte[length];
			in.readFully(fileContent);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();
			System.out.print(
				"\n\n[" + username + " - " + 
				socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort() + " - " + 
				dtf.format(now) + "] : Image " + imageName + " received for treatment.\n\n"
			);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(fileContent);
			return Sobel.process(ImageIO.read(byteStream));
		}
		
		public void sendImage(DataOutputStream out) throws IOException {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ImageIO.write(this.image , "jpg", byteStream);
			byte[] processedBytes = byteStream.toByteArray();
			out.writeInt(processedBytes.length);
			out.flush();
			out.write(processedBytes, 0, processedBytes.length);
			out.flush();
			byteStream.close();
		}
	}
}
