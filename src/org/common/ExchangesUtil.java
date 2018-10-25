package org.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Classes pour impl�menter le download et upload
 */
public class ExchangesUtil {

	/**
	 * permet de recevoir un InputStream et d'y creer le fichier
	 */
	public static void download(Socket socket, String rootForUpload) throws ClassNotFoundException, IOException {

		// pour r�cup�rer du client le fichier
		ObjectInputStream ois = null;
		ois = new ObjectInputStream(socket.getInputStream());

		// pour �crire le fichier
		FileOutputStream fos = null;
		// on �crit 100 bytes par 100 bytes
		byte[] buffer = new byte[100];
		Object o;

		o = ois.readObject();

		if (o instanceof String) {
			fos = new FileOutputStream(rootForUpload + "\\" + o.toString());
		}

		Integer bytesRead = 0;
		do {
			// le nombre de bytes dans le tableau
			o = ois.readObject();
			bytesRead = (Integer) o;
			// le tableau re�u
			o = ois.readObject();
			buffer = (byte[]) o;
			// on les �crits dans le fichier
			fos.write(buffer, 0, bytesRead);
		} while (bytesRead == 100);

		fos.close();
	}

	/**
	 * permet d'envoyer un fichier
	 */
	public static void upload(File file, Socket socket) throws IOException {
		ObjectOutputStream oos;
		// on envoie
		oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(file.getName());

		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[100];
		Integer bytesRead = 0;
		// on lit le fichier et on l'�crit dans le outputStream en m�me temps pour
		// l'envoie
		while ((bytesRead = fis.read(buffer)) > 0) {
			oos.writeObject(bytesRead);
			oos.writeObject(Arrays.copyOf(buffer, buffer.length));
		}

		fis.close();
	}
}
