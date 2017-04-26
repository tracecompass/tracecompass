/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.analysis;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisModuleOutputs;
import org.eclipse.tracecompass.tmf.ui.project.model.Messages;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Class that implements analysis output as a view. This just opens the view.
 * The view itself needs to manage how and when it will execute the analysis and
 * display which output.
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisViewOutput implements IAnalysisOutput, IExecutableExtension {

    private String fViewId;
    private final @Nullable String fSecondaryId;
    private final Map<String, String> fProperties = new HashMap<>();

    /**
     * Default constructor
     */
    public TmfAnalysisViewOutput() {
        fSecondaryId = null;
    }

    /**
     * Constructor
     *
     * @param viewid
     *            id of the view to display as output
     */
    public TmfAnalysisViewOutput(String viewid) {
        fViewId = viewid;
        fSecondaryId = null;
    }

    /**
     * Constructor
     *
     * @param viewid
     *            id of the view to display as output
     * @param secondaryId
     *            The secondary ID for this view, for example, an analysis ID
     *            for views supporting multiple analyses. If not
     *            <code>null</code>, the view will be opened with id
     *            "viewid:secondaryId", so multiple instances of the same view
     *            can be opened at the same time.
     * @since 3.0
     */
    public TmfAnalysisViewOutput(String viewid, String secondaryId) {
        fViewId = viewid;
        fSecondaryId = secondaryId;
    }

    /**
     * Returns the view id of the corresponding view
     *
     * @return The view id
     */
    public String getViewId() {
        return fViewId;
    }

    @Override
    public String getName() {
        IViewDescriptor descr = PlatformUI.getWorkbench().getViewRegistry().find(fViewId);
        String viewName = (descr != null) ? descr.getLabel() : fViewId + Messages.TmfAnalysisViewOutput_ViewUnavailable;
        return viewName;
    }

    /**
     * Opens the view for this output. This method should always be called from
     * the main thread.
     *
     * @return The view that was just opened
     * @throws PartInitException
     *             Exception if the view did not open correctly
     * @since 2.1
     */
    protected IViewPart openView() throws PartInitException {
        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

        String viewId = fViewId;
        String secondaryId = fSecondaryId;
        if (secondaryId != null) {
            viewId += ':' + secondaryId;
        }

        return activePage.showView(viewId);
    }

    @Override
    public void requestOutput() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IViewPart view = openView();
                    // Transfers the properties of this output to the view
                    if (!(fProperties.isEmpty()) && (view instanceof WorkbenchPart)) {
                        WorkbenchPart wbPart = (WorkbenchPart) view;
                        for (String key : fProperties.keySet()) {
                            wbPart.setPartProperty(key, fProperties.get(key));
                        }
                    }
                } catch (final PartInitException e) {
                    TraceUtils.displayErrorMsg(Messages.TmfAnalysisViewOutput_Title, "Error opening view " + getName() + e.getMessage()); //$NON-NLS-1$
                    Activator.getDefault().logError("Error opening view " + getName(), e); //$NON-NLS-1$
                }
            }
        });
    }

    @Override
    public void setOutputProperty(@NonNull String key, String value, boolean immediate) {
        if (value == null) {
            fProperties.remove(key);
        } else {
            fProperties.put(key, value);
            /*
             * If the property is immediate, we forward it to the view if the
             * view is active
             */
            if (immediate) {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
                IViewPart view = activePage.findView(fViewId);
                if (view instanceof WorkbenchPart) {
                    ((WorkbenchPart) view).setPartProperty(key, value);
                }
            }
        }
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        fViewId = config.getAttribute(TmfAnalysisModuleOutputs.ID_ATTR);
    }
}
