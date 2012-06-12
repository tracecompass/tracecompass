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

import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniTraceVersionException;
import org.eclipse.linuxtools.internal.lttng.jni_v2_3.JniTrace_v2_3;
import org.eclipse.linuxtools.internal.lttng.jni_v2_5.JniTrace_v2_5;
import org.eclipse.linuxtools.internal.lttng.jni_v2_6.JniTrace_v2_6;
import org.eclipse.linuxtools.lttng.jni.JniTrace;

/**
 * This class factory is responsible of returning the correct JniTrace implementation from a (valid) trace path.<p>
 *
 * The different version supported are listed below and the same version string are expected to be returned by JniTraceVersion.<br>
 * Each version need a different Lttv library so each need its liblttvtraceread-X.Y.so installed and available on the system.
 *
 * @version 0.1
 * @author William Bourque
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
     * Factory function : return the correct version of the JniTrace from a
     * given path
     * <p>
     * NOTE : The correct Lttv library (liblttvtraceread-X.Y.so) need to be
     * installed and accessible otherwise this function will return an
     * Exception.
     *
     * If the path is wrong or if the library is not supported (bad version or
     * missing library) an Exception will be throwed.
     *
     * @param path
     *            Path of the trace we want to open
     * @param traceLibPath
     *            Directory to the trace libraries
     * @param show_debug
     *            Should JniTrace print debug or not?
     *
     * @return a newly allocated JniTrace of the correct version
     *
     * @throws JniException
     *             If the JNI call fails
     */
	static public JniTrace getJniTrace(String path, String traceLibPath, boolean show_debug) throws JniException {

        try {
            JniTraceVersion traceVersion = new JniTraceVersion(path, traceLibPath);
            JniTrace trace = null;
            if (traceVersion.getVersionAsString().equals(TraceVersion_v2_6)) {
                trace = new JniTrace_v2_6(path, show_debug);
            } else if (traceVersion.getVersionAsString().equals(TraceVersion_v2_5)) {
                trace = new JniTrace_v2_5(path, show_debug);
            } else if (traceVersion.getVersionAsString().equals(TraceVersion_v2_3)) {
                trace = new JniTrace_v2_3(path, show_debug);
            }
            if (trace != null) {
                if (traceLibPath != null) {
                    trace.setTraceLibPath(traceLibPath);
                }
                trace.openTrace(path);
                return trace;
            }
            String errMsg = "Unrecognized/unsupported trace version\n\n" //$NON-NLS-1$
                    + "Library reported trace version " + traceVersion.getVersionAsString() + "\n\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "Make sure that you installed the corresponding parsing library (liblttvtraceread-" + traceVersion.getVersionAsString() + ".so) " //$NON-NLS-1$ //$NON-NLS-2$
                    + "and that it can be found from either your LD_LIBRARY_PATH or the Trace Library Path (in LTTng project properties)\n\n" //$NON-NLS-1$
                    + "Refer to the LTTng User Guide for more information"; //$NON-NLS-1$
            throw new JniException(errMsg);

        } catch (JniTraceVersionException e) {
            String errMsg = "Couldn't obtain the trace version\n\n" //$NON-NLS-1$
                    + "This usually means that the library loader (liblttvtraceread_loader.so) could not be found.\n\n" //$NON-NLS-1$
                    + "Make sure you installed the parsing library and that your LD_LIBRARY_PATH or Trace Library Path is set correctly\n\n" //$NON-NLS-1$
                    + "Refer to the LTTng User Guide for more information"; //$NON-NLS-1$

            throw new JniException(errMsg);
        }
    }

}
