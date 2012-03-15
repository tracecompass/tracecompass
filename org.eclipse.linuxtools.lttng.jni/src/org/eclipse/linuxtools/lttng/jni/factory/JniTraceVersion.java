package org.eclipse.linuxtools.lttng.jni.factory;
/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson, MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *   Yufen Kuo       (ykuo@mvista.com) - add support to allow user specify trace library path
 *******************************************************************************/

import java.io.File;

import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniTraceVersionException;

/**
 * <b><u>JniTraceVersion</u></b>
 * <p>
 * This class is responsible of returning the correct version number of a trace at a certain given path.<p>
 * 
 * The class will call the C library to get the correct version number from the trace.<p>
 * 
 * Lttv library loader (liblttvtraceread_loader.so) and the default Lttv library (liblttvtraceread.so) must be installed on system and available to java.
 * 
 */
public class JniTraceVersion {
	
	private static final String LTTVTRACEREAD_LOADER_LIBNAME = "lttvtraceread_loader"; //$NON-NLS-1$

	// Native access functions
	protected native void ltt_getTraceVersion(String tracepath);
	
	// Variables to store version number
	private int majorNumber = 0;
	private int minorNumber = 0;
	
	// To store the given tracepath
	private String tracepath = ""; //$NON-NLS-1$
	// To store the given trace lib path
	private String traceLibPath = ""; //$NON-NLS-1$
	
	// Was the trace read already?
	private boolean wasTraceRead = false;
	
	/**
	 * Default constructor.<p>
	 * 
	 * Do nothing, readVersionFromTrace(path) will need to be called by the user
	 */
	public JniTraceVersion() {
		// Nothing to do 
	}
	
	/**
     * Constructor that takes a tracepath parameter.<p>
     * 
     * This constructor read the version number from the trace, so it might throw.
     * 
     * @param newTracepath 		The <b>directory</b> of the trace to read.
     * @param traceLibPath 		The <b>directory</b> of the trace libraries.
     * 
     * @exception JniException	If the library can not be loaded,if the path is wrong or if something go wrong during the read.
     */
	public JniTraceVersion(String newTracepath, String traceLibPath) throws JniTraceVersionException {
		// Read the version number from the trace 
		readVersionFromTrace(newTracepath, traceLibPath);
	}
	
	/**
     * Copy constructor.
     * 
     * @param oldVersion  A reference to the JniTraceVersion to copy.           
     */
	public JniTraceVersion(JniTraceVersion oldVersion) {
		majorNumber = oldVersion.majorNumber;
		minorNumber = oldVersion.minorNumber;
	}
	
	/*
	 * Read the version from the (already set) tracepath.<p>
	 * 
	 * This version is used internally and will silently dismiss any exceptions.
	 * 
	 */
	private void readVersionNumberNofail() {
		try {
			readVersionFromTrace(tracepath, traceLibPath);
		}
		catch(JniTraceVersionException e) { 
			// Yes, we do ignore exception.
		}
	}
	
	/**
	 * Read the version from the (already set) tracepath.<p>
	 * 
	 * This function throw if the library can not be loaded, if the path is wrong or if something go wrong during the read.
	 * 
	 */
	public void readVersionNumber() throws JniTraceVersionException {
		readVersionFromTrace(tracepath, traceLibPath);
	}
	
	/**
	 * Read the version from a given tracepath.<p>
	 * MajorVersion and MinorVersion should be set after a successful execution of this function.<br>
	 * 
	 * This function throw if the library can not be loaded,if the path is wrong or if something go wrong during the read.
	 * 
	 */
	public void readVersionFromTrace(String newTracepath, String newTraceLibPath) throws JniTraceVersionException {
		
		// Verify that the tracepath isn't obliviously wrong (null or empty)
		if ( (newTracepath == null) || (newTracepath.equals("") ) ) { //$NON-NLS-1$
			throw new JniTraceVersionException("ERROR : Tracepath is null or empty! (readVersionNumber)"); //$NON-NLS-1$
		}
		else {
			// Otherwise set the path in case it was changed
			tracepath = newTracepath;
		}
		traceLibPath = newTraceLibPath;
		
		try {
			if (newTraceLibPath == null || newTraceLibPath.isEmpty()) {
				// Load the C library here.
				// If LD_LIBRARY_PATH is not set correctly this will raise a java.lang.UnsatisfiedLinkError
				System.loadLibrary(LTTVTRACEREAD_LOADER_LIBNAME); 
			} else {
				File loaderLib = new File(newTraceLibPath, System.mapLibraryName(LTTVTRACEREAD_LOADER_LIBNAME));
                System.load(loaderLib.getCanonicalPath());
			}
			// Assuming the C library loaded correctly, call the JNI here.
			ltt_getTraceVersion(tracepath);

			// We can now assume that the trace was read
			wasTraceRead = true;
		}
		// The library was unable to load -> Lttv not installed or bad version of it? 
		catch (java.lang.UnsatisfiedLinkError e) {
			throw new JniTraceVersionException("\nERROR : Could not get trace version. Is the library missing?" + //$NON-NLS-1$
											   "\nMake sure your \"LD_LIBRARY_PATH\" is setted correctly (readVersionNumber)\n"); //$NON-NLS-1$
		}
		// Something else failed -> Possibly a bad tracepath was given 
		catch (Exception e) {
			throw new JniTraceVersionException("\nERROR : Call to ltt_getTraceVersion failed. (readVersionNumber)\n"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Get major version number of the trace.<p>
	 * Note : readVersionFromTrace() will be called if it wasn't done but exception will be silently ignored.
	 * 
	 * @return major version
	 */
	public int getMajor() {
		if ( wasTraceRead == false ) {
			readVersionNumberNofail();
		}
		
		return majorNumber;
	}
	
	/**
	 * Get minor version number of the trace.<p>
	 * Note : readVersionFromTrace() will be called if it wasn't done but exception will be silently ignored.
	 * 
	 * @return minor version
	 */
	public int getMinor() {
		if ( wasTraceRead == false ) {
			readVersionNumberNofail();
		}
		
		return minorNumber;
	}
	
	/**
	 * Get full version number of the trace.<p>
	 * Note : readVersionFromTrace() will be called if it wasn't done but exception will be silently ignored.
	 * 
	 * @return Full Version as float
	 */
	public float getVersionAsFloat()  {
		if ( wasTraceRead == false ) {
			readVersionNumberNofail();
		}
		
		return ((float)majorNumber + ((float)minorNumber)/10);
	}
	
	/**
	 * Get full version number of the trace.<p>
	 * Note : readVersionFromTrace() will be called if it wasn't done but exception will be silently ignored.
	 * 
	 * @return Full Version as string
	 */
	public String getVersionAsString()  {
		if ( wasTraceRead == false ) {
			readVersionNumberNofail();
		}
		
		return majorNumber + "." + minorNumber; //$NON-NLS-1$
	}
	
	/**
	 * Get for the current tracepath
	 * 
	 * @return The tracepath was are currently using.
	 */
	public String getTracepath() {
		return tracepath;
	}
	
	/**
	 * Set for the tracepath.<p>
	 * NOTE  : Changing this will reset the version number currently loaded.
	 * NOTE2 : readVersionFromTrace() will be called but exception will be silently ignored.
	 * 
	 * @param newtracepath The net tracepath
	 */
	public void setTracepath(String newtracepath) {
		majorNumber = 0;
		minorNumber = 0;
		wasTraceRead = false;
		tracepath = newtracepath;
		
		// Call the read function. This will fill up all the number if it goes well.
		readVersionNumberNofail();
	}
	
	/*
	 * This function is be called from the C side to assign the version number the Java variable.
	 */
	private void setTraceVersionFromC(int newMajor, int newMinor) {
		majorNumber = newMajor;
		minorNumber = newMinor;
    }
	
    
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "JniTraceVersion [" + majorNumber + "." + minorNumber + "]";
	}
	
}
