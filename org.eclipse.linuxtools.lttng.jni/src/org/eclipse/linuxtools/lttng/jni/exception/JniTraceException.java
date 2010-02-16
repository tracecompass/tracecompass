package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniTraceException</u></b>
 * <p>
 * Basic exception class for the JniTrace class
 */
public class JniTraceException extends JniException {
    private static final long serialVersionUID = -6873007333085268143L;

    public JniTraceException(String errMsg) {
        super(errMsg);
    }
}
