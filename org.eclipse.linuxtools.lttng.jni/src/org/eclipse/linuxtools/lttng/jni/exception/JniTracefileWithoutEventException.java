package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniTracefileWithoutEventException</u></b>
 * <p>
 * Sub-exception class type for JniTracefileException
 * This type will get thrown when a trace file contain no readable events
 * The proper course of action would usually be to ignore this useless trace file
 */
public class JniTracefileWithoutEventException extends JniTracefileException {
    private static final long serialVersionUID = -8183967479236071261L;

    public JniTracefileWithoutEventException(String errMsg) {
        super(errMsg);
    }
}
