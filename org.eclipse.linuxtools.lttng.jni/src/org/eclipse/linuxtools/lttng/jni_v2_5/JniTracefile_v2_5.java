package org.eclipse.linuxtools.lttng.jni_v2_5;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniTracefile_v2_5 extends JniTracefile {
	
    protected JniTracefile_v2_5() {
    	super();
    }
    
    public JniTracefile_v2_5(JniTracefile_v2_5 oldTracefile) {
    	super(oldTracefile);
    }
    
    public JniTracefile_v2_5(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
    	super(newPtr, newParentTrace);
    }
	
    
    @Override
	public JniEvent allocateNewJniEvent(Jni_C_Pointer newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	return new JniEvent_v2_5(newEventPtr, newMarkersMap, newParentTracefile);
    }
    
    
    @Override
	public JniMarker allocateNewJniMarker(Jni_C_Pointer newMarkerPtr) throws JniException {
    	return new JniMarker_v2_5(newMarkerPtr);
    }
    
}
