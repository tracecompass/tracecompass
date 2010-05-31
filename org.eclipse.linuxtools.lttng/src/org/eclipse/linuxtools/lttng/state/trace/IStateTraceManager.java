package org.eclipse.linuxtools.lttng.state.trace;

import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

public interface IStateTraceManager extends ILttExperimentSelectedListener {
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
	public abstract ITmfTrace getTrace();

	/**
	 * Restore to the closest checkpoint from TmfTimestamp
	 * <p>
	 * Note : it is heavier to restore by timestamp than by event position,
	 * restore by event position whichever possible.
	 * 
	 * @param eventTime
	 *            The timestamp of the event to restore to
	 * 
	 * @return TmfTimestamp indicates the nearest time used to restore the
	 *         state, null sent if input is invalid
	 */
	public abstract TmfTimestamp restoreCheckPointByTimestamp(
			TmfTimestamp eventTime);

	/**
	 * @return
	 */
	public abstract TmfTimeRange getExperimentTimeWindow();

	/**
	 * Returns the State model instance associated with this Trace and given
	 * checkPointReference e.g. check point building state model, UI state
	 * model, etc.
	 * 
	 * @return
	 */
	public abstract LttngTraceState getStateModel(TmfTimestamp startingCheckPointReference);

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
