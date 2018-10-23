package org.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ExchangesUtil {

	public static void upload(Socket socket, String rootForUpload) throws ClassNotFoundException, IOException {

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
}
