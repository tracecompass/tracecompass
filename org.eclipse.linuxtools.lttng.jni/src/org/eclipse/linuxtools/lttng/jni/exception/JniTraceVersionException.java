package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniTraceVersionException</u></b>
 * <p>
 * Basic exception class for the JniTraceVersion class
 */
public class JniTraceVersionException extends JniException {
    private static final long serialVersionUID = -5891749123457304519L;

    public JniTraceVersionException(String errMsg) {
        super(errMsg);
    }
}