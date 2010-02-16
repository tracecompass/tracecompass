package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniOpenTraceFailedException</u></b>
 * <p>
 * Sub-exception class type for JniTraceException
 * This type will get thrown when a trace fail to open
 * Most likely to be caused by a bad tracepath
 */
public class JniOpenTraceFailedException extends JniTraceException {
    private static final long serialVersionUID = 877769692366394895L;

    public JniOpenTraceFailedException(String errMsg) {
        super(errMsg);
    }
}