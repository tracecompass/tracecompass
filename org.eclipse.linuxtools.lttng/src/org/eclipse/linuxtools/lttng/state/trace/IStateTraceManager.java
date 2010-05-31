package org.eclipse.linuxtools.lttng.state.trace;

import org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.request.IRequestStatusListener;
import org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener;
import org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

public interface IStateTraceManager extends ILttExperimentSelectedListener {
	/**
	 * TODO: Not ready for threading
	 * <p>
	 * Read events within specific time window, e.g. time range selection
	 * </p>
	 * 
	 * @param trange
	 * @param source
	 * @param listener
	 * @param processor
	 * @return
	 */
	public abstract ILttngSyntEventRequest executeDataRequest(
			TmfTimeRange trange, Object source,
			IRequestStatusListener listener, ITransEventProcessor processor);

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
	 * Returns the State model instance associated with this Trace
	 * 
	 * @return
	 */
	public abstract LttngTraceState getStateModel();

}