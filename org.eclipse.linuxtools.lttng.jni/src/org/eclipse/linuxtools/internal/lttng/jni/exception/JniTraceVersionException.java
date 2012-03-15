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
 * <b><u>JniTraceVersionException</u></b>
 * <p>
 * Basic exception class for the JniTraceVersion class
 */
public class JniTraceVersionException extends JniException {
    private static final long serialVersionUID = -5891749123457304519L;

    public JniTraceVersionException(String errMsg) {
        super(errMsg);
    }
}