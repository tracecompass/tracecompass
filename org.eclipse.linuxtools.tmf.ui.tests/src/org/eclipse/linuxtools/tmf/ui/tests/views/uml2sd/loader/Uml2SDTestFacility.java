/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.linuxtools.tmf.ui.tests.uml2sd.trace.TmfUml2SDTestTrace;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.FilterListDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfUml2SDSyncLoader;
import org.eclipse.swt.widgets.Display;
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
            fInstance.init();
        }
        return fInstance;
    }

    /**
     * Initial the test facility.
     */
    public void init() {

        if (!fIsInitialized) {

            fParser = new TmfUml2SDTestTrace();
            fTrace = setupTrace(fParser);
            fParser.setTrace(fTrace);

            IViewPart view;
            try {
                // Remove welcome view to avoid interference during test execution
                view = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .findView("org.eclipse.ui.internal.introview");

                if (view != null) {
                    PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage().hideView(view);
                }

                view = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .showView("org.eclipse.linuxtools.tmf.ui.tmfUml2SDSyncView");

            } catch (final PartInitException e) {
                throw new RuntimeException(e);
            }

            fSdView = (SDView) view;
            fLoader = (TmfUml2SDSyncLoader)LoadersManager.getInstance().createLoader(
                    "org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader.TmfUml2SDSyncLoader", fSdView);

            delay(3000);
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
            ITmfTrace trace = fTrace;
            TmfExperiment experiment = fExperiment;
            if (trace == null || experiment == null) {
                throw new IllegalStateException();
            }

            trace.broadcast(new TmfTraceClosedSignal(this, experiment));
            experiment.dispose();

            // Wait for all Eclipse jobs to finish
            waitForJobs();

            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(fSdView);
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
     * Waits for all Eclipse jobs to finish
     */
    public void waitForJobs() {
        while (!Job.getJobManager().isIdle()) {
            delay(IUml2SDTestConstants.WAIT_FOR_JOBS_DELAY);
        }
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
     * @see org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader.Uml2SDTestFacility#selectExperiment(boolean)
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

//        fTrace = setupTrace(fParser);

        final ITmfTrace traces[] = new ITmfTrace[1];
        traces[0] = fTrace;
        fExperiment = new TmfExperiment(ITmfEvent.class, "TestExperiment", traces);
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
