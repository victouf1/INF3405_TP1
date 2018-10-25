package org.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.common.ExchangesUtil;
import org.common.GuiUtil;

/**
 * Classe exécutable du serveur
 *
 */
public class MainServer {

	/**
	 * Executable
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerSocket serverSocket = null;

		// création du serveur
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(InetAddress.getByName(GuiUtil.getIPAdress()), GuiUtil.getPort()));

		try {
			while (true) {
				// création des connexion client, accept() est bloquant et attend un nouveau
				// client
				new Client(serverSocket.accept()).start();
			}
		} finally {
			// On éteint le serveur
			serverSocket.close();
		}
	}

	/**
	 * Classe Client héritant de thread ce qui permet d'avoir plusieurs client (dans
	 * des Threads différents)
	 *
	 */
	private static class Client extends Thread {
		private ObjectOutputStream m_ObjectOutput;
		private Socket m_Socket;
		/**
		 * Path pour le dossier racine du serveur
		 */
		private Path m_RootPath;
		/**
		 * Path où est le client
		 */
		private Path m_UserPath;
		private volatile boolean m_Running = true;

		/**
		 * Constructeur
		 * 
		 * @param socket liant au Client
		 */
		public Client(Socket socket) {
			this.m_Socket = socket;
			try {
				m_ObjectOutput = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				m_ObjectOutput.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			// on initialise le Path
			initializePath();
			while (m_Running) {
				// on récupère l'action (dans le socket)
				String action = getAction();
				if (action != null) {
					// on affiche l'ac tion demandé par le client dans la con sole du serveur
					System.out
							.println(GuiUtil.getOut(m_Socket.getInetAddress().toString(), m_Socket.getPort(), action));
					// on fait l'action
					doAction(action);
				}
			}
			try {
				// on ferme le socket quand on a finit
				m_Socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Définit le Path racine du serveur sur la machine
		 * 
		 * @throws CancellationException
		 */
		private void initializePath() {
			// création du root dont le client ne pourra pas s'échaper dans l'espace serveur
			String userName = System.getProperty("user.name");
			m_RootPath = Paths.get("C:", "Users", userName, "Documents"); // TODO remplacer pour que ca marche dans le
																			// laboratoire
			if (!Files.exists(Paths.get(m_RootPath.toString(), "root")))
				new File(m_RootPath.toString(), "root").mkdir();
			m_RootPath = Paths.get(m_RootPath.toString(), "root");
			m_UserPath = m_RootPath;
		}

		/**
		 * lit l'action demandée par le client
		 * 
		 * @return l'action
		 */
		private String getAction() {
			try {
				String action = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(m_Socket.getInputStream()));
				// on récupère l'action et on la return
				action = (String) in.readLine();
				return action;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					m_Running = false;
					m_Socket.close();
				} catch (IOException f) {
					return "";
				}
				return "";
			}
		}

		/**
		 * fait l'action demandée
		 * 
		 * @param action
		 */
		private void doAction(String action) {
			String command;
			String name;
			// on récupère la commande
			if (action.contains(" ")) {
				String[] commandAndName = action.split(" ");
				command = commandAndName[0];
				name = commandAndName[1];
			} else {
				command = action;
				name = "";
			}

			List<String> info = new ArrayList<String>();
			// on fait en fonction de la commande
			switch (command) {
			case "cd":
				cdAction(name);
				break;
			case "pwd":
				Path relativePath = m_RootPath.relativize(m_UserPath);
				if (relativePath.toString().equals(""))
					info.add(m_UserPath.toString().substring(m_UserPath.toString().lastIndexOf('\\') + 1));
				else
					info.add(relativePath.toString());
				sendInfoToClient(info);
				break;
			case "ls":
				lsAction();
				break;
			case "mkdir":
				new File(m_UserPath.toString(), name).mkdir();
				info.add("Le dossier " + name + " a été créé\n");
				sendInfoToClient(info);
				break;
			case "upload":
				uploadAction();
				break;
			case "download":
				downloadAction(name);
				break;
			case "exit":
				m_Running = false;
				info.add("Vous avez été déconnecté avec succès\n");
				sendInfoToClient(info);
				break;
			default:
				break;
			}
		}

		/**
		 * envoie les informations au client
		 * 
		 * @param info les informations à envoyer
		 */
		private void sendInfoToClient(List<String> info) {
			try {
				m_ObjectOutput.writeObject(info);
				m_ObjectOutput.flush();
			} catch (IOException e) {
				try {
					m_Running = false;
					m_Socket.close();
				} catch (IOException f) {
					f.printStackTrace();
				}
			}
		}

		/**
		 * fait l'action cd
		 * 
		 * @param name direction
		 */
		private void cdAction(String name) {
			List<String> info = new ArrayList<String>();
			Path beforeCommandPath = m_UserPath;
			try {
				// si name contien \ (une seule barre)
				if (name.contains("\\")) {
					// on split sur \ (une seule barre)
					for (String splitName : name.split("\\\\", 0)) {
						// cas du pagth avec 1 point : on part du path actuel
						if (splitName.equals(".")) {
							if (Files.exists(Paths.get(m_UserPath.toString())))
								m_UserPath = Paths.get(m_UserPath.toString());
							else {
								info.add("Le dossier n'existe pas\n");
								m_UserPath = beforeCommandPath;
								break;
							}
							// cas du path avec 2 points : on part du dossier parent
						} else if (splitName.equals("..")) {
							if (Files.exists(Paths.get(m_UserPath.getParent().toString()))) {
								if (m_UserPath.getParent().compareTo(m_RootPath) >= 0)
									m_UserPath = Paths.get(m_UserPath.getParent().toString());
								else {
									info.add("Le dossier n'existe pas\n");
									m_UserPath = beforeCommandPath;
									break;
								}
							}
						} else {
							// on avance dans le path en implémentant avec le for
							if (Files.exists(Paths.get(m_UserPath.toString(), splitName))) {
								if (m_UserPath.compareTo(m_RootPath) >= 0)
									m_UserPath = Paths.get(m_UserPath.toString(), splitName);
								else {
									info.add("Le dossier n'existe pas\n");
									m_UserPath = beforeCommandPath;
									break;
								}
							}
						}
					}
				} else {
					// cas où l'on a que .
					if (name.equals(".")) {
						if (Files.exists(Paths.get(m_UserPath.toString())))
							m_UserPath = Paths.get(m_UserPath.toString());
						else {
							info.add("Le dossier n'existe pas\n");
							m_UserPath = beforeCommandPath;
						}
						// cas où l'on a que ..
					} else if (name.equals("..")) {
						if (Files.exists(Paths.get(m_UserPath.getParent().toString()))) {
							if (m_UserPath.getParent().compareTo(m_RootPath) >= 0)
								m_UserPath = Paths.get(m_UserPath.getParent().toString());
							else {
								info.add("Le dossier n'existe pas\n");
								m_UserPath = beforeCommandPath;
							}
						}
					} else {
						// cas où l'on a que un nom de dossier
						if (Files.exists(Paths.get(m_UserPath.toString(), name))) {
							if (m_UserPath.compareTo(m_RootPath) >= 0)
								m_UserPath = Paths.get(m_UserPath.toString(), name);
							else {
								info.add("Le dossier n'existe pas\n");
								m_UserPath = beforeCommandPath;
							}
						}
					}
				}
			} catch (Exception e) {
				info.add("Le dossier n'existe pas\n");
				m_UserPath = beforeCommandPath;
			}

			Path relativePath = m_RootPath.relativize(m_UserPath);
			if (relativePath.toString().equals(""))
				info.add("Vous êtes dans le dossier : "
						+ m_UserPath.toString().substring(m_UserPath.toString().lastIndexOf('\\') + 1) + "\n");
			else
				info.add("Vous êtes dans le dossier : " + relativePath.toString() + "\n");

			sendInfoToClient(info);
		}

		/**
		 * fait l'action ls (afficher tous les dossier et fichier de là où on est)
		 */
		private void lsAction() {
			List<String> info = new ArrayList<String>();
			String[] lsArray = new File(m_UserPath.toString()).list();

			for (int i = 0; i < lsArray.length; i++) {
				if (new File(m_UserPath.toString(), lsArray[i]).isDirectory())
					info.add("[Folder]" + lsArray[i]);
			}

			for (int i = 0; i < lsArray.length; i++) {
				if (new File(m_UserPath.toString(), lsArray[i]).isFile())
					info.add("[File]" + lsArray[i]);
			}

			sendInfoToClient(info);
		}

		/**
		 * permet de faire la commande download (du serveur vers le client)
		 * 
		 * @param name fichier à download
		 */
		private void downloadAction(String name) {
			try {
				File file = new File(m_UserPath.toString(), name);
				if (file.exists() && file.isFile()) {
					// l'action download est un upload du point de vue du serveur
					ExchangesUtil.upload(file, m_Socket);
				} else {
					throw new InvalidPathException(file.getAbsolutePath(), "");
				}
			} catch (InvalidPathException e1) {
				List<String> info = new ArrayList<String>();
				info.add("Le fichier n'existe pas!\n");
				sendInfoToClient(info);
			} catch (IOException e2) {
				List<String> info = new ArrayList<String>();
				info.add("An error occured during the transfer");
				sendInfoToClient(info);
			}

		}

		/**
		 * permet de faire la commande upload (du client vers le serveur)
		 */
		private void uploadAction() {
			try {
				// l'action upload est un download du point de vue du serveur
				ExchangesUtil.download(m_Socket, m_UserPath.toString());
			} catch (Exception e) {
				List<String> info = new ArrayList<String>();
				info.add("Une Erreur a eu lieu durant le transfert");
				sendInfoToClient(info);
			}
		}
	}
}
