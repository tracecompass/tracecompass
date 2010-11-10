package org.eclipse.linuxtools.lttng.jni.factory;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTraceVersionException;
import org.eclipse.linuxtools.lttng.jni_v2_3.JniTrace_v2_3;
import org.eclipse.linuxtools.lttng.jni_v2_5.JniTrace_v2_5;
import org.eclipse.linuxtools.lttng.jni_v2_6.JniTrace_v2_6;

/**
 * <b><u>JniTraceFactory</u></b>
 * <p>
 * This class factory is responsible of returning the correct JniTrace implementation from a (valid) trace path.<p>
 * 
 * The different version supported are listed below and the same version string are expected to be returned by JniTraceVersion.<br>
 * Each version need a different Lttv library so each need its liblttvtraceread-X.Y.so installed and available on the system.
 * 
 */
public class JniTraceFactory {
	
	// ***
	// Version string of the supported library version
	// These will be used in the switch below to find the correct version
	// ***
	static final String TraceVersion_v2_3 = "2.3";  //$NON-NLS-1$
	static final String TraceVersion_v2_5 = "2.5";  //$NON-NLS-1$
	static final String TraceVersion_v2_6 = "2.6";  //$NON-NLS-1$
	
	/*
	 * Default constructor is forbidden
	 */
	private JniTraceFactory(){
	}
	
	/**
	 * Factory function : return the correct version of the JniTrace from a given path<p>
	 * NOTE : The correct Lttv library (liblttvtraceread-X.Y.so) need to be installed and accessible otherwise this
	 * 		  function will return an Exception. 
	 * 
	 * If the path is wrong or if the library is not supported (bad version or missing library) an Exception will be throwed. 
	 * 
	 * @param path			Path of the trace we want to open
	 * @param show_debug	Should JniTrace print debug or not?
	 * 
	 * @return				a newly allocated JniTrace of the correct version
	 * 
	 * @throws JniException
	 */
	static public JniTrace getJniTrace(String path, boolean show_debug) throws JniException {
		
		try {
			JniTraceVersion traceVersion = new JniTraceVersion(path);
			
			if ( traceVersion.getVersionAsString().equals(TraceVersion_v2_6) ) {
				return new JniTrace_v2_6(path, show_debug);
			}
			else if ( traceVersion.getVersionAsString().equals(TraceVersion_v2_5) ) {
				return new JniTrace_v2_5(path, show_debug);
			}
			else if ( traceVersion.getVersionAsString().equals(TraceVersion_v2_3) ) {
				return new JniTrace_v2_3(path, show_debug);
			}
			else {
				String errMsg = "\nERROR : Unrecognized/unsupported trace version." + //$NON-NLS-1$
								"\nLibrary reported a trace version " + traceVersion.getVersionAsString() + "." +  //$NON-NLS-1$ //$NON-NLS-2$
								"\nMake sure you installed the Lttv library that support this version (look for liblttvtraceread-" + traceVersion.getVersionAsString() + ".so).\n";  //$NON-NLS-1$ //$NON-NLS-2$
				throw new JniException(errMsg);
			}
		}
		catch (JniTraceVersionException e) {
			String errMsg = "\nERROR : Call to JniTraceVersion() failed." + //$NON-NLS-1$
							"\nThis usually means that the library (liblttvtraceread_loader.so) could not be found." + //$NON-NLS-1$
							"\nMake sure the LTTv library is installed and that your LD_LIBRARY_PATH is set correctly (see help for more details)\n."; //$NON-NLS-1$
			
			throw new JniException(errMsg);
		}
	}
	
}
