package org.eclipse.linuxtools.tmf;

import org.eclipse.core.runtime.Platform;

public class Tracer {

	static Boolean TRACE = Boolean.FALSE;

	private static String pluginID = TmfCorePlugin.PLUGIN_ID;

	public static void init() {
		String traceKey = Platform.getDebugOption(pluginID + "/trace");

		if (traceKey != null) {
			TRACE = (new Boolean(traceKey)).booleanValue();
		}
	}

	public static void trace(String message) {
		if (TRACE) {
			System.out.println(message);
		}
	}
}
