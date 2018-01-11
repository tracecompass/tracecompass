/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Replaced separate Condition objects by anonymous classes
 *   Patrick Tasse - Add projectElementHasChild and isEditorOpened conditions
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.TableCollection;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;
import org.swtchart.Chart;

/**
 * Is a tree node available
 *
 * @author Matthew Khouzam
 */
public final class ConditionHelpers {

    private ConditionHelpers() {}

    /**
     * Provide default implementations for some {@link ICondition} methods.
     */
    public abstract static class SWTBotTestCondition implements ICondition {

        @Override
        public abstract boolean test() throws Exception;

        @Override
        public final void init(SWTBot bot) {
        }

        @Override
        public String getFailureMessage() {
            return null;
        }
    }

    /**
     * Is a tree node available
     *
     * @param name
     *            the name of the node
     * @param tree
     *            the parent tree
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition IsTreeNodeAvailable(final String name, final SWTBotTree tree) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    final SWTBotTreeItem[] treeItems = tree.getAllItems();
                    for (SWTBotTreeItem ti : treeItems) {
                        final String text = ti.getText();
                        if (text.equals(name)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("No child of tree {0} found with text {1}. Child items: {2}",
                        new String[] { tree.toString(), name, Arrays.toString(tree.getAllItems()) });
            }
        };
    }

    /**
     * Is a table item available
     *
     * @param name
     *            the name of the item
     * @param table
     *            the parent table
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition isTableItemAvailable(final String name, final SWTBotTable table) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    return table.containsItem(name);
                } catch (Exception e) {
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("No child of table {0} found with text ''{1}''.", table, name);
            }
        };
    }

    /**
     * Is the treeItem's node available
     *
     * @param name
     *            the name of the node
     * @param treeItem
     *            the treeItem
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition IsTreeChildNodeAvailable(final String name, final SWTBotTreeItem treeItem) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    return treeItem.getNode(name) != null;
                } catch (Exception e) {
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("No child of tree item {0} found with text ''{1}''. Child items: {2}",
                        new String[] { treeItem.toString(), name, Arrays.toString(treeItem.getItems()) });
            }
        };
    }

    /**
     * Is the treeItem's node removed
     *
     * @param length
     *            length of the node after removal
     * @param treeItem
     *            the treeItem
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition isTreeChildNodeRemoved(final int length, final SWTBotTreeItem treeItem) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    return treeItem.getNodes().size() == length;
                } catch (Exception e) {
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("Child of tree item {0} found with text ''{1}'' not removed. Child items: {2}",
                        new String[] { treeItem.toString(), String.valueOf(length), Arrays.toString(treeItem.getItems()) });
            }
        };
    }

    /**
     * Condition to check if the number of direct children of the
     * provided tree item equals the specified count.
     *
     * @param treeItem
     *            the SWTBot tree item
     * @param count
     *            the expected count
     * @return ICondition for verification
     */
    public static ICondition treeItemCount(final SWTBotTreeItem treeItem, int count) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                return treeItem.rowCount() == count;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("Tree item count: {0} expected: {1}",
                        treeItem.rowCount(), count);
            }
        };
    }

    /**
     * Checks if the wizard's shell is null
     *
     * @param wizard
     *            the null
     * @return false if either are null
     */
    public static ICondition isWizardReady(final Wizard wizard) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                if (wizard.getShell() == null) {
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * Is the wizard on the page you want?
     *
     * @param wizard
     *            wizard
     * @param page
     *            the desired page
     * @return true or false
     */
    public static ICondition isWizardOnPage(final Wizard wizard, final IWizardPage page) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                if (wizard == null || page == null) {
                    return false;
                }
                final IWizardContainer container = wizard.getContainer();
                if (container == null) {
                    return false;
                }
                IWizardPage currentPage = container.getCurrentPage();
                return page.equals(currentPage);
            }
        };
    }

    /**
     * Wait for a view to close
     *
     * @param view
     *            bot view for the view
     * @return true if the view is closed, false if it's active.
     */
    public static ICondition ViewIsClosed(final SWTBotView view) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                return (view != null) && (!view.isActive());
            }
        };
    }

    /**
     * Wait for a view to open
     *
     * @param view
     *            bot view for the view
     * @return true if the view is open, false otherwise
     */
    public static ICondition viewIsOpened(final SWTBotView view) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                return (view != null) && (view.isActive());
            }
        };
    }

    /**
     * Wait till table cell has a given content.
     *
     * @param table
     *            the table bot reference
     * @param content
     *            the content to check
     * @param row
     *            the row of the cell
     * @param column
     *            the column of the cell
     * @return ICondition for verification
     */
    public static ICondition isTableCellFilled(final SWTBotTable table,
            final String content, final int row, final int column) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    String cell = table.cell(row, column);
                    if( cell == null ) {
                        return false;
                    }
                    return cell.contains(content);
                } catch (Exception e) {
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                String cell = table.cell(row, column);
                if (cell == null) {
                    return NLS.bind("Cell absent, expected: {0}", content);
                }
                return NLS.bind("Cell content: {0} expected: {1}", cell, content);
            }
        };
    }

    /**
     * Condition to check if a tracing project element has a child with the
     * specified name. A project element label may have a count suffix in the
     * format ' [n]'.
     */
    public static class ProjectElementHasChild extends DefaultCondition {

        private final SWTBotTreeItem fParentItem;
        private final String fName;
        private final String fRegex;
        private SWTBotTreeItem fItem = null;

        /**
         * Constructor.
         *
         * @param parentItem
         *            the parent item
         * @param name
         *            the child name to look for
         */
        public ProjectElementHasChild(final SWTBotTreeItem parentItem, final String name) {
            fParentItem = parentItem;
            fName = name;
            /* Project element labels may have count suffix or time range */
            fRegex = Pattern.quote(name) + "(\\s\\[(.*)+?\\])?";
        }

        @Override
        public boolean test() throws Exception {
            fParentItem.expand();
            for (SWTBotTreeItem item : fParentItem.getItems()) {
                if (item.getText().matches(fRegex)) {
                    fItem = item;
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getFailureMessage() {
            return NLS.bind("No child of {0} found with name {1}", fParentItem.getText(), fName);
        }

        /**
         * Returns the matching child item if the condition returned true.
         *
         * @return the matching item
         */
        public SWTBotTreeItem getItem() {
            return fItem;
        }
    }

    /**
     * Condition to check if an editor with the specified title is opened.
     *
     * @param bot
     *            a workbench bot
     * @param title
     *            the editor title
     * @return ICondition for verification
     */
    public static ICondition isEditorOpened(final SWTWorkbenchBot bot, final String title) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                Matcher<IEditorReference> withPartName = withPartName(title);
                return !bot.editors(withPartName).isEmpty();
            }
        };
    }

    /**
     * Condition to check if the selection range equals the specified range.
     *
     * @param range
     *            the selection range
     * @return ICondition for verification
     */
    public static ICondition selectionRange(final TmfTimeRange range) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                return TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange().equals(range);
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("Selection range: {0} expected: {1}",
                        TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange(), range);
            }
        };
    }

    /**
     * Condition to check if the window range equals the specified range.
     *
     * @param range
     *            the window range
     * @return ICondition for verification
     */
    public static ICondition windowRange(final TmfTimeRange range) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                return TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().equals(range);
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("Window range: {0} expected: {1}",
                        TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange(), range);
            }
        };
    }

    /**
     * Condition to check if the selection contains the specified text at the
     * specified column. The text is checked in any item of the tree selection.
     *
     * @param tree
     *            the SWTBot tree
     * @param column
     *            the column index
     * @param text
     *            the expected text
     * @return ICondition for verification
     */
    public static ICondition treeSelectionContains(final SWTBotTree tree, final int column, final String text) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                TableCollection selection = tree.selection();
                for (int row = 0; row < selection.rowCount(); row++) {
                    if (selection.get(row, column).equals(text)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("Tree selection [0,{0}]: {1} expected: {2}",
                        new Object[] { column, tree.selection().get(0, column), text});
            }
        };
    }

    /**
     * Condition to check if the selection contains the specified text at the
     * specified column. The text is checked in any item of the tree selection.
     *
     * @param timeGraph
     *            the {@link SWTBotTimeGraph}
     * @param column
     *            the column index
     * @param text
     *            the expected text
     * @return ICondition for verification
     */
    public static ICondition timeGraphSelectionContains(final SWTBotTimeGraph timeGraph, final int column, final String text) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                TableCollection selection = timeGraph.selection();
                for (int row = 0; row < selection.rowCount(); row++) {
                    if (selection.get(row, column).equals(text)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getFailureMessage() {
                return NLS.bind("Time graph selection [0,{0}]: {1} expected: {2}",
                        new Object[] { column, timeGraph.selection().get(0, column), text});
            }
        };
    }

    private static class EventsTableSelectionCondition extends DefaultCondition {
        private long fSelectionTime;
        private SWTWorkbenchBot fBot;
        private long fCurValue;

        private EventsTableSelectionCondition(SWTWorkbenchBot bot, long selectionTime) {
            fBot = bot;
            fSelectionTime = selectionTime;
        }

        @Override
        public boolean test() throws Exception {
            StructuredSelection eventsTableSelection = getEventsTableSelection();
            if (eventsTableSelection.isEmpty()) {
                return false;
            }
            fCurValue = ((ITmfEvent) eventsTableSelection.getFirstElement()).getTimestamp().getValue();
            return fCurValue == fSelectionTime;
        }

        @Override
        public String getFailureMessage() {
            return "The selection in the table was not an event with timestamp " + fSelectionTime + ". Actual is " + fCurValue;
        }

        private StructuredSelection getEventsTableSelection() {
            return UIThreadRunnable.syncExec(new Result<StructuredSelection>() {

                @Override
                public StructuredSelection run() {
                    SWTBotEditor eventsEditor = SWTBotUtils.activeEventsEditor(fBot);
                    TmfEventsEditor part = (TmfEventsEditor) eventsEditor.getReference().getPart(false);
                    StructuredSelection selection = (StructuredSelection) part.getSelection();
                    return selection;
                }
            });
        }
    }

    /**
     * Wait until the events table selection matches the specified time stamp.
     *
     * @param bot
     *            a workbench bot
     *
     * @param selectionTime
     *            the selection time
     * @return ICondition for verification
     */
    public static ICondition selectionInEventsTable(final SWTWorkbenchBot bot, long selectionTime) {
        return new EventsTableSelectionCondition(bot, selectionTime);
    }

    private static class TimeGraphIsReadyCondition extends DefaultCondition  {

        private @NonNull TmfTimeRange fSelectionRange;
        private @NonNull ITmfTimestamp fVisibleTime;
        private AbstractTimeGraphView fView;
        private String fFailureMessage;

        private TimeGraphIsReadyCondition(AbstractTimeGraphView view, @NonNull TmfTimeRange selectionRange, @NonNull ITmfTimestamp visibleTime) {
            fView = view;
            fSelectionRange = selectionRange;
            fVisibleTime = visibleTime;
        }

        @Override
        public boolean test() throws Exception {
            ICondition selectionRangeCondition = ConditionHelpers.selectionRange(fSelectionRange);
            if (!selectionRangeCondition.test()) {
                fFailureMessage = selectionRangeCondition.getFailureMessage();
                return false;
            }
            @NonNull TmfTimeRange curWindowRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            if (!curWindowRange.contains(fVisibleTime)) {
                fFailureMessage = "Current window range " + curWindowRange + " does not contain " + fVisibleTime;
                return false;
            }

            if (fView.isDirty()) {
                fFailureMessage = "Time graph is dirty";
                return false;

            }
            return true;
        }

        @Override
        public String getFailureMessage() {
            return fFailureMessage;
        }
    }

    /**
     *
     * Wait until the Time Graph view is ready. The Time Graph view is
     * considered ready if the selectionRange is selected, the visibleTime is
     * visible and the view is not dirty (its model is done updating).
     *
     * @param view
     *            the time graph view
     * @param selectionRange
     *            the selection that the time graph should have
     * @param visibleTime
     *            the visible time that the time graph should have
     * @return ICondition for verification
     */
    public static ICondition timeGraphIsReadyCondition(AbstractTimeGraphView view, @NonNull TmfTimeRange selectionRange, @NonNull ITmfTimestamp visibleTime) {
        return new TimeGraphIsReadyCondition(view, selectionRange, visibleTime);
    }

    private static class XYViewerIsReadyCondition extends DefaultCondition  {

        private TmfXYChartViewer fViewer;
        private String fFailureMessage;

        private XYViewerIsReadyCondition(TmfXYChartViewer view) {
            fViewer = view;
        }

        @Override
        public boolean test() throws Exception {

            if (fViewer.isDirty()) {
                fFailureMessage = "Time graph is dirty";
                return false;
            }
            return true;
        }

        @Override
        public String getFailureMessage() {
            return fFailureMessage;
        }
    }

    /**
     * Wait until the Time Graph view is ready. The Time Graph view is considered
     * ready when it displays the desired trace at the desired window range.
     *
     * @param view
     *            the time graph view
     * @param trace
     *            the trace that we want the Time Graph View to show
     * @param windowRange
     *            the desired window range
     * @return ICondition for verification
     */
    public static ICondition timeGraphRangeCondition(AbstractTimeGraphView view, @NonNull ITmfTrace trace, @NonNull TmfTimeRange windowRange) {
        return new TimeGraphRangeCondition(view, trace, windowRange);
    }

    private static class TimeGraphRangeCondition extends DefaultCondition {

        private AbstractTimeGraphView fView;
        private @NonNull ITmfTrace fTrace;
        private @NonNull TmfTimeRange fWindowRange;
        private String fFailureMessage;

        private TimeGraphRangeCondition(AbstractTimeGraphView view, @NonNull ITmfTrace trace, @NonNull TmfTimeRange windowRange) {
            fView = view;
            fTrace = trace;
            fWindowRange = windowRange;
        }

        @Override
        public boolean test() throws Exception {
            ITmfTrace trace = fView.getTrace();
            if (!fTrace.equals(trace)) {
                String traceName = trace != null ? trace.getName() : "none";
                fFailureMessage = "Expected view to display trace:" + fTrace.getName() + " but was displaying trace: " + traceName;
            }
            @NonNull TmfTimeRange curWindowRange = TmfTraceManager.getInstance().getTraceContext(fTrace).getWindowRange();
            if (!curWindowRange.equals(fWindowRange)) {
                fFailureMessage = "Current window range " + curWindowRange + " is not expected " + fWindowRange;
                return false;
            }

            if (fView.isDirty()) {
                fFailureMessage = "Time graph is dirty";
                return false;

            }
            return true;
        }

        @Override
        public String getFailureMessage() {
            return fFailureMessage;
        }
    }

    /**
     *
     * Wait until the XY chart viewer is ready. The XY chart viewer is
     * considered ready when it is not updating.
     *
     * @param viewer
     *            the XY chart viewer
     * @return ICondition for verification
     */
    public static ICondition xyViewerIsReadyCondition(TmfXYChartViewer viewer) {
        return new XYViewerIsReadyCondition(viewer);
    }

    private static class NumberOfEventsCondition extends DefaultCondition {

        private ITmfTrace fTrace;
        private long fNbEvents;

        private NumberOfEventsCondition(ITmfTrace trace, long nbEvents) {
            fTrace = trace;
            fNbEvents = nbEvents;
        }

        @Override
        public boolean test() throws Exception {
            return fTrace.getNbEvents() == fNbEvents;
        }

        @Override
        public String getFailureMessage() {
            return fTrace.getName() + " did not contain the expected number of " + fNbEvents + " events. Current: " + fTrace.getNbEvents();
        }
    }

    /**
     * Wait until the trace contains the specified number of events.
     *
     * @param trace
     *            the trace
     * @param nbEvents
     *            the number of events to wait for
     * @return ICondition for verification
     */
    public static ICondition numberOfEventsInTrace(ITmfTrace trace, long nbEvents) {
        return new NumberOfEventsCondition(trace, nbEvents);
    }

    /**
     * Wait until there is an active events editor. A title can also be
     * specified to wait until a more specific editor.
     */
    public static final class ActiveEventsEditor extends DefaultCondition {
        private final SWTWorkbenchBot fWorkbenchBot;
        private SWTBotEditor fEditor;
        private String fEditorTitle;

        /**
         * Wait until there is an active events editor.
         *
         * @param workbenchBot
         *            a workbench bot
         * @param editorTitle
         *            If specified, wait until an active events editor with this
         *            title. Can be set to null.
         */
        public ActiveEventsEditor(SWTWorkbenchBot workbenchBot, String editorTitle) {
            fWorkbenchBot = workbenchBot;
            fEditorTitle = editorTitle;
        }

        @Override
        public boolean test() throws Exception {
            List<SWTBotEditor> editors = fWorkbenchBot.editors(WidgetMatcherFactory.withPartId(TmfEventsEditor.ID));
            for (SWTBotEditor e : editors) {
                // We are careful not to call e.getWidget() here because it actually forces the editor to show.
                // This is especially a problem for cases where we wait until there is no active editor.
                if (e.isActive()) {
                    if (fEditorTitle != null && !fEditorTitle.equals(e.getTitle())) {
                        return false;
                    }
                    fEditor = e;
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getFailureMessage() {
            String editorMessage = fEditorTitle != null ? " " + fEditorTitle : "";
            return "Active events editor" + editorMessage + " not found";
        }

        /**
         * @return The active editor found
         */
        public SWTBotEditor getActiveEditor() {
            return fEditor;
        }
    }

    private static class NumberOfSeries extends DefaultCondition {
        private String fFailureMessage;
        private Chart fChart;
        private final int fNumberOfSeries;

        public NumberOfSeries(Chart chart, int numberOfSeries) {
            fChart = chart;
            fNumberOfSeries = numberOfSeries;
        }

        @Override
        public boolean test() throws Exception {
            int length = fChart.getSeriesSet().getSeries().length;
            if (length != fNumberOfSeries){
                fFailureMessage = "Chart did not contain the expected number series. Actual " + length + ", expected " + fNumberOfSeries;
                return false;
            }

            return true;
        }

        @Override
        public String getFailureMessage() {
            return fFailureMessage;
        }
    }

    /**
     * Wait until the chart has the specified number of series.
     *
     * @param chart
     *            the chart
     * @param numberOfSeries
     *            the number of expected series
     *
     * @return ICondition for verification
     */
    public static ICondition numberOfSeries(Chart chart, int numberOfSeries) {
        return new NumberOfSeries(chart, numberOfSeries);
    }

    /**
     * Condition to check if the tree item has children
     *
     * @param treeItem
     *            the tree item that should have children
     * @return ICondition for verification
     */
    public static ICondition treeItemHasChildren(SWTBotTreeItem treeItem) {
        return new TreeItemHasChildren(treeItem);
    }

    private static final class TreeItemHasChildren extends DefaultCondition {
        private SWTBotTreeItem fTreeItem;

        public TreeItemHasChildren(SWTBotTreeItem treeItem) {
            fTreeItem = treeItem;
        }

        @Override
        public boolean test() throws Exception {
            return fTreeItem.getItems().length > 0;
        }

        @Override
        public String getFailureMessage() {
            return NLS.bind("No child of tree item {0} found.", new String[] { fTreeItem.toString() });
        }
    }
}
