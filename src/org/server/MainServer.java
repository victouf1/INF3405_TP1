package org.server;
import org.client.GuiUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CancellationException;


import java.net.InetAddress;
import java.net.InetSocketAddress;


import java.io.File;

public class MainServer {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerSocket serverSocket = null;		

		//String serverAddress = GuiUtil.getIPAdress();
		//int port = GuiUtil.getPort();

		InetAddress IPAddress = InetAddress.getByName(/*serverAddress*/"192.168.0.100");
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(IPAddress, /*port*/5000));
		
		try {
			while (true) {	
				new Client(serverSocket.accept()).start();
			}
		}
		finally {
			serverSocket.close();			
		}
	}
	
	// Creates a new thread for each client
	private static class Client extends Thread {
		private Socket socket;
		private Path rootPath;
		private Path userPath;
		private volatile boolean running = true;
		
		public Client(Socket socket) {
		    this.socket = socket;
		}
		
		public void run() {
			try {
				
				initializePath();
			    while (running) {
			    	String action = getAction();
			    	if(action != null)
			    	{
					    System.out.println(GuiUtil.getOut(socket.getInetAddress().toString(), socket.getPort(), action));
					    doAction(action);
			    	}
			    }
			}
            finally{
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
			rootPath = Paths.get("C:","Users",userName,"Documents"); // TODO remplacer pour que ca marche dans le laboratoire
			if(!Files.exists(Paths.get(rootPath.toString(),"root")))
				new File(rootPath.toString(),"root").mkdir();
			rootPath = Paths.get(rootPath.toString(),"root");
			userPath = rootPath;
		}
	    
		// Read the action required by the client
		private String getAction() throws CancellationException {
			try {
				String action = null;
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				action = (String) in.readObject();							
				return action;
			}
			catch (IOException e) {
				e.printStackTrace();
				try {
					running = false;
					socket.close();
				}catch (IOException f) {
					// TODO Auto-generated catch block
					f.printStackTrace();
				}
				return "";
			}
			catch (ClassNotFoundException a) {
				running = false;
				return "";
			}
		}
		
		// Do the action
		private void doAction(String action) throws CancellationException {
			String command = action;
			String name = "";
			if(action.contains(" "))
			{
				String[] commandAndName = action.split(" ");
				command = commandAndName[0];
				name = commandAndName[1];
			}
			
			List<String> info = new ArrayList<String>();
			switch(command) {
			case "cd":
				cdAction(name);
				break;
			case "pwd":
				Path relativePath = rootPath.relativize(userPath);
				if(relativePath.toString().equals(""))
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
				break;
			case "download":// TODO
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
			ObjectOutputStream objectOutput = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			objectOutput.writeObject(info);
			objectOutput.flush();
			}
			catch (IOException e) {
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
				if(name.contains("\\"))
				{					
					for (String splitName: name.split("\\\\",0))
					{
						if(splitName.equals("."))
						{
							if(Files.exists(Paths.get(userPath.toString())))
								userPath = Paths.get(userPath.toString());					
							else
							{
								info.add("Le dossier n'existe pas\n");
								userPath = beforeCommandPath;
								break;
							}
						}
						else if(splitName.equals(".."))
						{
							if(Files.exists(Paths.get(userPath.getParent().toString())))
							{
								if(userPath.getParent().compareTo(rootPath) >= 0)
									userPath = Paths.get(userPath.getParent().toString());
								else
								{
									info.add("Le dossier n'existe pas\n");
									userPath = beforeCommandPath;
									break;
								}
							}
						}
						else
						{
							
							if(Files.exists(Paths.get(userPath.toString(),splitName)))
							{
								if(userPath.compareTo(rootPath) >= 0)
									userPath = Paths.get(userPath.toString(),splitName);
								else
								{
									info.add("Le dossier n'existe pas\n");
									userPath = beforeCommandPath;
									break;
								}
							}
						}
					}			
				}
				else
				{
					if(name.equals("."))
					{
						if(Files.exists(Paths.get(userPath.toString())))
							userPath = Paths.get(userPath.toString());					
						else
						{
							info.add("Le dossier n'existe pas\n");
							userPath = beforeCommandPath;
						}
					}
					else if(name.equals(".."))
					{
						if(Files.exists(Paths.get(userPath.getParent().toString())))
						{
							if(userPath.getParent().compareTo(rootPath) >= 0)
								userPath = Paths.get(userPath.getParent().toString());
							else
							{
								info.add("Le dossier n'existe pas\n");
								userPath = beforeCommandPath;
							}
						}
					}
					else
					{
						
						if(Files.exists(Paths.get(userPath.toString(),name)))
						{
							if(userPath.compareTo(rootPath) >= 0)
								userPath = Paths.get(userPath.toString(),name);
							else
							{
								info.add("Le dossier n'existe pas\n");
								userPath = beforeCommandPath;
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				info.add("Le dossier n'existe pas\n");
				userPath = beforeCommandPath;
			}
								
			Path relativePath = rootPath.relativize(userPath);
			if(relativePath.toString().equals(""))
				info.add("Vous êtes dans le dossier : " + userPath.toString().substring(userPath.toString().lastIndexOf('\\') + 1) + "\n");
			else
				info.add("Vous êtes dans le dossier : " + relativePath.toString() + "\n");
			
			sendInfoToClient(info);
		}
		
		private void lsAction() throws CancellationException {
			List<String> info = new ArrayList<String>();			
			String[] lsArray = new File(userPath.toString()).list();
			
			for(int i= 0; i < lsArray.length; i++)
			{
				if(new File(userPath.toString(),lsArray[i]).isDirectory())
					info.add("[Folder]" + lsArray[i]);
			}
			
			for(int i= 0; i < lsArray.length; i++)
			{
				if(new File(userPath.toString(),lsArray[i]).isFile())
					info.add("[File]" + lsArray[i]);
			}
			
			sendInfoToClient(info);
		}
	}
}
