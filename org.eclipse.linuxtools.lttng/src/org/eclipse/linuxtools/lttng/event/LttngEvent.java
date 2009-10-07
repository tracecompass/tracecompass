package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.LttngException;

/**
 * <b><u>LttngEvent</u></b><p>
 * 
 * Lttng specific TmfEvent implementation.<p>
 * 
 * The main difference from the basic Tmf implementation is that we keep an internal reference to the JniEvent<br>
 * The conversion from this LttngEvent to the JniEvent is then possible.
 */
@SuppressWarnings("unused")
public class LttngEvent extends TmfEvent {
    
    // Reference to the JNI JniEvent. Should only used INTERNALLY
    private JniEvent jniEventReference = null;
    
    /**
     * Constructor with parameters.<p>
     * 
     * @param timestamp  The timestamp of this event   
     * @param source     The source of this event
     * @param type       The type of this event
     * @param content    The content of this event
     * @param reference  The reference of this event
     * @param lttEvent   A reference to a valid JniEvent object
     * 
     * @see org.eclipse.linuxtools.tmf.event.TmfTimestamp
     * @see org.eclipse.linuxtools.tmf.event.TmfEventSource
     * @see org.eclipse.linuxtools.lttng.event.LttngEventType
     * @see org.eclipse.linuxtools.lttng.event.LttngEventContent
     * @see org.eclipse.linuxtools.lttng.event.LttngEventReference
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public LttngEvent(LttngTimestamp timestamp, TmfEventSource source, LttngEventType type, LttngEventContent content, LttngEventReference reference, JniEvent lttEvent) throws LttngException { 
        super(timestamp, source, type, content, reference);
        
        // Sanity checks
        if ( (timestamp == null) || (source == null) || (type == null) || (content == null) || (reference == null) || (lttEvent == null) ) {
        	throw new LttngException("Event creation with null values is forbidden!");
        }
        
        jniEventReference = lttEvent;
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param oldEvent		Event we want to copy from.
     * 
     */
    public LttngEvent(LttngEvent oldEvent) throws LttngException { 
        this(	(LttngTimestamp)oldEvent.getTimestamp(), 
        		(TmfEventSource)oldEvent.getSource(), 
        		(LttngEventType)oldEvent.getType(), 
        		(LttngEventContent)oldEvent.getContent(), 
        		(LttngEventReference)oldEvent.getReference(), 
        		oldEvent.jniEventReference
        	);
    }
    
    
    /**
     * Return the channel name of this event.<p>
     * 
     * @return The name of the channel for this event
     */
    public String getChannelName() {
        String returnedValue = "";
        
        // This should always be true
        if ( this.getType() instanceof LttngEventType ) {
            returnedValue = ( (LttngEventType)this.getType() ).getChannelName();
        }
        return returnedValue;
    }
    
    /**
     * Cpu id number of this event.<p>
     * 
     * @return The cpu id
     */
    public long getCpuId() {
        long returnedValue =-1;
        
        // This should always be true
        if ( this.getType() instanceof LttngEventType ) {
            returnedValue = ( (LttngEventType)this.getType() ).getCpuId();
        }
        return returnedValue;
    }
    
    /**
     * Marker name of this event.<p>
     * 
     * @return The marker name
     */
    public String getMarkerName() {
        String returnedValue = "";
        
        // This should always be true
        if ( this.getType() instanceof LttngEventType ) {
            returnedValue = ( (LttngEventType)this.getType() ).getMarkerName();
        }
        return returnedValue;
    }
    
    /**
     * Convert this event into a Jni JniEvent.<p>
     * 
     * Note : Some verifications are done to make sure the event is still valid on 
     * the Jni side before conversion.<br> If it is not the case, null will be returned.
     * 
     * @return The converted JniEvent
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent convertEventTmfToJni() {
        JniEvent tmpEvent = null;
        
        // We don't want to send away events that are outdated as their informations could be invalid
        //  If the timestamp between the event and the trace are not coherent we will not perform the conversion
        if ( jniEventReference.getParentTracefile().getParentTrace().getCurrentEventTimestamp().getTime() == getTimestamp().getValue() ) {
            tmpEvent = jniEventReference;
        }
        return tmpEvent;
    }
    
    @Override
	public String toString() {
    	String returnedData="";
    	
    	returnedData += "Event timestamp:" + this.getTimestamp().getValue() + " ";
    	returnedData += "Channel:" + getChannelName() + " ";
    	returnedData += "CPU Id:" + getCpuId() + " ";
    	returnedData += "Marker:" + getMarkerName() + " ";
    	
    	return returnedData;
    }
}
