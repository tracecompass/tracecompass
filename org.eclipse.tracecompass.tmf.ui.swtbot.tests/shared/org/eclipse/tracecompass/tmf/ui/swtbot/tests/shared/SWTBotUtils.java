/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.views.TracingPerspectiveFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.hamcrest.Matcher;


/**
 * SWTBot Helper functions
 *
 * @author Matthew Khouzam
 */
public final class SWTBotUtils {

    private SWTBotUtils() {}

    private static final String TRACING_PERSPECTIVE_ID = TracingPerspectiveFactory.ID;

    /**
     * Waits for all Eclipse jobs to finish
     */
    public static void waitForJobs() {
        while (!Job.getJobManager().isIdle()) {
            delay(100);
        }
    }

    /**
     * Sleeps current thread for a given time.
     *
     * @param waitTimeMillis
     *            time in milliseconds to wait
     */
    public static void delay(final long waitTimeMillis) {
        try {
            Thread.sleep(waitTimeMillis);
        } catch (final InterruptedException e) {
            // Ignored
        }
    }

    /**
     * Create a tracing project
     *
     * @param projectName
     *            the name of the tracing project
     */
    public static void createProject(final String projectName) {
        /*
         * Make a new test
         */
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IProject project = TmfProjectRegistry.createProject(projectName, null, new NullProgressMonitor());
                assertNotNull(project);
            }
        });

        SWTBotUtils.waitForJobs();
    }

    /**
     * Deletes a tracing project
     *
     * @param projectName
     *            the name of the tracing project
     * @param bot
     *            the workbench bot
     */
    public static void deleteProject(String projectName, SWTWorkbenchBot bot) {
        // Wait for any analysis to complete because it might create
        // supplementary files
        SWTBotUtils.waitForJobs();
        try {
            ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
        }

        SWTBotUtils.waitForJobs();

        final SWTBotView projectViewBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectViewBot.setFocus();

        SWTBotTree treeBot = projectViewBot.bot().tree();
        SWTBotTreeItem treeItem = treeBot.getTreeItem(projectName);
        SWTBotMenu contextMenu = treeItem.contextMenu("Delete");
        contextMenu.click();

        bot.shell("Delete Resources").setFocus();
        final SWTBotCheckBox checkBox = bot.checkBox();
        bot.waitUntil(Conditions.widgetIsEnabled(checkBox));
        checkBox.click();

        final SWTBotButton okButton = bot.button("OK");
        bot.waitUntil(Conditions.widgetIsEnabled(okButton));
        okButton.click();

        SWTBotUtils.waitForJobs();
    }

    /**
     * Focus on the main window
     *
     * @param shellBots
     *            swtbotshells for all the shells
     */
    public static void focusMainWindow(SWTBotShell[] shellBots) {
        for (SWTBotShell shellBot : shellBots) {
            if (shellBot.getText().toLowerCase().contains("eclipse")) {
                shellBot.activate();
            }
        }
    }

    /**
     * Close a view with a title
     *
     * @param title
     *            the title, like "welcome"
     * @param bot
     *            the workbench bot
     */
    public static void closeView(String title, SWTWorkbenchBot bot) {
        final List<SWTBotView> openViews = bot.views();
        for (SWTBotView view : openViews) {
            if (view.getTitle().equalsIgnoreCase(title)) {
                view.close();
                bot.waitUntil(ConditionHelpers.ViewIsClosed(view));
            }
        }
    }

    /**
     * Switch to the tracing perspective
     */
    public static void switchToTracingPerspective() {
        switchToPerspective(TRACING_PERSPECTIVE_ID);
    }

    /**
     * Switch to a given perspective
     *
     * @param id
     *            the perspective id (like
     *            "org.eclipse.linuxtools.tmf.ui.perspective"
     */
    public static void switchToPerspective(final String id) {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().showPerspective(id, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                } catch (WorkbenchException e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    /**
     * If the test is running in the UI thread then fail
     */
    public static void failIfUIThread() {
        if (Display.getCurrent() != null && Display.getCurrent().getThread() == Thread.currentThread()) {
            fail("SWTBot test needs to run in a non-UI thread. Make sure that \"Run in UI thread\" is unchecked in your launch configuration or"
                    + " that useUIThread is set to false in the pom.xml");
        }

    }

    /**
     * Open a trace, this does not perform any validation though
     *
     * @param projectName
     *            The project name
     * @param tracePath
     *            the path of the trace file (absolute or relative)
     * @param traceType
     *            the trace canonical string (eg:
     *            org.eclipse.linuxtools.btf.trace)
     */
    public static void openTrace(final String projectName, final String tracePath, final String traceType) {
        openTrace(projectName, tracePath, traceType, true);
    }

    /**
     * Open a trace, this does not perform any validation though
     *
     * @param projectName
     *            The project name
     * @param tracePath
     *            the path of the trace file (absolute or relative)
     * @param traceType
     *            the trace canonical string (eg:
     *            org.eclipse.linuxtools.btf.trace)
     * @param delay
     *            delay and wait for jobs
     */
    public static void openTrace(final String projectName, final String tracePath, final String traceType, boolean delay) {
        final Exception exception[] = new Exception[1];
        exception[0] = null;
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                    TmfTraceFolder destinationFolder = TmfProjectRegistry.getProject(project, true).getTracesFolder();
                    TmfOpenTraceHelper.openTraceFromPath(destinationFolder, tracePath, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), traceType);
                } catch (CoreException e) {
                    exception[0] = e;
                }
            }
        });
        if (exception[0] != null) {
            fail(exception[0].getMessage());
        }

        if (delay) {
            delay(1000);
            waitForJobs();
        }
    }

    /**
     * Finds an editor and sets focus to the editor
     *
     * @param bot
     *            the workbench bot
     * @param editorName
     *            the editor name
     * @return the corresponding SWTBotEditor
     */
    public static SWTBotEditor activateEditor(SWTWorkbenchBot bot, String editorName) {
        Matcher<IEditorReference> matcher = WidgetMatcherFactory.withPartName(editorName);
        final SWTBotEditor editorBot = bot.editor(matcher);
        IEditorPart iep = editorBot.getReference().getEditor(true);
        final TmfEventsEditor tmfEd = (TmfEventsEditor) iep;
        editorBot.show();
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                tmfEd.setFocus();
            }
        });

        SWTBotUtils.waitForJobs();
        SWTBotUtils.delay(1000);
        assertNotNull(tmfEd);
        return editorBot;
    }

    /**
     * Opens a trace in an editor and get the TmfEventsEditor
     *
     * @param bot
     *            the workbench bot
     * @param projectName
     *            the name of the project that contains the trace
     * @param elementPath
     *            the trace element path (relative to Traces folder)
     * @return TmfEventsEditor the opened editor
     */
    public static TmfEventsEditor openEditor(SWTWorkbenchBot bot, String projectName, IPath elementPath) {
        final SWTBotView projectExplorerBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectExplorerBot.setFocus();

        final SWTBotTree tree = bot.tree();
        final SWTBotTreeItem treeItem = tree.getTreeItem(projectName);
        treeItem.expand();

        String nodeName = getFullNodeName(treeItem, TmfTracesFolder.TRACES_FOLDER_NAME);
        bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(nodeName, treeItem));
        SWTBotTreeItem tracesNode = treeItem.getNode(nodeName);
        tracesNode.expand();

        SWTBotTreeItem currentNode = tracesNode;
        for (String segment : elementPath.segments()) {
            String fullNodeName = getFullNodeName(currentNode, segment);
            bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(fullNodeName, currentNode));
            SWTBotTreeItem newNode = currentNode.getNode(fullNodeName);
            newNode.select();
            newNode.doubleClick();
            currentNode = newNode;
        }

        SWTBotUtils.delay(1000);
        SWTBotUtils.waitForJobs();
        final String expectedTitle = elementPath.toString();

        final IEditorPart iep[] = new IEditorPart[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IEditorReference[] ieds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                assertNotNull(ieds);
                iep[0] = null;
                for (IEditorReference ied : ieds) {
                    if (ied.getTitle().equals(expectedTitle)) {
                        iep[0] = ied.getEditor(true);
                        break;
                    }
                }
            }
        });
        assertNotNull(iep[0]);
        return (TmfEventsEditor) iep[0];
    }

    private static String getFullNodeName(final SWTBotTreeItem treeItem, String prefix) {
        List<String> nodes = treeItem.getNodes();
        String nodeName = "";
        for (String node : nodes) {
            if (node.startsWith(prefix)) {
                nodeName = node;
            }
        }
        return nodeName;
    }
}
