package com.schedule.util;

public class Preconditions {

	public static void checkArgument(boolean expression) {
		if (!expression)
			throw new IllegalArgumentException();
	}
	
	public static void checkArgument(boolean expression, String message) {
		if (!expression)
			throw new IllegalArgumentException(message);
	}

	public static void checkState(boolean expression) {
		if (!expression)
			throw new IllegalStateException();
	}

	public static void checkState(boolean expression, String message) {
		if (!expression)
			throw new IllegalStateException(message);
	}
	
	public static <T> T checkNotNull(T ref) {
		if (ref == null)
			throw new NullPointerException();
		return ref;
	}
	
	public static <T> T checkNotNull(T ref, String message) {
		if (ref == null)
			throw new NullPointerException(message);
		return ref;
	}
	
}
