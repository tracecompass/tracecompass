package org.eclipse.linuxtools.lttng.jni_v2_6;

import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarkerField_v2_6 extends JniMarkerField {
	
	protected JniMarkerField_v2_6() {
		super();
    }
	
	
    public JniMarkerField_v2_6(JniMarkerField_v2_6 oldMarkerField) {
    	super(oldMarkerField);
    }
    
    
    public JniMarkerField_v2_6(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	super(newMarkerFieldPtr);
    }
    
}
