package org.eclipse.linuxtools.lttng.jni;
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

    JniException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniTraceException</u></b>
 * <p>
 * Basic exception class for the JniTrace class
 */
class JniTraceException extends JniException {
    private static final long serialVersionUID = -6873007333085268143L;

    JniTraceException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniOpenTraceFailedException</u></b>
 * <p>
 * Sub-exception class type for JniTraceException
 * This type will get thrown when a trace fail to open
 * Most likely to be caused by a bad tracepath
 */
class JniOpenTraceFailedException extends JniTraceException {
    private static final long serialVersionUID = 877769692366394895L;

    JniOpenTraceFailedException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniNoNextEventInTraceException</u></b>
 * <p>
 * Sub-exception class type for JniTraceException
 * This type will get thrown when we can't find any "next" event
 * This should usually mean there is no more event in the trace

 */
class JniNoNextEventInTraceException extends JniTraceException {
    private static final long serialVersionUID = -2887528566100063849L;

    JniNoNextEventInTraceException(String errMsg) {
        super(errMsg);
    }
}

// 
/**
 * <b><u>JniTracefileException</u></b>
 * <p>
 * Basic exception class for the JniTracefile class
 */
class JniTracefileException extends JniException {
    private static final long serialVersionUID = 5081317864491800084L;

    JniTracefileException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniTracefileWithoutEventException</u></b>
 * <p>
 * Sub-exception class type for JniTracefileException
 * This type will get thrown when a trace file contain no readable events
 * The proper course of action would usually be to ignore this useless trace file
 */
class JniTracefileWithoutEventException extends JniTracefileException {
    private static final long serialVersionUID = -8183967479236071261L;

    JniTracefileWithoutEventException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniEventException</u></b>
 * <p>
 * Basic exception class for the JniEvent class
 */
class JniEventException extends JniException {
    private static final long serialVersionUID = -5891749130387304519L;

    JniEventException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniNoSuchEventException</u></b>
 * <p>
 * Sub-exception type for the JniEventException type
 * This exception type will get thrown when an event is unavailable
 * This might happen at construction because some events type are not present in
 * the trace
 */
class JniNoSuchEventException extends JniEventException {
    private static final long serialVersionUID = -4379712949891538051L;

    JniNoSuchEventException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniEventOutOfRangeException</u></b>
 * <p>
 * Sub-exception type for the JniEventException type
 * This exception type will get thrown when there is no more event of this type
 * available
 */
class JniEventOutOfRangeException extends JniEventException {
    private static final long serialVersionUID = -4645877232795324541L;

    JniEventOutOfRangeException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniMarkerException</u></b>
 * <p>
 * Basic Exception class for the JniMarker class
 */
class JniMarkerException extends JniException {
    private static final long serialVersionUID = -4694173610721983794L;

    JniMarkerException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>JniMarkerFieldException</u></b>
 * <p>
 * Basic Exception class for the JniMarkerField class
 */
class JniMarkerFieldException extends JniException {
    private static final long serialVersionUID = 6066381741374806879L;

    JniMarkerFieldException(String errMsg) {
        super(errMsg);
    }
}
