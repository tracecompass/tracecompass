/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers.ProjectElementHasChild;
import org.eclipse.tracecompass.tmf.ui.views.TracingPerspectiveFactory;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.hamcrest.Matcher;

/**
 * SWTBot Helper functions
 *
 * @author Matthew Khouzam
 */
public final class SWTBotUtils {

    private static final String WINDOW_MENU = "Window";
    private static final String PREFERENCES_MENU_ITEM = "Preferences";

    private SWTBotUtils() {
    }

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
     * Deletes a project
     *
     * @param projectName
     *            the name of the tracing project
     * @param deleteResources
     *            whether or not to deleted resources under the project
     * @param bot
     *            the workbench bot
     */
    public static void deleteProject(final String projectName, boolean deleteResources, SWTWorkbenchBot bot) {
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

        if (deleteResources) {
            bot.shell("Delete Resources").setFocus();
            final SWTBotCheckBox checkBox = bot.checkBox();
            bot.waitUntil(Conditions.widgetIsEnabled(checkBox));
            checkBox.click();
        }

        final SWTBotButton okButton = bot.button("OK");
        bot.waitUntil(Conditions.widgetIsEnabled(okButton));
        okButton.click();

        SWTBotUtils.waitForJobs();
    }

    /**
     * Deletes a project and its resources
     *
     * @param projectName
     *            the name of the tracing project
     * @param bot
     *            the workbench bot
     */
    public static void deleteProject(String projectName, SWTWorkbenchBot bot) {
        deleteProject(projectName, true, bot);
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
     * Close a view with an id
     *
     * @param viewId
     *            the view id, like "org.eclipse.linuxtools.tmf.ui.views.histogram"
     * @param bot
     *            the workbench bot
     */
    public static void closeViewById(String viewId, SWTWorkbenchBot bot) {
        final SWTBotView view = bot.viewById(viewId);
        view.close();
        bot.waitUntil(ConditionHelpers.ViewIsClosed(view));
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
     * Initialize the environment for SWTBot
     */
    public static void initialize() {
        failIfUIThread();

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        UIThreadRunnable.syncExec(() -> {

            // There seems to be problems on some system where the main shell is
            // not in focus initially. This was seen using Xvfb and Xephyr on some occasions.
            focusMainWindow(bot.shells());

            Shell shell = bot.activeShell().widget;

            // Only adjust shell if it appears to be the top-most
            if (shell.getParent() == null) {
                makeShellFullyVisible(shell);
            }
        });
    }

    /**
     * If the test is running in the UI thread then fail
     */
    private static void failIfUIThread() {
        if (Display.getCurrent() != null && Display.getCurrent().getThread() == Thread.currentThread()) {
            fail("SWTBot test needs to run in a non-UI thread. Make sure that \"Run in UI thread\" is unchecked in your launch configuration or"
                    + " that useUIThread is set to false in the pom.xml");
        }
    }

    /**
     * Try to make the shell fully visible in the display. If the shell cannot
     * fit the display, it will be positioned so that top-left corner is at
     * <code>(0, 0)</code> in display-relative coordinates.
     *
     * @param shell
     *            the shell to make fully visible
     */
    private static void makeShellFullyVisible(Shell shell) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Point absCoord = shell.toDisplay(0, 0);
        Point shellSize = shell.getSize();

        Point newLocation = new Point(absCoord.x, absCoord.y);
        newLocation.x = Math.max(0, Math.min(absCoord.x, displayBounds.width - shellSize.x));
        newLocation.y = Math.max(0, Math.min(absCoord.y, displayBounds.height - shellSize.y));
        if (!newLocation.equals(absCoord)) {
            shell.setLocation(newLocation);
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
     *            the trace type id (eg: org.eclipse.linuxtools.btf.trace)
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
     *            the trace type id (eg: org.eclipse.linuxtools.btf.trace)
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
        final SWTBotView projectExplorerView = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectExplorerView.setFocus();
        SWTBot projectExplorerBot = projectExplorerView.bot();

        final SWTBotTree tree = projectExplorerBot.tree();
        projectExplorerBot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(projectName, tree));
        final SWTBotTreeItem treeItem = tree.getTreeItem(projectName);
        treeItem.expand();

        SWTBotTreeItem tracesNode = getTraceProjectItem(projectExplorerBot, treeItem, TmfTracesFolder.TRACES_FOLDER_NAME);
        tracesNode.expand();

        SWTBotTreeItem currentItem = tracesNode;
        for (String segment : elementPath.segments()) {
            currentItem = getTraceProjectItem(projectExplorerBot, currentItem, segment);
            currentItem.select();
            currentItem.doubleClick();
        }

        SWTBotEditor editor = bot.editorByTitle(elementPath.toString());
        IEditorPart editorPart = editor.getReference().getEditor(false);
        assertTrue(editorPart instanceof TmfEventsEditor);
        return (TmfEventsEditor) editorPart;
    }

    /**
     * Returns the child tree item of the specified item with the given name.
     * The project element label may have a count suffix in the format ' [n]'.
     *
     * @param bot
     *            a given workbench bot
     * @param parentItem
     *            the parent tree item
     * @param name
     *            the desired child element name (without suffix)
     * @return the a {@link SWTBotTreeItem} with the specified name
     */
    public static SWTBotTreeItem getTraceProjectItem(SWTBot bot, final SWTBotTreeItem parentItem, final String name) {
        ProjectElementHasChild condition = new ProjectElementHasChild(parentItem, name);
        bot.waitUntil(condition);
        return condition.getItem();
    }

    /**
     * Select the traces folder
     *
     * @param bot
     *            a given workbench bot
     * @param projectName
     *            the name of the project (it needs to exist or else it would
     *            time out)
     * @return a {@link SWTBotTreeItem} of the "Traces" folder
     */
    public static SWTBotTreeItem selectTracesFolder(SWTWorkbenchBot bot, String projectName) {
        SWTBotTreeItem projectTreeItem = selectProject(bot, projectName);
        projectTreeItem.select();
        SWTBotTreeItem tracesFolderItem = getTraceProjectItem(bot, projectTreeItem, TmfTracesFolder.TRACES_FOLDER_NAME);
        tracesFolderItem.select();
        return tracesFolderItem;
    }

    /**
     * Select the project in Project Explorer
     *
     * @param bot
     *            a given workbench bot
     * @param projectName
     *            the name of the project (it needs to exist or else it would time out)
     * @return a {@link SWTBotTreeItem} of the project
     */
    public static SWTBotTreeItem selectProject(SWTWorkbenchBot bot, String projectName) {
        SWTBotView projectExplorerBot = bot.viewByTitle("Project Explorer");
        projectExplorerBot.show();
        SWTBotTreeItem treeItem = projectExplorerBot.bot().tree().getTreeItem(projectName);
        treeItem.select();
        return treeItem;
    }

    /**
     * Open a view by id.
     *
     * @param id
     *            view id.
     */
    public static void openView(final String id) {
        final PartInitException res[] = new PartInitException[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
                } catch (PartInitException e) {
                    res[0] = e;
                }
            }
        });
        if (res[0] != null) {
            fail(res[0].getMessage());
        }
        waitForJobs();
    }

    /**
     * Maximize a table
     *
     * @param tableBot
     *            the {@link SWTBotTable} table
     */
    public static void maximizeTable(SWTBotTable tableBot) {
        try {
            tableBot.pressShortcut(KeyStroke.getInstance(IKeyLookup.CTRL_NAME + "+"), KeyStroke.getInstance("M"));
        } catch (ParseException e) {
            fail();
        }
    }

    /**
     * Get the bounds of a cell (SWT.Rectangle) for the specified row and column
     * index in a table
     *
     * @param table
     *            the table
     * @param row
     *            the row of the table to look up
     * @param col
     *            the column of the table to look up
     * @return the bounds in display relative coordinates
     */
    public static Rectangle getCellBounds(final Table table, final int row, final int col) {
        return UIThreadRunnable.syncExec(new Result<Rectangle>() {
            @Override
            public Rectangle run() {
                TableItem item = table.getItem(row);
                Rectangle bounds = item.getBounds(col);
                Point p = table.toDisplay(bounds.x, bounds.y);
                Rectangle rect = new Rectangle(p.x, p.y, bounds.width, bounds.height);
                return rect;
            }
        });
    }

    /**
     * Get the tree item from a tree at the specified location
     *
     * @param bot
     *            the SWTBot
     * @param tree
     *            the tree to find the tree item in
     * @param nodeNames
     *            the path to the tree item, in the form of node names (from
     *            parent to child).
     * @return the tree item
     */
    public static SWTBotTreeItem getTreeItem(SWTBot bot, SWTBotTree tree, String... nodeNames) {
        if (nodeNames.length == 0) {
            return null;
        }

        bot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(nodeNames[0], tree));
        SWTBotTreeItem currentNode = tree.getTreeItem(nodeNames[0]);
        for (int i = 1; i < nodeNames.length; i++) {
            currentNode.expand();

            String nodeName = nodeNames[i];
            bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(nodeName, currentNode));
            SWTBotTreeItem newNode = currentNode.getNode(nodeName);
            currentNode = newNode;
        }

        return currentNode;
    }

    /**
     * Get the active events editor. Note that this will wait until such editor
     * is available.
     *
     * @param workbenchBot
     *            a given workbench bot
     * @return the active events editor
     */
    public static SWTBotEditor activeEventsEditor(final SWTWorkbenchBot workbenchBot) {
        final SWTBotEditor editor[] = new SWTBotEditor[1];
        workbenchBot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                List<SWTBotEditor> editors = workbenchBot.editors(WidgetMatcherFactory.withPartId(TmfEventsEditor.ID));
                for (SWTBotEditor e : editors) {
                    if (e.isActive() && !e.getWidget().isDisposed()) {
                        editor[0] = e;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return "Active events editor not found";
            }
        });
        return editor[0];
    }

    /**
     * Open the preferences dialog and return the corresponding shell.
     *
     * @param bot
     *            a given workbench bot
     * @return the preferences shell
     */
    public static SWTBotShell openPreferences(SWTBot bot) {
        if (SWTUtils.isMac()) {
            // On Mac, the Preferences menu item is under the application name.
            // For some reason, we can't access the application menu anymore so
            // we use the keyboard shortcut.
            try {
                bot.activeShell().pressShortcut(KeyStroke.getInstance(IKeyLookup.COMMAND_NAME + "+"), KeyStroke.getInstance(","));
            } catch (ParseException e) {
                fail();
            }
        } else {
            bot.menu(WINDOW_MENU).menu(PREFERENCES_MENU_ITEM).click();
        }

        bot.waitUntil(Conditions.shellIsActive(PREFERENCES_MENU_ITEM));
        return bot.activeShell();
    }
}
