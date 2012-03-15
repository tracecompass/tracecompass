package org.eclipse.linuxtools.internal.lttng.jni_v2_6;
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

import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;

/**
 * <b><u>JniTracefile_v2_6</u></b>
 * <p>
 * JniTracefile version to support Lttng traceformat of version 2.6<br>
 * This class extend abstract class JniTracefile with (possibly) version specific implementation.<br>
 * <p>
 */
public class JniTracefile_v2_6 extends JniTracefile {
	
	/*
	 * Forbid access to the default constructor
	 */
    protected JniTracefile_v2_6() {
    	super();
    }
    
    
    public JniTracefile_v2_6(JniTracefile_v2_6 oldTracefile) {
    	super(oldTracefile);
    }
    
    public JniTracefile_v2_6(Jni_C_Pointer_And_Library_Id newPtr, JniTrace newParentTrace) throws JniException {
    	super(newPtr, newParentTrace);
    }
	
    
    /**
     * Allocate (call constructor for) a new JniEvent.<p>
     * 
     * This method is made to bypass limitation related to abstract class, see comment in JniTracefile
     * 
     * @return JniEvent 	a newly allocated JniEvent
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    @Override
	public JniEvent allocateNewJniEvent(Jni_C_Pointer_And_Library_Id newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	return new JniEvent_v2_6(newEventPtr, newMarkersMap, newParentTracefile);
    }
    
    
    /**
     * Allocate (call constructor for) a new JniMarker.<p>
     * 
     * This method is made to bypass limitation related to abstract class, see comment in JniTracefile
     * 
     * @return JniMarker 	a newly allocated JniMarker
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    @Override
	public JniMarker allocateNewJniMarker(Jni_C_Pointer_And_Library_Id newMarkerPtr) throws JniException {
    	return new JniMarker_v2_6(newMarkerPtr);
    }
    
}
