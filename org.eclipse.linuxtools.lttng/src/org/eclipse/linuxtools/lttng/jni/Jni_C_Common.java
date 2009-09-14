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
 * Common constante and methods that should be shared between JNI objects
 */
public abstract class Jni_C_Common {

    // Needed for native types
    public static final int NULL = 0;

    // C errno correspondance. Used to interpret LTT return value
    public static final int EOK    =  0;
    public static final int EPERM  =  1;
    public static final int ERANGE = 34;

    // Timestamps are in nanoseconds
    public static final long NANO = 1000000000;

    // Native console printing function
    private native void ltt_printC(String string_to_print);

    // Load LTTV library (order is important)
    static {
        System.loadLibrary("lttvtraceread");
    }

    /**
     * Java-side console printing function.
     * 
     * Call the C printing function to make sure all printing happen on the same side.
     * 
     * @param msg
     */
    public void printC(String msg) {
        // Need to escape "%"
        msg = msg.replaceAll("%", "%%");
        ltt_printC(msg);
    }

    /**
     * Java-side console printing function. Add a return line at the end of the message.
     * 
     * Call the C printing function to make sure all printing happen on the same side.
     * 
     * @param msg
     */
    public void printlnC(String msg) {
        printC(msg + "\n");
    }

    /**
     * This method is to be used as an "alternate" .toString()<br>
     * <br>
     * Simulates the way Java Object implements "toString()"
     * 
     * @return the Java hashed UID of the object (i.e. : NAME@HASH)
     */
    public String getReferenceToString() {
        return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode());
    }
}

/**
 * <b><u>C_Pointer</u></b>
 * <p>
 * Class pointer to handle properly "C pointer" <br>
 * 
 * Can transparently handle pointer of 32 or 64 bits.
 */
class C_Pointer extends Jni_C_Common {

    private long ptr = NULL;
    private boolean isLong = true;

    public C_Pointer() {
        ptr  = NULL;
    }

    public C_Pointer(long newPtr) {
        ptr = newPtr;
        isLong = true; 
    }

    public C_Pointer(int newPtr) {
        ptr = (long)newPtr;
        isLong = false; 
    }

    public long getPointer() {
        return ptr;
    }

    public void setPointer(long newPtr) {
        ptr = newPtr;
        isLong = true;
    }

    public void setPointer(int newPtr) {
        ptr = newPtr;
        isLong = false;
    }

    /**
     * toString() method<br>
     * <br>
     * Convert the pointer to a nice looking hexadecimal format 
     * 
     * @return String Attributes of the object concatenated in String
     */
    public String toString() {
        String returnData = "0x";

        if (isLong == true) {
            returnData += Long.toHexString(ptr);
        }
        else {
            returnData += Integer.toHexString((int) ptr);
        }

        return returnData;
    }
}
