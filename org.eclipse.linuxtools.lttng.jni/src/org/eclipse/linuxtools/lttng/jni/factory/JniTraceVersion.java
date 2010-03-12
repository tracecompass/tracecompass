package org.eclipse.linuxtools.lttng.jni.factory;

import org.eclipse.linuxtools.lttng.jni.exception.JniTraceVersionException;

public class JniTraceVersion {
	
	protected native void ltt_getTraceVersion(String tracepath);
	protected native void ltt_setLibraryPath(String ldLibraryPath);
	
	private int majorNumber = 0;
	private int minorNumber = 0;
	
	private String tracepath = "";

	public JniTraceVersion() {
		// Nothing to do 
	}
	
	public JniTraceVersion(String newTracepath) throws JniTraceVersionException {
		// Read the version number from the trace 
		readVersionNumber(newTracepath);
	}
	
	public JniTraceVersion(JniTraceVersion oldVersion) {
		majorNumber = oldVersion.majorNumber;
		minorNumber = oldVersion.minorNumber;
	}
	
	public void readVersionNumber() throws JniTraceVersionException {
		readVersionNumber(tracepath);
	}
	
	public void readVersionNumber(String tracepath) throws JniTraceVersionException {
		
		// Verify that the tracepath isn't obliviously wrong (null or empty)
		if ( (tracepath == null) || (tracepath.equals("") ) ) {
			throw new JniTraceVersionException("ERROR : Tracepath is null or empty! (readVersionNumber)");
		}
		
		try {
			// Load the C library here. 
			// If LD_LIBRARY_PATH is not set correctly this will raise a java.lang.UnsatisfiedLinkError
			System.loadLibrary("lttvtraceread_loader");
			
			// The user's LD_LIBRARY_PATH environnement variable doesn't seem to be get passed to the C
			// We will force C to load it here
			String ldLibraryPath = "LD_LIBRARY_PATH:" + System.getenv("LD_LIBRARY_PATH");
			ltt_setLibraryPath(ldLibraryPath);
			
			// Assuming the C library loaded correctly, call the JNI here.
			ltt_getTraceVersion(tracepath);
		}
		catch (java.lang.UnsatisfiedLinkError e) {
			throw new JniTraceVersionException("ERROR : Could not get trace version. Is the library missing?\n" +
											   "        Make sure you setted either \"java.library.path\" or \"LD_LIBRARY_PATH\" (readVersionNumber)");
		}
		catch (Exception e) {
			throw new JniTraceVersionException("ERROR : Call to ltt_getTraceVersion failed. (readVersionNumber)");
		}
	}
	
	
	public int getMajor() {
		return majorNumber;
	}
	
	public int getMinor() {
		return minorNumber;
	}
	
	public float getVersionAsFloat()  {
		return ((float)majorNumber + ((float)minorNumber)/10);
	}
	
	public String getTracepath() {
		return tracepath;
	}
	
	public void setTracepath(String newtracepath) {
		tracepath = newtracepath;
	}
	
	
    @SuppressWarnings("unused")
	private void setTraceVersionFromC(int newMajor, int newMinor) {
		majorNumber = newMajor;
		minorNumber = newMinor;
    }
	
	@Override
	public String toString() {
		return majorNumber + "." + minorNumber;
	}
	
}
