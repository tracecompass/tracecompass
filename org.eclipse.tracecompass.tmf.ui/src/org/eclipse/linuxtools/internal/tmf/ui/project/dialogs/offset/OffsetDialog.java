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
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.offset;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.viewers.ArrayTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Offset wizard dialog
 *
 * @author Matthew Khouzam
 *
 */
public class OffsetDialog extends Dialog {

    private static final int TREE_EDITOR_MIN_WIDTH = 50;
    private static final String EDITOR_KEY = "$editor$";  //$NON-NLS-1$
    private static final String WIDTH_KEY = "$width$";  //$NON-NLS-1$

    private static final TmfTimestampFormat TIME_FORMAT = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS SSS SSS"); //$NON-NLS-1$
    private static final TmfTimestampFormat OFFSET_FORMAT = new TmfTimestampFormat("T.SSS SSS SSS"); //$NON-NLS-1$

    private final Map<TmfTraceElement, Long> fOffsetMap;
    private final Map<TmfTraceElement, ITmfTimestamp> fRefTimeMap;
    private final Map<TmfTraceElement, ITmfTimestamp> fTargetTimeMap;

    private Label fBasicMessageLabel;
    private Group fButtonGroup;
    private Label fAdvancedMessageLabel;
    private FilteredTree fViewer;

    private boolean fAdvancedMode = true;
    private TreeColumn fRefTimeColumn;
    private TreeViewerColumn fButtonViewerColumn;
    private TreeColumn fTargetTimeColumn;

    private abstract class ColumnEditingSupport extends EditingSupport {
        private final TextCellEditor textCellEditor;

        private ColumnEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer);
            this.textCellEditor = textCellEditor;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return textCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    private class TimeEditingSupport extends ColumnEditingSupport {
        private Map<TmfTraceElement, ITmfTimestamp> map;

        private TimeEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor, Map<TmfTraceElement, ITmfTimestamp> map) {
            super(viewer, textCellEditor);
            this.map = map;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof String) {
                String string = (String) value;
                if (string.trim().isEmpty()) {
                    map.remove(element);
                } else {
                    try {
                        ITmfTimestamp refTime = map.get(element);
                        long ref = refTime == null ? 0 : refTime.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                        Long newVal = TIME_FORMAT.parseValue(string, ref);
                        map.put((TmfTraceElement) element, new TmfNanoTimestamp(newVal));
                    } catch (ParseException e) {
                        /* Ignore and reload previous value */
                    }
                }
                fViewer.getViewer().update(element, null);
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (map.get(element) == null) {
                return ""; //$NON-NLS-1$
            }
            return TIME_FORMAT.format(map.get(element).normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
        }
    }

    private class RefTimeEditingSupport extends TimeEditingSupport {
        private RefTimeEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer, textCellEditor, fRefTimeMap);
        }
    }

    private class TargetTimeEditingSupport extends TimeEditingSupport {
        private TargetTimeEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer, textCellEditor, fTargetTimeMap);
        }
    }

    private class OffsetEditingSupport extends ColumnEditingSupport {
        private OffsetEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer, textCellEditor);
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof String) {
                String string = (String) value;
                if (string.trim().isEmpty()) {
                    fOffsetMap.put((TmfTraceElement) element, 0L);
                } else {
                    try {
                        Long newVal = OFFSET_FORMAT.parseValue(string);
                        fOffsetMap.put((TmfTraceElement) element, newVal);
                    } catch (ParseException e) {
                        /* Ignore and reload previous value */
                    }
                }
                fViewer.getViewer().update(element, null);
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (fOffsetMap.get(element) == 0) {
                return ""; //$NON-NLS-1$
            }
            return OFFSET_FORMAT.format((long) fOffsetMap.get(element));
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     * @param results
     *            results to put the data into
     */
    public OffsetDialog(Shell parent, Map<TmfTraceElement, Long> results) {
        super(parent);
        setShellStyle(getShellStyle() & ~SWT.APPLICATION_MODAL);
        fOffsetMap = results;
        fRefTimeMap = new HashMap<>();
        fTargetTimeMap = new HashMap<>();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.OffsetDialog_Title);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(area, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        composite.setLayout(new GridLayout());
        createBasicMessage(composite);
        createButtonGroup(composite);
        createAdvancedMessage(composite);
        createViewer(composite);

        /* set label width hint equal to tree width */
        int widthHint = fViewer.getViewer().getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        GridData gd = (GridData) fBasicMessageLabel.getLayoutData();
        gd.widthHint = widthHint;
        gd = (GridData) fAdvancedMessageLabel.getLayoutData();
        gd.widthHint = widthHint;
        gd = (GridData) composite.getLayoutData();
        gd.heightHint = composite.computeSize(widthHint, SWT.DEFAULT).y;
        setBasicMode();

        TmfSignalManager.register(this);
        composite.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                TmfSignalManager.deregister(this);
            }
        });
        return area;
    }

    private void createBasicMessage(final Composite parent) {
        fBasicMessageLabel = new Label(parent, SWT.WRAP);
        fBasicMessageLabel.setText(Messages.OffsetDialog_BasicMessage);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 0;
        gd.heightHint = SWT.DEFAULT;
        fBasicMessageLabel.setLayoutData(gd);
    }

    private void createButtonGroup(final Composite parent) {
        fButtonGroup = new Group(parent, SWT.SHADOW_NONE);
        fButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fButtonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));

        final Button basicButton = new Button(fButtonGroup, SWT.RADIO);
        basicButton.setText(Messages.OffsetDialog_BasicButton);
        basicButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!basicButton.getSelection() || !fAdvancedMode) {
                    return;
                }
                setBasicMode();
                parent.layout();
            }
        });
        basicButton.setSelection(true);

        final Button advancedButton = new Button(fButtonGroup, SWT.RADIO);
        advancedButton.setText(Messages.OffsetDialog_AdvancedButton);
        advancedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!advancedButton.getSelection() || fAdvancedMode) {
                    return;
                }
                setAdvancedMode();
                parent.layout();
            }
        });
    }

    private void createAdvancedMessage(final Composite parent) {
        fAdvancedMessageLabel = new Label(parent, SWT.WRAP);
        fAdvancedMessageLabel.setText(Messages.OffsetDialog_AdvancedMessage);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 0;
        gd.heightHint = SWT.DEFAULT;
        fAdvancedMessageLabel.setLayoutData(gd);
    }

    private void createViewer(Composite parent) {

        // Define the TableViewer
        fViewer = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER, new PatternFilter() {
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                return wordMatches(((TmfTraceElement) element).getElementPath());
            }
        }, true);

        // Make lines and make header visible
        final Tree tree = fViewer.getViewer().getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(fViewer.getViewer(), new FocusCellOwnerDrawHighlighter(fViewer.getViewer()));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(fViewer.getViewer());
        TreeViewerEditor.create(fViewer.getViewer(), focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        final TextCellEditor textCellEditor = new TextCellEditor(fViewer.getViewer().getTree(), SWT.RIGHT);

        fViewer.getViewer().setColumnProperties(new String[] { Messages.OffsetDialog_TraceName, Messages.OffsetDialog_ReferenceTime, Messages.OffsetDialog_OffsetTime });

        TreeViewerColumn column = createTreeViewerColumn(Messages.OffsetDialog_TraceName, SWT.NONE);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TmfTraceElement) element).getElementPath();
            }
        });

        column = createTreeViewerColumn(Messages.OffsetDialog_ReferenceTime, SWT.RIGHT);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(fRefTimeMap.get(element));
            }
        });
        column.setEditingSupport(new RefTimeEditingSupport(fViewer.getViewer(), textCellEditor));
        fRefTimeColumn = column.getColumn();

        column = createTreeViewerColumn(Messages.OffsetDialog_OffsetTime, SWT.RIGHT);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (fOffsetMap.get(element) != 0) {
                    return super.getText(OFFSET_FORMAT.format((long) fOffsetMap.get(element)));
                }
                return ""; //$NON-NLS-1$
            }
        });
        column.setEditingSupport(new OffsetEditingSupport(fViewer.getViewer(), textCellEditor));

        column = createTreeViewerColumn("", SWT.NONE); //$NON-NLS-1$
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ""; //$NON-NLS-1$
            }
        });
        column.getColumn().setWidth(TREE_EDITOR_MIN_WIDTH);
        column.getColumn().setResizable(false);
        fButtonViewerColumn = column;

        column = createTreeViewerColumn(Messages.OffsetDialog_TargetTime, SWT.RIGHT);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(fTargetTimeMap.get(element));
            }
        });
        column.setEditingSupport(new TargetTimeEditingSupport(fViewer.getViewer(), textCellEditor));
        fTargetTimeColumn = column.getColumn();

        List<TmfTraceElement> traces = new ArrayList<>(fOffsetMap.keySet());
        Collections.sort(traces, new Comparator<TmfTraceElement>() {
            @Override
            public int compare(TmfTraceElement o1, TmfTraceElement o2) {
                IPath folder1 = new Path(o1.getElementPath()).removeLastSegments(1);
                IPath folder2 = new Path(o2.getElementPath()).removeLastSegments(1);
                if (folder1.equals(folder2)) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
                if (folder1.isPrefixOf(folder2)) {
                    return 1;
                } else if (folder2.isPrefixOf(folder1)) {
                    return -1;
                }
                return folder1.toString().compareToIgnoreCase(folder2.toString());
            }
        });

        fViewer.getViewer().setContentProvider(new ArrayTreeContentProvider());
        fViewer.getViewer().setInput(traces);

        /* add button as tree editors to fourth column of every item */
        for (TreeItem treeItem : tree.getItems()) {
            TreeEditor treeEditor = new TreeEditor(tree);
            Button applyButton = new Button(tree, SWT.PUSH);
            applyButton.setText("<<"); //$NON-NLS-1$
            applyButton.setData(treeItem.getData());
            applyButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TmfTraceElement traceElement = (TmfTraceElement) e.widget.getData();
                    ITmfTimestamp targetTime = fTargetTimeMap.get(traceElement);
                    ITmfTimestamp refTime = fRefTimeMap.get(traceElement);
                    if (targetTime != null && refTime != null) {
                        long offset = new TmfNanoTimestamp(targetTime).getValue() -
                                new TmfNanoTimestamp(refTime).getValue();
                        fOffsetMap.put(traceElement, offset);
                        fViewer.getViewer().update(traceElement, null);
                    }
                }
            });
            treeEditor.grabHorizontal = true;
            treeEditor.minimumWidth = TREE_EDITOR_MIN_WIDTH;
            treeEditor.setEditor(applyButton, treeItem, 3);
            treeItem.setData(EDITOR_KEY, applyButton);
        }

        /* put temporary values in maps to pack according to time formats */
        fRefTimeMap.put(traces.get(0), new TmfNanoTimestamp());
        fTargetTimeMap.put(traces.get(0), new TmfNanoTimestamp());
        fViewer.getViewer().update(traces.get(0), null);
        for (final TreeColumn treeColumn : tree.getColumns()) {
            if (treeColumn.getResizable()) {
                treeColumn.pack();
            }
        }
        fRefTimeMap.clear();
        fTargetTimeMap.clear();
        fViewer.getViewer().update(traces.get(0), null);

        for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
            for (ITmfTrace parentTrace : TmfTraceManager.getInstance().getOpenedTraces()) {
                for (ITmfTrace trace : TmfTraceManager.getTraceSet(parentTrace)) {
                    if (traceElement.getResource().equals(trace.getResource())) {
                        fRefTimeMap.put(traceElement, trace.getStartTime());
                        fViewer.getViewer().update(traceElement, null);
                        break;
                    }
                }
                if (fRefTimeMap.get(traceElement) != null) {
                    break;
                }
            }
        }

        /* open trace when double-clicking a tree item */
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                TmfTraceElement traceElement = (TmfTraceElement) e.item.getData();
                TmfOpenTraceHelper.openTraceFromElement(traceElement);
            }
        });

        tree.setFocus();
    }

    private TreeViewerColumn createTreeViewerColumn(String title, int style) {
        final TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer.getViewer(), style);
        final TreeColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        return viewerColumn;
    }

    private void setBasicMode() {
        fAdvancedMode = false;
        fRefTimeColumn.setData(WIDTH_KEY, fRefTimeColumn.getWidth());
        fTargetTimeColumn.setData(WIDTH_KEY, fTargetTimeColumn.getWidth());
        for (TreeItem treeItem : fViewer.getViewer().getTree().getItems()) {
            Control editor = (Control) treeItem.getData(EDITOR_KEY);
            editor.setVisible(false);
        }
        fRefTimeColumn.setWidth(0);
        fRefTimeColumn.setResizable(false);
        fButtonViewerColumn.getColumn().setWidth(0);
        fTargetTimeColumn.setWidth(0);
        fTargetTimeColumn.setResizable(false);
        fAdvancedMessageLabel.setText("");  //$NON-NLS-1$
    }

    private void setAdvancedMode() {
        fAdvancedMode = true;
        fRefTimeColumn.setWidth((Integer) fRefTimeColumn.getData(WIDTH_KEY));
        fRefTimeColumn.setResizable(true);
        fButtonViewerColumn.getColumn().setWidth(TREE_EDITOR_MIN_WIDTH);
        fTargetTimeColumn.setWidth((Integer) fTargetTimeColumn.getData(WIDTH_KEY));
        fTargetTimeColumn.setResizable(true);
        for (TreeItem treeItem : fViewer.getViewer().getTree().getItems()) {
            Control editor = (Control) treeItem.getData(EDITOR_KEY);
            editor.setVisible(true);
        }
        fAdvancedMessageLabel.setText(Messages.OffsetDialog_AdvancedMessage);
    }

    /**
     * Handler for the event selected signal
     *
     * @param signal
     *            the event selected signal
     */
    @TmfSignalHandler
    public void eventSelected(final TmfEventSelectedSignal signal) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
                    if (traceElement.getResource().equals(signal.getEvent().getTrace().getResource())) {
                        fRefTimeMap.put(traceElement, signal.getEvent().getTimestamp());
                        fViewer.getViewer().update(traceElement, null);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Handler for the time selected signal
     *
     * @param signal
     *            the event selected signal
     */
    @TmfSignalHandler
    public void timeSelected(final TmfTimeSynchSignal signal) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
                    fTargetTimeMap.put(traceElement, signal.getBeginTime());
                    fViewer.getViewer().update(traceElement, null);
                }
            }
        });
    }

    /**
     * Handler for the trace opened signal
     *
     * @param signal
     *            the trace opened signal
     */
    @TmfSignalHandler
    public void traceOpened(final TmfTraceOpenedSignal signal) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (ITmfTrace trace : TmfTraceManager.getTraceSet(signal.getTrace())) {
                    for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
                        if (traceElement.getResource().equals(trace.getResource())) {
                            if (fRefTimeMap.get(traceElement) == null) {
                                fRefTimeMap.put(traceElement, trace.getStartTime());
                                fViewer.getViewer().update(traceElement, null);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }
}
