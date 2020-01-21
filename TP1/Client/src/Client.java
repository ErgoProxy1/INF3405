import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import java.awt.image.BufferedImage;

public class Client {
	private static Socket socket;
	private static Scanner input = new Scanner(System.in);
	
	// Demande à l'utilisateur d'entrer une adresse IP jusqu'à ce qu'elle soit valide
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
	
	// Demande à l'utilisateur d'entrer un numero de port jusqu'à ce qu'il soit valide
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
	
	public static void sendImage(DataOutputStream out) throws IOException {
		System.out.println("Provide Image Name : ");
		String imageName = input.next();
		
		out.write(1);
		out.flush();
		
		File imageFile = new File(imageName+".jpg");
		BufferedImage image = ImageIO.read(imageFile);
		ImageOutputStream imageOutput = ImageIO.createImageOutputStream(socket.getOutputStream());
		ImageIO.write(image, "JPG", imageOutput);
		imageOutput.close();
	}
	
	public static void getImage(String name, DataOutputStream out) throws IOException {
		out.write(2);
		out.flush();
		
		ImageInputStream imageInput = ImageIO.createImageInputStream(socket.getInputStream());
		BufferedImage processed = ImageIO.read(imageInput);
		imageInput.close();
		
		ImageIO.write(processed, "jpg", new File(name+".jpg"));
	}
	
	public static void main(String[] args) throws Exception {
		
		String server = inputAndValidateIP();
		
		int port = inputAndValidatePort();
		
		System.out.println("Provide Username : ");
		String username = input.next();
		
		System.out.println("Provide Password : ");
		String password = input.next();
	
		socket = new Socket(server, port);
		
		while(true) {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			System.out.println("A) Process Image\nB) Exit");
			String selection = input.next().strip().toUpperCase();
			if(selection.matches("A")) {
				sendImage(out);
				System.out.println("Provide Generated Image Name : ");
				String newName = input.next();
				getImage(newName, out);
			} else if(selection.matches("B")) {
				out.write(3);
				out.close();
				break;
			} else {
				System.out.println("Invalid Input");
				continue;
			}
		}
		
		//String messageFromServer = in.readUTF();
		//System.out.println(messageFromServer);
				
		input.close();
		socket.close();
	}
}
