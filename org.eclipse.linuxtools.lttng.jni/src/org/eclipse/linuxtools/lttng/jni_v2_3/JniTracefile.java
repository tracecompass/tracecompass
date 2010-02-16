package org.eclipse.linuxtools.lttng.jni_v2_3;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniTracefile extends org.eclipse.linuxtools.lttng.jni.JniTracefile {

	static {
		System.loadLibrary("lttvtraceread2.3");
	}
	
    protected JniTracefile() {
    	super();
    }
    
    public JniTracefile(JniTracefile oldTracefile) {
    	super(oldTracefile);
    }
    
    public JniTracefile(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
    	super(newPtr, newParentTrace);
    }     
	
}
