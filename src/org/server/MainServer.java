package org.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.client.GuiUtil;

public class MainServer {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerSocket serverSocket = null;

		// String serverAddress = GuiUtil.getIPAdress();
		// int port = GuiUtil.getPort();

		InetAddress IPAddress = InetAddress.getByName(/* serverAddress */"192.168.0.100");
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(IPAddress, /* port */5000));

		try {
			while (true) {
				new Client(serverSocket.accept()).start();
			}
		} finally {
			serverSocket.close();
		}
	}

	// Creates a new thread for each client
	private static class Client extends Thread {
		private ObjectOutputStream objectOutput;
		private Socket socket;
		private Path rootPath;
		private Path userPath;
		private volatile boolean running = true;

		public Client(Socket socket) {
			this.socket = socket;
			try {
			objectOutput = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			objectOutput.flush();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				initializePath();
				while (running) {
					String action = getAction();
					if (action != null) {
						System.out.println(GuiUtil.getOut(socket.getInetAddress().toString(), socket.getPort(), action));
						doAction(action);
					}
				}
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void initializePath() throws CancellationException {
			// create the root from which the client cannot escape (server space)
			String userName = System.getProperty("user.name");
			rootPath = Paths.get("C:", "Users", userName, "Documents"); // TODO remplacer pour que ca marche dans le
																		// laboratoire
			if (!Files.exists(Paths.get(rootPath.toString(), "root")))
				new File(rootPath.toString(), "root").mkdir();
			rootPath = Paths.get(rootPath.toString(), "root");
			userPath = rootPath;
		}

		// Read the action required by the client
		private String getAction() throws CancellationException {
			try {
				String action = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				action = (String) in.readLine();
				return action;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					running = false;
					socket.close();
				} catch (IOException f) {
					return "";
				}
				return "";
			}
		}

		// Do the action
		private void doAction(String action) throws CancellationException {
			String command = action;
			String name = "";
			if (action.contains(" ")) {
				String[] commandAndName = action.split(" ");
				command = commandAndName[0];
				name = commandAndName[1];
			}

			List<String> info = new ArrayList<String>();
			switch (command) {
			case "cd":
				cdAction(name);
				break;
			case "pwd":
				Path relativePath = rootPath.relativize(userPath);
				if (relativePath.toString().equals(""))
					info.add(userPath.toString().substring(userPath.toString().lastIndexOf('\\') + 1));
				else
					info.add(relativePath.toString());
				sendInfoToClient(info);
				break;
			case "ls":
				lsAction();
				break;
			case "mkdir":
				new File(userPath.toString(), name).mkdir();
				info.add("Le dossier " + name + " a été créé\n");
				sendInfoToClient(info);
				break;
			case "upload":// TODO
				uploadAction();
				break;
			case "download":// TODO
				downloadAction(name);
				break;
			case "exit":
				running = false;
				info.add("Vous avez été déconnecté avec succès\n");
				sendInfoToClient(info);
				break;
			default:
				break;
			}
		}

		private void sendInfoToClient(List<String> info) throws CancellationException {
			try {
				objectOutput.writeObject(info);
				objectOutput.flush();
			} catch (IOException e) {
				try {
					running = false;
					socket.close();
				} catch (IOException f) {
					// TODO Auto-generated catch block
					f.printStackTrace();
				}
			}
		}

		private void cdAction(String name) throws CancellationException {
			List<String> info = new ArrayList<String>();
			Path beforeCommandPath = userPath;
			try {
				if (name.contains("\\")) {
					for (String splitName : name.split("\\\\", 0)) {
						if (splitName.equals(".")) {
							if (Files.exists(Paths.get(userPath.toString())))
								userPath = Paths.get(userPath.toString());
							else {
								info.add("Le dossier n'existe pas\n");
								userPath = beforeCommandPath;
								break;
							}
						} else if (splitName.equals("..")) {
							if (Files.exists(Paths.get(userPath.getParent().toString()))) {
								if (userPath.getParent().compareTo(rootPath) >= 0)
									userPath = Paths.get(userPath.getParent().toString());
								else {
									info.add("Le dossier n'existe pas\n");
									userPath = beforeCommandPath;
									break;
								}
							}
						} else {

							if (Files.exists(Paths.get(userPath.toString(), splitName))) {
								if (userPath.compareTo(rootPath) >= 0)
									userPath = Paths.get(userPath.toString(), splitName);
								else {
									info.add("Le dossier n'existe pas\n");
									userPath = beforeCommandPath;
									break;
								}
							}
						}
					}
				} else {
					if (name.equals(".")) {
						if (Files.exists(Paths.get(userPath.toString())))
							userPath = Paths.get(userPath.toString());
						else {
							info.add("Le dossier n'existe pas\n");
							userPath = beforeCommandPath;
						}
					} else if (name.equals("..")) {
						if (Files.exists(Paths.get(userPath.getParent().toString()))) {
							if (userPath.getParent().compareTo(rootPath) >= 0)
								userPath = Paths.get(userPath.getParent().toString());
							else {
								info.add("Le dossier n'existe pas\n");
								userPath = beforeCommandPath;
							}
						}
					} else {

						if (Files.exists(Paths.get(userPath.toString(), name))) {
							if (userPath.compareTo(rootPath) >= 0)
								userPath = Paths.get(userPath.toString(), name);
							else {
								info.add("Le dossier n'existe pas\n");
								userPath = beforeCommandPath;
							}
						}
					}
				}
			} catch (Exception e) {
				info.add("Le dossier n'existe pas\n");
				userPath = beforeCommandPath;
			}

			Path relativePath = rootPath.relativize(userPath);
			if (relativePath.toString().equals(""))
				info.add("Vous êtes dans le dossier : "
						+ userPath.toString().substring(userPath.toString().lastIndexOf('\\') + 1) + "\n");
			else
				info.add("Vous êtes dans le dossier : " + relativePath.toString() + "\n");

			sendInfoToClient(info);
		}

		private void lsAction() throws CancellationException {
			List<String> info = new ArrayList<String>();
			String[] lsArray = new File(userPath.toString()).list();

			for (int i = 0; i < lsArray.length; i++) {
				if (new File(userPath.toString(), lsArray[i]).isDirectory())
					info.add("[Folder]" + lsArray[i]);
			}

			for (int i = 0; i < lsArray.length; i++) {
				if (new File(userPath.toString(), lsArray[i]).isFile())
					info.add("[File]" + lsArray[i]);
			}

			sendInfoToClient(info);
		}
		
		private void downloadAction(String name) throws CancellationException {	                   
			File file = new File(userPath.toString(), name);
			ObjectOutputStream oos;
			try {
				 oos = new ObjectOutputStream(socket.getOutputStream());
		         oos.writeObject(file.getName());
		   	  
		         FileInputStream fis = new FileInputStream(file);
		         byte [] buffer = new byte[100];
		         Integer bytesRead = 0;
		  
		         while ((bytesRead = fis.read(buffer)) > 0) {
		             oos.writeObject(bytesRead);
		             oos.writeObject(Arrays.copyOf(buffer, buffer.length));
		         }
		         
		         fis.close();
			} catch (Exception e) {
				List<String> info = new ArrayList<String>();
				info.add("An error occured during the transfer");
				sendInfoToClient(info);
			}  
		}
		
		private void uploadAction() throws CancellationException {	                   
			try {
				ObjectInputStream ois = null;
				ois = new ObjectInputStream(socket.getInputStream());
				
				FileOutputStream fos = null;
		        byte [] buffer = new byte[100];
		        Object o;
		        
		        o = ois.readObject();
		        
		        if (o instanceof String)
		        {
					fos = new FileOutputStream(userPath.toString() + "\\" + o.toString());
		        }
		        
		        Integer bytesRead = 0;
		        do {
					o = ois.readObject();
					bytesRead = (Integer)o;
					o = ois.readObject();
					buffer = (byte[])o;
					// 3. Write data to output file.
		            fos.write(buffer, 0, bytesRead);     
		        } while (bytesRead == 100);
		        
		        fos.close();
			} catch (Exception e) {
				List<String> info = new ArrayList<String>();
				info.add("An error occured during the transfer");
				sendInfoToClient(info);
			}        
		}
	}
}
