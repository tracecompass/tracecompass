/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for gdbtrace.core.trace
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gdbtrace.core.trace.messages"; //$NON-NLS-1$

    /** Executable not set - trace is not associated to an executable.*/
    public static String GdbTrace_ExecutableNotSet;

    /** Failed to initialize trace */
    public static String GdbTrace_FailedToInitializeTrace;

    /** File not found */
    public static String GdbTrace_FileNotFound;

    /** Trace must be a file, not a directory or something else part 1*/
    public static String GdbTrace_GdbTracesMustBeAFile;

    /** Trace is not a file. part 2 */
    public static String GdbTrace_IsNotAFile;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
