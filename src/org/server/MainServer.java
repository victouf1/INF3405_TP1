package org.server;
import org.client.GuiUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CancellationException;


import java.net.InetAddress;
import java.net.InetSocketAddress;


import java.io.File;

public class MainServer {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerSocket serverSocket = null;		

		String serverAddress = GuiUtil.getIPAdress();
		int port = GuiUtil.getPort();

		InetAddress IPAddress = InetAddress.getByName(serverAddress);
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(IPAddress, port));
		
		try {
			while (true) {	
				new Client(serverSocket.accept()).start();
			}
		}
		finally {
			serverSocket.close();			
		}
	}
	
	private static class Client extends Thread {
		private Socket socket;
        
		public Client(Socket socket) {
		    this.socket = socket;
		}
		
		public void run(){
			try {
			    while (true) {
			    	String action = getAction();
				    System.out.println(GuiUtil.getOut(socket.getInetAddress().toString(), socket.getPort(), action));
				    doAction(action);
			    }
			}
			finally {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	    
		private String getAction() throws CancellationException {
			try {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				@SuppressWarnings("unchecked")
				List<String> strings = (List<String>) in.readObject();
				Stack<String> stackOfLines = new Stack<String>();

				for (int i = 0; i < strings.size(); i++) {
					stackOfLines.push(strings.get(i));
				}
				
				String action = stackOfLines.pop();
				return action;
			}
			catch (IOException e) {
				e.printStackTrace();
				return "";
			}
			catch (ClassNotFoundException a) {
				return "";
			}
		}
		
		private void doAction(String action) throws CancellationException {
		
			String[] commandAndName = action.split(" ");
			String command = commandAndName[0];
			String name = commandAndName[1];
			
			String userName = System.getProperty("user.name");			
			Path root = Paths.get("C:","Users",userName,"Documents");
			if(!Files.exists(Paths.get(root.toString(),"root")))
				new File(root.toString(),"TestRoot").mkdir();
			
			switch(command) {
			case "cd":
				
				break;
			case "pwd":
				break;
			case "ls":
				break;
			case "mkdir":
				break;
			case "upload":
				break;
			case "dowload":
				break;
			case "exit":
				break;
			default:
				break;					
			}
		}
	}
}
