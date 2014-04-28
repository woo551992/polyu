package com.schedule.util;

import java.util.HashMap;

public class Log {
	
	private static HashMap<String, Boolean> sTagEnabledMap = new HashMap<String, Boolean>();

	public static boolean isDebugEnabled(String tag) {
		return Boolean.TRUE.equals(sTagEnabledMap.get(tag));
	}
	
	public static void setDebugEnabled(String tag, boolean enabled) {
		sTagEnabledMap.put(tag, enabled);
	}
	
	public static void d(String tag, Object message) {
		if (!isDebugEnabled(tag))
			return;
		System.out.println("DEBUG\t" + fitToSize(tag, 20) + message);
	}
	
	private static String fitToSize(String string, int size) {
		StringBuilder sb = new StringBuilder(string);
		for (int i = string.length(); i < size; i++) {
			sb.append(" ");
		}
		sb.setLength(size);
		return sb.toString();
	}
	
}
