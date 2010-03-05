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
import java.util.HashMap;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.event.LttngEventSource;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

public class LTTngTextTrace extends TmfTrace<LttngEvent> implements ITmfTrace {
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
        super(LttngEvent.class, path, 1);
        
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
        	fIndexPageSize = 1000;
        	
        	// Skip indexing if asked
        	if ( skipIndexing == true ) {
        		fCheckpoints.add(new TmfCheckpoint(new LttngTimestamp(0L), new TmfLocation<Long>(0L)));
        	}
        	else {
        		indexTrace(true);
        	}
        	
        	Long endTime = currentLttngEvent.getTimestamp().getValue();
        	positionToFirstEvent();
        	
        	getNextEvent(new TmfContext(null, 0));
        	Long starTime = currentLttngEvent.getTimestamp().getValue();
        	positionToFirstEvent();
        	
        	setTimeRange( new TmfTimeRange( new LttngTimestamp(starTime), 
  				    						new LttngTimestamp(endTime)
                  		 ) );
        }
    }
    
    
    private LTTngTextTrace(LTTngTrace oldStream) throws Exception { 
    	super(LttngEvent.class, null);
    	throw new Exception("Copy constructor should never be use with a LTTngTrace!");
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
    
    private void skipToPosition(TmfLocation<Long> skip) {
    	try {
    			long skipPosition = skip.getLocation();
    			if ( skipPosition < 0 ) {
    				skipPosition = 0L;
    			}
    		
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
	@SuppressWarnings("unchecked")
	public TmfContext seekLocation(ITmfLocation<?> location) {
    	if (location == null) {
    		location = new TmfLocation<Long>(0L);
    	}

    	if (!((TmfLocation<Long>) location).getLocation().equals(nbCharRead)) {
    		skipToPosition((TmfLocation<Long>) location);
    	}

    	TmfContext tmpTraceContext =  new TmfContext(location, 0L);
    	
    	return tmpTraceContext;
    }
    
     private LttngEvent parseMyNextEvent(TmfContext context) {
    	
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
	    		// We should check in a better way (string comparison and such) but it make the whole process to weight a lot more
	    		// Conclusion : this is ugly but fast.
	    		if ( tmpCurIndex < 0 ) {
	    			if ( showDebug == true ) {
	    				System.out.println("END OF FILE.");
	    				System.out.println();
	    			}
	    			return null;
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
    public ITmfLocation<?> getCurrentLocation() {
    	return new TmfLocation<Long>(nbCharRead);
    }
    
	@Override
	public LttngEvent parseEvent(TmfContext context) {
		context = seekLocation(context.getLocation());
		return parseMyNextEvent(context);
		
    }
    
	public int getCpuNumber() {
    	return cpuNumber;
    }

	@Override
	public ITmfContext setContext(TmfDataRequest<LttngEvent> request) {
		// TODO Auto-generated method stub
		return null;
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