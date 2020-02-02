import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Server {
	private static ServerSocket listener;
	private static Scanner input = new Scanner(System.in);
	
	public static String inputAndValidateIP() {
		boolean isValid = false;
		String ip = "";
		String prompt = "Entrez l'adresse IP: ";
		while(!isValid) {
			System.out.print(prompt);
			ip = input.next().strip();
			isValid = ip.matches(
			"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
			);
			prompt = "Le format de l'adresse IP est incorrect!\n\nEntrez l'adresse IP: ";
		}
		return ip;
	}
	
	public static int inputAndValidatePort() {
		boolean isValid = false;
		int port = 0;
		String prompt = "Entrez le numero du port (5000-5050): ";
		while (!isValid) {
			System.out.print(prompt);
			try {
				input.nextLine();
				port = input.nextInt();
			} catch (InputMismatchException e) {
				isValid = false;
				port = 0;
				prompt = "Donnees invalides!\nEntrez le numero du port (5000-5050): ";
				continue;
			}
			isValid = (port >= 5000) && (port <= 5050);
			prompt = "Le numero du port n'est pas dans l'intervalle permise\n\nEntrez le numero du port (5000-5050): ";
		}
		return port;
	}
	
	// Lire le fichier de bd 
	public static boolean readFile(String name, String password) {
		try {
			File file = new File("Database.txt");
			file.createNewFile();
			FileReader reader = new FileReader(file);
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
			System.out.println("Erreur lors de l'acces a la base de donnes");
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
		private String imageName;
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
				
				/* Boucle des actions du clients. 
				 * 1) Reception et lecture d'une image du client
				 * 2) Renvoyer l'image traiter au client
				 * 3) Deconnecter le client du serveur
				 */
				while(!done) {
					int userAction = in.read();
					if(userAction == 1) {
						imageName = in.readUTF();
						this.image = this.readImage(in);
					} else if (userAction == 2) {
						this.sendImage(out);
					} else if (userAction == 3) {
						System.out.println("\nClient " + username + " a ete deconnecte.");
						done = true;
					}
				}
			} catch (IOException e){
				System.out.println("Erreur");
			} finally {
				try {
					socket.close();
				} catch(IOException e) {
					System.out.println("Erreur de socket");
				}
				System.out.println("Fermeture de l'instance de " + username);
			}
		}
		
		public BufferedImage readImage(DataInputStream in) throws IOException {
			int length = in.readInt();
			byte[] fileContent = new byte[length];
			in.readFully(fileContent);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();
			System.out.print(
				"\n\n[" + username + " - " + 
				socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort() + " - " + 
				dtf.format(now) + "] : L'image " + imageName + " a ete recu pour traitement.\n\n"
			);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(fileContent);
			return Sobel.process(ImageIO.read(byteStream));
		}
		
		public void sendImage(DataOutputStream out) throws IOException {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			String imageNameParts[] = this.imageName.split("\\.");
			ImageIO.write(this.image, imageNameParts[1], byteStream);
			byte[] processedBytes = byteStream.toByteArray();
			out.writeInt(processedBytes.length);
			out.flush();
			out.write(processedBytes, 0, processedBytes.length);
			out.flush();
			byteStream.close();
		}
	}
}
