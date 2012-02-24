package org.eclipse.linuxtools.lttng.core.tests;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.core.TraceDebug;
import org.eclipse.linuxtools.lttng.core.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.core.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.core.state.experiment.IStateExperimentManager;
import org.eclipse.linuxtools.lttng.core.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

@SuppressWarnings("nls")
public abstract class LttngTestPreparation extends TestCase {
	// ========================================================================
	// Data
	// ========================================================================
	private final static String ftracepath_T1 = "traceset/trace-15316events_nolost_newformat";
	final static String fTextTracepath_T1 = "traceset/trace-15316events_nolost_newformat.txt";

	private static final Long CHECK_POINT_INTERVAL = 1000L;

	final Long[] expectedEvents_T1 = new Long[20];
	final Long[] requestIntervals_T1 = new Long[32];

	static LTTngTextTrace ftextStream_T1 = null;
	private static LTTngTrace frealStream = null;

	private TmfExperiment<LttngEvent> fTestExperiment = null;
	protected volatile int feventCount = 0;
	protected boolean validSequence = true;

	public LttngTestPreparation() {
		super();
		init();
	}

	public LttngTestPreparation(String name) {
		super(name);
		init();
	}

	protected void init() {
		fillInRequestIntervals();
		fillInExpectedEvents();
		feventCount = 0;
	}

	/**
	 * @return
	 */
	protected TmfExperiment<LttngEvent> prepareExperimentToTest() {
		if (fTestExperiment == null) {
			String expId = "testExperiment";
			int nbTraces = 1;

			// Define traces in experiment
			@SuppressWarnings("unchecked")
            ITmfTrace<LttngEvent>[] traces = new ITmfTrace[nbTraces];
			ITmfTrace<LttngEvent> trace = prepareStreamToTest();
			traces[0] = trace;

			// create experiment and associate traces
			fTestExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class,
					expId, traces, TmfTimestamp.Zero, TmfExperiment.DEFAULT_BLOCK_SIZE, true);
			// fTestExperiment.indexExperiment(waitForCompletion);

			// Set the current selected experiment as the test experiment
			TmfExperimentSelectedSignal<LttngEvent> signal = new TmfExperimentSelectedSignal<LttngEvent>(
					this, fTestExperiment);
			fTestExperiment.experimentSelected(signal);
		}

		return fTestExperiment;
	}

	/**
	 * @return
	 */
	protected TmfExperiment<LttngEvent> prepareTextExperimentToTest() {
		if (fTestExperiment == null) {
			String expId = "testExperiment";
			int nbTraces = 1;

			// Define traces in experiment
            @SuppressWarnings("unchecked")
            ITmfTrace<LttngEvent>[] traces = new ITmfTrace[nbTraces];
			ITmfTrace<LttngEvent> trace = prepareTextStreamToTest();
			traces[0] = trace;

			// create experiment and associate traces
			fTestExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class,
					expId, traces);

			// Set the current selected experiment as the test experiment
			TmfExperimentSelectedSignal<LttngEvent> signal = new TmfExperimentSelectedSignal<LttngEvent>(
					this, fTestExperiment);
			fTestExperiment.experimentSelected(signal);

		}

		return fTestExperiment;
	}

	protected LTTngTrace prepareStreamToTest() {
		if (frealStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(ftracepath_T1),
						null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTrace tmpStream = new LTTngTrace(testfile.getName(), testfile.getPath(), false);
				frealStream = tmpStream;
			} catch (Exception e) {
				System.out.println("ERROR : Could not open " + ftracepath_T1);
				frealStream = null;
			}
		} else {
			frealStream.seekEvent(0L);
		}

		return frealStream;
	}

	protected LTTngTextTrace prepareTextStreamToTest() {
		if (ftextStream_T1 == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(),
						new Path(fTextTracepath_T1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getName(), testfile.getPath());
				ftextStream_T1 = tmpStream;

			} catch (Exception e) {
				System.out.println("ERROR : Could not open " + fTextTracepath_T1);
				ftextStream_T1 = null;
			}
		} else {
			ftextStream_T1.seekEvent(0);
		}

		return ftextStream_T1;
	}

	protected IStateExperimentManager prepareExperimentContext(
			boolean waitForRequest) {
		// Create a new Experiment manager
		IStateExperimentManager expManager = StateManagerFactory
				.getExperimentManager();
		// Configure the interval to create check points so this can be tested
		// with medium size files i.e. default is 50000 events
		StateManagerFactory.setTraceCheckPointInterval(CHECK_POINT_INTERVAL);

		// Lets wait for the request completion to analyse the results
		LttngCoreProviderFactory.getEventProvider(0)
				.setWaitForRequest(waitForRequest);
		return expManager;
	}

	/**
	 * @param <T>
	 * @param k
	 * @param startIdx
	 *            , > 0 and between 0 - 31
	 * @param endIdx
	 *            , > startIdx and between 0 - 31
	 * @return
	 */
	protected <T extends LttngEvent> TmfEventRequest<T> prepareEventRequest(Class<T> k, int startIdx, int endIdx) {
		return prepareEventRequest(k, startIdx, endIdx, true);
	}

	/**
	 * @param <T>
	 * @param k
	 * @param startIdx
	 *            , > 0 and between 0 - 31
	 * @param endIdx
	 *            , > startIdx and between 0 - 31
	 * @param printFirst20
	 *            , print the first expected events vs actual events
	 * @return
	 */
	protected <T extends LttngEvent> TmfEventRequest<T> prepareEventRequest(
			Class<T> k, final int startIdx, int endIdx, final boolean printFirst20) {
		// verify bounds
		if (!(endIdx > startIdx && startIdx >= 0 && endIdx <= 31)) {
			TraceDebug.debug("Event request indexes out of bounds");
			return null;
		}

		int DEFAULT_CHUNK = 1;
	
		// time range
		TmfTimeRange trange = new TmfTimeRange(new LttngTimestamp(
				requestIntervals_T1[startIdx]), new LttngTimestamp(
				requestIntervals_T1[endIdx]));
	
		// request
		validSequence = true;
		TmfEventRequest<T> request = new TmfEventRequest<T>(k, trange, TmfDataRequest.ALL_DATA, DEFAULT_CHUNK) {
	
			@Override
			public void handleData(T event) {
				if (event == null) {
					System.out
							.println("Syntheric Event Received is null, after event: "
									+ feventCount);
					return;
				}
	
				// Listen to only one variant of synthetic event to keep
				// track of
				if (event instanceof LttngSyntheticEvent) {
					if (((LttngSyntheticEvent) event).getSynType() != SequenceInd.BEFORE) {
						return;
					}
				}
	
				// Validating the orders of the first 20 events
				if (printFirst20 && feventCount < 20) {
					long timevalue = event.getTimestamp().getValue();
					if (timevalue != expectedEvents_T1[feventCount]) {
						validSequence = false;
						System.out.println("Expected Event: "
								+ expectedEvents_T1[feventCount] + " actual: "
								+ event.getTimestamp().getValue());
					} else {
						System.out.println("Synthetic Event: " + feventCount
								+ " matched expected time");
					}
				}
	
				// increment count
				incrementCount();
			}

			/**
			 * possibly increased by multiple request threads
			 */
			private synchronized void incrementCount() {
				feventCount++;
			}

			@Override
			public void handleCompleted() {
				// if (isCancelled() || isFailed()) {
				// // No notification to end request handlers
				// } else {
				// // notify the associated end request handlers
				// requestCompleted();
				// }
	
				System.out.println("handleCompleted(request:" + startIdx + ") Number of events processed: " + feventCount);
			}
	
		};
		return request;
	}

	/**
	 * @param <T>
	 * @param k
	 * @param startIdx
	 *            , > 0 and between 0 - 31
	 * @param endIdx
	 *            , > startIdx and between 0 - 31
	 * @param printFirst20
	 *            , print the first expected events vs actual events
	 * @return
	 */
	protected <T extends LttngEvent> TmfEventRequest<T> prepareEventRequest2(
			Class<T> k, final int startIdx, int endIdx, final boolean printFirst20) {
		// verify bounds
		if (!(endIdx > startIdx && startIdx >= 0 && endIdx <= 31)) {
			TraceDebug.debug("Event request indexes out of bounds");
			return null;
		}

		int DEFAULT_CHUNK = 1;
	
		// time range
		TmfTimeRange trange = new TmfTimeRange(new LttngTimestamp(
				requestIntervals_T1[startIdx]), new LttngTimestamp(
				requestIntervals_T1[endIdx]));
	
		// request
		validSequence = true;
		TmfEventRequest<T> request = new TmfEventRequest<T>(k, trange, TmfDataRequest.ALL_DATA, DEFAULT_CHUNK) {
	
			@Override
			public void handleData(T event) {
				if (event == null) {
					System.out
							.println("Syntheric Event Received is null, after event: "
									+ feventCount);
					return;
				}
	
				// Listen to only one variant of synthetic event to keep
				// track of
				if (event instanceof LttngSyntheticEvent) {
					if (((LttngSyntheticEvent) event).getSynType() != SequenceInd.BEFORE) {
						return;
					}
				}
	
				// Validating the orders of the first 20 events
				if (printFirst20 && feventCount < 20) {
					long timevalue = event.getTimestamp().getValue();
					if (timevalue != expectedEvents_T1[feventCount]) {
						validSequence = false;
						System.out.println("Expected Event: "
								+ expectedEvents_T1[feventCount] + " actual: "
								+ event.getTimestamp().getValue());
					} else {
						System.out.println("Synthetic Event: " + feventCount
								+ " matched expected time");
					}
				}
	
				// increment count
				incrementCount();
			}

			/**
			 * possibly increased by multiple request threads
			 */
			private synchronized void incrementCount() {
				feventCount++;
			}

			@Override
			public void handleCompleted() {
				// if (isCancelled() || isFailed()) {
				// // No notification to end request handlers
				// } else {
				// // notify the associated end request handlers
				// requestCompleted();
				// }
	
				System.out.println("handleCompleted(request:" + startIdx + ") Number of events processed: " + feventCount);
			}
	
		};
		return request;
	}

	/**
	 * Validation points
	 */
	private void fillInExpectedEvents() {
		expectedEvents_T1[0] = 13589759412128L;
		expectedEvents_T1[1] = 13589759419903L;
		expectedEvents_T1[2] = 13589759422785L;
		expectedEvents_T1[3] = 13589759425598L;
		expectedEvents_T1[4] = 13589759430979L;
		expectedEvents_T1[5] = 13589759433694L;
		expectedEvents_T1[6] = 13589759436212L;
		expectedEvents_T1[7] = 13589759438797L;
		expectedEvents_T1[8] = 13589759441253L;
		expectedEvents_T1[9] = 13589759444795L;
		expectedEvents_T1[10] = 13589759447800L;
		expectedEvents_T1[11] = 13589759450836L;
		expectedEvents_T1[12] = 13589759453835L;
		expectedEvents_T1[13] = 13589759459351L;
		expectedEvents_T1[14] = 13589759464411L;
		expectedEvents_T1[15] = 13589759467021L;
		expectedEvents_T1[16] = 13589759469668L;
		expectedEvents_T1[17] = 13589759474938L;
		expectedEvents_T1[18] = 13589759477536L;
		expectedEvents_T1[19] = 13589759480485L;
	}

	/**
	 * Intervals for trace 1, separated %500 + last event
	 */
	private void fillInRequestIntervals() {
		requestIntervals_T1[0] = 13589759412128L; /* check point expected */
		requestIntervals_T1[1] = 13589763490945L; /* between check points */
		requestIntervals_T1[2] = 13589778265041L; /* check point expected */
		requestIntervals_T1[3] = 13589783143445L; /* between check points */
		requestIntervals_T1[4] = 13589786300104L; /* check point expected */
		requestIntervals_T1[5] = 13589790722564L; /* between check points */
		requestIntervals_T1[6] = 13589796139823L; /* check point expected */
		requestIntervals_T1[7] = 13589800400562L; /* between check points */
		requestIntervals_T1[8] = 13589801594374L; /* check point expected */
		requestIntervals_T1[9] = 13589802750295L; /* between check points */
		requestIntervals_T1[10] = 13589804071157L; /* check point expected */
		requestIntervals_T1[11] = 13589810124488L; /* between check points */
		requestIntervals_T1[12] = 13589822493183L; /* check point expected */
		requestIntervals_T1[13] = 13589824131273L; /* between check points */
		requestIntervals_T1[14] = 13589825398284L; /* check point expected */
		requestIntervals_T1[15] = 13589826664185L; /* between check points */
		requestIntervals_T1[16] = 13589827811998L; /* check point expected */
		requestIntervals_T1[17] = 13589828915763L; /* between check points */
		requestIntervals_T1[18] = 13589830074220L; /* check point expected */
		requestIntervals_T1[19] = 13589831232050L; /* between check points */
		requestIntervals_T1[20] = 13589832394049L; /* check point expected */
		requestIntervals_T1[21] = 13589833852883L; /* between check points */
		requestIntervals_T1[22] = 13589839626892L; /* check point expected */
		requestIntervals_T1[23] = 13589849509798L; /* between check points */
		requestIntervals_T1[24] = 13589850728538L; /* check point expected */
		requestIntervals_T1[25] = 13589851889230L; /* between check points */
		requestIntervals_T1[26] = 13589853294800L; /* check point expected */
		requestIntervals_T1[27] = 13589859414998L; /* between check points */
		requestIntervals_T1[28] = 13589878046089L; /* check point expected */
		requestIntervals_T1[29] = 13589886468603L; /* between check points */
		requestIntervals_T1[30] = 13589902256918L; /* check point expected */
		requestIntervals_T1[31] = 13589906758692L; /* last event in T1 */
	}

}