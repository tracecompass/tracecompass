package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.LttngException;

/**
 * <b><u>LttngEvent</u></b>
 * <p>
 * Lttng specific TmfEvent implementation
 * <p>
 * The main difference from the basic Tmf implementation is that we keep an internal reference to the Jni JniEvent<br>
 * The conversion from this LttngEvent to the JniEvent is then possible. 
 * </ul>
 */
@SuppressWarnings("unused")
public class LttngEvent extends TmfEvent {
    // Reference to the JNI JniEvent. Should only used INTERNALLY
    private JniEvent jniEventReference = null;
    
    /**
     * Constructor with parameters <br>
     * <br>
     * 
     * @param timestamp  The timestamp of this event   
     * @param source     The source of this event
     * @param type       The type of this event
     * @param content    The content of this event
     * @param reference  The reference of this event
     * @param lttEvent   A reference to a valid JniEvent object
     * 
     * @see org.eclipse.linuxtools.tmf.event.TmfTimestamp
     * @see org.eclipse.linuxtools.lttng.event.LttngEventSource
     * @see org.eclipse.linuxtools.lttng.event.LttngEventType
     * @see org.eclipse.linuxtools.lttng.event.LttngEventContent
     * @see org.eclipse.linuxtools.lttng.event.LttngEventReference
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     * 
     */
    public LttngEvent(LttngTimestamp timestamp, TmfEventSource source, LttngEventType type, LttngEventContent content, LttngEventReference reference, JniEvent lttEvent) throws LttngException { 
        super(timestamp, source, type, content, reference);
        
        if ( (timestamp == null) || (source == null) || (type == null) || (content == null) || (reference == null) || (lttEvent == null) ) {
        	throw new LttngException("Event creation with null values is forbidden!");
        }
        
        jniEventReference = lttEvent;
    }
    
    /**
     * Copy constructor <br>
     * <br>
     * 
     * @param oldEvent		Event we want to copy from
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
     * Return the channel name of this event<br>
     * 
     * @return String The name of the channel
     */
    public String getChannelName() {
        String returnedValue = "";
        
        if ( this.getType() instanceof LttngEventType ) {
            returnedValue = ( (LttngEventType)this.getType() ).getChannelName();
        }
        
        return returnedValue;
    }
    
    /**
     * Return the cpu id number of this event<br>
     * 
     * @return long The cpu id
     */
    public long getCpuId() {
        long returnedValue =-1;
        
        if ( this.getType() instanceof LttngEventType ) {
            returnedValue = ( (LttngEventType)this.getType() ).getCpuId();
        }
        
        return returnedValue;
    }
    
    /**
     * Return the marker name of this event<br>
     * 
     * @return String The marker name
     */
    public String getMarkerName() {
        String returnedValue = "";
        
        if ( this.getType() instanceof LttngEventType ) {
            returnedValue = ( (LttngEventType)this.getType() ).getMarkerName();
        }
        
        return returnedValue;
    }
    
    /**
     * Convert this event into a Jni JniEvent<br>
     * <br>
     * Note : Some verification are done to make sure the event is still valid on the Jni side.<br>
     * If it is not the case, null will be returned.
     * 
     * @return JniEvent The converted event
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
