package org.eclipse.linuxtools.lttng.jni.factory;

import org.eclipse.linuxtools.lttng.jni.exception.JniTraceVersionException;

// *** FIXME ***
// THIS IS A SKELETON IMPLEMENTATION
// Nothing here will work as the C library does not support any of those call YET
// ***

public class JniTraceVersion {
	
	protected native void ltt_getTraceVersion(String tracepath);
	
	static {
		System.loadLibrary("lttvtraceread");
	}
	
	private int majorNumber = 0;
	private int minorNumber = 0;
	
	private JniTraceVersion() {
	}
	
	public JniTraceVersion(int newMajor, int newMinor) {
		majorNumber = newMajor;
		minorNumber = newMinor;
	}
	
	public JniTraceVersion(String tracepath) throws JniTraceVersionException {
		try {
			ltt_getTraceVersion(tracepath);
		}
		catch (java.lang.UnsatisfiedLinkError e) {
			throw new JniTraceVersionException("ERROR : Could not get trace version. Is the library missing?");
		}
		catch (Exception e) {
			throw new JniTraceVersionException("ERROR : Call to ltt_getTraceVersion failed.");
		}
	}
	
	public JniTraceVersion(JniTraceVersion oldVersion) {
		majorNumber = oldVersion.majorNumber;
		minorNumber = oldVersion.minorNumber;
	}
	
	public int getMajor() {
		return majorNumber;
	}
	
	public int getMinor() {
		return minorNumber;
	}
	
    private void setTraceVersionFromC(int newMajor, int newMinor) {
		majorNumber = newMajor;
		minorNumber = newMinor;
    }
	
	@Override
	public String toString() {
		return majorNumber + "." + minorNumber;
	}
	
}
