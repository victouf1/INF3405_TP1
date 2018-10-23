package org.client;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.common.ExchangesUtil;
import org.common.GuiUtil;

/**
 * classe exécutable du client
 */
public class MainClient {

	private ObjectInputStream m_ObjectInput;
	private PrintWriter m_Output;
	private Socket m_Socket;
	Scanner m_ConsoleScanner = new Scanner(System.in);

	/**
	 * exécutable
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		// crée le client
		MainClient client = new MainClient();
		try {
			// se connecte au client
			client.connectToServer();
			while (true) {
				// attends et fait les instruction par la commande
				client.readAndDoCommand();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// on ferme tout
			client.close();
		}
	}

	/**
	 * Attends la commande de la console et l'exécute
	 */
	@SuppressWarnings("unchecked")
	private void readAndDoCommand() {
		// on lit la commande passée en console
		String command = m_ConsoleScanner.nextLine();
		// on l'envoie au serveur
		m_Output.println(command);
		m_Output.flush();

		String name = "";
		if (command.contains(" ")) {
			String[] commandAndName = command.split(" ", 2);
			command = commandAndName[0];
			name = commandAndName[1];
		}
		// cas du download et de l'upload à gérer différemment
		if (command.equals("download"))
			downloadAction();
		else if (command.equals("upload"))
			uploadAction(name);
		// cas normal où l'on transfère que des string et listes de string
		else {
			List<String> response;
			try {
				// lecture de la réponse
				response = (List<String>) m_ObjectInput.readObject();
				if (response == null) {
					System.exit(0);
				}
			} catch (EOFException e) {
				// cas où l'on est déconnecté du serveur
				response = new ArrayList<>();
				response.add("Erreur vous êtes déconnectés du serveur");
			} catch (IOException | ClassNotFoundException ex) {
				response = new ArrayList<>();
				response.add("Error: " + ex);
			}
			// affichage de la réponse
			for (int i = 0; i < response.size(); i++)
				System.out.println(response.get(i));
		}
	}

	/**
	 * se connecte au serveur
	 */
	@SuppressWarnings("resource")
	private void connectToServer() throws IOException {

		// Recupere l'adresse IP et le port
		String serverAddress = GuiUtil.getIPAdress();
		int port = GuiUtil.getPort();
		// TODO CONNECTEXCEPTION
		// création du socket
		m_Socket = new Socket(serverAddress, port);

		System.out.format("Vous êtes connectés au serveur %s:%d%n", serverAddress, port);

		// création des input et output
		m_ObjectInput = new ObjectInputStream(new BufferedInputStream(m_Socket.getInputStream()));
		m_Output = new PrintWriter(m_Socket.getOutputStream());
	}

	/**
	 * ferme le client
	 */
	private void close() throws IOException {
		m_ConsoleScanner.close();
		m_Socket.close();
	}

	/**
	 * fait la commande download (du serveur vers le client)
	 */
	private void downloadAction() {
		try {
			ExchangesUtil.upload(m_Socket, "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads");
			System.out.println("Le fichier à bien été téléchargé");
		} catch (Exception e) {
			System.out.println("Erreur lors du téléchargement du fichier");
			e.printStackTrace();
		}
	}

	/**
	 * fait la commande upload (du client vers le serveur)
	 * 
	 * @param name path du fichier à upload
	 */
	private void uploadAction(String name) {
		if (Files.exists(Paths.get(name)) && new File(name).isFile()) {
			File file = new File(name);
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(m_Socket.getOutputStream());
				oos.writeObject(file.getName());

				FileInputStream fis = new FileInputStream(file);
				byte[] buffer = new byte[100];
				Integer bytesRead = 0;

				while ((bytesRead = fis.read(buffer)) > 0) {
					oos.writeObject(bytesRead);
					oos.writeObject(Arrays.copyOf(buffer, buffer.length));
				}

				fis.close();

				System.out.println("Le fichier " + file.getName() + " à bien été téléversé\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Ce n'est pas un fichier!\n");
		}
	}
}
