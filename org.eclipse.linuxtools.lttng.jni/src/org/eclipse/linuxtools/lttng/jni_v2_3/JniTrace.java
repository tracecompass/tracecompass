package org.eclipse.linuxtools.lttng.jni_v2_3;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTracefileWithoutEventException;
import org.eclipse.linuxtools.lttng.jni_v2_3.JniTracefile;
import org.eclipse.linuxtools.lttng.jni_v2_3.Jni_C_Pointer;

public class JniTrace extends org.eclipse.linuxtools.lttng.jni.JniTrace {

	static {
		System.loadLibrary("lttvtraceread2.3");
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
    
    protected JniTracefile createNewTracefile(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
    	return new JniTracefile(newPtr, newParentTrace);
    }
    
}
