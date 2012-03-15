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
 * <b><u>JniMarkerFieldException</u></b>
 * <p>
 * Basic Exception class for the JniMarkerField class
 */
public class JniMarkerFieldException extends JniException {
    private static final long serialVersionUID = 6066381741374806879L;

    public JniMarkerFieldException(String errMsg) {
        super(errMsg);
    }
}
