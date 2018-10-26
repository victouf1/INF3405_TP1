package org.client;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.common.ExchangesUtil;
import org.common.GuiUtil;

/**
 * classe ex�cutable du client
 */
public class MainClient {

	private ObjectInputStream m_ObjectInput;
	private PrintWriter m_Output;
	private Socket m_Socket;
	public boolean m_Running = true;
	Scanner m_ConsoleScanner = new Scanner(System.in);

	/**
	 * ex�cutable
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		// cr�e le client
		MainClient client = new MainClient();
		try {
			// se connecte au client
			client.connectToServer();
			while (client.m_Running) {
				// attends et fait les instructions par la commande
				client.readAndDoCommand();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// on ferme tout
			client.close();
			System.exit(0);
		}
	}

	/**
	 * Attends la commande de la console et l'ex�cute
	 */
	@SuppressWarnings("unchecked")
	private void readAndDoCommand() {
		// on lit la commande pass�e en console
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
		// cas du download et de l'upload � g�rer diff�remment
		if (command.equals("download"))
			downloadAction();
		else if (command.equals("upload"))
			uploadAction(name);
		// cas normal o� l'on transf�re que des string et listes de string
		else if(command.equals("exit")) {
			readResponse();
			m_Running = false;
		}
		else
			readResponse();
	}

	/**
	 * se connecte au serveur
	 */
	private void connectToServer() throws IOException {

		// Recupere l'adresse IP et le port
		String serverAddress = GuiUtil.getIPAdress();
		int port = GuiUtil.getPort();
		// cr�ation du socket, le temps qu'on arrive pas � cr�er le socket, on redemande
		// une adresse IP et Port
		try {
			m_Socket = new Socket(serverAddress, port);
		} catch (Exception e) {
			connectToServer();
		}
		System.out.format("Vous �tes connect�s au serveur %s:%d%n", serverAddress, port);

		// cr�ation des input et output
		m_ObjectInput = new ObjectInputStream(new BufferedInputStream(m_Socket.getInputStream()));
		m_Output = new PrintWriter(m_Socket.getOutputStream());
	}
	
	/**
	 * lit la r�ponse du serveur
	 */
	private void readResponse() {
		List<String> response;
		try {
			// lecture de la r�ponse
			response = (List<String>) m_ObjectInput.readObject();
			if (response == null) {
				System.exit(0);
			}
		} catch (EOFException e) {
			// cas o� l'on est d�connect� du serveur
			response = new ArrayList<>();
			response.add("Erreur vous �tes d�connect�s du serveur");
		} catch (IOException | ClassNotFoundException ex) {
			response = new ArrayList<>();
			response.add("Error: " + ex);
		}
		
		// affichage de la r�ponse
		for (int i = 0; i < response.size(); i++)
			System.out.println(response.get(i));
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
			List<String> response = (List<String>) m_ObjectInput.readObject();
			
			if(response.get(0).equals("1"))
			{
				ExchangesUtil.download(m_Socket, "C:\\Users\\" + System.getProperty("user.name") + "\\Documents");
				System.out.println("Le fichier � bien �t� t�l�charg�");
			}
			else
				System.out.println(response.get(0));
		} catch (Exception e) {
			System.out.println("Erreur lors du t�l�chargement du fichier");
		}
	}

	/**
	 * fait la commande upload (du client vers le serveur)
	 * 
	 * @param name path du fichier � upload
	 */
	private void uploadAction(String name) {
		try {
			File file = new File(name);
			if (file.exists() && file.isFile()) {
				ExchangesUtil.upload(file, m_Socket);
				System.out.println("Le fichier " + file.getName() + " � bien �t� t�l�vers�\n");
			} else {
				throw new InvalidPathException(name, "");
			}
		} catch (InvalidPathException e1) {
			System.out.println("Ce n'est pas un fichier!");
		} catch (IOException e2) {
			System.out.println("Erreur lors de l'upload");
		}

	}
}
