package org.eclipse.linuxtools.lttng.jni_v2_5;

import org.eclipse.linuxtools.lttng.jni.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTracefileWithoutEventException;
import org.eclipse.linuxtools.lttng.jni_v2_3.JniTracefile;

public class JniTrace extends org.eclipse.linuxtools.lttng.jni.JniTrace {

	static {
		System.loadLibrary("lttvtraceread2.5");
	}
	
	protected JniTrace() {
		super();
    }
    
	
    public JniTrace(String newpath, boolean newPrintDebug) throws JniException {
    	super(newpath, newPrintDebug);
    }
    
    
    public JniTrace(JniTrace oldTrace) {
    	super(oldTrace);
    }        
    
    public JniTrace(Jni_C_Pointer newPtr, boolean newPrintDebug) throws JniException {
    	super(newPtr, newPrintDebug);
    }
}
