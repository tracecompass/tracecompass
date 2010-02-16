package org.eclipse.linuxtools.lttng.jni_v2_3;

import org.eclipse.linuxtools.lttng.jni.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarkerField extends org.eclipse.linuxtools.lttng.jni.JniMarkerField {

	static {
		System.loadLibrary("lttvtraceread2.3");
	}
	
	protected JniMarkerField() {
		super();
    }
	
	
    public JniMarkerField(JniMarkerField oldMarkerField) {
    	super(oldMarkerField);
    }
    
    
    public JniMarkerField(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	super(newMarkerFieldPtr);
    }
	
}
