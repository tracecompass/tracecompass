package org.eclipse.linuxtools.internal.lttng.jni.common;
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
 * Common constants and methods that should be shared between JNI objects.<p>
 * 
 * This class is abstract and is intended to be extended by LTTng modules that need the constants.
 */
public abstract class Jni_C_Constant {
    
    // Needed for native types
    public static final int NULL = 0;

    // C errno correspondance. Used to interpret LTT return value
    public static final int EOK    =  0;
    public static final int EPERM  =  1;
    public static final int ERANGE = 34;
    
    // Timestamps are in nanoseconds, this const ease up the math
    public static final long NANO = 1000000000;
    
    /**
     * Default constructor
     */
    public Jni_C_Constant() {
    }
    
    /**
     * "Alternate" .toString()<p>
     * 
     * Simulates the way Java Object implements "toString()"
     * 
     * @return The Java hashed UID of the object (i.e. : NAME@HASH)
     */
    @SuppressWarnings("nls")
    public String getReferenceToString() {
        return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode());
    }
}