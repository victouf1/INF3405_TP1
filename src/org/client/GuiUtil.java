package org.client;

import java.awt.Frame;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CancellationException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

public class GuiUtil {
	public static String getOut(String IP, int port, String action) {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'@'HH:mm:ss");
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(IP);
		builder.append(":");
		builder.append(port);
		builder.append("-");
		builder.append(formater.format(new Date()));
		builder.append("] : ");
		builder.append(action);
		return builder.toString();
	}

	public static String getIPAdress() throws CancellationException {
		Frame frame = new JFrame("Mon Cloud");
		String serverAddress = JOptionPane.showInputDialog(frame, "Entrez l'adresse IP de cloud",
				JOptionPane.QUESTION_MESSAGE);
		while (!isIPv4(serverAddress)) {
			serverAddress = JOptionPane.showInputDialog(frame, "Entrez l'adresse IP de cloud",
					JOptionPane.QUESTION_MESSAGE);
		}
		return serverAddress;

	}

	private static boolean isIPv4(String IP) throws CancellationException {
		if (IP == null) {
			throw new CancellationException();
		}
		String[] numbers = IP.split("\\.");
		if (numbers.length != 4) {
			return false;
		}
		try {
			for (String n : numbers) {
				int i = Integer.parseInt(n);
				if (i < 0 || i > 255) {
					return false;
				}
			}
		} catch (ParseException e) {
			return false;
		}
		return !IP.endsWith(".");
	}
}
