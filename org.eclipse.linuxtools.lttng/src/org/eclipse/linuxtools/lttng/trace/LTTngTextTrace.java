/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.trace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.event.LttngEventSource;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

public class LTTngTextTrace extends TmfTrace implements ITmfTrace, ITmfRequestHandler<TmfEvent> {
	private LttngTimestamp                  eventTimestamp   = null;
    private LttngEventSource                eventSource      = null;
    private LttngEventType                  eventType        = null;
    private TextLttngEventContent           eventContent     = null;
    private LttngEventReference             eventReference   = null;
    // The actual event
    private  TextLttngEvent                 currentLttngEvent = null;             
    
    private  HashMap<String, LttngEventType> traceTypes       = null;
    
    private  String tracepath = "";
    private  FileReader fr = null;
    private  BufferedReader br = null;
    private  Long nbCharRead = 0L;
    
    private int cpuNumber = -1;
    
    private  boolean showDebug = false;
    
    public LTTngTextTrace(String path) throws Exception {
    	this(path, false);
    }
    
    public LTTngTextTrace(String path, boolean skipIndexing) throws Exception {
        super(path, 1, true);
        
        tracepath = path;
        traceTypes      = new HashMap<String, LttngEventType>();
        
        eventTimestamp        = new LttngTimestamp();
        eventSource           = new LttngEventSource();
        eventType             = new LttngEventType();
        eventContent          = new TextLttngEventContent(currentLttngEvent);
        eventReference        = new LttngEventReference(this.getName());
        
        currentLttngEvent = new TextLttngEvent(eventTimestamp, eventSource, eventType, eventContent, eventReference);
        eventContent.setEvent(currentLttngEvent);
        
        if ( positionToFirstEvent() == false ) {
        	throw new IOException("Fail to position to the beginning of the trace");
        }
        else {
        	fCacheSize = 1000;
        	
        	// Skip indexing if asked
        	if ( skipIndexing == true ) {
        		fCheckpoints.add(new TmfTraceCheckpoint(new LttngTimestamp(0L), 0L));
        	}
        	else {
        		indexStream();
        	}
        	
        	Long endTime = currentLttngEvent.getTimestamp().getValue();
        	positionToFirstEvent();
        	
        	getNextEvent(new TmfTraceContext(null, null, 0));
        	Long starTime = currentLttngEvent.getTimestamp().getValue();
        	positionToFirstEvent();
        	
        	setTimeRange( new TmfTimeRange( new LttngTimestamp(starTime), 
  				    						new LttngTimestamp(endTime)
                  		 ) );
        }
    }
    
    
    private LTTngTextTrace(LTTngTrace oldStream) throws Exception { 
    	super(null);
    	throw new Exception("Copy constructor should never be use with a LTTngTrace!");
    }
    
    @Override
    public void indexStream() {
    	// Position the trace at the beginning
        TmfTraceContext context = seekLocation(null);
        
       	long nbEvents=1L;
       	fCheckpoints.add(new TmfTraceCheckpoint(new LttngTimestamp(0L), 0L));
       	
       	LttngTimestamp startTime = null;
       	LttngTimestamp lastTime  = new LttngTimestamp();
       	LttngTimestamp timestamp = null;
        Long previousCharRead = 0L;
        
        TextLttngEvent tmpEvent = (TextLttngEvent)getNextEvent(context);
        
        while ( tmpEvent != null) {
        	timestamp = (LttngTimestamp)context.getTimestamp();
        	previousCharRead = nbCharRead;
        	
        	if ( startTime == null ) {
        		startTime = new LttngTimestamp(timestamp.getValue());
        	}
        	
        	if ((++nbEvents % fCacheSize) == 0) {
           		fCheckpoints.add(new TmfTraceCheckpoint(new LttngTimestamp(timestamp.getValue()), previousCharRead));
        	}
        	
        	tmpEvent = (TextLttngEvent)getNextEvent(context);
        }
        
        if (timestamp != null) {
			lastTime.setValue(timestamp.getValue());
				
           		setTimeRange( new TmfTimeRange(startTime, lastTime) );
           		notifyListeners(getTimeRange());
		}
        
        fNbEvents = nbEvents;
        
        if ( showDebug == true ) {
	        for ( int pos=0; pos < fCheckpoints.size(); pos++) {
	        	System.out.print(pos + ": " + "\t");
	        	System.out.print( fCheckpoints.get(pos).getTimestamp() + "\t" );
	        	System.out.println( fCheckpoints.get(pos).getLocation() );
	        }
        }
        
    }
    
    private boolean positionToFirstEvent() {
    	
    	boolean isSuccessful = true;
    	
    	try {
	    	if ( br != null ) {
	    		br.close();
	    		fr.close();
	    	}
	    	
	    	fr = new FileReader(tracepath);
	        br = new BufferedReader(fr);
	        
	        // Skip the 2 lines header
	        br.readLine();
	        br.readLine();
	        
	        // Make sure the event time is consistent
	        eventTimestamp.setValue(0L);
    	}
    	catch (Exception e) {
    		isSuccessful = false;
    	}
    	
    	return isSuccessful;
    }
    
    private void skipToPosition(Long skipPosition) {
    	try {
				if ( showDebug == true ) {
					System.out.println("skipToPosition(Long skipPosition)");
					System.out.println("\tSkipping to : " + skipPosition);
					System.out.println();
				}
				positionToFirstEvent();
				br.skip(skipPosition);
				
				nbCharRead = skipPosition;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @Override
    public TmfTraceContext seekLocation(Object location) {
    	if (location == null) {
    		location = 0L;
    	}
    	
    	TmfTraceContext tmpTraceContext =  new TmfTraceContext(nbCharRead, (LttngTimestamp)currentLttngEvent.getTimestamp(), 0L);
		Long previousCharRead = nbCharRead;
        Long previousTimestamp = currentLttngEvent.getTimestamp().getValue();
    	Long tmploc = (Long)location;
		
		if ( showDebug == true ) {
			System.out.println("seekLocation(Object location)");
    		System.out.println("\ttimestamp: " + (LttngTimestamp)currentLttngEvent.getTimestamp());
    		System.out.println("\tnbCharRead:" + nbCharRead);
    		System.out.println("\tlocation:  " + location);
    		System.out.println();
    	}
		
		if ( tmploc < nbCharRead ) {
			skipToPosition(tmploc);
		}
		
		if ( tmploc > nbCharRead ) {
			while ( tmploc > nbCharRead ) {
				previousCharRead = nbCharRead;
	        	previousTimestamp = currentLttngEvent.getTimestamp().getValue();
				getNextEvent(tmpTraceContext);
			}
		}
		
		tmpTraceContext.setTimestamp(new LttngTimestamp(previousTimestamp));
		tmpTraceContext.setLocation(previousCharRead);
    	
    	return tmpTraceContext;
    }
    
    
    @Override
    public TmfTraceContext seekEvent(TmfTimestamp timestamp) {

    	if (timestamp == null) {
    		timestamp = getStartTime();
    	}
        
    	int index = Collections.binarySearch(fCheckpoints, new TmfTraceCheckpoint(timestamp, 0));
    	
        if (index < 0) {
            index = 0;
        }
        
        Object location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        
    	if ( showDebug == true ) {
    		System.out.println("seekEvent(TmfTimestamp timestamp)");
    		System.out.println("\ttimestamp: " + timestamp.getValue());
    		System.out.println("\tindex:     " + index);
    		System.out.println("\tlocation:  " + location);
    		System.out.println();
    	}
    	
    	// *** HACK ***
    	// We only know the timestamp AFTER we read the actual event
    	// For this reason, we save the current "position" in byte (nbCharRead) and we seek back 1 event after we find our event
        TmfTraceContext currentEventContext = seekLocation(location);
        
        Long previousCharRead = nbCharRead;
        Long previousTimestamp = currentLttngEvent.getTimestamp().getValue();
        TmfEvent event = getNextEvent(currentEventContext);
        
        while ( (event != null) && (event.getTimestamp().getValue() < timestamp.getValue()) ) {
        	previousCharRead = nbCharRead;
        	previousTimestamp = currentLttngEvent.getTimestamp().getValue();
        	event = getNextEvent(currentEventContext);
        }
        
        if ( event != null ) {
        	skipToPosition(previousCharRead);
        	currentEventContext.setLocation(previousCharRead);
        	currentEventContext.setTimestamp(new LttngTimestamp(previousTimestamp));
        }
        
        return currentEventContext;
    }
    
    @Override
    public TmfTraceContext seekEvent(long position) {
    	
        int checkPointPos = ((int)position / fCacheSize);
        
        Object location;
        location = ( checkPointPos < fCheckpoints.size()) ? fCheckpoints.elementAt(checkPointPos).getLocation() : null;
        
        long index = ((position / fCacheSize)*fCacheSize)-1;
        
        if ( index < 0) { 
        	index = 0; 
        }
        
    	if ( showDebug == true ) {
    		System.out.println("seekEvent(long position)");
    		System.out.println("\tposition:  " + position);
    		System.out.println("\tindex:     " + index);
    		System.out.println("\tlocation:  " + location);
    		System.out.println();
    	}
        TmfTraceContext currentEventContext = seekLocation(location);
        Long previousCharRead = (Long)currentEventContext.getLocation();
        Long previousTimestamp = currentEventContext.getTimestamp().getValue();
        
        TmfEvent event = null;
        while (index < position) {
        	event = getNextEvent(currentEventContext);
        	previousCharRead = nbCharRead;
        	previousTimestamp = currentLttngEvent.getTimestamp().getValue();
        	index++;
        }
        
        if ( event != null ) {
        	skipToPosition(previousCharRead);
        	currentEventContext.setLocation(previousCharRead);
        	currentEventContext.setTimestamp(new LttngTimestamp(previousTimestamp));
        }
        
        return currentEventContext;
    }
    
    @Override
    public TmfEvent getNextEvent(TmfTraceContext context) {
    	
    	// All parsing variables declared here so to be able to print them into the catch if needed
    	String tmpContent = null;
    	int tmpCurIndex = 0;
    	int tmpPrevIndex = 0;
    	
    	String tracefile = "";
		long tmpCpu = 0;
		String marker = "";
    	
		long tmpSecond = 0;
		long tmpNanosecond = 0;
		
		String parsedPayload = "";
		String markerName = "";
		Object payload = "";
		
		HashMap<String, LttngEventField> fieldsMap = null;
		
		LttngEvent returnedEvent = null;
		
    	try {
    		tmpContent = br.readLine();
    		
    		if (tmpContent != null) {
    			// *** NOTE : 
    			// -1 is the skip the end of line (\n)
    			nbCharRead += (tmpContent.length()+1);
	    		
    			if ( (currentLttngEvent != null) && (currentLttngEvent.getContent().getRawContent() != null) ) {
    				currentLttngEvent.getContent().emptyContent();
    			}
    			
	    		// EventSource is always the same for now :
	    		eventSource.setSourceId("Kernel Core");
	    		
	    		
	    		// Tracefile and marker are first in the file
	    		// Sound like : 
	    		//		kernel.syscall_entry:
	    		tmpCurIndex = tmpContent.indexOf(".", tmpPrevIndex);
	    		
	    		// *** HACK ***
	    		// Evil exit case here : the two last line of the text dump does not contain "."
	    		// We should check in a better way (string comparaison and such) but it make the whole process to weight a lot more
	    		// Conclusion : this is ugly but fast.
	    		if ( tmpCurIndex < 0 ) {
	    			if ( showDebug == true ) {
	    				System.out.println("END OF FILE.");
	    				System.out.println();
	    			}
	    			return returnedEvent;
	    		}
	    		
	    		tracefile = tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim();
	    		/*System.out.println(tracefile);*/
	    		
	    		tmpPrevIndex = tmpCurIndex;
	    		tmpCurIndex = tmpContent.indexOf(":", tmpPrevIndex);
	    		marker = tmpContent.substring(tmpPrevIndex+1, tmpCurIndex ).trim();
	    		/*System.out.println(marker);*/
	    		
	    		// Timestamp is next but is presented in second.milisecond format, we have to split them
	    		// Sound like : 
	    		//		952.162637168
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(".", tmpPrevIndex);
	    		tmpSecond = Long.parseLong( tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim() );
	    		/*System.out.println(tmpSecond);*/
	    		
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(" ", tmpPrevIndex);
	    		tmpNanosecond = Long.parseLong( tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim() );
	    		/*System.out.println(tmpNanosecond);*/
	    		
	    		// We have enough information here to set the timestamp
	    		eventTimestamp.setValue( (tmpSecond * 1000000000) + tmpNanosecond );
	    		/*System.out.println(eventTimestamp.toString());*/
	    		
	    		// Next field is the reference
	    		// A long string enclosed by parenthesis and ending with a comma
	    		// 		(/home/william/workspace/org.eclipse.linuxtools.lttng.tests/traceset/trace-618339events-1293lost-1cpu/kernel_0),
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf("(", tmpPrevIndex);
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf("),", tmpPrevIndex);
	    		String fullTracePath = tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim();
	    		/*System.out.println(fullTracePath);*/
	    		
	    		eventReference.setValue(fullTracePath);
	    		String traceName = fullTracePath.substring(fullTracePath.lastIndexOf("/")+1).trim();
	    		/*System.out.println(traceName);*/
	    		eventReference.setTracepath(traceName);
	    		
	    		
	    		// The next few fields are relatives to the state system (pid, ppid, etc...) we need to skip them.
	    		// They should be like the following :
	    		//		4175, 4175, hal-acl-tool, , 4168,
	    		
	    		// 1st comma
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		// 2nd comma
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		// 3rd comma
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		// 4th comma
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		// 5th comma
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		// 6th comma
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		
	    		// The next field is the CPU, in hexadecimal format
	    		// Should be like : 
	    		//		0x0,
	    		tmpPrevIndex = tmpCurIndex+1;
	    		tmpCurIndex = tmpContent.indexOf("0x", tmpPrevIndex);
	    		tmpPrevIndex = tmpCurIndex+2;
	    		
	    		tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex);
	    		tmpCpu = Long.parseLong( tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim() );
	    		
	    		// Set the cpu number of trace if we found a "new" cpu
	    		if ( cpuNumber < (tmpCpu + 1) ) {
	    			cpuNumber = (int)(tmpCpu+1);
	    		}
	    		/*System.out.println(tmpCpu);*/
	    		
	    		
	    		// The last field is the parsed content
	    		// It is enclosed by { }
	    		// Look like : 
	    		//		SYSCALL { ip = 0xb7f05422, syscall_id = 221 [sys_fcntl64+0x0/0x79] }
	    		//
	    		// NOTE : it seems some state system events do not respect this format as they have no payload. 
	    		//		We will create empty payload then.
	    		int tmpIndex = tmpContent.indexOf("{", tmpPrevIndex);
	    		if ( tmpIndex != -1 ) {
		    		tmpPrevIndex = tmpCurIndex+1;
		    		tmpCurIndex = tmpIndex;
		    		tmpPrevIndex = tmpCurIndex+1;
		    		tmpCurIndex = tmpContent.indexOf("}", tmpPrevIndex);
		    		parsedPayload = tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim();
		    		
		    		// Now add each LttngField
		    		boolean isDone = false;
		    		int tmpIndexBegin = 0;
		    		int tmpIndexEqual = 0;
		    		int tmpIndexEnd = 0;
		    		
		    		fieldsMap = new HashMap<String, LttngEventField>(); 
		    		
		    		while ( isDone == false ) {
		    			tmpIndexEqual = parsedPayload.indexOf("=", (int)tmpIndexBegin);
		    			tmpIndexEnd = parsedPayload.indexOf(", ", (int)tmpIndexEqual);
		    			if ( tmpIndexEnd == -1 ) {
		    				tmpIndexEnd = parsedPayload.length();
		    				isDone = true;
		    			}
		    			
		    			markerName = parsedPayload.substring((int)tmpIndexBegin, (int)tmpIndexEqual-1 ).trim();
		    			payload = ((String)parsedPayload.substring((int)tmpIndexEqual+1, (int)tmpIndexEnd )).replace("\"", " ").trim();
		    			
		    			// Try to cast the payload into the correct type
		    			try {
		    				payload = Long.parseLong((String)payload);
		    			}
		    			catch (NumberFormatException e) { }
		    			
		    			LttngEventField tmpField = new LttngEventField(eventContent, markerName, payload);
		    			fieldsMap.put(markerName, tmpField);
		    			
		    			tmpIndexBegin = tmpIndexEnd+1;
		    		}
	    		}
	    		else {
	    			fieldsMap = new HashMap<String, LttngEventField>(); 
		    		
	    			markerName = "";
	    			payload = "";
	    			
	    			LttngEventField tmpField = new LttngEventField(eventContent, markerName, payload);
	    			fieldsMap.put(markerName, tmpField);
	    		}
	    		
	    		eventContent = new TextLttngEventContent(currentLttngEvent, fieldsMap);
	    		
	    		// We now have what we need for the type
	    		String tmpTypeKey = tracefile + "/" + tmpCpu + "/" + marker;
	    		if ( traceTypes.get(tmpTypeKey) == null ) {
	    			traceTypes.put(tmpTypeKey, new LttngEventType(tracefile, tmpCpu, marker, fieldsMap.keySet().toArray(new String[fieldsMap.size()] )) );
	    		}
	    		
	    		currentLttngEvent.setContent(eventContent);
	    		currentLttngEvent.setType(traceTypes.get(tmpTypeKey));
	    		
	    		context.setTimestamp(eventTimestamp);
	    		context.setLocation(nbCharRead);
	    		
	    		returnedEvent = currentLttngEvent;
    		}
    		else if ( showDebug == true ) {
					System.out.println("NULL READING");
					System.out.println();
					returnedEvent = null;
			}
    	}
    	catch (Exception e) {
    		System.out.println("Pos is :" + nbCharRead);
    		if ( tmpContent != null ) {
    			System.out.println("Erroneous content is :" + tmpContent);
    		}
    		
    		tmpContent = null;
    		e.printStackTrace();
    		returnedEvent = null;
    	}
    	
    	return returnedEvent;
    }
    
    @Override
    public Object getCurrentLocation() {
    	return nbCharRead;
    }
    
    @Override
	public LttngEvent parseEvent(TmfTraceContext context) {
		Long location = null;
		LttngEvent returnedEvent = null;
		
		if ( (currentLttngEvent!= null) && (! currentLttngEvent.getTimestamp().equals(context.getTimestamp()) ) ) {
			seekEvent(context.getTimestamp());
			getNextEvent(context);
		}
		
		if ( currentLttngEvent != null ) {
		    returnedEvent = currentLttngEvent;
		}
		
		location = (Long)getCurrentLocation();
    	
   		context.setLocation(location);
   		context.setTimestamp(currentLttngEvent.getTimestamp());
   		context.incrIndex();
   		
   		return returnedEvent;
    }
    
    public int getCpuNumber() {
    	return cpuNumber;
    }
}


// Redefine event to override method we know won't work with a Text tracefile 
class TextLttngEvent extends LttngEvent {
	
	public TextLttngEvent(	LttngTimestamp timestamp, 
							LttngEventSource source, 
							LttngEventType type, 
							LttngEventContent content, 
							LttngEventReference reference) 
	{
		super(timestamp, source, type, content, reference, null);
	}
	
	public TextLttngEvent(TextLttngEvent oldEvent) {
		this(
				(LttngTimestamp)oldEvent.getTimestamp(), 
				(LttngEventSource)oldEvent.getSource(), 
				(LttngEventType)oldEvent.getType(), 
				(LttngEventContent)oldEvent.getContent(), 
				(LttngEventReference)oldEvent.getReference()
			 );
	}
	
	@Override
	public JniEvent convertEventTmfToJni() {
		System.out.println("WARNING : Cannot use convertEventTmfToJni() on a trace in text format.");
		return null;
	}
	
	@Override
	public void updateJniEventReference(JniEvent newJniEventReference) {
		System.out.println("WARNING : Cannot use updateJniEventReference on a trace in text format. Using null.");
    }
}


class TextLttngEventContent extends LttngEventContent {
	
	public TextLttngEventContent() {
        super();
    }
    
    public TextLttngEventContent(TextLttngEvent thisParent) {
        super(thisParent, null);
    }
    
    public TextLttngEventContent(TextLttngEvent thisParent, HashMap<String, LttngEventField> thisContent) {
        super(thisParent, thisContent);
    }
    
    public TextLttngEventContent(TextLttngEventContent oldContent) {
    	this( (TextLttngEvent)oldContent.fParentEvent, oldContent.getRawContent());
    }
    
    @Override
    public LttngEventField[] getFields() {
    	return getRawContent().values().toArray(new LttngEventField[getRawContent().size()]);
    }
    
    @Override
    public LttngEventField getField(String name) {
        LttngEventField returnedField = getRawContent().get(name);
        
        return returnedField;
    }
    
    @Override
    public LttngEventField getField(int position) {
    	LttngEventField returnedField = null;
    	String label = fParentEvent.getType().getLabel(position);
        
        if ( label != null ) {
            returnedField = this.getField(label);
        }
        
        return returnedField;
    }
}