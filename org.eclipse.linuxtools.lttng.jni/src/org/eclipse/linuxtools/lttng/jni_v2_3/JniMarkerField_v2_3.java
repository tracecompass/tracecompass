package org.eclipse.linuxtools.lttng.jni_v2_3;

import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarkerField_v2_3 extends JniMarkerField {
	
	protected JniMarkerField_v2_3() {
		super();
    }
	
	
    public JniMarkerField_v2_3(JniMarkerField_v2_3 oldMarkerField) {
    	super(oldMarkerField);
    }
    
    
    public JniMarkerField_v2_3(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	super(newMarkerFieldPtr);
    }
    
}
