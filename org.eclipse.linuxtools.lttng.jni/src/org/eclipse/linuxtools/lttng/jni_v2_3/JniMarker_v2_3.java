package org.eclipse.linuxtools.lttng.jni_v2_3;

import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarker_v2_3 extends JniMarker {

	protected JniMarker_v2_3() {
		super();
    }
    
    
    public JniMarker_v2_3(JniMarker_v2_3 oldMarker) {
    	super(oldMarker);
    }
    
    
    public JniMarker_v2_3(Jni_C_Pointer newMarkerPtr) throws JniException {
    	super(newMarkerPtr);
    }
	
    
    public JniMarkerField allocateNewJniMarkerField(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	return new JniMarkerField_v2_3(newMarkerFieldPtr);
    }
    
}
