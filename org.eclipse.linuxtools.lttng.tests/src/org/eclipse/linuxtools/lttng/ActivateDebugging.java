package org.eclipse.linuxtools.lttng;

public class ActivateDebugging {

	public static void activate() {
		TraceDebug.DEBUG = true;
	}
	
	public static void deactivate() {
		TraceDebug.DEBUG = false;
	}
}
