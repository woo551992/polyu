package com.schedule.util;

import static com.schedule.util.Preconditions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

public class Log {
	
	private static HashMap<String, Boolean> sTagEnabledMap = new HashMap<String, Boolean>();
	private static final PrintStream DEFAULT_PRINT = System.out;
	private static PrintStream sOut = DEFAULT_PRINT;

	public static boolean isDebugEnabled(String tag) {
		return Boolean.TRUE.equals(sTagEnabledMap.get(tag));
	}
	
	public static void setDebugEnabled(String tag, boolean enabled) {
		sTagEnabledMap.put(tag, enabled);
	}
	
	public static void d(String tag, Object message) {
		if (!isDebugEnabled(tag))
			return;
		sOut.println(fitToSize(tag, 20) + message);
	}
	
	private static String fitToSize(String string, int size) {
		StringBuilder sb = new StringBuilder(string);
		for (int i = string.length(); i < size; i++) {
			sb.append(" ");
		}
		sb.setLength(size);
		return sb.toString();
	}
	
	public static void startCustomPrint() {
		sOut = new CustomPrintStream();
	}
	
	/** End the custom print stream which created by {@link #startCustomPrint()}, returns the printing string. */
	public static String endCustomPrint() {
		checkState(sOut instanceof CustomPrintStream, "startCustomPrint not called");
		CustomPrintStream cOut = (CustomPrintStream) sOut;
		sOut = DEFAULT_PRINT;
		return cOut.toString();
	}
	
	private static class CustomPrintStream extends PrintStream {

		public CustomPrintStream() {
			super(new ByteArrayOutputStream());
		}
		
		@Override
		public String toString() {
			return out.toString();
		}
		
	}
	
}
