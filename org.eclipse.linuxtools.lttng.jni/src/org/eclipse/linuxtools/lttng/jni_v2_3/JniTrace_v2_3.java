package org.eclipse.linuxtools.lttng.jni_v2_3;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniTrace_v2_3 extends JniTrace {
	
	// *** FIXME ***
	// Eventually, it should be something like this :
	//	private static final String LIBRARY_NAME = "liblttvtraceread2.3.so";
	// However, the C library is not ready yet, so we will keep the general one : 
	private static final String LIBRARY_NAME = "liblttvtraceread.so";
	
	protected JniTrace_v2_3() {
		super();
    }
    
	public JniTrace_v2_3(String newpath) throws JniException {
		super(newpath);
	}
	
    public JniTrace_v2_3(String newpath, boolean newPrintDebug) throws JniException {
    	super(newpath, newPrintDebug);
    }
    
    
    public JniTrace_v2_3(JniTrace_v2_3 oldTrace) {
    	super(oldTrace);
    }        
    
    public JniTrace_v2_3(Jni_C_Pointer newPtr, boolean newPrintDebug) throws JniException {
    	super(newPtr, newPrintDebug);
    }
    
    
    public boolean initializeLibrary() {
    	return ltt_initializeHandle(LIBRARY_NAME);
    }
    
    
    public JniTracefile allocateNewJniTracefile(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
    	return new JniTracefile_v2_3(newPtr, newParentTrace);
    }
    
    
    
}
