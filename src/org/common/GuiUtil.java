package org.common;

import java.awt.Frame;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CancellationException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * classe pour les affichages
 *
 */
public class GuiUtil {
	/**
	 * affichage des informations dans la console du serveur
	 */
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

	/**
	 * fenêtre demandant l'adresse IP
	 */
	public static String getIPAdress() throws CancellationException {
		Frame frame = new JFrame("Mon Cloud");
		String serverAddress = JOptionPane.showInputDialog(frame, "Entrez l'adresse IP du cloud", "Mon Cloud",
				JOptionPane.QUESTION_MESSAGE);
		while (!isIPv4(serverAddress)) {
			JOptionPane.showMessageDialog(frame, "L'adresse IP n'a pas le bon format", "Erreur",
					JOptionPane.ERROR_MESSAGE);
			serverAddress = JOptionPane.showInputDialog(frame, "Entrez l'adresse IP du cloud", "Mon Cloud",
					JOptionPane.QUESTION_MESSAGE);
		}
		return serverAddress;

	}

	/**
	 * test si l'IP est bien une IPv 4
	 */
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
		} catch (NumberFormatException e) {
			return false;
		}
		return !IP.endsWith(".");
	}

	/**
	 * fenêtre demandant le port
	 */
	public static int getPort() throws CancellationException {
		Frame frame = new JFrame("Mon Cloud");
		String serverAddress = JOptionPane.showInputDialog(frame, "Entrez le port du cloud", "Mon Cloud",
				JOptionPane.QUESTION_MESSAGE);

		while (!isGoodPort(serverAddress)) {
			JOptionPane.showMessageDialog(frame, "Le port doit être entre 5000 et 5050", "Erreur",
					JOptionPane.ERROR_MESSAGE);
			serverAddress = JOptionPane.showInputDialog(frame, "Entrez le port du cloud", "Mon Cloud",
					JOptionPane.QUESTION_MESSAGE);
		}
		return Integer.parseInt(serverAddress);
	}

	/**
	 * test si le port est bien entre 5000 et 5050
	 */
	private static boolean isGoodPort(String port) {
		if (port == null) {
			throw new CancellationException();
		}
		try {
			int intPort = Integer.parseInt(port);
			return intPort >= 5000 && intPort <= 5050;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
