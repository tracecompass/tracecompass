/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Adapted to new model-view-controller design
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.AddDialog;
import org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.DeleteDialog;
import org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.ListDialog;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.Config;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphModelListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.LatencyController;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.LatencyGraphModel;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * <b><u>LatencyView</u></b>
 * <p>
 * TmfView displaying the latency views (i.e. the two latency charts).
 * 
 * @author Philippe Sawicki
 */
public class LatencyView extends TmfView implements IGraphModelListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    // The initial window span (in nanoseconds)
    public static long INITIAL_WINDOW_SPAN = (1L * 100 * 1000 * 1000); // .1sec

    /**
     * The view's unique ID.
     */
    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.latency.LatencyView"; //$NON-NLS-1$

    /**
     * A reference to the currently selected experiment.
     */
    protected TmfExperiment<LttngEvent> fExperiment = null;

    /**
     * Parent composite.
     */
    protected Composite fParent;

    /**
     * Graph view.
     */
    protected GraphViewer fGraphViewer;
    
    /**
     * Histogram view.
     */
    protected HistogramViewer fHistogramViewer;

    /**
     * Action executed when the user wants to see the list of matching events.
     */
    protected Action fListMatchingEvents;
    
    /**
     * Action executed when the user wants to add matching events.
     */
    protected Action fAddMatchingEvents;
    
    /**
     * Action executed when the user wants to delete matching events.
     */
    protected Action fDeleteMatchingEvents;
    
    /**
     * Action executed when the user wants to increase the width of the histogram bars.
     */
    protected Action fIncreaseBarWidth;
    
    /**
     * Action executed when the user wants to decrease the width of the histogram bars.
     */
    protected Action fDecreaseBarWidth;

    /**
     * The current histogram window time range.
     */
    protected TmfTimeRange fTimeRange = null;

    /**
     * Controller of the latency model which is responsible to retrieve data from the trace
     */
    final private LatencyController fController;

    /**
     * Flag to notify that TimeSyncSignal was received and is being processed.
     */
    private boolean fSyncSignalReceived = false;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public LatencyView() {
        super(Messages.LatencyView_ViewName);
        fController = LatencyController.getInstance();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Create the UI controls of this view.
     * 
     * @param parent
     *            The composite parent of this view.
     */
    @Override
    public void createPartControl(Composite parent) {
        // Save the parent
        fParent = parent;

        makeActions();
        contributeToActionBars();

        // Add a control listener to handle the view resize events (to redraw the canvas)
        fParent.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent event) {
                fHistogramViewer.clearBackground();
                fGraphViewer.clearBackground();
                fController.handleCompleted();
            }

            @Override
            public void controlResized(ControlEvent event) {
                fHistogramViewer.clearBackground();
                fGraphViewer.clearBackground();
                fController.handleCompleted();
            }
        });

        // ///////////////////////////////////////////////////////////////////////////////////
        // Layout for the whole view, other elements will be in a child composite of this one
        // Contains :
        // Composite layoutSelectionWindow
        // Composite layoutTimesSpinner
        // Composite layoutExperimentHistogram
        // ///////////////////////////////////////////////////////////////////////////////////
        Composite layoutFullView = new Composite(fParent, SWT.FILL);
        FillLayout gridFullView = new FillLayout();
        gridFullView.marginHeight = 0;
        gridFullView.marginWidth = 0;
        layoutFullView.setLayout(gridFullView);

        // Create the graph views
        fGraphViewer = new GraphViewer(layoutFullView, SWT.DOUBLE_BUFFERED);
        fGraphViewer.setDrawLabelEachNTicks(2);
        fGraphViewer.setGraphTitle(Messages.LatencyView_Graphs_Graph_Title);
        fGraphViewer.setXAxisLabel(Messages.LatencyView_Graphs_Graph_XAxisLabel, 40);
        fGraphViewer.setYAxisLabel(Messages.LatencyView_Graphs_Graph_YAxisLabel);

        fHistogramViewer = new HistogramViewer(layoutFullView, SWT.DOUBLE_BUFFERED);
        fHistogramViewer.setDrawLabelEachNTicks(2);
        fHistogramViewer.setGraphTitle(Messages.LatencyView_Graphs_Histogram_Title);
        fHistogramViewer.setXAxisLabel(Messages.LatencyView_Graphs_Histogram_XAxisLabel, 55);
        fHistogramViewer.setYAxisLabel(Messages.LatencyView_Graphs_Histogram_YAxisLabel);

        fController.registerModel(fGraphViewer.getModel());
        fController.registerModel(fHistogramViewer.getModel());
        
        ((LatencyGraphModel)fGraphViewer.getModel()).addGraphModelListener(this);
        
        @SuppressWarnings("unchecked")
        TmfExperiment<TmfEvent> experiment = (TmfExperiment<TmfEvent>) TmfExperiment.getCurrentExperiment();
        if (experiment != null) {

            TmfTimeRange experimentTRange = experiment.getTimeRange();

            if (experimentTRange != TmfTimeRange.Null) {
                TmfExperimentSelectedSignal<TmfEvent> signal = new TmfExperimentSelectedSignal<TmfEvent>(this, experiment);
                experimentSelected(signal);
            }
        }
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "["+ Messages.LatencyView_ViewName+"]";
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<TmfEvent> signal) {
        // Clear the views
        fGraphViewer.clear();
        fHistogramViewer.clear();

        if (fParent != null) {
            // Update the trace reference
            fExperiment = (TmfExperiment<LttngEvent>) signal.getExperiment();

            fTimeRange = TmfTimeRange.Null;
            TmfTimeRange experimentTRange = fExperiment.getTimeRange();

            if (!experimentTRange.equals(TmfTimeRange.Null)) {
                fTimeRange = new TmfTimeRange(experimentTRange.getStartTime(), 
                        new TmfTimestamp(experimentTRange.getStartTime().getValue() + INITIAL_WINDOW_SPAN, experimentTRange.getStartTime().getScale(), experimentTRange.getStartTime().getPrecision()));
                fController.refreshModels(fExperiment, fTimeRange);
            }
        }
    }    
    @TmfSignalHandler
    public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
        if (fTimeRange == TmfTimeRange.Null && signal.getExperiment().equals(fExperiment)) {
            TmfTimeRange experimentTRange = signal.getRange();

            if (experimentTRange != TmfTimeRange.Null) {
                fTimeRange = new TmfTimeRange(experimentTRange.getStartTime(), 
                        new TmfTimestamp(experimentTRange.getStartTime().getValue() + INITIAL_WINDOW_SPAN, experimentTRange.getStartTime().getScale(), experimentTRange.getStartTime().getPrecision()));
                fController.refreshModels(fExperiment, fTimeRange);
            }
        }
    }
    
    @TmfSignalHandler
    public void experimentDisposed(TmfExperimentDisposedSignal<TmfEvent> signal) {
        fTimeRange = TmfTimeRange.Null;
        fExperiment = null;
        fController.clear();
    }

    /**
     * Called when the LatencyView is closed: disposes of the canvas and unregisters models from views.
     */
    @Override
    public void dispose() {
        fController.dispose();
        fController.deregisterModel(fGraphViewer.getModel());
        fController.deregisterModel(fHistogramViewer.getModel());
        ((LatencyGraphModel)fGraphViewer.getModel()).removeGraphModelListener(this);

        fGraphViewer.dispose();
        fHistogramViewer.dispose();

        super.dispose();
    }

    /**
     * Method called when synchronization is active and that the user select an event.
     * 
     * The models will be updated with the new current selected time.
     * 
     * @param signal
     *            Signal received from the framework. Contain the event.
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
        if (signal.getSource() != this) {
            fSyncSignalReceived = true;
            fController.setCurrentEventTime(signal.getCurrentTime().getValue());
            fSyncSignalReceived = false;
        }
    }

    /**
     * Method called when synchronization is active and that the user changed the current time range.

     * The models will be updated with the new time range.
     * 
     * @param signal
     *            Signal received from the framework. Contain the new time range.
     */
    @TmfSignalHandler
    public void synchToTimeRange(TmfRangeSynchSignal signal) {
        if (signal.getSource() != this) {
            // Erase the graph views
            fGraphViewer.clear();
            fHistogramViewer.clear();
            
            TmfTimestamp startTime = signal.getCurrentRange().getStartTime();
            TmfTimestamp endTime = signal.getCurrentRange().getEndTime();
            fTimeRange = new TmfTimeRange(startTime, endTime);

            fController.refreshModels(fExperiment, fTimeRange);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphModelListener#graphModelUpdated()
     */
    @Override
    public void graphModelUpdated() {
        // Nothing to do - update of viewers will be done in the viewers
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphModelListener#currentEventUpdated(long)
     */
    @Override
    public void currentEventUpdated(final long currentEventTime) {
        if (fExperiment != null && 
                !fSyncSignalReceived && // Don't broadcast the current time that was received just before with a time sync signal
                currentEventTime != Config.INVALID_EVENT_TIME) {

            // Queue update in the event request queue 
            TmfTimeRange timeRange = new TmfTimeRange(new TmfTimestamp(currentEventTime, Config.TIME_SCALE), TmfTimestamp.BigCrunch);
            TmfEventRequest<LttngEvent> request = new TmfEventRequest<LttngEvent>(LttngEvent.class, timeRange, 0, 1, ExecutionType.FOREGROUND) {
                @Override
                public void handleCompleted() {
                    broadcast(new TmfTimeSynchSignal(this, new TmfTimestamp(currentEventTime, Config.TIME_SCALE)));
                }
            };
            fExperiment.sendRequest(request);
        }
    }
    
    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------
    
    /**
     * Fills the local pull down menu.
     * @param manager
     *            The menu manager.
     */
    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(new Separator());
        manager.add(fIncreaseBarWidth);
        manager.add(fDecreaseBarWidth);
        manager.add(new Separator());
        manager.add(fListMatchingEvents);
        manager.add(fAddMatchingEvents);
        manager.add(fDeleteMatchingEvents);
        manager.add(new Separator());
    }

    /**
     * Fills the local toolbar.
     * @param manager
     *            The toolbar manager
     */
    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(new Separator());
        manager.add(fIncreaseBarWidth);
        manager.add(fDecreaseBarWidth);
        manager.add(new Separator());
        manager.add(fListMatchingEvents);
        manager.add(fAddMatchingEvents);
        manager.add(fDeleteMatchingEvents);
        manager.add(new Separator());
    }

    /**
     * Creates the actions required by the dialog events.
     */
    private void makeActions() {
        // Increase the histogram bar width
        fIncreaseBarWidth = new Action() {
            @Override
            public void run() {
                fHistogramViewer.increaseBarWidth();
                fGraphViewer.increaseBarWidth();
            }
        };
        String tooltipText = Messages.LatencyView_Action_IncreaseBarWidth_Tooltip;
        fIncreaseBarWidth.setText(tooltipText);
        fIncreaseBarWidth.setToolTipText(tooltipText);
        fIncreaseBarWidth.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Messages.LatencyView_tmf_UI, "icons/elcl16/increasebar_button.gif")); //$NON-NLS-1$

        // Decrease the histogram bar width
        fDecreaseBarWidth = new Action() {
            @Override
            public void run() {
                fHistogramViewer.decreaseBarWidth();
                fGraphViewer.decreaseBarWidth();
            }
        };
        tooltipText = Messages.LatencyView_Action_DecreaseBarWidth_Tooltip;
        fDecreaseBarWidth.setText(tooltipText);
        fDecreaseBarWidth.setToolTipText(tooltipText);
        fDecreaseBarWidth.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Messages.LatencyView_tmf_UI, "icons/elcl16/decreasebar_button.gif")); //$NON-NLS-1$

        // List matching events dialog
        fListMatchingEvents = new Action() {
            @Override
            public void run() {
                ListDialog listDialog = new ListDialog(fParent.getShell(), Messages.LatencyView_Dialogs_ListEvents_Title, Messages.LatencyView_Dialogs_ListEvents_Message);
                listDialog.create();
                listDialog.open();
            }
        };
        tooltipText = Messages.LatencyView_Action_ListEvents_Tooltip;
        fListMatchingEvents.setText(tooltipText);
        fListMatchingEvents.setToolTipText(tooltipText);
        fListMatchingEvents.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Messages.LatencyView_tmf_UI, "icons/eview16/events_view.gif")); //$NON-NLS-1$

        // Add matching events dialog
        fAddMatchingEvents = new Action() {
            @Override
            public void run() {
                AddDialog addDialog = new AddDialog(fParent.getShell(), Messages.LatencyView_Dialogs_AddEvents_Title, Messages.LatencyView_Dialogs_AddEvents_Message);
                addDialog.create();
                addDialog.open();
            }
        };
        tooltipText = Messages.LatencyView_Action_AddEvents_Tooltip;
        fAddMatchingEvents.setText(tooltipText);
        fAddMatchingEvents.setToolTipText(tooltipText);
        fAddMatchingEvents.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Messages.LatencyView_tmf_UI, "icons/elcl16/add_button.gif")); //$NON-NLS-1$

        // Remove matching events dialog
        fDeleteMatchingEvents = new Action() {
            @Override
            public void run() {
                DeleteDialog deleteDialog = new DeleteDialog(fParent.getShell(), Messages.LatencyView_Dialogs_DeleteEvents_Title,
                        Messages.LatencyView_Dialogs_DeleteEvents_Message);
                deleteDialog.create();
                deleteDialog.open();
            }
        };
        tooltipText = Messages.LatencyView_Action_DeleteEvents_Tooltip;
        fDeleteMatchingEvents.setText(tooltipText);
        fDeleteMatchingEvents.setToolTipText(tooltipText);
        fDeleteMatchingEvents.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Messages.LatencyView_tmf_UI, "icons/elcl16/delete_button.gif")); //$NON-NLS-1$
    }

    /**
     * Build the toolbar and menu by adding action buttons for dialogs.
     */
    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }
}