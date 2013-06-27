/**********************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.uml2sd.ITmfSyncSequenceDiagramEvent;
import org.eclipse.linuxtools.tmf.core.uml2sd.TmfSyncSequenceDiagramEvent;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterListDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * <p>
 * This class is a reference implementation of the
 * <code>org.eclipse.linuxtools.tmf.ui.Uml2SDLoader</code> extension point. It
 * provides a Sequence Diagram loader for a user space trace with specific trace
 * content for sending and receiving signals between components. I also includes
 * a default implementation for the <code>ITmfEvent</code> parsing.
 * </p>
 *
 * The class <code>TmfUml2SDSyncLoader</code> analyzes events from type
 * <code>ITmfEvent</code> and creates events type
 * <code>ITmfSyncSequenceDiagramEvent</code> if the <code>ITmfEvent</code>
 * contains all relevant information. The analysis checks that the event type
 * strings contains either string SEND or RECEIVE. If event type matches these
 * key words, the analyzer will look for strings sender, receiver and signal in
 * the event fields of type <code>ITmfEventField</code>. If all the data is
 * found a sequence diagram event from can be created. Note that Sync Messages
 * are assumed, which means start and end time are the same. <br>
 * <br>
 * The parsing of the <code>ITmfEvent</code> is done in the method
 * <code>getSequnceDiagramEvent()</code> of class
 * <code>TmfUml2SDSyncLoader</code>. By extending the class
 * <code>TmfUml2SDSyncLoader</code> and overwriting
 * <code>getSequnceDiagramEvent()</code> a customized parsing algorithm can be
 * implemented.<br>
 * <br>
 * Note that combined traces of multiple components, that contain the trace
 * information about the same interactions are not supported in the class
 * <code>TmfUml2SDSyncLoader</code>.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfUml2SDSyncLoader extends TmfComponent implements IUml2SDLoader, ISDFindProvider, ISDFilterProvider, ISDAdvancedPagingProvider, ISelectionListener  {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Default title name.
     */
    protected static final String TITLE = Messages.TmfUml2SDSyncLoader_ViewName;
    /**
     * Default block size for background request.
     */
    protected static final int DEFAULT_BLOCK_SIZE = 50000;
    /**
     * Maximum number of messages per page.
     */
    protected static final int MAX_NUM_OF_MSG = 10000;

    private static final int INDEXING_THREAD_SLEEP_VALUE = 100;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Experiment attributes
    /**
     * The TMF trace reference.
     * @since 2.0
     */
    protected ITmfTrace fTrace = null;
    /**
     * The current indexing event request.
     */
    protected ITmfEventRequest fIndexRequest = null;
    /**
     * The current request to fill a page.
     */
    protected ITmfEventRequest fPageRequest = null;
    /**
     * Flag whether the time range signal was sent by this loader class or not
     */
    protected volatile boolean fIsSignalSent = false;

    // The view and event attributes
    /**
     * The sequence diagram view reference.
     */
    protected SDView fView = null;
    /**
     * The current sequence diagram frame reference.
     */
    protected Frame fFrame = null;
    /**
     * The list of sequence diagram events of current page.
     */
    protected List<ITmfSyncSequenceDiagramEvent> fEvents = new ArrayList<ITmfSyncSequenceDiagramEvent>();

    // Checkpoint and page attributes
    /**
     * The checkpoints of the whole sequence diagram trace (i.e. start time stamp of each page)
     */
    protected List<TmfTimeRange> fCheckPoints = new ArrayList<TmfTimeRange>(MAX_NUM_OF_MSG);
    /**
     * The current page displayed.
     */
    protected volatile int fCurrentPage = 0;
    /**
     * The current time selected.
     */
    protected ITmfTimestamp fCurrentTime = null;
    /**
     * Flag to specify that selection of message is done by selection or by signal.
     */
    protected volatile boolean fIsSelect = false;

    // Search attributes
    /**
     * The job for searching across pages.
     */
    protected SearchJob fFindJob = null;
    /**
     * List of found nodes within a page.
     */
    protected List<GraphNode> fFindResults = new ArrayList<GraphNode>();
    /**
     * The current find criteria reference
     */
    protected Criteria fFindCriteria = null;
    /**
     * The current find index within the list of found nodes (<code>fFindeResults</code> within a page.
     */
    protected volatile int fCurrentFindIndex = 0;

    // Filter attributes
    /**
     * The list of active filters.
     */
    protected List<FilterCriteria> fFilterCriteria = null;

    // Thread synchronization
    /**
     * The synchronization lock.
     */
    protected ReentrantLock fLock = new ReentrantLock();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public TmfUml2SDSyncLoader() {
        super(TITLE);
    }

    /**
     * Constructor
     *
     * @param name Name of loader
     */
    public TmfUml2SDSyncLoader(String name) {
        super(name);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns the current time if available else null.
     *
     * @return the current time if available else null
     * @since 2.0
     */
    public ITmfTimestamp getCurrentTime() {
        fLock.lock();
        try {
            if (fCurrentTime != null) {
                return fCurrentTime;
            }
            return null;
        } finally {
            fLock.unlock();
        }
    }

    /**
     * Waits for the page request to be completed
     */
    public void waitForCompletion() {
        fLock.lock();
        ITmfEventRequest request = fPageRequest;
        fLock.unlock();
        if (request != null) {
            try {
                request.waitForCompletion();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * Handler for the trace opened signal.
     * @param signal The trace opened signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        loadTrace();
    }


    /**
     * Signal handler for the trace selected signal.
     *
     * Spawns a request to index the trace (checkpoints creation) as well as it fills
     * the first page.
     *
     * @param signal The trace selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        // Update the trace reference
        ITmfTrace trace = signal.getTrace();
        if (!trace.equals(fTrace)) {
            fTrace = trace;
        }
        loadTrace();
    }

    /**
     * Method for loading the current selected trace into the view.
     * Sub-class need to override this method to add the view specific implementation.
     * @since 2.0
     */
    protected void loadTrace() {
        ITmfEventRequest indexRequest = null;
        fLock.lock();

        try {
            final Job job = new IndexingJob("Indexing " + getName() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
            job.setUser(false);
            job.schedule();

            indexRequest = fIndexRequest;

            cancelOngoingRequests();

            TmfTimeRange window = TmfTimeRange.ETERNITY;

            fIndexRequest = new TmfEventRequest(ITmfEvent.class, window, TmfDataRequest.ALL_DATA, DEFAULT_BLOCK_SIZE, ITmfDataRequest.ExecutionType.BACKGROUND) {

                private ITmfTimestamp fFirstTime = null;
                private ITmfTimestamp fLastTime = null;
                private int fNbSeqEvents = 0;
                private final List<ITmfSyncSequenceDiagramEvent> fSdEvents = new ArrayList<ITmfSyncSequenceDiagramEvent>(MAX_NUM_OF_MSG);

                @Override
                public void handleData(ITmfEvent event) {
                    super.handleData(event);

                    ITmfSyncSequenceDiagramEvent sdEvent = getSequenceDiagramEvent(event);

                    if (sdEvent != null) {
                        ++fNbSeqEvents;

                        if (fFirstTime == null) {
                            fFirstTime = event.getTimestamp();
                        }

                        fLastTime = event.getTimestamp();

                        if ((fNbSeqEvents % MAX_NUM_OF_MSG) == 0) {
                            fLock.lock();
                            try {
                                fCheckPoints.add(new TmfTimeRange(fFirstTime, fLastTime));
                                if (fView != null) {
                                    fView.updateCoolBar();
                                }
                            } finally {
                                fLock.unlock();
                            }
                            fFirstTime = null;

                        }

                        if (fNbSeqEvents > MAX_NUM_OF_MSG) {
                            // page is full
                            return;
                        }

                        fSdEvents.add(sdEvent);

                        if (fNbSeqEvents == MAX_NUM_OF_MSG) {
                            fillCurrentPage(fSdEvents);
                        }
                    }
                }

                @Override
                public void handleSuccess() {
                    if ((fFirstTime != null) && (fLastTime != null)) {

                        fLock.lock();
                        try {
                            fCheckPoints.add(new TmfTimeRange(fFirstTime, fLastTime));
                            if (fView != null) {
                                fView.updateCoolBar();
                            }
                        } finally {
                            fLock.unlock();
                        }
                    }

                    if (fNbSeqEvents <= MAX_NUM_OF_MSG) {
                        fillCurrentPage(fSdEvents);
                    }

                    super.handleSuccess();
                }

                @Override
                public void handleCompleted() {
                    if (fEvents.isEmpty()) {
                        fFrame = new Frame();
                        // make sure that view is not null when setting frame
                        SDView sdView;
                        fLock.lock();
                        try {
                            sdView = fView;
                        } finally {
                            fLock.unlock();
                        }
                        if (sdView != null) {
                            sdView.setFrameSync(fFrame);
                        }
                    }
                    super.handleCompleted();
                    job.cancel();
                }
            };

        } finally {
            fLock.unlock();
        }
        if (indexRequest != null && !indexRequest.isCompleted()) {
            indexRequest.cancel();
        }
        resetLoader();
        fTrace.sendRequest(fIndexRequest);
    }

    /**
     * Signal handler for the trace closed signal.
     *
     * @param signal The trace closed signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }
        ITmfEventRequest indexRequest = null;
        fLock.lock();
        try {
            indexRequest = fIndexRequest;
            fIndexRequest = null;

            cancelOngoingRequests();

            if (fFilterCriteria != null) {
                fFilterCriteria.clear();
            }

            FilterListDialog.deactivateSavedGlobalFilters();
        } finally {
            fTrace = null;
            fLock.unlock();
        }
        if (indexRequest != null && !indexRequest.isCompleted()) {
            indexRequest.cancel();
        }

        resetLoader();
    }

    /**
     * Moves to the page that contains the time provided by the signal. The messages will be selected
     * if the provided time is the time of a message.
     *
     * @param signal The Time synch signal.
     */
    @TmfSignalHandler
    public void synchToTime(TmfTimeSynchSignal signal) {
        fLock.lock();
        try {
            if ((signal.getSource() != this) && (fFrame != null) && (fCheckPoints.size() > 0)) {
                fCurrentTime = signal.getBeginTime();
                fIsSelect = true;
                moveToMessage();
            }
        } finally {
            fLock.unlock();
        }
    }

    /**
     * Moves to the page that contains the current time provided by signal.
     * No message will be selected however the focus will be set to the message
     * if the provided time is the time of a message.
     *
     * @param signal The time range sync signal
     */
    @TmfSignalHandler
    public void synchToTimeRange(TmfRangeSynchSignal signal) {
        fLock.lock();
        try {
            if ((signal.getSource() != this) && (fFrame != null) && !fIsSignalSent && (fCheckPoints.size() > 0)) {
                TmfTimeRange newTimeRange = signal.getCurrentRange();

                fIsSelect = false;
                fCurrentTime = newTimeRange.getStartTime();

                moveToMessage();
            }
        } finally {
            fLock.unlock();
        }

    }

    @Override
    public void setViewer(SDView viewer) {

        fLock.lock();
        try {
            fView = viewer;
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
            fView.setSDFindProvider(this);
            fView.setSDPagingProvider(this);
            fView.setSDFilterProvider(this);

            resetLoader();
            IEditorPart editor = fView.getSite().getPage().getActiveEditor();
            if (editor instanceof ITmfTraceEditor) {
                ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
                if (trace != null) {
                    traceSelected(new TmfTraceSelectedSignal(this, trace));
                }
            }
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public String getTitleString() {
        return getName();
    }

    @Override
    public void dispose() {
       super.dispose();
       ITmfEventRequest indexRequest = null;
       fLock.lock();
       try {
           IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
           // During Eclipse shutdown the active workbench window is null
           if (window != null) {
               window.getSelectionService().removePostSelectionListener(this);
           }

           indexRequest = fIndexRequest;
           fIndexRequest = null;
           cancelOngoingRequests();

           fView.setSDFindProvider(null);
           fView.setSDPagingProvider(null);
           fView.setSDFilterProvider(null);
           fView = null;
       } finally {
           fLock.unlock();
       }
       if (indexRequest != null && !indexRequest.isCompleted()) {
           indexRequest.cancel();
       }
    }

    @Override
    public boolean isNodeSupported(int nodeType) {
        switch (nodeType) {
        case ISDGraphNodeSupporter.LIFELINE:
        case ISDGraphNodeSupporter.SYNCMESSAGE:
            return true;

        default:
            break;
        }
        return false;
    }

    @Override
    public String getNodeName(int nodeType, String loaderClassName) {
        switch (nodeType) {
        case ISDGraphNodeSupporter.LIFELINE:
            return Messages.TmfUml2SDSyncLoader_CategoryLifeline;
         case ISDGraphNodeSupporter.SYNCMESSAGE:
             return Messages.TmfUml2SDSyncLoader_CategoryMessage;
        default:
            break;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        ISelection sel = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if ((sel != null) && (sel instanceof StructuredSelection)) {
            StructuredSelection stSel = (StructuredSelection) sel;
            if (stSel.getFirstElement() instanceof TmfSyncMessage) {
                TmfSyncMessage syncMsg = ((TmfSyncMessage) stSel.getFirstElement());
                broadcast(new TmfTimeSynchSignal(this, syncMsg.getStartTime()));
            }
        }
    }

    @Override
    public boolean find(Criteria toSearch) {
        fLock.lock();
        try {
            if (fFrame == null) {
                return false;
            }

            if ((fFindResults == null) || (fFindCriteria == null) || !fFindCriteria.compareTo(toSearch)) {
                fFindResults = new CopyOnWriteArrayList<GraphNode>();
                fFindCriteria = toSearch;
                if (fFindCriteria.isLifeLineSelected()) {
                    for (int i = 0; i < fFrame.lifeLinesCount(); i++) {
                        if (fFindCriteria.matches(fFrame.getLifeline(i).getName())) {
                            fFindResults.add(fFrame.getLifeline(i));
                        }
                    }
                }

                ArrayList<GraphNode> msgs = new ArrayList<GraphNode>();
                if (fFindCriteria.isSyncMessageSelected()) {
                    for (int i = 0; i < fFrame.syncMessageCount(); i++) {
                        if (fFindCriteria.matches(fFrame.getSyncMessage(i).getName())) {
                            msgs.add(fFrame.getSyncMessage(i));
                        }
                    }
                }

                if (!msgs.isEmpty()) {
                    fFindResults.addAll(msgs);
                }

                List<GraphNode> selection = fView.getSDWidget().getSelection();
                if ((selection != null) && (selection.size() == 1)) {
                    fCurrentFindIndex = fFindResults.indexOf(selection.get(0)) + 1;
                } else {
                    fCurrentFindIndex = 0;
                }
            } else {
                fCurrentFindIndex++;
            }

            if (fFindResults.size() > fCurrentFindIndex) {
                GraphNode current = fFindResults.get(fCurrentFindIndex);
                fView.getSDWidget().moveTo(current);
                return true;
            }
            fFindResults = null;
            fCurrentFindIndex =0;
            return findInNextPages(fFindCriteria); // search in other page
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void cancel() {
        cancelOngoingRequests();
    }

    @Override
    public boolean filter(List<FilterCriteria> filters) {
        fLock.lock();
        try {
            cancelOngoingRequests();

            if (filters == null) {
                fFilterCriteria =  new ArrayList<FilterCriteria>();
            } else {
                List<FilterCriteria> list = filters;
                fFilterCriteria =  new ArrayList<FilterCriteria>(list);
            }

            fillCurrentPage(fEvents);

        } finally {
            fLock.unlock();
        }
        return true;
    }

    @Override
    public boolean hasNextPage() {
        fLock.lock();
        try {
            int size = fCheckPoints.size();
            if (size > 0) {
                return fCurrentPage < (size - 1);
            }
        } finally {
            fLock.unlock();
        }
        return false;
    }

    @Override
    public boolean hasPrevPage() {
        fLock.lock();
        try {
            return fCurrentPage > 0;
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void nextPage() {
        fLock.lock();
        try {
            // Safety check
            if (fCurrentPage >= (fCheckPoints.size() - 1)) {
                return;
            }

            cancelOngoingRequests();
            fCurrentTime = null;
            fCurrentPage++;
            moveToPage();
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void prevPage() {
        fLock.lock();
        try {
            // Safety check
            if (fCurrentPage <= 0) {
                return;
            }

            cancelOngoingRequests();
            fCurrentTime = null;
            fCurrentPage--;
            moveToPage();
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void firstPage() {
        fLock.lock();
        try {

            cancelOngoingRequests();
            fCurrentTime = null;
            fCurrentPage = 0;
            moveToPage();
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void lastPage() {
        fLock.lock();
        try {
            cancelOngoingRequests();
            fCurrentTime = null;
            fCurrentPage = fCheckPoints.size() - 1;
            moveToPage();
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public int currentPage() {
        fLock.lock();
        try {
            return fCurrentPage;
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public int pagesCount() {
        fLock.lock();
        try {
            return fCheckPoints.size();
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void pageNumberChanged(int pagenNumber) {
        int localPageNumber = pagenNumber;

        fLock.lock();
        try {
            cancelOngoingRequests();

            if (localPageNumber < 0) {
                localPageNumber = 0;
            }
            int size = fCheckPoints.size();
            if (localPageNumber > (size - 1)) {
                localPageNumber = size - 1;
            }
            fCurrentPage = localPageNumber;
            moveToPage();
        } finally {
            fLock.unlock();
        }
    }

    @Override
    public void broadcast(TmfSignal signal) {
        fIsSignalSent = true;
        super.broadcast(signal);
        fIsSignalSent = false;
    }

    /**
     * Cancels any ongoing find operation
     */
    protected void cancelOngoingRequests() {
        fLock.lock();
        ITmfEventRequest pageRequest = null;
        try {
            // Cancel the search thread
            if (fFindJob != null) {
                fFindJob.cancel();
            }

            fFindResults = null;
            fFindCriteria = null;
            fCurrentFindIndex = 0;

            pageRequest = fPageRequest;
            fPageRequest = null;
        } finally {
            fLock.unlock();
        }
        if (pageRequest != null && !pageRequest.isCompleted()) {
            pageRequest.cancel();
        }
    }

    /**
     * Resets loader attributes
     */
    protected  void resetLoader() {
        fLock.lock();
        try {
            fCurrentTime = null;
            fEvents.clear();
            fCheckPoints.clear();
            fCurrentPage = 0;
            fCurrentFindIndex = 0;
            fFindCriteria = null;
            fFindResults = null;
            fView.setFrameSync(new Frame());
            fFrame = null;
        }
        finally {
            fLock.unlock();
        }

    }

    /**
     * Fills current page with sequence diagram content.
     *
     * @param events sequence diagram events
     */
    protected void fillCurrentPage(List<ITmfSyncSequenceDiagramEvent> events) {

        fLock.lock();
        try {
            fEvents = new ArrayList<ITmfSyncSequenceDiagramEvent>(events);
            if (fView != null && !events.isEmpty()) {
                fView.toggleWaitCursorAsync(true);
            }
        } finally {
            fLock.unlock();
        }

        final Frame frame = new Frame();

        if (!events.isEmpty()) {
            Map<String, Lifeline> nodeToLifelineMap = new HashMap<String, Lifeline>();

            frame.setName(Messages.TmfUml2SDSyncLoader_FrameName);

            for (int i = 0; i < events.size(); i++) {

                ITmfSyncSequenceDiagramEvent sdEvent = events.get(i);

                if ((nodeToLifelineMap.get(sdEvent.getSender()) == null) && (!filterLifeLine(sdEvent.getSender()))) {
                    Lifeline lifeline = new Lifeline();
                    lifeline.setName(sdEvent.getSender());
                    nodeToLifelineMap.put(sdEvent.getSender(), lifeline);
                    frame.addLifeLine(lifeline);
                }

                if ((nodeToLifelineMap.get(sdEvent.getReceiver()) == null) && (!filterLifeLine(sdEvent.getReceiver()))) {
                    Lifeline lifeline = new Lifeline();
                    lifeline.setName(sdEvent.getReceiver());
                    nodeToLifelineMap.put(sdEvent.getReceiver(), lifeline);
                    frame.addLifeLine(lifeline);
                }
            }

            int eventOccurence = 1;

            for (int i = 0; i < events.size(); i++) {
                ITmfSyncSequenceDiagramEvent sdEvent = events.get(i);

                // Check message filter
                if (filterMessage(sdEvent)) {
                    continue;
                }

                // Set the message sender and receiver
                Lifeline startLifeline = nodeToLifelineMap.get(sdEvent.getSender());
                Lifeline endLifeline = nodeToLifelineMap.get(sdEvent.getReceiver());

                // Check if any of the lifelines were filtered
                if ((startLifeline == null) || (endLifeline == null)) {
                    continue;
                }

                int tmp = Math.max(startLifeline.getEventOccurrence(), endLifeline.getEventOccurrence());
                eventOccurence = Math.max(eventOccurence, tmp);

                startLifeline.setCurrentEventOccurrence(eventOccurence);
                endLifeline.setCurrentEventOccurrence(eventOccurence);

                TmfSyncMessage message = new TmfSyncMessage(sdEvent, eventOccurence++);

                message.setStartLifeline(startLifeline);
                message.setEndLifeline(endLifeline);

                message.setTime(sdEvent.getStartTime());

                // add the message to the frame
                frame.addMessage(message);

            }
            fLock.lock();
            try {
                if (!fView.getSDWidget().isDisposed()) {
                    fView.getSDWidget().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {

                            fLock.lock();
                            try {
                                // check if view was disposed in the meanwhile
                                if ((fView != null) && (!fView.getSDWidget().isDisposed())) {
                                    fFrame = frame;
                                    fView.setFrame(fFrame);

                                    if (fCurrentTime != null) {
                                        moveToMessageInPage();
                                    }

                                    if (fFindCriteria != null) {
                                        find(fFindCriteria);
                                    }

                                    fView.toggleWaitCursorAsync(false);
                                }
                            }  finally {
                                fLock.unlock();
                            }

                        }
                    });
                }
            }
            finally {
                fLock.unlock();
            }
        }
    }

    /**
     * Moves to a certain message defined by timestamp (across pages)
     */
    protected void moveToMessage() {
        int page = 0;

        fLock.lock();
        try {
            page = getPage(fCurrentTime);

            if (page == fCurrentPage) {
                moveToMessageInPage();
                return;
            }
            fCurrentPage = page;
            moveToPage(false);
        } finally {
            fLock.unlock();
        }
    }

    /**
     * Moves to a certain message defined by timestamp in current page
     */
    protected void moveToMessageInPage() {
        fLock.lock();
        try {
        	if (!fView.getSDWidget().isDisposed()) {
        		// Check for GUI thread
        		if(Display.getCurrent() != null) {
        			// Already in GUI thread - execute directly
        			TmfSyncMessage prevMessage = null;
        			TmfSyncMessage syncMessage = null;
        			boolean isExactTime = false;
        			for (int i = 0; i < fFrame.syncMessageCount(); i++) {
        				if (fFrame.getSyncMessage(i) instanceof TmfSyncMessage) {
        					syncMessage = (TmfSyncMessage) fFrame.getSyncMessage(i);
        					if (syncMessage.getStartTime().compareTo(fCurrentTime, false) == 0) {
        						isExactTime = true;
        						break;
        					} else if ((syncMessage.getStartTime().compareTo(fCurrentTime, false) > 0) && (prevMessage != null)) {
        					    syncMessage = prevMessage;
        					    break;
        					}
        					prevMessage = syncMessage;
        				}
        			}
        			if (fIsSelect && isExactTime) {
        				fView.getSDWidget().moveTo(syncMessage);
        			}
        			else {
        				fView.getSDWidget().ensureVisible(syncMessage);
        				fView.getSDWidget().clearSelection();
        				fView.getSDWidget().redraw();
        			}
        		}
        		else {
        			// Not in GUI thread - queue action in GUI thread.
        			fView.getSDWidget().getDisplay().asyncExec(new Runnable() {
        				@Override
        				public void run() {
        					moveToMessageInPage();
        				}
        			});
        		}
        	}
        }
        finally {
            fLock.unlock();
        }
    }

    /**
     * Moves to a certain message defined by timestamp (across pages)
     */
    protected void moveToPage() {
        moveToPage(true);
    }

    /**
     * Moves to a certain page.
     *
     * @param notifyAll true to broadcast time range signal to other signal handlers else false
     */
    protected void moveToPage(boolean notifyAll) {

        TmfTimeRange window = null;

        fLock.lock();
        try {
            // Safety check
            if (fCurrentPage > fCheckPoints.size()) {
                return;
            }
            window = fCheckPoints.get(fCurrentPage);
        } finally {
            fLock.unlock();
        }

        if (window == null) {
            window = TmfTimeRange.ETERNITY;
        }

        fPageRequest = new TmfEventRequest(ITmfEvent.class, window, TmfDataRequest.ALL_DATA, 1, ITmfDataRequest.ExecutionType.FOREGROUND) {
            private final List<ITmfSyncSequenceDiagramEvent> fSdEvent = new ArrayList<ITmfSyncSequenceDiagramEvent>();

            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);

                ITmfSyncSequenceDiagramEvent sdEvent = getSequenceDiagramEvent(event);

                if (sdEvent != null) {
                    fSdEvent.add(sdEvent);
                }
            }

            @Override
            public void handleSuccess() {
                fillCurrentPage(fSdEvent);
                super.handleSuccess();
            }

        };

        fTrace.sendRequest(fPageRequest);

        if (notifyAll) {
            TmfTimeRange timeRange = getSignalTimeRange(window.getStartTime());
            broadcast(new TmfRangeSynchSignal(this, timeRange));
        }
    }

    /**
     * Gets page that contains timestamp
     *
     * @param time The timestamp
     * @return page that contains the time
     * @since 2.0
     */
    protected int getPage(ITmfTimestamp time) {
        int page;
        int size;
        fLock.lock();
        try {
            size = fCheckPoints.size();
            for (page = 0; page < size; page++) {
                TmfTimeRange timeRange = fCheckPoints.get(page);
                if (timeRange.getEndTime().compareTo(time, false) >= 0) {
                    break;
                }
            }
            if (page >= size) {
                page = size - 1;
            }
            return page;
        } finally {
            fLock.unlock();
        }
    }

    /**
     * Background search in trace for expression in criteria.
     *
     * @param findCriteria The find criteria
     * @return true if background request was started else false
     */
    protected boolean findInNextPages(Criteria findCriteria) {
        fLock.lock();
        try {
            if (fFindJob != null) {
                return true;
            }

            int nextPage = fCurrentPage + 1;

            if ((nextPage) >= fCheckPoints.size()) {
                // we are at the end
                return false;
            }

            TmfTimeRange window = new TmfTimeRange(fCheckPoints.get(nextPage).getStartTime(), fCheckPoints.get(fCheckPoints.size()-1).getEndTime());
            fFindJob = new SearchJob(findCriteria, window);
            fFindJob.schedule();
            fView.toggleWaitCursorAsync(true);
        } finally {
            fLock.unlock();
        }
        return true;
    }

    /**
     * Gets time range for time range signal.
     *
     * @param startTime The start time of time range.
     * @return the time range
     * @since 2.0
     */
    protected TmfTimeRange getSignalTimeRange(ITmfTimestamp startTime) {
        fLock.lock();
        try {
            TmfTimeRange currentRange = TmfTraceManager.getInstance().getCurrentRange();
            long offset = fTrace == null ? 0 : currentRange.getEndTime().getDelta(currentRange.getStartTime()).normalize(0, startTime.getScale()).getValue();
            TmfTimestamp initialEndOfWindow = new TmfTimestamp(startTime.getValue() + offset, startTime.getScale(), startTime.getPrecision());
            return new TmfTimeRange(startTime, initialEndOfWindow);
        }
        finally {
            fLock.unlock();
        }
    }

    /**
     * Checks if filter criteria matches the message name in given SD event.
     *
     * @param sdEvent The SD event to check
     * @return true if match else false.
     */
    protected boolean filterMessage(ITmfSyncSequenceDiagramEvent sdEvent) {
        fLock.lock();
        try {
            if (fFilterCriteria != null) {
                for(FilterCriteria criteria : fFilterCriteria) {
                    if (criteria.isActive() && criteria.getCriteria().isSyncMessageSelected() && criteria.getCriteria().matches(sdEvent.getName())) {
                        return true;
                    }
                }
            }
        } finally {
            fLock.unlock();
        }
        return false;
    }

    /**
     * Checks if filter criteria matches a lifeline name (sender or receiver) in given SD event.
     *
     * @param lifeline the message receiver
     * @return true if match else false.
     */
    protected boolean filterLifeLine(String lifeline) {
        fLock.lock();
        try {
            if (fFilterCriteria != null) {
                for(FilterCriteria criteria : fFilterCriteria) {
                    if (criteria.isActive() && criteria.getCriteria().isLifeLineSelected() && criteria.getCriteria().matches(lifeline)) {
                        return true;
                    }
                }
            }
        } finally {
            fLock.unlock();
        }
        return false;
    }

    /**
     * Job to search in trace for given time range.
     */
    protected class SearchJob extends Job {

        /**
         * The search event request.
         */
        protected final SearchEventRequest fSearchRequest;

        /**
         * Constructor
         *
         * @param findCriteria The search criteria
         * @param window Time range to search in
         * @since 2.0
         */
        public SearchJob(Criteria findCriteria, TmfTimeRange window) {
            super(Messages.TmfUml2SDSyncLoader_SearchJobDescrition);
            fSearchRequest = new SearchEventRequest(window, TmfDataRequest.ALL_DATA, 1, ITmfDataRequest.ExecutionType.FOREGROUND, findCriteria);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            fSearchRequest.setMonitor(monitor);

            fTrace.sendRequest(fSearchRequest);

            try {
                fSearchRequest.waitForCompletion();
            } catch (InterruptedException e) {
                Activator.getDefault().logError("Search request interrupted!", e); //$NON-NLS-1$
            }

            IStatus status = Status.OK_STATUS;
            if (fSearchRequest.isFound() && (fSearchRequest.getFoundTime() != null)) {
                fCurrentTime = fSearchRequest.getFoundTime();

                // Avoid double-selection. Selection will be done when calling find(criteria)
                // after moving to relevant page
                fIsSelect = false;
                if (!fView.getSDWidget().isDisposed()) {
                    fView.getSDWidget().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            moveToMessage();
                        }
                    });
                }
            }
            else {
                if (monitor.isCanceled()) {
                    status = Status.CANCEL_STATUS;
                }
                else {
                    // String was not found
                    status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, Messages.TmfUml2SDSyncLoader_SearchNotFound);
                }
                setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            }
            monitor.done();

            fLock.lock();
            try {
                fView.toggleWaitCursorAsync(false);
                fFindJob = null;
            } finally {
                fLock.unlock();
            }

            return status;
        }

        @Override
        protected void canceling() {
            fSearchRequest.cancel();
            fLock.lock();
            try {
                fFindJob = null;
            } finally {
                fLock.unlock();
            }
        }
    }

    /**
     *  TMF event request for searching within trace.
     */
    protected class SearchEventRequest extends TmfEventRequest {

        /**
         * The find criteria.
         */
        private final Criteria fCriteria;
        /**
         * A progress monitor
         */
        private IProgressMonitor fMonitor;
        /**
         * Flag to indicate that node was found according the criteria .
         */
        private boolean fIsFound = false;
        /**
         * Time stamp of found item.
         */
        private ITmfTimestamp fFoundTime = null;

        /**
         * Constructor
         * @param range @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#TmfEventRequest(...)
         * @param nbRequested @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#handleData(...)
         * @param blockSize @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#handleData(...)
         * @param execType @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#handleData(...)
         * @param criteria The search criteria
         */
        public SearchEventRequest(TmfTimeRange range, int nbRequested, int blockSize, ExecutionType execType, Criteria criteria) {
            this(range, nbRequested, blockSize, execType, criteria, null);
        }

        /**
         * Constructor
         * @param range @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#TmfEventRequest(...)
         * @param nbRequested @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#TmfEventRequest(...)
         * @param blockSize @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#TmfEventRequest(...)
         * @param execType @see org.eclipse.linuxtools.tmf.request.TmfEventRequest#TmfEventRequest(...)
         * @param criteria The search criteria
         * @param monitor progress monitor
         */
        public SearchEventRequest(TmfTimeRange range, int nbRequested, int blockSize, ExecutionType execType, Criteria criteria, IProgressMonitor monitor) {
            super(ITmfEvent.class, range, nbRequested, blockSize, execType);
            fCriteria = new Criteria(criteria);
            fMonitor = monitor;
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);

            if ((fMonitor!= null) && fMonitor.isCanceled()) {
                super.cancel();
                return;
            }

            ITmfSyncSequenceDiagramEvent sdEvent = getSequenceDiagramEvent(event);

            if (sdEvent != null) {

                if (fCriteria.isLifeLineSelected()) {
                    if (fCriteria.matches(sdEvent.getSender())) {
                        fFoundTime = event.getTimestamp();
                        fIsFound = true;
                        super.cancel();
                    }

                    if (fCriteria.matches(sdEvent.getReceiver())) {
                        fFoundTime = event.getTimestamp();
                        fIsFound = true;
                        super.cancel();
                    }
                }

                if (fCriteria.isSyncMessageSelected() && fCriteria.matches(sdEvent.getName())) {
                    fFoundTime = event.getTimestamp();
                    fIsFound = true;
                    super.cancel();
                }
            }
        }

        /**
         * Set progress monitor.
         *
         * @param monitor The monitor to assign
         */
        public void setMonitor(IProgressMonitor monitor) {
            fMonitor = monitor;
        }

        /**
         * Check if find criteria was met.
         *
         * @return true if find criteria was met.
         */
        public boolean isFound() {
            return fIsFound;
        }

        /**
         * Returns timestamp of found time.
         *
         * @return timestamp of found time.
         * @since 2.0
         */
        public ITmfTimestamp getFoundTime() {
            return fFoundTime;
        }
    }

    /**
     * Job class to provide progress monitor feedback.
     *
     * @version 1.0
     * @author Bernd Hufmann
     *
     */
    protected static class IndexingJob extends Job {

        /**
         * @param name The job name
         */
        public IndexingJob(String name) {
            super(name);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            while (!monitor.isCanceled()) {
                try {
                    Thread.sleep(INDEXING_THREAD_SLEEP_VALUE);
                } catch (InterruptedException e) {
                    return Status.OK_STATUS;
                }
            }
            monitor.done();
            return Status.OK_STATUS;
        }
    }


    /**
     * Returns sequence diagram event if details in given event are available else null.
     *
     * @param tmfEvent Event to parse for sequence diagram event details
     * @return sequence diagram event if details are available else null
     * @since 2.0
     */
    protected ITmfSyncSequenceDiagramEvent getSequenceDiagramEvent(ITmfEvent tmfEvent){
        //type = .*RECEIVE.* or .*SEND.*
        //content = sender:<sender name>:receiver:<receiver name>,signal:<signal name>
        String eventType = tmfEvent.getType().toString();
        if (eventType.contains(Messages.TmfUml2SDSyncLoader_EventTypeSend) || eventType.contains(Messages.TmfUml2SDSyncLoader_EventTypeReceive)) {
            Object sender = tmfEvent.getContent().getField(Messages.TmfUml2SDSyncLoader_FieldSender);
            Object receiver = tmfEvent.getContent().getField(Messages.TmfUml2SDSyncLoader_FieldReceiver);
            Object name = tmfEvent.getContent().getField(Messages.TmfUml2SDSyncLoader_FieldSignal);
            if ((sender instanceof ITmfEventField) && (receiver instanceof ITmfEventField) && (name instanceof ITmfEventField)) {
                ITmfSyncSequenceDiagramEvent sdEvent = new TmfSyncSequenceDiagramEvent(tmfEvent,
                                ((ITmfEventField) sender).getValue().toString(),
                                ((ITmfEventField) receiver).getValue().toString(),
                                ((ITmfEventField) name).getValue().toString());

                return sdEvent;
            }
        }
        return null;
    }
}
