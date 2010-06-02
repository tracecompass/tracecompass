package org.eclipse.linuxtools.tmf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.event.TmfData;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;

public class Tracer {

	private static String pluginID = TmfCorePlugin.PLUGIN_ID;

	static Boolean ERROR     = Boolean.FALSE;
	static Boolean WARNING   = Boolean.FALSE;
	static Boolean INFO      = Boolean.FALSE;

	static Boolean COMPONENT = Boolean.FALSE;
	static Boolean REQUEST   = Boolean.FALSE;
	static Boolean SIGNAL    = Boolean.FALSE;
	static Boolean EVENT     = Boolean.FALSE;
	static Boolean EXCEPTION = Boolean.FALSE;

	private static BufferedWriter fTraceLog = null;

	private static BufferedWriter openLogFile(String filename) {
		BufferedWriter outfile = null;
		try {
			outfile = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outfile;
	}

	public static void init() {

		String traceKey;
		boolean isTracing = false;
		
		traceKey = Platform.getDebugOption(pluginID + "/error");
		if (traceKey != null) {
			ERROR = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= ERROR;
		}

		traceKey = Platform.getDebugOption(pluginID + "/warning");
		if (traceKey != null) {
			WARNING = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= WARNING;
		}

		traceKey = Platform.getDebugOption(pluginID + "/info");
		if (traceKey != null) {
			INFO = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= INFO;
		}

		traceKey = Platform.getDebugOption(pluginID + "/component");
		if (traceKey != null) {
			COMPONENT = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= COMPONENT;
		}

		traceKey = Platform.getDebugOption(pluginID + "/request");
		if (traceKey != null) {
			REQUEST = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= REQUEST;
		}

		traceKey = Platform.getDebugOption(pluginID + "/signal");
		if (traceKey != null) {
			SIGNAL = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= SIGNAL;
		}

		traceKey = Platform.getDebugOption(pluginID + "/event");
		if (traceKey != null) {
			EVENT = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= EVENT;
		}

		traceKey = Platform.getDebugOption(pluginID + "/exception");
		if (traceKey != null) {
			EXCEPTION = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= EXCEPTION;
		}

		// Create trace log file if needed
		if (isTracing) {
			fTraceLog = openLogFile("trace.log");
		}
	}

	// Predicates
	public static boolean isErrorTraced() {
		return ERROR;
	}

	public static boolean isComponentTraced() {
		return COMPONENT;
	}
	
	public static boolean isRequestTraced() {
		return REQUEST;
	}
	
	public static boolean isEventTraced() {
		return EVENT;
	}
	
	public static boolean isExceptionTraced() {
		return EXCEPTION;
	}

	// Tracers
	public static void trace(String msg) {
		long currentTime = System.currentTimeMillis();
		StringBuilder message = new StringBuilder("[");
		message.append(currentTime / 1000);
		message.append(".");
		message.append(currentTime % 1000);
		message.append("] ");
		message.append(msg);
		System.out.println(message);
		try {
			if (fTraceLog != null) {
				fTraceLog.write(message.toString());
				fTraceLog.newLine();
				fTraceLog.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void traceComponent(ITmfComponent component, String msg) {
		String message = ("[CMP] Thread=" + Thread.currentThread().getId() + " Cmp=" + component.getName() + " " + msg);
		trace(message);
	}

	public static void traceRequest(ITmfDataRequest<?> request, String msg) {
		String message = ("[REQ] Thread=" + Thread.currentThread().getId() + " Req=" + request.getRequestId() + ", Type=" + request.getDataType().getSimpleName() + " " + msg);
		trace(message);
	}

	public static void traceEvent(ITmfDataProvider<?> provider, ITmfDataRequest<?> request, TmfData data) {
		String message = ("[EVT] Provider=" + provider.toString() + ", Req=" + request.getRequestId() + ", Event=" + data.toString());
		trace(message);
	}

	public static void traceException(Exception e) {
	}

	public static void traceError(String msg) {
		String message = ("[ERR] Thread=" + Thread.currentThread().getId() + " " + msg);
		trace(message);
	}

}
