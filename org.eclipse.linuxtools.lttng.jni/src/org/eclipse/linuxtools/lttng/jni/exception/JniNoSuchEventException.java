package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniNoSuchEventException</u></b>
 * <p>
 * Sub-exception type for the JniEventException type
 * This exception type will get thrown when an event is unavailable
 * This might happen at construction because some events type are not present in
 * the trace
 */
public class JniNoSuchEventException extends JniEventException {
    private static final long serialVersionUID = -4379712949891538051L;

    public JniNoSuchEventException(String errMsg) {
        super(errMsg);
    }
}
