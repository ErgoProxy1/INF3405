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
			prompt = "Le format de l'adresse IP est incorrect!\n\nEntrez l'adresse IP: ";
		}
		return ip;
	}

	// Demande a l'utilisateur d'entrer un numero de port jusqu'a ce qu'il soit
	// valide
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
	
	public static String inputAndValidateImageName(boolean creatingImage) {
		boolean nameFormatValid = false;
		String imageName = "";
		while (!nameFormatValid) {
			imageName = input.next();
			nameFormatValid = imageName.matches("([\\w]+(\\.(?i)(jpg|png|gif|bmp))$)");
			if (!nameFormatValid) {
				if(creatingImage)
					System.out.print("\nVeuillez entrer un nom d'image valide!\n\nEntrez le nom de la nouvelle image(.jpg ou .png): ");
				else
					System.out.print("\nVeuillez entrer un nom d'image valide!\n\nEntrez le nom de l'image(.jpg ou .png): ");
				input.nextLine();
			}
		}
		return imageName;
	}

	public static void sendImage(DataOutputStream out) throws IOException {
		System.out.print("\nEntrez le nom de l'image(.jpg ou .png): ");
		String imageName = inputAndValidateImageName(false);

		File imageFile = new File(imageName);
		while(!imageFile.exists()) {
			System.out.print("Le fichier n'existe pas!\nEntrez le nom de l'image(.jpg ou .png): ");
			imageName = inputAndValidateImageName(false);
			imageFile = new File(imageName);
		}
		
		out.write(1);
		out.flush();
		
		out.writeUTF(imageName);
		out.flush();
		
		byte[] fileContent = Files.readAllBytes(imageFile.toPath());
		out.writeInt(fileContent.length);
		out.flush();
		out.write(fileContent, 0, fileContent.length);
		out.flush();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		System.out.print("[" + dtf.format(now) + "]" + " L'image " + imageName + " a ete envoye au serveur.\n\n");
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
		System.out.print("Fichier " + name + " genere sous " + System.getProperty("user.dir") + "\n\n");
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
				System.out.print("\nConnection Refusee! Auncun serveur correspondant a l'adresse IP et/ou le numero du port n'a ete trouve\n\n");
				continue;
			}
		}

		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());

		// Boucle d'attente de connection à un compte utilisateur
		boolean connected = false;
		while (!connected) {
			System.out.print("Entrez le nom d'utilisateur : ");
			String username = input.next();

			System.out.print("Provide Password : ");
			String password = input.next();

			out.writeUTF(username);
			out.flush();
			out.writeUTF(password);
			out.flush();
			connected = in.readBoolean();
			if (!connected) {
				System.out.print("\nErreur dans la saisie du mot de passe. Le nom d'utilisateur existe deja sous un autre mot de passe\n");
				continue;
			}
		}
		System.out.println("La connection au serveur a reussi");

		// Boucle d'attente des instructions de l'utilisateur
		while (true) {

			System.out.print("\n***************\nA) Traiter une image\nB) Deconnecter et quitter\nSelection: ");

			String selection = input.next().strip().toUpperCase();

			if (selection.matches("A")) {
				sendImage(out);
				System.out.print("Entrez le nom de la nouvelle image(.jpg ou .png): ");
				String newName = inputAndValidateImageName(true);
				getImage(newName, out, in);
			} else if (selection.matches("B")) {
				out.write(3);
				out.close();
				break;
			} else {
				System.out.print("Saisie invalide\n\n");
				continue;
			}
		}
		System.out.print("\nAu Revoir!\n");
		input.close();
		socket.close();
	}
}
