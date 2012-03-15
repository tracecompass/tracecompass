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
package org.eclipse.linuxtools.lttng.ui.tests.control.model.component;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.ControlView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

/**
 *  Singleton class to facilitate the test cases. Creates UML2SD view and loader objects as well as provides
 *  utility methods for interacting with the loader/view.  
 */
public class TraceControlTestFacility {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    final static public int WAIT_FOR_JOBS_DELAY = 1000; 
    final static public int GUI_REFESH_DELAY = 500;
    
    final static public String COMMAND_CATEGORY_PREFIX = "org.eclipse.linuxtools.internal.lttng.ui.commands.control."; //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static TraceControlTestFacility fInstance = null;
    private ControlView fControlView = null;
    private boolean fIsInitialized = false;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    private TraceControlTestFacility() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    public static TraceControlTestFacility getInstance() {
        if (fInstance == null) {
            fInstance = new TraceControlTestFacility();
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
                view = PlatformUI.getWorkbench()
                                 .getActiveWorkbenchWindow()
                                 .getActivePage()
                                 .showView(ControlView.ID); 
                
            } catch (PartInitException e) {
                throw new RuntimeException(e);
            }

            fControlView = (ControlView) view;

            delay(3000);
            fIsInitialized = true;
        }
    }

    
    public void dispose() {
        if (fIsInitialized) {
            waitForJobs();

            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(fControlView);
            fIsInitialized = false;
        }
    }
    
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
            delay(WAIT_FOR_JOBS_DELAY);
        }
    }


    /**
     * @return current control view
     */
    public ControlView getControlView() {
        return fControlView;
    }

    public void executeCommand(String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
//        ICommandService commandService = (ICommandService) fControlView.getSite().getService(ICommandService.class);
        IHandlerService handlerService = (IHandlerService) fControlView.getSite().getService(IHandlerService.class);
        handlerService.executeCommand(COMMAND_CATEGORY_PREFIX + commandId, null);
    }

}
