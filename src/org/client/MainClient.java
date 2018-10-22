package org.client;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainClient {

	private DataInputStream in;
	private DataOutputStream out;
	private JFrame frame = new JFrame("Capitalize Client");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 60);
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		MainClient client = new MainClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.pack();
		client.frame.setVisible(true);
		client.connectToServer();
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
				//envoie du fichier
				// TODO reception du fichier
//				Util.recieveFile(dataField.getText(), in);
				messageArea.append("succeeeeeed \n");
				dataField.selectAll();
			}
		});
	}
	
	@SuppressWarnings("resource")
	public void connectToServer() throws IOException {

		// Get the server address from a dialog box.
		String serverAddress = GuiUtil.getIPAdress();
		int port = GuiUtil.getPort();

		Socket socket;
		socket = new Socket(serverAddress, port);

		System.out.format("The capitalization server is running on %s:%d%n", serverAddress, port);

		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());

		// Consume the initial welcoming messages from the server
		for (int i = 0; i < 3; i++) {
			messageArea.append(i + "\n");
		}
	}
	
}
