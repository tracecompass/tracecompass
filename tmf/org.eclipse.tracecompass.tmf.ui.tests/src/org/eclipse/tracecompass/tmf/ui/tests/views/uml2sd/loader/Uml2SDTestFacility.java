/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfEventParser;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.ui.tests.shared.JobUtils;
import org.eclipse.tracecompass.tmf.ui.tests.uml2sd.trace.TmfUml2SDTestTrace;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.dialogs.FilterListDialog;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.loader.TmfUml2SDSyncLoader;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;

/**
 *  Singleton class to facilitate the test cases. Creates UML2SD view and loader objects as well as provides
 *  utility methods for interacting with the loader/view.
 *
 *  @author Bernd Hufmann
 */
public class Uml2SDTestFacility {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String SD_VIEW_ID = "org.eclipse.linuxtools.tmf.ui.tmfUml2SDSyncView";

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static Uml2SDTestFacility fInstance = null;

    private TmfUml2SDSyncLoader fLoader;
    private SDView fSdView;
    private TmfTraceStub fTrace = null;
    private TmfUml2SDTestTrace    fParser = null;
    private TmfExperiment fExperiment = null;

    private volatile boolean fIsInitialized = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    private Uml2SDTestFacility() {
        fParser = new TmfUml2SDTestTrace();
        fTrace = setupTrace(fParser);
        fParser.setTrace(fTrace);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the singleton instance.
     */
    public synchronized static Uml2SDTestFacility getInstance() {
        if (fInstance == null) {
            fInstance = new Uml2SDTestFacility();
        }
        return fInstance;
    }

    /**
     * Initial the test facility.
     */
    public void init() {

        if (!fIsInitialized) {
            IViewPart view;
            try {
                // Remove welcome view to avoid interference during test execution
                hideView("org.eclipse.ui.internal.introview");
                view = showView(SD_VIEW_ID);
            } catch (final PartInitException e) {
                throw new RuntimeException(e);
            }

            fSdView = (SDView) view;
            fLoader = (TmfUml2SDSyncLoader) LoadersManager.getInstance().createLoader(
                    "org.eclipse.tracecompass.tmf.ui.views.uml2sd.loader.TmfUml2SDSyncLoader", fSdView);

            fIsInitialized = true;
        }
    }

    private TmfTraceStub setupTrace(final ITmfEventParser parser) {

        try {
            // Create test trace object
            final URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path("tracesets/sdEvents"), null);
            final File test = new File(FileLocator.toFileURL(location).toURI());
            return new TmfTraceStub(test.getPath(), 500, true, parser);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (final URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Dispose the resource
     */
    public void dispose() {
        if (fIsInitialized) {
            // Wait for all Eclipse jobs to finish
            waitForJobs();
            hideView(SD_VIEW_ID);
            delay(200);
            fIsInitialized = false;
        }
    }

    /**
     * Sleeps current thread or GUI thread for a given time.
     * @param waitTimeMillis time in milliseconds to wait
     */
    public void delay(final long waitTimeMillis) {
        final Display display = Display.getCurrent();
        if (display != null) {
            final long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
            while(System.currentTimeMillis() < endTimeMillis) {
                if (!display.readAndDispatch()) {
                    // We do not use Display.sleep because it might never wake up
                    // if there is no user interaction
                    try {
                        Thread.sleep(Math.min(waitTimeMillis, 10));
                    } catch (final InterruptedException e) {
                        // Ignored
                    }
                }
                display.update();
            }
        } else {
            try {
                Thread.sleep(waitTimeMillis);
            } catch (final InterruptedException e) {
                // Ignored
            }
        }
    }

    /**
     * Waits for a view to be closed
     *
     * @param viewId
     *                a view id
     */
    public void waitForViewClosed(String viewId) {
        for (int i = 1; i < 5000 && (getViewPart(viewId) != null); i *= 2) {
            delay(i);
        }
    }

    /**
     * Waits for a view to be opened
     *
     * @param viewId
     *                a view id
     */
    public void waitForViewOpened(String viewId) {
        for (int i = 1; i < 5000 && (getViewPart(viewId) == null); i *= 2) {
            delay(i);
        }
    }

    private IViewPart showView(String viewId) throws PartInitException {
        IViewPart view = getViewPart(viewId);

        if (view == null) {
            view = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage().showView(viewId);

            waitForViewOpened(viewId);
        }
        assertNotNull(view);
        return view;
    }

    private void hideView(String viewId) {
        IViewPart view = getViewPart(viewId);
        if (view != null) {
            PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage().hideView(view);
        }
        waitForViewClosed(viewId);
    }

    private static IViewPart getViewPart(String viewId) {
        return PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow()
        .getActivePage()
        .findView(viewId);
    }

    /**
     * Waits for all Eclipse jobs to finish
     */
    public void waitForJobs() {
        JobUtils.waitForJobs();
    }

    /**
     * @return current UML2SD loader
     */
    public TmfUml2SDSyncLoader getLoader() {
        return fLoader;
    }

    /**
     * @return current SD view
     */
    public SDView getSdView() {
        return fSdView;
    }

    /**
     * @return current trace
     */
    public TmfTraceStub getTrace() {
        return fTrace;
    }

    /**
     * @return Trace parser
     */
    public TmfUml2SDTestTrace getParser() {
        return fParser;
    }

    /**
     * @return current experiment.
     */
    public TmfExperiment getExperiment() {
        return fExperiment;
    }

    /**
     * Go to next page;
     */
    public void nextPage() {
        fLoader.nextPage();
        fLoader.waitForCompletion();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * Go to previous page.
     */
    public void prevPage() {
        fLoader.prevPage();
        fLoader.waitForCompletion();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * Go to last page.
     */
    public void lastPage() {
        fLoader.lastPage();
        fLoader.waitForCompletion();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * Go to first page.
     */
    public void firstPage() {
        fLoader.firstPage();
        fLoader.waitForCompletion();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * @param page number to set
     */
    public void setPage(final int page) {
        fLoader.pageNumberChanged(page);
        fLoader.waitForCompletion();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * @see org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader.Uml2SDTestFacility#selectExperiment(boolean)
     */
    public void selectExperiment() {
        this.selectExperiment(true);
    }

    /**
     * Selects the experiment.
     * @param wait true to wait for indexing to finish else false
     */
    public void selectExperiment(final boolean wait) {
        fParser = new TmfUml2SDTestTrace();
        fTrace = setupTrace(fParser);
        fParser.setTrace(fTrace);

        final ITmfTrace traces[] = new ITmfTrace[1];
        traces[0] = fTrace;
        fExperiment = new TmfExperiment(ITmfEvent.class, "TestExperiment",
                traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null) {
            @Override
            protected ITmfTraceIndexer createIndexer(int interval) {
                return new TmfCheckpointIndexer(this, interval);
            }
        };
        fTrace.broadcast(new TmfTraceOpenedSignal(this, fExperiment, null));
        fTrace.broadcast(new TmfTraceSelectedSignal(this, fExperiment));
        if (wait) {
            while (fExperiment.getNbEvents() == 0) {
                delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
            }
            waitForJobs();
            delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
        }
    }

    /**
     * Disposes the experiment.
     */
    public void disposeExperiment() {
        ITmfTrace trace = fTrace;
        TmfExperiment experiment = fExperiment;
        if (trace == null || experiment == null) {
            throw new IllegalStateException();
        }
        trace.broadcast(new TmfTraceClosedSignal(this, experiment));
        experiment.dispose();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }

    /**
     * Creates some global filter criteria and saves them to disk.
     */
    public void createFilterCriteria() {
        // Create Filter Criteria and save tme
        final List<FilterCriteria> filterToSave = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.FIRST_PLAYER_NAME);
        filterToSave.add(new FilterCriteria(criteria, true, false));

        criteria = new Criteria();
        criteria.setSyncMessageSelected(true);
        criteria.setExpression("BALL_.*");
        filterToSave.add(new FilterCriteria(criteria, true, false));
        FilterListDialog.saveFiltersCriteria(filterToSave);
    }


}
