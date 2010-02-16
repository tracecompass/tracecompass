package org.eclipse.linuxtools.lttng.jni_v2_5;

import org.eclipse.linuxtools.lttng.jni.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarker extends org.eclipse.linuxtools.lttng.jni.JniMarker {

	static {
		System.loadLibrary("lttvtraceread2.5");
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
