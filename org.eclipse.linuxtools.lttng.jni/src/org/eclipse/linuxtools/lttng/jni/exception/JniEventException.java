package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniEventException</u></b>
 * <p>
 * Basic exception class for the JniEvent class
 */
public class JniEventException extends JniException {
    private static final long serialVersionUID = -5891749130387304519L;

    public JniEventException(String errMsg) {
        super(errMsg);
    }
}