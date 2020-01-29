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
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;

public class Client {
	private static Socket socket;
	private static Scanner input = new Scanner(System.in);

	// Demande a l'utilisateur d'entrer une adresse IP jusqu'a ce qu'elle soit
	// valide
	public static String inputAndValidateIP() {
		boolean isValid = false;
		String ip = "";
		String prompt = "Entrez l'adresse IP: ";
		while (!isValid) {
			System.out.print(prompt);
			ip = input.next().strip();
			isValid = ip.matches(
					"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
			prompt = "IP Address format is incorrect!\n\nProvide IP Address : ";
		}
		return ip;
	}

	// Demande a l'utilisateur d'entrer un numero de port jusqu'a ce qu'il soit
	// valide
	public static int inputAndValidatePort() {
		boolean isValid = false;
		int port = 0;
		String prompt = "Provide port number (5000-5050): ";
		while (!isValid) {
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
	
	public static String inputAndValidateImageName() {
		boolean nameFormatValid = false;
		String imageName = "";
		while (!nameFormatValid) {
			imageName = input.next();
			nameFormatValid = imageName.matches("([\\w]+(\\.(?i)(jpg|png|gif|bmp))$)");
			if (!nameFormatValid) {
				System.out.print("\nNot a valid image file!\n\nProvide Image File Name (Supports .jpg and .png): ");
				input.nextLine();
			}
		}
		return imageName;
	}

	public static void sendImage(DataOutputStream out) throws IOException {
		System.out.print("\nProvide Image File Name (Supports .jpg and .png): ");
		String imageName = inputAndValidateImageName();

		out.write(1);
		out.flush();

		out.writeUTF(imageName);
		out.flush();

		File imageFile = new File(imageName);
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
		
		String imageNameParts[] = name.split("\\.");

		int length = in.readInt();
		byte[] processedBytes = new byte[length];
		in.readFully(processedBytes);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(processedBytes);
		
		ImageIO.write(ImageIO.read(byteStream), imageNameParts[1], new File(name));
		System.out.print("File " + name + " created under " + System.getProperty("user.dir") + "'\n\n");
	}

	public static void main(String[] args) throws Exception {

		// Boucle d'attente d'une connection valide
		while (true) {
			try {
				String server = inputAndValidateIP();

				int port = inputAndValidatePort();

				socket = new Socket(server, port);
				break;

			} catch (ConnectException e) {
				System.out.print("\nConnection Refused! No server found on IP Address and/or Port Number\n\n");
				continue;
			}
		}

		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());

		// Boucle d'attente de connection à un compte utilisateur
		boolean connected = false;
		while (!connected) {
			System.out.print("Provide Username : ");
			String username = input.next();

			System.out.print("Provide Password : ");
			String password = input.next();

			out.writeUTF(username);
			out.flush();
			out.writeUTF(password);
			out.flush();
			connected = in.readBoolean();
			if (!connected) {
				System.out.println("The password is incorrect!\r\n" + " Erreur dans la saisie du mot de passe »");
				continue;
			}
		}
		System.out.println("You have been connected to the server");

		// Boucle d'attente des instructions de l'utilisateur
		while (true) {

			System.out.print("\n***************\nA) Process Image\nB) Exit\nSelection: ");

			String selection = input.next().strip().toUpperCase();

			if (selection.matches("A")) {
				sendImage(out);
				System.out.print("Provide Generated Image Name : ");
				String newName = inputAndValidateImageName();
				getImage(newName, out, in);
			} else if (selection.matches("B")) {
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
