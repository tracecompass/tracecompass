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
 * <b><u>JniOpenTraceFailedException</u></b>
 * <p>
 * Sub-exception class type for JniTraceException
 * This type will get thrown when a trace fail to open
 * Most likely to be caused by a bad tracepath
 */
public class JniOpenTraceFailedException extends JniTraceException {
    private static final long serialVersionUID = 877769692366394895L;

    public JniOpenTraceFailedException(String errMsg) {
        super(errMsg);
    }
}