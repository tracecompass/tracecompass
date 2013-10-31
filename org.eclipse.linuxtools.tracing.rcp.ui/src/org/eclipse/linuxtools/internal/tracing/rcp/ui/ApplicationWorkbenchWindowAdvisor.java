/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.tracing.rcp.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.cli.CliParser;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * The WorkbenchAdvisor implementation of the LTTng RCP.
 *
 * @author Bernd Hufmann
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    @SuppressWarnings("nls")
    private static final String[] UNWANTED_ACTION_SET = {
        "org.eclipse.search.searchActionSet",
        "org.eclipse.rse.core.search.searchActionSet",
        "org.eclipse.debug.ui.launchActionSet",
        "org.eclipse.debug.ui.debugActionSet",
        "org.eclipse.debug.ui.breakpointActionSet",
        "org.eclipse.team.ui",
        "org.eclipse.ui.externaltools.ExternalToolsSet",
//        "org.eclipse.update.ui.softwareUpdates",
//        "org.eclipse.ui.edit.text.actionSet.navigation",
//        "org.eclipse.ui.actionSet.keyBindings",
//        "org.eclipse.ui.edit.text.actionSet.navigation",
        "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo",
//        "org.eclipse.ui.edit.text.actionSet.annotationNavigation",
//        "org.eclipse.ui.NavigateActionSet",
//        "org.eclipse.jdt.ui.JavaActionSet",
//        "org.eclipse.jdt.ui.A_OpenActionSet",
//        "org.eclipse.jdt.ui.text.java.actionSet.presentation",
//        "org.eclipse.jdt.ui.JavaElementCreationActionSet",
//        "org.eclipse.jdt.ui.CodingActionSet",
//        "org.eclipse.jdt.ui.SearchActionSet",
//        "org.eclipse.jdt.debug.ui.JDTDebugActionSet",
        "org.eclipse.ui.edit.text.actionSet.openExternalFile",
//        "org.eclipse.debug.ui.profileActionSet"
    };

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param configurer
     *            - the workbench window configurer
     */
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
    }

    @Override
    public void postWindowCreate() {
        super.postWindowOpen();
        TracingRcpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new PerspectiveListener());
        createDefaultProject();
        hideActionSets();
        openTraceIfNecessary();
    }



    private static void openTraceIfNecessary() {
        String traceToOpen = TracingRcpPlugin.getDefault().getCli().getArgument(CliParser.OPEN_FILE_LOCATION);
        if (traceToOpen != null) {
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();
            final IWorkspaceRoot root = workspace.getRoot();
            IProject project = root.getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
            final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
            ncp.getChildren( project ); // force the model to be populated
            TmfOpenTraceHelper oth = new TmfOpenTraceHelper();
            try {
                oth.openTraceFromPath(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME,traceToOpen, TracingRcpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
            } catch (CoreException e) {
                TracingRcpPlugin.getDefault().logError(e.getMessage());
            }

        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Hides the unwanted action sets
     */
    private static void hideActionSets() {

        IWorkbenchPage page = TracingRcpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

        for (int i = 0; i < UNWANTED_ACTION_SET.length; i++) {
            page.hideActionSet(UNWANTED_ACTION_SET[i]);
        }
    }

    private static void createDefaultProject() {
        TmfProjectRegistry.createProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
    }

    /**
     * A perspective listener implementation
     *
     * @author Bernd Hufmann
     */
    public class PerspectiveListener implements IPerspectiveListener {

        /**
         * Default Constructor
         */
        public PerspectiveListener() {
        }

        @Override
        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            createDefaultProject();
            hideActionSets();
        }

        @Override
        public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
        }
    }

}
