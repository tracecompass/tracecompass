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
 * <b><u>JniNoNextEventInTraceException</u></b>
 * <p>
 * Sub-exception class type for JniTraceException
 * This type will get thrown when we can't find any "next" event
 * This should usually mean there is no more event in the trace

 */
public class JniNoNextEventInTraceException extends JniTraceException {
    private static final long serialVersionUID = -2887528566100063849L;

    public JniNoNextEventInTraceException(String errMsg) {
        super(errMsg);
    }
}
