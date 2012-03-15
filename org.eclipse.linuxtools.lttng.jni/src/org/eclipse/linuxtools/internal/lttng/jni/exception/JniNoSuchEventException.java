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
 * <b><u>JniNoSuchEventException</u></b>
 * <p>
 * Sub-exception type for the JniEventException type
 * This exception type will get thrown when an event is unavailable
 * This might happen at construction because some events type are not present in
 * the trace
 */
public class JniNoSuchEventException extends JniEventException {
    private static final long serialVersionUID = -4379712949891538051L;

    public JniNoSuchEventException(String errMsg) {
        super(errMsg);
    }
}
