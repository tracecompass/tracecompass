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
 * <b><u>JniMarkerException</u></b>
 * <p>
 * Basic Exception class for the JniMarker class
 */
public class JniMarkerException extends JniException {
    private static final long serialVersionUID = -4694173610721983794L;

    public JniMarkerException(String errMsg) {
        super(errMsg);
    }
}
