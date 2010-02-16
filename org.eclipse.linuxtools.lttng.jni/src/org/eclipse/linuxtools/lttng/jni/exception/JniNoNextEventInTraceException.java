package org.eclipse.linuxtools.lttng.jni.exception;

/**
 * <b><u>JniNoNextEventInTraceException</u></b>
 * <p>
 * Sub-exception class type for JniTraceException
 * This type will get thrown when we can't find any "next" event
 * This should usually mean there is no more event in the trace

 */
public class JniNoNextEventInTraceException extends JniTraceException {
    private static final long serialVersionUID = -2887528566100063849L;

    public JniNoNextEventInTraceException(String errMsg) {
        super(errMsg);
    }
}
