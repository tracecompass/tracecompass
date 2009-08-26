/**
 * 
 */
package org.eclipse.linuxtools.tmf.trace;


/**
 * @author francois
 *
 */
public class TmfTraceUpdateEvent implements ITmfTraceEvent {

	private final TmfTrace fEventLog;
	
	public TmfTraceUpdateEvent(TmfTrace eventLog) {
		fEventLog = eventLog;
	}

	public TmfTrace getEventLog() {
		return fEventLog;
	}
}
