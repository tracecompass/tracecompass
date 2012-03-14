package org.eclipse.linuxtools.internal.lttng.core.state.trace;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;

public interface IStateTraceManager {
//	/**
//	 * TODO: Not ready for threading
//	 * <p>
//	 * Read events within specific time window, e.g. time range selection
//	 * </p>
//	 * 
//	 * @param trange
//	 * @param source
//	 * @param listener
//	 * @param processor
//	 * @return
//	 */
//	public abstract ILttngSyntEventRequest executeDataRequest(
//			TmfTimeRange trange, Object source,
//			IRequestStatusListener listener, ITransEventProcessor processor);

	/**
	 * used to obtain details on the log associated with this manager e.g.
	 * logid.
	 * 
	 * @return
	 */
	public abstract ITmfTrace<?> getStateTrace();

	/**
	 * Restore to the closest checkpoint from TmfTimestamp
	 * <p>
	 * Note : it is heavier to restore by timestamp than by event position,
	 * restore by event position whichever possible.
	 * 
	 * @param eventTime
	 *            The timestamp of the event to restore to
	 * 
	 * @return TmfCheckpoint indicates the nearest checkpoint used to restore the
	 *         state, null sent if input is invalid
	 */
	public abstract TmfCheckpoint restoreCheckPointByTimestamp(
			ITmfTimestamp eventTime);

	/**
	 * Restore to the closest checkpoint from index
	 * 
	 * @param eventIndex
	 *            The index of the event to restore to
	 * 
	 * @return TmfCheckpoint indicates the nearest checkpoint used to restore the
	 *         state, null sent if input is invalid
	 */
	public abstract TmfCheckpoint restoreCheckPointByIndex(long eventIndex);

	/**
	 * @return
	 */
	public abstract TmfTimeRange getExperimentTimeWindow();

	/**
	 * Returns the State model used to build the check points for the state
	 * system
	 * 
	 * @return
	 */
	public abstract LttngTraceState getCheckPointStateModel();

	/**
	 * Returns the State model instance associated with this Trace i.e. not the
	 * checkpoint build state model
	 * Returns the State model instance associated with this Trace
	 * 
	 * @return
	 */
	public abstract LttngTraceState getStateModel();

		
	/**
	 * Reset previously stored check points, and initialize the associated state
	 * model
	 */
	public void clearCheckPoints();

	/**
	 * handles incoming events used to build the associated check points, The
	 * user must call clearCheckPoints before the processing the first synthetic
	 * event.
	 * 
	 * @param synEvent
	 * @param eventCount
	 */
	public void handleEvent(LttngSyntheticEvent synEvent, Long eventCount);
}
