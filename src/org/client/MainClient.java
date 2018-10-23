package org.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainClient {

	private ObjectInputStream in;
	private PrintWriter out;
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
				messageArea.append(dataField.getText());
				out.println(dataField.getText());
				out.flush();
				List<String> response;
				try {
					response = (List<String>) in.readObject();
					if (response == null || response.equals("")) {
						System.exit(0);
					}
				} catch (IOException | ClassNotFoundException ex) {
					response = new ArrayList<>();
					response.add("Error: " + ex);
				}
				messageArea.append(response + "\n");
				dataField.selectAll();
			}
		});
	}

	@SuppressWarnings("resource")
	public void connectToServer() throws IOException {

		// Get the server address from a dialog box.
		String serverAddress = GuiUtil.getIPAdress();
		int port = GuiUtil.getPort();
		// CONNECTEXCEPTION
		Socket socket;
		socket = new Socket(serverAddress, port);

		System.out.format("The capitalization server is running on %s:%d%n", serverAddress, port);

		in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream());

		// Consume the initial welcoming messages from the server
		for (int i = 0; i < 3; i++) {
			messageArea.append(i + "\n");
		}
	}

}
