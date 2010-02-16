package org.eclipse.linuxtools.lttng.jni_v2_3;

public class JniParser extends org.eclipse.linuxtools.lttng.jni.JniParser {

	static {
		System.loadLibrary("lttvtraceread2.3");
	}
	
	protected JniParser() {
		super();
    }
}
