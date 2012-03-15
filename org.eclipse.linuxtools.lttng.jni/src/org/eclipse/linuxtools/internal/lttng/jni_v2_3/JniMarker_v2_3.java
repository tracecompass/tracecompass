package org.eclipse.linuxtools.internal.lttng.jni_v2_3;
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

import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;

/**
 * <b><u>JniMarker_v2_3</u></b>
 * <p>
 * JniMarker version to support Lttng traceformat of version 2.3<br>
 * This class extend abstract class JniMarker with (possibly) version specific implementation.<br>
 * <p>
 */
public class JniMarker_v2_3 extends JniMarker {
	
	/*
	 * Forbid access to the default constructor
	 */
	protected JniMarker_v2_3() {
		super();
    }
    
    
    public JniMarker_v2_3(JniMarker_v2_3 oldMarker) {
    	super(oldMarker);
    }
    
    public JniMarker_v2_3(Jni_C_Pointer_And_Library_Id newMarkerPtr) throws JniException {
    	super(newMarkerPtr);
    }
	
    
    /**
     * Allocate (call constructor for) a new JniMarkerField.<p>
     * 
     * This method is made to bypass limitation related to abstract class, see comment in JniMarker
     * 
     * @return JniMarkerField 	a newly allocated JniMarkerField
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     */
    @Override
	public JniMarkerField allocateNewJniMarkerField(Jni_C_Pointer_And_Library_Id newMarkerFieldPtr) throws JniException {
    	return new JniMarkerField_v2_3(newMarkerFieldPtr);
    }
    
}
