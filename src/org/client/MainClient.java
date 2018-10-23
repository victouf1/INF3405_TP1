package org.client;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainClient {

	private ObjectInputStream in;
	private PrintWriter out;
	private Socket socket;
	private JFrame frame = new JFrame("Capitalize Client");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 60);

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		MainClient client = new MainClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.pack();
		client.frame.setVisible(true);
		try {
			client.connectToServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MainClient() {

		// Layout GUI
		messageArea.setEditable(false);
		frame.getContentPane().add(dataField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");

		// Add Listeners
		dataField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending the contents
			 * of the text field to the server and displaying the response from the server
			 * in the text area. If the response is "." we exit the whole application, which
			 * closes all sockets, streams and windows.
			 */
			public void actionPerformed(ActionEvent e) {
				messageArea.append(dataField.getText()+ "\n");
				out.println(dataField.getText()+ "\n");
				out.flush();
				
				String command = dataField.getText();
				String name = "";
				if (command.contains(" ")) {
					String[] commandAndName = command.split(" ",2);
					command = commandAndName[0];
					name = commandAndName[1];
				}
				
				if(command.equals("download"))
					downloadAction();
				else if(command.equals("upload"))
					uploadAction(name);
				else
				{			
					List<String> response;
					try {
						response = (List<String>) in.readObject();
						for(int i= 0; i < response.size(); i++)
							System.out.println(response.get(i));
						if (response == null || response.equals("")) {
							System.exit(0);
						}
					} catch (IOException | ClassNotFoundException ex) {
						response = new ArrayList<>();
						response.add("Error: " + ex);
					}
					for(int i= 0; i < response.size(); i++)
						messageArea.append(response.get(i) + "\n");
					messageArea.append("\n");
					dataField.selectAll();
				}
			}
		});
	}

	@SuppressWarnings("resource")
	public void connectToServer() throws IOException {

		// Get the server address from a dialog box.
		String serverAddress = GuiUtil.getIPAdress();
		int port = GuiUtil.getPort();
		// CONNECTEXCEPTION
		socket = new Socket(serverAddress, port);

		System.out.format("The capitalization server is running on %s:%d%n", serverAddress, port);

		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream());

		// Consume the initial welcoming messages from the server
		for (int i = 0; i < 3; i++) {
			messageArea.append(i + "\n");
		}
	}
	
    public void downloadAction() {
		try {
			ObjectInputStream ois = null;
			ois = new ObjectInputStream(socket.getInputStream());
			
			FileOutputStream fos = null;
	        byte [] buffer = new byte[100];
	        Object o;
	        
	        o = ois.readObject();
	        String name = o.toString();
	        
	        if (o instanceof String)
	        {
	        	String userName = System.getProperty("user.name");
				fos = new FileOutputStream("C:\\Users\\"+ userName + "\\Downloads\\" + name);
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
	        
			System.out.println("Le fichier " + name + " à bien été téléchargé");
			messageArea.append("Le fichier " + name + " à bien été téléchargé");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
    }
    
    public void uploadAction(String name) {
    	File file = new File(name);
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
	         

			System.out.println("Le fichier " + file.getName() + " à bien été téléversé\n");
			messageArea.append("Le fichier " + file.getName() + " à bien été téléversé\n");
		} catch (Exception e) {
			e.printStackTrace();
		}      
    }
}
