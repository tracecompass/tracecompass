package org.eclipse.linuxtools.lttng.jni_v2_5;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniEvent_v2_5 extends JniEvent {

	
	protected JniEvent_v2_5() {
		super();
    }
	
	public JniEvent_v2_5(JniEvent_v2_5 oldEvent) {
		super(oldEvent);
	}
    
    
    
    public JniEvent_v2_5(Jni_C_Pointer newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	super(newEventPtr, newMarkersMap, newParentTracefile);
    }

	
}
