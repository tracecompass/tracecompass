package org.eclipse.linuxtools.lttng.jni_v2_5;

public class Jni_C_Common extends org.eclipse.linuxtools.lttng.jni.Jni_C_Common {

	static {
		System.loadLibrary("lttvtraceread2.5");
	}

}
