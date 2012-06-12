package org.eclipse.linuxtools.lttng.jni;

import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Constant;

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


/**
 * Common constants and methods that should be shared between JNI objects.
 *
 * <b>NOTE</b><p>
 * This class is ABSTRACT, and will be extended by each LTTng structure (Trac, Tracefile, Event, ...)
 *
 * @version 0.1
 * @author William Bourque
 */
public abstract class Jni_C_Common extends Jni_C_Constant
{
    // Native console printing function
    protected native void ltt_printC(int libId, String string_to_print);

    /**
     * Java-side console printing function.
     * <p>
     *
     * Call the C printing function to make sure all printing happen on the same
     * side.
     *
     * @param libId
     *            The ID of the trace-reading library
     * @param msg
     *            The string to print in C.
     */
    public void printC(int libId, String msg) {
        // Need to escape "%" for C printf
        msg = msg.replaceAll("%", "%%"); //$NON-NLS-1$ //$NON-NLS-2$
        ltt_printC(libId, msg);
    }

    /**
     * Java-side console printing function that add carriage return.
     * <p>
     *
     * Call the C printing function to make sure all printing happen on the same
     * side.
     *
     * @param libId
     *            The ID of the trace-reading library
     * @param msg
     *            The string to print in C.
     */
    public void printlnC(int libId, String msg) {
        printC(libId, msg + "\n"); //$NON-NLS-1$
    }

}
