/**
 * 
 */
package org.eclipse.linuxtools.tmf.trace;


/**
 * @author francois
 *
 */
public class TmfTraceSelectedEvent implements ITmfTraceEvent {

	private final TmfTrace fEventLog;
	
	public TmfTraceSelectedEvent(TmfTrace eventLog) {
		fEventLog = eventLog;
	}

	public TmfTrace getEventLog() {
		return fEventLog;
	}
}
