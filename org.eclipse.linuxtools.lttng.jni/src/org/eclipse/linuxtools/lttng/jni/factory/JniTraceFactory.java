package org.eclipse.linuxtools.lttng.jni.factory;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTraceVersionException;
import org.eclipse.linuxtools.lttng.jni_v2_3.JniTrace_v2_3;
import org.eclipse.linuxtools.lttng.jni_v2_5.JniTrace_v2_5;
import org.eclipse.linuxtools.lttng.jni_v2_6.JniTrace_v2_6;

public class JniTraceFactory {
	
	static final String TraceVersion_v2_3 = "2.3"; 
	static final String TraceVersion_v2_5 = "2.5";
	static final String TraceVersion_v2_6 = "2.6";
	
	private JniTraceFactory(){
		// Default constructor is forbidden
	}
	
	static public JniTrace getJniTrace(String path, boolean show_debug) throws JniException {
		
		try {
			JniTraceVersion traceVersion = new JniTraceVersion(path);
			
			if ( traceVersion.toString().equals(TraceVersion_v2_6) ) {
				return new JniTrace_v2_6(path);
			}
			else if ( traceVersion.toString().equals(TraceVersion_v2_5) ) {
				return new JniTrace_v2_5(path);
			}
			else if ( traceVersion.toString().equals(TraceVersion_v2_3) ) {
				return new JniTrace_v2_3(path);
			}
			else {
				throw new JniException("ERROR : Unrecognized/unsupported trace version.");
			}
		}
		catch (JniTraceVersionException e) {
			
			// *** FIXME ***
			// We should probably fail if the trace version can't be found, however unless
			//	the C library is ajusted, we will always ends up here. 
			// For now, we will default to the trace version 2.5 (what we use for unit tests) so it should still work. 
			// ***
			
			//throw new JniException("ERROR : Call to JniTraceVersion() failed.");
			System.out.println("WARNING : Call to JniTraceVersion() failed, defaulting to version 2.5.");
			return new JniTrace_v2_5(path);
		}
	}
	
}
