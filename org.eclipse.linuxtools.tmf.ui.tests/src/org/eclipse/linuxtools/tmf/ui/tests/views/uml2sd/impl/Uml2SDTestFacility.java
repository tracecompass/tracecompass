/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.stubs.trace.TmfTraceStub;
import org.eclipse.linuxtools.tmf.ui.tests.TmfUITestPlugin;
import org.eclipse.linuxtools.tmf.ui.tests.uml2sd.trace.TmfUml2SDTestTrace;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets.Criteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets.FilterCriteria;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets.FilterListDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.impl.TmfUml2SDSyncLoader;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 *  Singleton class to facilitate the test cases. Creates UML2SD view and loader objects as well as provides
 *  utility methods for interacting with the loader/view.  
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
    private TmfExperiment<TmfEvent> fExperiment = null;
    
    private boolean fIsInitialized = false;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    private Uml2SDTestFacility() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    public static Uml2SDTestFacility getInstance() {
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

            fParser = new TmfUml2SDTestTrace();
            fTrace = setupTrace(fParser);
            
            IViewPart view;
            try {
                view = PlatformUI.getWorkbench()
                                 .getActiveWorkbenchWindow()
                                 .getActivePage()
                                 .showView("org.eclipse.linuxtools.tmf.ui.tmfUml2SDSyncView"); //$NON-NLS-1$
                
            } catch (PartInitException e) {
                throw new RuntimeException(e);
            }

            fSdView = (SDView) view;
            fLoader = (TmfUml2SDSyncLoader)LoadersManager.getInstance().createLoader(
                    "org.eclipse.linuxtools.tmf.ui.views.uml2sd.impl.TmfUml2SDSyncLoader", fSdView); //$NON-NLS-1$

            delay(3000);
            fIsInitialized = true;
        }
    }

    
    private TmfTraceStub setupTrace(ITmfEventParser parser) {
        
            try {
                // Create test trace object
                URL location = FileLocator.find(TmfUITestPlugin.getDefault().getBundle(), new Path("tracesets/sdEvents"), null); //$NON-NLS-1$
                File test = new File(FileLocator.toFileURL(location).toURI());
                return new TmfTraceStub(test.getPath(), 500, true, parser);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
    }
    
    /**
     * Dispose the resource
     */
    public void dispose() {
        if (fIsInitialized) {
            fExperiment.dispose();

            // Wait for all Eclipse jobs to finish
            waitForJobs();

            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(fSdView);
            fIsInitialized = false;
        }
    }
    
    /**
     * Sleeps current thread or GUI thread for a given time. 
     * @param waitTimeMillis
     */
    public void delay(long waitTimeMillis) {
        Display display = Display.getCurrent();
        if (display != null) {
            long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
            while(System.currentTimeMillis() < endTimeMillis) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
                display.update();
            }
        } else {
            try {
                Thread.sleep(waitTimeMillis);
            } catch (InterruptedException e) {
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
    public TmfExperiment<TmfEvent> getExperiment() {
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
    public void setPage(int page) {
        fLoader.pageNumberChanged(page);;
        fLoader.waitForCompletion();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }
    
    /**
     * @see org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.impl.selectExperiment(boolean)
     */
    public void selectExperiment() {
        this.selectExperiment(true);
    }
    
    /**
     * Selects the experiment. 
     * @param wait true to wait for indexing to finish else false
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void selectExperiment(boolean wait) {
        fTrace = setupTrace(fParser);

        ITmfTrace traces[] = new ITmfTrace[1];
        traces[0] = fTrace;
        fExperiment = new TmfExperiment<TmfEvent>(TmfEvent.class, "TestExperiment", traces); //$NON-NLS-1$
        fTrace.broadcast(new TmfExperimentSelectedSignal<TmfEvent>(this, fExperiment));
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
        fExperiment.dispose();
        delay(IUml2SDTestConstants.GUI_REFESH_DELAY);
    }
    
    /**
     * Creates some global filter criteria and saves them to disk. 
     */
    public void createFilterCriteria() {
        // Create Filter Criteria and save tme
        List<FilterCriteria> filterToSave = new ArrayList<FilterCriteria>();
        Criteria criteria = new Criteria();
        criteria.setLifeLineSelected(true);
        criteria.setExpression(IUml2SDTestConstants.FIRST_PLAYER_NAME);
        filterToSave.add(new FilterCriteria(criteria, true, false));
        
        criteria = new Criteria();
        criteria.setSyncMessageSelected(true);
        criteria.setExpression("BALL_.*"); //$NON-NLS-1$
        filterToSave.add(new FilterCriteria(criteria, true, false));
        FilterListDialog.saveFiltersCriteria(filterToSave);
    }


}
