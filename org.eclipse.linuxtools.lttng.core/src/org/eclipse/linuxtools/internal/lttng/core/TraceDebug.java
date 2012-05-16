package org.eclipse.linuxtools.internal.lttng.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

@SuppressWarnings("nls")
public class TraceDebug {
	static boolean DEBUG = false;
	static boolean INFO = false;
	static boolean WARN = false;

	private static Plugin plugin = Activator.getDefault();
	private static String pluginID = Activator.PLUGIN_ID;
	private static SimpleDateFormat stimeformat = new SimpleDateFormat(
			"HH:mm:ss:SSS");

	public static void init() {
		// Update JniTrace configuration options
		String debugTrace = Platform.getDebugOption(pluginID + "/debug");
		String infoTrace = Platform.getDebugOption(pluginID + "/info");
		String warnTrace = Platform.getDebugOption(pluginID + "/warn");

		if (debugTrace != null) {
			DEBUG = (Boolean.valueOf(debugTrace)).booleanValue();
		}

		if (infoTrace != null) {
			INFO = (Boolean.valueOf(infoTrace)).booleanValue();
		}

		if (warnTrace != null) {
			WARN = (Boolean.valueOf(warnTrace)).booleanValue();
		}
	}

	public static void info(String message) {
		if (INFO) {
			ILog logger = plugin.getLog();
			logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
					IStatus.OK, message, null));
		}
	}

	public static void warn(String message) {
		if (WARN) {
			ILog logger = plugin.getLog();
			logger.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
					IStatus.WARNING, message, null));
		}
	}

	public static void debug(String message) {
		if (DEBUG) {
			String location = getCallingLocation();
			System.out.println(location + "\n\t-> " + message);

		}
	}

	public static void debug(String message, int additionalStackLines) {
		if (DEBUG) {
			String location = getCallingLocation(additionalStackLines);
			System.out.println(location + "\n\t-> " + message);
		}
	}

	public static void throwException(String message) {
		if (DEBUG) {
			try {
				triggerException(message);
			} catch (Exception e) {
			    Activator.getDefault().logError("Unexpected Error", e);  //$NON-NLS-1$
			}
		}
	}

	private static void triggerException(String message) throws Exception {
		throw new Exception(message);
	}

	private static String getCallingLocation() {
		StringBuilder sb = new StringBuilder();
		sb.append(trace(Thread.currentThread().getStackTrace(), 4));
		sb.append("\n" + trace(Thread.currentThread().getStackTrace(), 3));
		return sb.toString();
	}

	private static String getCallingLocation(int numOfStackLines) {
		int stackCalledFromIdx = 3;
		int earliestRequested = numOfStackLines > 0 ? stackCalledFromIdx
				+ numOfStackLines : stackCalledFromIdx;
		StringBuilder sb = new StringBuilder();
		int max = Thread.currentThread().getStackTrace().length - 1;
		earliestRequested = earliestRequested > max ? max : earliestRequested;
		for (int i = earliestRequested; i >= stackCalledFromIdx; i--) {
			sb.append(trace(Thread.currentThread().getStackTrace(), i) + "\n");
		}
		return sb.toString();
	}

	private static String trace(StackTraceElement e[], int level) {
		if (e != null) {
			level = level >= e.length ? e.length - 1 : level;
			StackTraceElement s = e[level];
			if (s != null) {
				String simpleClassName = s.getClassName();
				String[] clsNameSegs = simpleClassName.split("\\.");
				if (clsNameSegs.length > 0)
					simpleClassName = clsNameSegs[clsNameSegs.length - 1];
				return stimeformat.format(new Date()) + " " + simpleClassName
						+ "." + s.getLineNumber() + "." + s.getMethodName();
			}
		}
		return null;
	}

	public static boolean setDEBUG(boolean newValue) {
		boolean oldValue = DEBUG;
		DEBUG = newValue;
		return oldValue;
	}

	public static boolean setINFO(boolean newValue) {
		boolean oldValue = INFO;
		INFO = newValue;
		return oldValue;
	}

	public static boolean setWARN(boolean newValue) {
		boolean oldValue = WARN;
		WARN = newValue;
		return oldValue;
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public static boolean isINFO() {
		return INFO;
	}

	public static boolean isWARN() {
		return WARN;
	}
}
