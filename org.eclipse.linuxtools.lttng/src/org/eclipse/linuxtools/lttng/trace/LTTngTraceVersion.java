package org.eclipse.linuxtools.lttng.trace;

import org.eclipse.linuxtools.lttng.LttngException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTraceVersionException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceVersion;

public class LTTngTraceVersion {
	
	private String tracepath = null;
	private JniTraceVersion traceVersion = new JniTraceVersion();
	
	@SuppressWarnings("unused")
	private LTTngTraceVersion() {
		// Default constructor forbidden
	}
	
	public LTTngTraceVersion(String newPath) throws LttngException {
		tracepath = newPath;
		
		// Fill the new traceversion object
		fillJniTraceVersion(tracepath);
	}
	
	private void fillJniTraceVersion(String newTracepath) throws LttngException {
		try {
			traceVersion.readVersionNumber(newTracepath);
		}
		catch (JniTraceVersionException e) {
			throw new LttngException("Could not get trace version!\nReturned error was : " + e.toString() + " (fillJniTraceVersion)");
		}
	}
	
	public String getTraceVersionString() {
		return traceVersion.toString();
	}
	
	public int getTraceMinorVersion() {
		return traceVersion.getMinor();
	}
	
	public int getTraceMajorVersion() {
		return traceVersion.getMajor();
	}
	
	public float getTraceFloatVersion() {
		return traceVersion.getVersionAsFloat();
	}
	
	public boolean isValidLttngTrace() {
		if ( traceVersion.getVersionAsFloat() > 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String getTracepath() {
		return tracepath;
	}
	
	public void setTracepath(String newTracepath) {
		try {
			fillJniTraceVersion(newTracepath);
			tracepath = newTracepath;
		}
		catch (LttngException e) {
			System.out.println("Could not get the trace version from the given path." +
							   "Please check that the given path is a valid LTTng trace. (getTracepath)");
		}
	}
	
	@Override
	public String toString() {
		return "LTTngTraceVersion : [" + getTraceFloatVersion() + "]";
	}
	
}
