package org.eclipse.linuxtools.lttng.jni_v2_5;

import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniMarkerField_v2_5 extends JniMarkerField {
	
	protected JniMarkerField_v2_5() {
		super();
    }
	
	
    public JniMarkerField_v2_5(JniMarkerField_v2_5 oldMarkerField) {
    	super(oldMarkerField);
    }
    
    
    public JniMarkerField_v2_5(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	super(newMarkerFieldPtr);
    }
    
}
