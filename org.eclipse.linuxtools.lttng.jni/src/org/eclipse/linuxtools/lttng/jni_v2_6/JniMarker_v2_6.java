package org.eclipse.linuxtools.lttng.jni_v2_6;

import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarker_v2_6 extends JniMarker {
	
	protected JniMarker_v2_6() {
		super();
    }
    
    
    public JniMarker_v2_6(JniMarker_v2_6 oldMarker) {
    	super(oldMarker);
    }
    
    
    public JniMarker_v2_6(Jni_C_Pointer newMarkerPtr) throws JniException {
    	super(newMarkerPtr);
    }
	
    
    public JniMarkerField allocateNewJniMarkerField(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	return new JniMarkerField_v2_6(newMarkerFieldPtr);
    }
    
}
