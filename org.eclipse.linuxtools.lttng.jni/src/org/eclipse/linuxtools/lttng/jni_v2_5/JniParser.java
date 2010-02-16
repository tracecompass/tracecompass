package org.eclipse.linuxtools.lttng.jni_v2_5;

public class JniParser extends org.eclipse.linuxtools.lttng.jni.JniParser {

	static {
		System.loadLibrary("lttvtraceread2.5");
	}
	
	protected JniParser() {
		super();
    }
}
