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
				String errMsg = "\nERROR : Unrecognized/unsupported trace version." +
								"\nLibrary reported a trace version " + traceVersion.toString() + "." + 
								"\nMake sure you installed the Lttv library that support this version (look for liblttvtraceread-" + traceVersion.toString() + ".so).\n"; 
				throw new JniException(errMsg);
			}
		}
		catch (JniTraceVersionException e) {
			String errMsg = "\nERROR : Call to JniTraceVersion() failed." +
							"\nThis usually means that the library (liblttvtraceread_loader.so) could not be found." +
							"\nMake sure the LTTv library is installed and that your LD_LIBRARY_PATH is set correctly (see help for more details)\n.";
			
			throw new JniException(errMsg);
		}
	}
	
}
