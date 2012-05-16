package org.eclipse.linuxtools.internal.tmf.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

@SuppressWarnings("nls")
public class TmfUiTracer {

	private static String pluginID = TmfUiPlugin.PLUGIN_ID;

	static Boolean ERROR     = Boolean.FALSE;
	static Boolean WARNING   = Boolean.FALSE;
	static Boolean INFO      = Boolean.FALSE;

	static Boolean INDEX         = Boolean.FALSE;
	static Boolean DISPLAY       = Boolean.FALSE;
	static Boolean SORTING       = Boolean.FALSE;

	private static String LOGNAME = "traceUI.log";
	private static BufferedWriter fTraceLog = null;

	private static BufferedWriter openLogFile(String filename) {
		BufferedWriter outfile = null;
		try {
			outfile = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
		    TmfUiPlugin.getDefault().logError("Error creating log file " + LOGNAME, e); //$NON-NLS-1$
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

		traceKey = Platform.getDebugOption(pluginID + "/updateindex");
		if (traceKey != null) {
		    INDEX = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= INDEX;
		}

		traceKey = Platform.getDebugOption(pluginID + "/display");
		if (traceKey != null) {
		    DISPLAY = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= DISPLAY;
		}

		traceKey = Platform.getDebugOption(pluginID + "/sorting");
		if (traceKey != null) {
		    SORTING = (Boolean.valueOf(traceKey)).booleanValue();
			isTracing |= SORTING;
		}

		// Create trace log file if needed
		if (isTracing) {
			fTraceLog = openLogFile(LOGNAME);
		}
	}

	public static void stop() {
		if (fTraceLog == null)
			return;

		try {
			fTraceLog.close();
			fTraceLog = null;
		} catch (IOException e) {
	          TmfUiPlugin.getDefault().logError("Error closing log file " + LOGNAME, e); //$NON-NLS-1$
		}
	}

	// Predicates
	public static boolean isErrorTraced() {
		return ERROR;
	}

	public static boolean isIndexTraced() {
		return INDEX;
	}
	
	public static boolean isDisplayTraced() {
		return DISPLAY;
	}
	
	public static boolean isSortingTraced() {
		return SORTING;
	}

	// Tracers
	public static void trace(String msg) {
		long currentTime = System.currentTimeMillis();
		StringBuilder message = new StringBuilder("[");
		message.append(currentTime / 1000);
		message.append(".");
		message.append(String.format("%1$03d", currentTime % 1000));
		message.append("] ");
		message.append(msg);

		if (fTraceLog != null) {
			try {
				fTraceLog.write(message.toString());
				fTraceLog.newLine();
				fTraceLog.flush();
			} catch (IOException e) {
		         TmfUiPlugin.getDefault().logError("Error writing to log file " + LOGNAME, e); //$NON-NLS-1$
			}
		}
	}

	public static void traceIndex(String msg) {
	    String message = ("[INDEX] " + msg);
	    trace(message);
	}
	
	public static void traceDisplay(String msg) {
		String message = ("[DISPLAY]" + msg);
		trace(message);
	}

	public static void traceSorting(String msg) {
		String message = ("[SORT] " + msg);
		trace(message);
	}

	public static void traceError(String msg) {
		String message = ("[ERR] Thread=" + Thread.currentThread().getId() + " " + msg);
		trace(message);
	}

	public static void traceWarning(String msg) {
	    String message = ("[WARN] Thread=" + Thread.currentThread().getId() + " " + msg);
	    trace(message);
	}
	
	public static void traceInfo(String msg) {
		String message = ("[INF] Thread=" + Thread.currentThread().getId() + " " + msg);
		trace(message);
	}
	
	

}
