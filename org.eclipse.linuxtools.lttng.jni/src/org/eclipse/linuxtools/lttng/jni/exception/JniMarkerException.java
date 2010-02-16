package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniMarkerException</u></b>
 * <p>
 * Basic Exception class for the JniMarker class
 */
public class JniMarkerException extends JniException {
    private static final long serialVersionUID = -4694173610721983794L;

    public JniMarkerException(String errMsg) {
        super(errMsg);
    }
}
