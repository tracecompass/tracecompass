package org.eclipse.linuxtools.lttng.tests;

import org.eclipse.linuxtools.lttng.TraceDebug;

public class ActivateDebugging {

	public static void activate() {
		TraceDebug.setDEBUG(true);
	}
	
	public static void deactivate() {
		TraceDebug.setDEBUG(false);
	}
}
