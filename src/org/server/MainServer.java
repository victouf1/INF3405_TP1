package org.server;
import org.client.GuiUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Stack;



import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.io.InputStream;
import java.util.Scanner;

public class MainServer {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		while (true) {
			ServerSocket serverSocket = null;
			Scanner scanner = null;
						
			try {
				while (true) {
				String serverAddress = GuiUtil.getIPAdress();
				int port = GuiUtil.getPort();
				
				InetAddress IPAddress = InetAddress.getByName(serverAddress);
				serverSocket = new ServerSocket();
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress(IPAddress, port));
				}
				
			} finally {
				serverSocket.close();
				scanner.close();			
			}
		}
	}
	
	public class Client() extends Thread {
	     public void run(){
	     
	   }
	}
}
