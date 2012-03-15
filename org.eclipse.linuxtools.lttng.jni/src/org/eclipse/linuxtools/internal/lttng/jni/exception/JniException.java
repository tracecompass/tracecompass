package org.eclipse.linuxtools.internal.lttng.jni.exception;
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
 * <b><u>JniException</u></b>
 * <p>
 * Super class for JNI exception.
 */
public class JniException extends Exception {
    private static final long serialVersionUID = -6620784221853154537L;

    public JniException(String errMsg) {
        super(errMsg);
    }
}

