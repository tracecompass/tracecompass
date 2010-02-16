package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniTracefileException</u></b>
 * <p>
 * Basic exception class for the JniTracefile class
 */
public class JniTracefileException extends JniException {
    private static final long serialVersionUID = 5081317864491800084L;

    public JniTracefileException(String errMsg) {
        super(errMsg);
    }
}
