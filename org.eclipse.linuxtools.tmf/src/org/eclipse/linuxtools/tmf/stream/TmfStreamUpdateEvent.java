/**
 * 
 */
package org.eclipse.linuxtools.tmf.stream;

import org.eclipse.linuxtools.tmf.trace.ITmfTraceEvent;

/**
 * @author francois
 *
 */
public class TmfStreamUpdateEvent implements ITmfTraceEvent {

	private final ITmfEventStream fEventStream;
	
	public TmfStreamUpdateEvent(ITmfEventStream stream) {
		fEventStream = stream;
	}

	public ITmfEventStream getEventStream() {
		return fEventStream;
	}
}
