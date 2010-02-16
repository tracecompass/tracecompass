package org.eclipse.linuxtools.lttng.jni_v2_3;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarker extends org.eclipse.linuxtools.lttng.jni.JniMarker {

	static {
		System.loadLibrary("lttvtraceread2.3");
	}
	
	protected JniMarker() {
		super();
    }
    
    
    public JniMarker(JniMarker oldMarker) {
    	super(oldMarker);
    }
    
    
    public JniMarker(Jni_C_Pointer newMarkerPtr) throws JniException {
    	super(newMarkerPtr);
    }
}
