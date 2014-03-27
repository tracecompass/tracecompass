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

package org.eclipse.linuxtools.lttng2.ui.tests.control.model.component;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

/**
 *  Singleton class to facilitate the test cases. Creates UML2SD view and loader objects as well as provides
 *  utility methods for interacting with the loader/view.
 */
@SuppressWarnings("javadoc")
public class TraceControlTestFacility {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    public final static int WAIT_FOR_JOBS_DELAY = 50;
    public final static int GUI_REFESH_DELAY = 500;

    public final static String DIRECTORY = "testfiles";
    public final static String COMMAND_CATEGORY_PREFIX = "org.eclipse.linuxtools.internal.lttng2.ui.commands.control.";
    public final static String SCEN_INIT_TEST = "Initialize";
    public final static String SCEN_SCENARIO_SESSION_HANDLING = "SessionHandling";
    public final static String SCEN_SCENARIO_SESSION_HANDLING_WITH_PATH = "SessionHandlingWithPath";

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
                        .findView("org.eclipse.ui.internal.introview");

                if (view != null) {
                    PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage().hideView(view);
                }

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


    /**
     * Disposes the facility (and GUI)
     */
    public void dispose() {
        if (fIsInitialized) {
            waitForJobs();

            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(fControlView);
            fIsInitialized = false;
        }
    }

    /**
     * Creates a delay for given time.
     * @param waitTimeMillis - time in milli seconds
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
            delay(WAIT_FOR_JOBS_DELAY);
        }
    }

    /**
     * @return current control view
     */
    public ControlView getControlView() {
        return fControlView;
    }

    /**
     * Executes an Eclipse command with command ID after selecting passed component
     * @param component - component to select in the tree
     * @param commandId - command ID
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void executeCommand(ITraceControlComponent component, String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        setSelection(component);
        executeCommand(commandId);
    }

    /**
     * Executes an Eclipse command with command ID after selecting passed components
     * @param components - array of components to select in the tree
     * @param commandId - command ID
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void executeCommand(ITraceControlComponent[] components, String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        setSelection(components);
        executeCommand(commandId);
    }

    /**
     * Executes an Eclipse command with command ID
     * @param commandId
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void executeCommand(String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        IHandlerService handlerService = (IHandlerService) fControlView.getSite().getService(IHandlerService.class);
        handlerService.executeCommand(COMMAND_CATEGORY_PREFIX + commandId, null);
        waitForJobs();
    }

    /**
     * Selects passed component
     * @param component - component to select in the tree
     * @param commandId - command ID
     */
    public void setSelection(ITraceControlComponent component) {
        fControlView.setSelection(component);
        // Selection is done in own job
        waitForJobs();
    }


    /**
     * Selects passed components
     * @param components - array of component to select in the tree
     * @param commandId - command ID
     */
    public void setSelection(ITraceControlComponent[] components) {
        fControlView.setSelection(components);

        // Selection is done in own job
        waitForJobs();
    }

    /**
     * Creates session on passed session group.
     * @param group - session group
     * @return - trace session group if it's successful else null
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public TraceSessionComponent createSession(ITraceControlComponent group) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(group, "createSession");

        ITraceControlComponent[] sessions = group.getChildren();
        if ((sessions == null) || (sessions.length == 0)) {
            return null;
        }
        return (TraceSessionComponent)sessions[0];
    }

    /**
     * Destroys a given session.
     * @param session - session to destroy
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void destroySession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(session, "destroySession");
    }

    /**
     * Starts a given session
     * @param session - session to start
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void startSession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(session, "start");
    }

    /**
     * Stops a given session
     * @param session - session to stop
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void stopSession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(session, "stop");
    }
}
