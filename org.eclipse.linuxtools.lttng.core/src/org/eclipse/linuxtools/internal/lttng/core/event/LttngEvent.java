package org.eclipse.linuxtools.internal.lttng.core.event;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * <b><u>LttngEvent</u></b>
 * <p>
 * Lttng specific TmfEvent implementation.
 * <p>
 * The main difference from the basic Tmf implementation is that we keep an
 * internal reference to the JniEvent
 * <p>
 * The conversion from this LttngEvent to the JniEvent is then possible.
 */
public class LttngEvent extends TmfEvent {

    // Reference to the JNI JniEvent. Should only be used INTERNALLY
    private JniEvent jniEventReference = null;
    
    // Parameter-less constructor
    public LttngEvent() {
        super();
        super.setType(LttngEventType.DEFAULT_EVENT_TYPE);
    }

    /**
     * Constructor with parameters.
     * <p>
     * 
     * @param timestamp The timestamp of this event
     * @param source The source of this event
     * @param type The type of this event
     * @param content The content of this event
     * @param reference The reference of this event
     * @param lttEvent A reference to a valid JniEvent object
     * 
     * @see org.eclipse.linuxtools.tmf.core.event.TmfTimestamp
     * @see org.eclipse.linuxtools.tmf.core.event.TmfEventSource
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngEventType
     * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngEventContent
     * @see org.eclipse.linuxtools.lttng.core.event.LttngEventReference
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public LttngEvent(TmfTrace<LttngEvent> parent, LttngTimestamp timestamp, String source, LttngEventType type, LttngEventContent content,
            String reference, JniEvent lttEvent)
    {
        super(parent, timestamp, source, type, content, reference);
        jniEventReference = lttEvent;
        super.setTrace(parent);
    }

    /**
     * Copy constructor.
     * <p>
     * 
     * @param oldEvent Event we want to copy from.
     */
    @SuppressWarnings("unchecked")
    public LttngEvent(LttngEvent oldEvent) {
        this(	
        		(TmfTrace<LttngEvent>) oldEvent.getTrace(),
        		(LttngTimestamp)oldEvent.getTimestamp(), 
        		oldEvent.getSource(), 
        		(LttngEventType)oldEvent.getType(), 
        		(LttngEventContent)oldEvent.getContent(), 
        		oldEvent.getReference(), 
        		oldEvent.jniEventReference
        	);
    }
    /**
     * Set a new parent trace for this event
     * 
     * @param parentTrace The new parent
     */
    public void setParentTrace(TmfTrace<LttngEvent> parentTrace) {
        super.setTrace(parentTrace);
	}
    
    
	/**
     * Return the channel name of this event.<p>
     * 
     * @return Channel (tracefile) for this event
     */
    public String getChannelName() {
        return this.getType().getTracefileName();
    }

    /**
     * Cpu id number of this event.
     * <p>
     * 
     * @return CpuId
     */
    public long getCpuId() {
        return this.getType().getCpuId();
    }

    /**
     * Marker name of this event.
     * <p>
     * 
     * @return Marker name
     */
    public String getMarkerName() {
        return this.getType().getMarkerName();
    }

    /**
     * Marker id of this event.
     * <p>
     * 
     * @return Marker id
     */
    public int getMarkerId() {
        return this.getType().getMarkerId();
    }

    @Override
    public LttngEventContent getContent() {
        return (LttngEventContent) super.getContent();
    }

    public void setContent(LttngEventContent newContent) {
        super.setContent(newContent);
    }

    @Override
    public void setReference(String reference) {
        super.setReference(reference);
    }

    @Override
    public LttngEventType getType() {
        return (LttngEventType) super.getType();
    }

    public void setType(LttngEventType newType) {
        super.setType(newType);
    }

    /**
     * Set a new JniReference for this event.
     * <p>
     * 
     * Note : Reference is used to get back to the Jni during event parsing and
     * need to be consistent.
     * 
     * @param newJniEventReference New reference
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public synchronized void updateJniEventReference(JniEvent newJniEventReference) {
        this.jniEventReference = newJniEventReference;
    }

    /**
     * Convert this event into a Jni JniEvent.
     * <p>
     * 
     * Note : Some verifications are done to make sure the event is still valid
     * on the Jni side before conversion.<br>
     * If it is not the case, null will be returned.
     * 
     * @return The converted JniEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public synchronized JniEvent convertEventTmfToJni() {
        JniEvent tmpEvent = null;

        // ***TODO***
        // Should we remove the check to save some time??

        // We don't want to send away events that are outdated as their
        // informations could be invalid
        // If the timestamp between the event and the trace are not coherent we
        // will not perform the conversion
        if (jniEventReference.getParentTracefile().getParentTrace().getCurrentEventTimestamp().getTime() == getTimestamp().getValue()) {
            tmpEvent = jniEventReference;
        } else {
            System.out
                    .println("convertEventTmfToJni() failed: Unsynced Timestamp > TMF:" + getTimestamp().getValue() + " <--> JNI:" + jniEventReference.getParentTracefile().getParentTrace().getCurrentEventTimestamp().getTime()); //$NON-NLS-1$//$NON-NLS-2$
        }
        return tmpEvent;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuffer result = new StringBuffer("[LttngEvent(");
        result.append("Timestamp:" + getTimestamp().getValue());
        result.append(",Channel:" + getChannelName());
        result.append(",CPU:" + getCpuId());
        result.append(",Marker:" + getMarkerName());
        result.append(",Content:" + getContent() + ")]");

        return result.toString();
    }

    @Override
	public LttngEvent clone() {
    	LttngEvent clone = (LttngEvent) super.clone();
    	clone.getContent().setEvent(clone);
		clone.jniEventReference = jniEventReference;
    	return clone;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public synchronized int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((jniEventReference == null) ? 0 : jniEventReference.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public synchronized boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LttngEvent)) {
            return false;
        }
        LttngEvent other = (LttngEvent) obj;
        if (jniEventReference == null) {
            if (other.jniEventReference != null) {
                return false;
            }
        } else if (!jniEventReference.equals(other.jniEventReference)) {
            return false;
        }
        return true;
    }

}
