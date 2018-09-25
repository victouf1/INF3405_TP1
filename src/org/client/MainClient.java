package org.client;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainClient {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		
		Socket clientSocket = null;
		try {
			clientSocket = new Socket("127.0.0.1", 5000);
			ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
			List<String> linesToSend = readFile("text.txt");
			objectOutput.writeObject(linesToSend);
			objectOutput.flush();
			ObjectInputStream obj = new ObjectInputStream(clientSocket.getInputStream());
			@SuppressWarnings("unchecked")
			Stack<String> receivedStack = (Stack<String>) obj.readObject();
			writeToFile(receivedStack, "FichierInversee.txt");
		} finally {
			clientSocket.close();
		}
	}
	
	// Fonction permettant de lire un fichier et de stocker son contenu dans une liste.
	private static List<String> readFile(String nomFichier) throws IOException {
		List<String> listOfLines = new ArrayList<String>();
		String line = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(nomFichier);

			bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				listOfLines.add(line);
			}
		} finally {
			fileReader.close();
			bufferedReader.close();
		}
		return listOfLines;
	}

	// Fonction permettant d'écrire dans un fichier les données contenues dans la
	// stack reçu du serveur.
	private static void writeToFile(Stack<String> myStack, String nomFichier) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(nomFichier));
			while (!myStack.isEmpty()) {
				out.write(myStack.pop() + "\n");
			}
		} finally {
			out.close();
		}
	}
}
