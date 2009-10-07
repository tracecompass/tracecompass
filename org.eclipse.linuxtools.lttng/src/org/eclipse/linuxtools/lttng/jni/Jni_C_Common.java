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

package org.eclipse.linuxtools.lttng.jni;

/**
 * <b><u>Jni_C_Common</u></b>
 * <p>
 * Common constants and methods that should be shared between JNI objects
 */
public abstract class Jni_C_Common {
    
    // Needed for native types
    public static final int NULL = 0;

    // C errno correspondance. Used to interpret LTT return value
    public static final int EOK    =  0;
    public static final int EPERM  =  1;
    public static final int ERANGE = 34;

    // Timestamps are in nanoseconds, this const ease up the math
    public static final long NANO = 1000000000;

    // Native console printing function
    private native void ltt_printC(String string_to_print);

    // Load LTTV library (order is important)
    static {
        System.loadLibrary("lttvtraceread");
    }

    /**
     * Java-side console printing function.<p>
     * 
     * Call the C printing function to make sure all printing happen on the same side.
     * 
     * @param msg   The string to print in C.
     */
    public void printC(String msg) {
        // Need to escape "%" for C printf 
        msg = msg.replaceAll("%", "%%");
        ltt_printC(msg);
    }

    /**
     * Java-side console printing function that add carriage return. <p>
     * 
     * Call the C printing function to make sure all printing happen on the same side.
     * 
     * @param msg   The string to print in C.
     */
    public void printlnC(String msg) {
        printC(msg + "\n");
    }

    /**
     * "Alternate" .toString()<p>
     * 
     * Simulates the way Java Object implements "toString()"
     * 
     * @return The Java hashed UID of the object (i.e. : NAME@HASH)
     */
    public String getReferenceToString() {
        return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode());
    }
}