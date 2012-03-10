package org.eclipse.linuxtools.lttng.jni_v2_6;
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

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

/**
 * <b><u>JniEvent_v2_6</u></b>
 * <p>
 * JniEvent version to support Lttng traceformat of version 2.6<br>
 * This class extend abstract class JniEvent with (possibly) version specific implementation.<br>
 * <p>
 */
public class JniEvent_v2_6 extends JniEvent {
	
	/*
	 * Forbid access to the default constructor
	 */
	protected JniEvent_v2_6() {
		super();
    }

	public JniEvent_v2_6(JniEvent_v2_6 oldEvent) {
		super(oldEvent);
	}
    
    public JniEvent_v2_6(Jni_C_Pointer_And_Library_Id newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	super(newEventPtr, newMarkersMap, newParentTracefile);
    }

}
