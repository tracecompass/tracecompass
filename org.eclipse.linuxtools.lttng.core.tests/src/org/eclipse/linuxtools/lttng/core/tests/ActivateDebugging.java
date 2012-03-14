package org.eclipse.linuxtools.lttng.core.tests;

import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;

public class ActivateDebugging {

	public static void activate() {
		TraceDebug.setDEBUG(true);
	}
	
	public static void deactivate() {
		TraceDebug.setDEBUG(false);
	}
}

