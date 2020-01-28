import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Client {
	private static Socket socket;
	private static Scanner input = new Scanner(System.in);
	
	// Demande à l'utilisateur d'entrer une adresse IP jusqu'à ce qu'elle soit valide
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
			prompt = "IP Address format is incorrect!\n\nProvide IP Address : ";
		}
		return ip;
	}
	
	// Demande à l'utilisateur d'entrer un numero de port jusqu'à ce qu'il soit valide
	public static int inputAndValidatePort() {
		boolean isValid = false;
		int port = 0;
		String prompt = "Provide port number (5000-5050) : ";
		while(!isValid) {
			System.out.print(prompt);
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
			prompt = "Port number not in range\n\nProvide port number (5000-5050) : ";
		}
		return port;
	}
	
	public static void sendImage(DataOutputStream out) throws IOException {
		System.out.print("\nProvide Image Name : ");
		String imageName = input.next();
		
		out.write(1);
		out.flush();
		
		out.writeUTF(imageName);
		out.flush();
		
		File imageFile = new File(imageName+".jpg");
		byte[] fileContent = Files.readAllBytes(imageFile.toPath());
		out.writeInt(fileContent.length);
		out.flush();
		out.write(fileContent, 0, fileContent.length);
		out.flush();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();
		System.out.print("[" + dtf.format(now) + "]" + " Image " + imageName + " sent to server.\n\n");
	}
	
	public static void getImage(String name, DataOutputStream out, DataInputStream in) throws IOException {
		out.write(2);
		out.flush();
		
		int length = in.readInt();
		byte[] processedBytes = new byte[length];
		in.readFully(processedBytes);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(processedBytes);
		
		ImageIO.write(ImageIO.read(byteStream), "jpg", new File(name+".jpg"));
		System.out.print("File " + name + " created under " + System.getProperty("user.dir") + "'\n\n");
	}
	
	public static void main(String[] args) throws Exception {
		
		//Attente d'une connection valide
		while(true) {
			try {
				String server = inputAndValidateIP();
				
				int port = inputAndValidatePort();
				
				socket = new Socket(server, port);
				break;
				
			} catch(ConnectException e) {
				System.out.print("\nConnection Refused! No server found on IP Address and/or Port Number\n\n");
				continue;
			}
		}
		
		System.out.print("Provide Username : ");
		String username = input.next();
		
		System.out.print("Provide Password : ");
		String password = input.next();
		
		//Loop du program en attente d'instructions de l'utilisateur
		while(true) {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			System.out.print("\n***************\nA) Process Image\nB) Exit\nSelection: ");
			String selection = input.next().strip().toUpperCase();
			
			if(selection.matches("A")) {
				sendImage(out);
				System.out.print("Provide Generated Image Name : ");
				String newName = input.next();
				getImage(newName, out, in);
			} else if(selection.matches("B")) {
				out.write(3);
				out.close();
				break;
			} else {
				System.out.print("Invalid Input\n\n");
				continue;
			}
		}
		System.out.print("\nGoodbye!\n");
		input.close();
		socket.close();
	}
}
