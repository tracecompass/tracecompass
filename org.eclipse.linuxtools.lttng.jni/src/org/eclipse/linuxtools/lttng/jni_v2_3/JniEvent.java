package org.eclipse.linuxtools.lttng.jni_v2_3;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniEvent extends org.eclipse.linuxtools.lttng.jni.JniEvent {

	static {
		System.loadLibrary("lttvtraceread2.3");
	}
	
	static {
		System.loadLibrary("lttvtraceread2.5");
	}
	
	protected JniEvent() {
		super();
    }
	
	public JniEvent(JniEvent oldEvent) {
		super(oldEvent);
	}
    
    
    
    public JniEvent(Jni_C_Pointer newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	super(newEventPtr, newMarkersMap, newParentTracefile);
    }
	
}
