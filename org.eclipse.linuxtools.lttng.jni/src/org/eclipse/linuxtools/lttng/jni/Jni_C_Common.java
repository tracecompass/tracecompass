package org.eclipse.linuxtools.lttng.jni;

import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Constant;

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
 * <b><u>Jni_C_Common</u></b>
 * <p>
 * Common constants and methods that should be shared between JNI objects
 */
public abstract class Jni_C_Common extends Jni_C_Constant {
    
    // Native console printing function
    protected native void ltt_printC(String string_to_print);

    // Load LTTV library (order is important)
    // *** FIXME ***
    // To uncomment as soon as the library will be able to load multiple version at once
	// static {
	//	System.loadLibrary("lttvtraceread_loader");
	//}

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
}