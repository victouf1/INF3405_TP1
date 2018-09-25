package org.client;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
