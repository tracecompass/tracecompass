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
 * <b><u>JniTracefileWithoutEventException</u></b>
 * <p>
 * Sub-exception class type for JniTracefileException
 * This type will get thrown when a trace file contain no readable events
 * The proper course of action would usually be to ignore this useless trace file
 */
public class JniTracefileWithoutEventException extends JniTracefileException {
    private static final long serialVersionUID = -8183967479236071261L;

    public JniTracefileWithoutEventException(String errMsg) {
        super(errMsg);
    }
}
