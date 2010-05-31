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

	static Boolean COMPONENT = Boolean.FALSE;
	static Boolean REQUEST   = Boolean.FALSE;
	static Boolean EVENT     = Boolean.FALSE;
	static Boolean EXCEPTION = Boolean.FALSE;

//	static Boolean SIGNALS    = Boolean.FALSE;
//	static Boolean INTERNALS  = Boolean.FALSE;

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
		
		traceKey = Platform.getDebugOption(pluginID + "/error");
		if (traceKey != null) {
			ERROR = (new Boolean(traceKey)).booleanValue();
		}

		traceKey = Platform.getDebugOption(pluginID + "/component");
		if (traceKey != null) {
			COMPONENT = (new Boolean(traceKey)).booleanValue();
		}

		traceKey = Platform.getDebugOption(pluginID + "/request");
		if (traceKey != null) {
			REQUEST = (new Boolean(traceKey)).booleanValue();
		}

		traceKey = Platform.getDebugOption(pluginID + "/event");
		if (traceKey != null) {
			EVENT = (new Boolean(traceKey)).booleanValue();
		}

		traceKey = Platform.getDebugOption(pluginID + "/exception");
		if (traceKey != null) {
			EXCEPTION = (new Boolean(traceKey)).booleanValue();
		}

		// Create trace log file if needed
		if (ERROR || COMPONENT || REQUEST || EVENT || EXCEPTION) {
			fTraceLog = openLogFile("trace.log");
		}

//		String signalsKey = Platform.getDebugOption(pluginID + "/signals");
//		if (signalsKey != null) {
//			SIGNALS = (new Boolean(signalsKey)).booleanValue();
//		}
//
//		String internalsKey = Platform.getDebugOption(pluginID + "/internals");
//		if (internalsKey != null) {
//			INTERNALS = (new Boolean(signalsKey)).booleanValue();
//		}
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
	private static void trace(String message) {
		System.out.println(message);
		try {
			if (fTraceLog != null) {
				fTraceLog.write(message);
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
		String message = ("[ERR] Thread=" + Thread.currentThread().getId() + msg);
		trace(message);
	}

//	public static void traceComponent(String message) {
//		if (COMPONENTS)
//			trace(Thread.currentThread() + ": " + message);
//	}
//
//	public static void traceSignal(TmfSignal signal) {
//		if (SIGNALS)
//			trace(Thread.currentThread() + ": " + signal.toString());
//	}
//
//	public static void traceInternal(String message) {
//		if (INTERNALS)
//			trace(Thread.currentThread() + ": " + message);
//	}

}
