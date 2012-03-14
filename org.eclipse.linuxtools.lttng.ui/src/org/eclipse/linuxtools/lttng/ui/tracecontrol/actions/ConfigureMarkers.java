/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.tracecontrol.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service.ILttControllerService;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.TraceControlConstants;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.dialogs.ConfigureMarkersDialog;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.subsystems.TraceSubSystem;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * <b><u>ConfigureMarkers</u></b>
 * <p>
 * Action implementation to configure markers.
 * </p>
 */
public class ConfigureMarkers implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private List<TargetResource> fSelectedTargets;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ConfigureMarkers() {
        fSelectedTargets = new ArrayList<TargetResource>();
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {

    }

    /**
     * Returns the first of all selected targets.
     * 
     * @return first selected target.
     */
    protected TargetResource getFirstSelectedTarget() {
        if (fSelectedTargets.size() > 0) {
            return (TargetResource) fSelectedTargets.get(0);
        }
        return null;
    }

    /**
     * Returns the SubSystem reference for the selected targets.
     * 
     * @return Trace SubSystem
     */
    protected ISubSystem getSubSystem() {
        return getFirstSelectedTarget().getSubSystem();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction arg0) {
        Shell shell = getShell();

        final TargetResource tr = (TargetResource) fSelectedTargets.get(0);
        TraceSubSystem subSystem = (TraceSubSystem)tr.getSubSystem();

        ConfigureMarkersDialog dialog = new ConfigureMarkersDialog(shell, subSystem);
        Map<String, Boolean> map = dialog.open(tr);
        if (map == null) {
            return;
        }

        for (Iterator<Map.Entry<String, Boolean>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Boolean> entry = (Map.Entry<String, Boolean>) it.next();
            final String key = (String) entry.getKey();

            final Boolean value = (Boolean) entry.getValue();

            try {

                final ILttControllerService service = subSystem.getControllerService();

                // Create future task
                @SuppressWarnings("unused")
                Boolean ok = new TCFTask<Boolean>() {
                    @Override
                    public void run() {

                        // Set marker enable using Lttng controller service proxy
                        service.setMarkerEnable(tr.getParent().getName(), tr.getName(), key, value.booleanValue(), new ILttControllerService.DoneSetMarkerEnable() {

                            @Override
                            public void doneSetMarkerEnable(IToken token, Exception error, Object str) {
                                if (error != null) {
                                    // Notify with error
                                    error(error);
                                    return;
                                }

                                // Notify about success
                                done(Boolean.valueOf(true));
                            }
                        });
                    }}.get(TraceControlConstants.DEFAULT_TCF_TASK_TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception e) {
                SystemMessageException sysExp;
                if (e instanceof SystemMessageException) {
                    sysExp = (SystemMessageException)e;
                } else {
                    sysExp = new SystemMessageException(LTTngUiPlugin.getDefault().getMessage(e));    
                }
                SystemBasePlugin.logError(Messages.Lttng_Control_ErrorConfigureMarkers + " (" +  //$NON-NLS-1$
                        Messages.Lttng_Resource_Target + ": "  + tr.getName() + ")", sysExp); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fSelectedTargets.clear();
            // store the selected targets to be used when running
            Iterator<IStructuredSelection> theSet = ((IStructuredSelection) selection).iterator();
            while (theSet.hasNext()) {
                Object obj = theSet.next();
                if (obj instanceof TargetResource) {
                    fSelectedTargets.add((TargetResource)obj);
                }
            }
        }
    }

    /**
     * Returns the active workbench shell of this plug-in.
     * 
     * @return active workbench shell.
     */
    protected Shell getShell() {
        return SystemBasePlugin.getActiveWorkbenchShell();
    }
 
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
}