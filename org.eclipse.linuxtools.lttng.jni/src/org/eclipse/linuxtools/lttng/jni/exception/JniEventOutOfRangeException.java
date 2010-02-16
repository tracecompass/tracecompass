package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniEventOutOfRangeException</u></b>
 * <p>
 * Sub-exception type for the JniEventException type
 * This exception type will get thrown when there is no more event of this type
 * available
 */
public class JniEventOutOfRangeException extends JniEventException {
    private static final long serialVersionUID = -4645877232795324541L;

    public JniEventOutOfRangeException(String errMsg) {
        super(errMsg);
    }
}

