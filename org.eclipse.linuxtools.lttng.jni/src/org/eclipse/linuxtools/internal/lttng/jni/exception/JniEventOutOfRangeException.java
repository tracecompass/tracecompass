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
 * <b><u>JniEventOutOfRangeException</u></b>
 * <p>
 * Sub-exception type for the JniEventException type
 * This exception type will get thrown when there is no more event of this type
 * available
 */
public class JniEventOutOfRangeException extends JniEventException {
    private static final long serialVersionUID = -4645877232795324541L;

    public JniEventOutOfRangeException(String errMsg) {
        super(errMsg);
    }
}

