/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * The implementation of TMF project model element.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfTraceFolder fTraceFolder = null;
    private TmfExperimentFolder fExperimentFolder = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * Creates the TMF project model element.
     * @param name The name of the project.
     * @param project The project reference.
     * @param parent The parent element
     */
    public TmfProjectElement(String name, IProject project, ITmfProjectModelElement parent) {
        super(name, project, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IProject getResource() {
        return (IProject) fResource;
    }

    @Override
    public void addChild(ITmfProjectModelElement child) {
        super.addChild(child);
        if (child instanceof TmfTraceFolder) {
            fTraceFolder = (TmfTraceFolder) child;
            return;
        }
        if (child instanceof TmfExperimentFolder) {
            fExperimentFolder = (TmfExperimentFolder) child;
            return;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the containing trace folder element.
     * @return the TMF trace folder element.
     */
    public TmfTraceFolder getTracesFolder() {
        return fTraceFolder;
    }

    /**
     * Returns the containing experiment folder element.
     * @return the TMF experiment folder element.
     */
    public TmfExperimentFolder getExperimentsFolder() {
        return fExperimentFolder;
    }

    // ------------------------------------------------------------------------
    // TmfProjectElement
    // ------------------------------------------------------------------------

    @Override
    public void refresh() {
        Display.getDefault().asyncExec(new Runnable(){
            @Override
            public void run() {
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchWindow wbWindow = wb.getActiveWorkbenchWindow();
                if (wbWindow == null) {
                    return;
                }
                IWorkbenchPage activePage = wbWindow.getActivePage();
                if (activePage == null) {
                    return;
                }

                for (IViewReference viewReference : activePage.getViewReferences()) {
                    IViewPart viewPart = viewReference.getView(false);
                    if (viewPart instanceof CommonNavigator) {
                        CommonViewer commonViewer = ((CommonNavigator) viewPart).getCommonViewer();
                        commonViewer.refresh();
                    }
                }
            }});
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            refresh();
        }
    }

    @Override
    public TmfProjectElement getProject() {
        return this;
    }
}
