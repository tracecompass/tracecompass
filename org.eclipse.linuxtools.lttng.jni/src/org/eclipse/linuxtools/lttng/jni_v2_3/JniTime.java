package org.eclipse.linuxtools.lttng.jni_v2_3;


public class JniTime extends org.eclipse.linuxtools.lttng.jni.JniTime {

	static {
		System.loadLibrary("lttvtraceread2.3");
	}
	
	public JniTime() {
        super();
    }
	
    public JniTime(JniTime oldTime) {
    	super(oldTime);
    }

    public JniTime(long newSec, long newNanoSec) {
    	super(newNanoSec);
    }

    public JniTime(long newNanoSecTime) {
    	super(newNanoSecTime);
    }
	
}
