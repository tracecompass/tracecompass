package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniMarkerFieldException</u></b>
 * <p>
 * Basic Exception class for the JniMarkerField class
 */
public class JniMarkerFieldException extends JniException {
    private static final long serialVersionUID = 6066381741374806879L;

    public JniMarkerFieldException(String errMsg) {
        super(errMsg);
    }
}
