/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.dialog;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.model.IDataChartProvider;
import org.eclipse.tracecompass.internal.tmf.chart.ui.dialog.Messages;
import org.eclipse.tracecompass.internal.tmf.chart.ui.type.BarChartTypeDefinition;
import org.eclipse.tracecompass.internal.tmf.chart.ui.type.IChartTypeDefinition;
import org.eclipse.tracecompass.internal.tmf.chart.ui.type.ScatterChartTypeDefinition;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This dialog is used for configuring series before making a chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartMakerDialog extends Dialog {

    private static final Image DELETE_IMAGE = NonNullUtils.checkNotNull(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Data model used for the creation of the chart
     */
    private final IDataChartProvider<?> fDataProvider;
    /**
     * Table for displaying chart types
     */
    private final TableViewer fTypeTable;
    /**
     * Table for displaying series
     */
    private final TableViewer fSeriesTable;
    /**
     * Table for displaying valid X series
     */
    private final TableViewer fSelectionXTable;
    /**
     * Checkbox table for displaying valid Y series
     */
    private final CheckboxTableViewer fSelectionYTable;
    /**
     * Button used for creating a series
     */
    private final Button fAddButton;
    /**
     * Warning label that choices might be restricted after selection
     */
    private final Label fWarningLabel;
    /**
     * Checkbox for indicating whether X axis is logarithmic
     */
    private final Button fXLogscaleButton;
    /**
     * Checkbox for indicating whether Y axis is logarithmic
     */
    private final Button fYLogscaleButton;
    /**
     * List of created series
     */
    private final List<ChartSeries> fSelectedSeries = new ArrayList<>();
    /**
     * Parent composite
     */
    private Composite fComposite;
    /**
     * Currently selected chart type
     */
    private @Nullable IChartTypeDefinition fType;

    /**
     * Filter for X data descriptors
     */
    private @Nullable IDataChartDescriptor<?, ?> fXFilter;
    /**
     * Filter for Y data descriptors
     */
    private @Nullable IDataChartDescriptor<?, ?> fYFilter;
    /**
     * Chart data created after the dialog
     */
    private @Nullable ChartData fDataSeries;
    /**
     * Chart model created after the dialog
     */
    private @Nullable ChartModel fChartModel;

    // ------------------------------------------------------------------------
    // Important methods
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *            Parent shell
     * @param model
     *            Model to choose source from
     */
    public ChartMakerDialog(Shell parent, IDataChartProvider<?> model) {
        super(parent);

        fComposite = parent;
        fDataProvider = model;

        /* Create tables */
        fTypeTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSeriesTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSelectionXTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL);
        fSelectionYTable = checkNotNull(CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL));

        /* Create buttons */
        fAddButton = new Button(parent, SWT.NONE);
        fXLogscaleButton = new Button(parent, SWT.CHECK);
        fYLogscaleButton = new Button(parent, SWT.CHECK);
        fWarningLabel = new Label(parent, SWT.NONE);

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    /**
     * @return The configured data series
     */
    public @Nullable ChartData getDataSeries() {
        return fDataSeries;
    }

    /**
     * @return The configured chart model
     */
    public @Nullable ChartModel getChartModel() {
        return fChartModel;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public Point getInitialSize() {
        return new Point(800, 600);
    }

    @Override
    public void create() {
        super.create();

        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    public Composite createDialogArea(@Nullable Composite parent) {
        fComposite = checkNotNull((Composite) super.createDialogArea(parent));
        getShell().setText(Messages.ChartMakerDialog_Title);

        /* Layouts */
        GridLayout baseLayout = new GridLayout();
        baseLayout.numColumns = 2;

        GridData genericFillGridData = new GridData();
        genericFillGridData.horizontalAlignment = SWT.FILL;
        genericFillGridData.verticalAlignment = SWT.FILL;
        genericFillGridData.grabExcessHorizontalSpace = true;
        genericFillGridData.grabExcessVerticalSpace = true;

        fComposite.setLayout(baseLayout);

        /* Chart type selector */
        createTypeTable();

        /* Selected series viewer */
        createSelectedSeriesGroup(genericFillGridData);

        /* Series creator */
        createSeriesCreatorGroup(genericFillGridData);

        /* Options */
        createOptionsGroup();

        return fComposite;
    }

    // Create the type table, on the left side of the dialog
    private void createTypeTable() {
        GridData typeGridData = new GridData();
        typeGridData.verticalSpan = 3;
        typeGridData.horizontalAlignment = SWT.FILL;
        typeGridData.verticalAlignment = SWT.FILL;
        typeGridData.grabExcessHorizontalSpace = false;
        typeGridData.grabExcessVerticalSpace = true;

        TableViewerColumn typeColumn = new TableViewerColumn(fTypeTable, SWT.NONE);
        typeColumn.getColumn().setWidth(50);
        typeColumn.setLabelProvider(new TypeLabelProvider());

        List<IChartTypeDefinition> types = new ArrayList<>();
        types.add(new BarChartTypeDefinition());
        types.add(new ScatterChartTypeDefinition());

        fTypeTable.getTable().setParent(fComposite);
        fTypeTable.getTable().setLayoutData(typeGridData);
        fTypeTable.setContentProvider(ArrayContentProvider.getInstance());
        fTypeTable.addSelectionChangedListener(new TypeSelectionListener());
        fTypeTable.setInput(types);
    }

    // Create the group that shows the selected series
    private void createSelectedSeriesGroup(GridData genericFillGridData) {
        /**
         * FIXME: The labels in the first column cannot be aligned to the
         * center. The workaround is to put a dummy column that won't appear.
         */
        GridLayout genericGridLayout = new GridLayout();

        TableViewerColumn dummyColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        dummyColumn.setLabelProvider(new SeriesDummyLabelProvider());

        /* X series column */
        TableViewerColumn xSelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        xSelectionColumn.getColumn().setText(Messages.ChartMakerDialog_XSeries);
        xSelectionColumn.getColumn().setAlignment(SWT.CENTER);
        xSelectionColumn.getColumn().setResizable(false);
        xSelectionColumn.setLabelProvider(new SeriesXLabelProvider());

        /* Y series column */
        TableViewerColumn ySelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        ySelectionColumn.getColumn().setText(Messages.ChartMakerDialog_YSeries);
        ySelectionColumn.getColumn().setAlignment(SWT.CENTER);
        ySelectionColumn.getColumn().setResizable(false);
        ySelectionColumn.setLabelProvider(new SeriesYLabelProvider());

        /* Remove buttons column */
        TableViewerColumn removeColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        removeColumn.getColumn().setResizable(false);
        removeColumn.setLabelProvider(new SeriesRemoveLabelProvider());

        TableColumnLayout seriesLayout = new TableColumnLayout();
        seriesLayout.setColumnData(dummyColumn.getColumn(), new ColumnPixelData(0));
        seriesLayout.setColumnData(xSelectionColumn.getColumn(), new ColumnWeightData(50));
        seriesLayout.setColumnData(ySelectionColumn.getColumn(), new ColumnWeightData(50));
        seriesLayout.setColumnData(removeColumn.getColumn(), new ColumnPixelData(34));

        Group seriesGroup = new Group(fComposite, SWT.BORDER | SWT.FILL);
        seriesGroup.setText(Messages.ChartMakerDialog_SelectedSeries);
        seriesGroup.setLayout(genericGridLayout);
        seriesGroup.setLayoutData(genericFillGridData);

        Composite seriesComposite = new Composite(seriesGroup, SWT.NONE);
        seriesComposite.setLayout(seriesLayout);
        seriesComposite.setLayoutData(genericFillGridData);

        fSeriesTable.getTable().setParent(seriesComposite);
        fSeriesTable.getTable().setHeaderVisible(true);
        fSeriesTable.getTable().addListener(SWT.MeasureItem, new SeriesRowResize());
        fSeriesTable.setContentProvider(ArrayContentProvider.getInstance());
        fSeriesTable.setInput(fSelectedSeries);
    }

    // Create the series selection group, listing all the available series
    private void createSeriesCreatorGroup(GridData genericFillGridData) {
        GridLayout creatorLayout = new GridLayout();
        creatorLayout.numColumns = 2;
        creatorLayout.makeColumnsEqualWidth = true;

        Group creatorGroup = new Group(fComposite, SWT.BORDER);
        creatorGroup.setText(Messages.ChartMakerDialog_SeriesCreator);
        creatorGroup.setLayout(creatorLayout);
        creatorGroup.setLayoutData(genericFillGridData);

        GridData creatorLabelGridData = new GridData();
        creatorLabelGridData.horizontalAlignment = SWT.CENTER;
        creatorLabelGridData.verticalAlignment = SWT.BOTTOM;

        /* Top labels */
        Label creatorLabelX = new Label(creatorGroup, SWT.NONE);
        creatorLabelX.setText(Messages.ChartMakerDialog_XAxis);
        creatorLabelX.setLayoutData(creatorLabelGridData);

        Label creatorLabelY = new Label(creatorGroup, SWT.NONE);
        creatorLabelY.setText(Messages.ChartMakerDialog_YAxis);
        creatorLabelY.setLayoutData(creatorLabelGridData);

        /* X axis table */
        TableViewerColumn creatorXColumn = new TableViewerColumn(fSelectionXTable, SWT.NONE);
        creatorXColumn.getColumn().setResizable(false);
        creatorXColumn.setLabelProvider(new DataDescriptorLabelProvider());

        TableColumnLayout creatorXLayout = new TableColumnLayout();
        creatorXLayout.setColumnData(creatorXColumn.getColumn(), new ColumnWeightData(100));

        Composite creatorXComposite = new Composite(creatorGroup, SWT.NONE);
        creatorXComposite.setLayout(creatorXLayout);
        creatorXComposite.setLayoutData(genericFillGridData);

        fSelectionXTable.getTable().setParent(creatorXComposite);
        fSelectionXTable.setContentProvider(ArrayContentProvider.getInstance());
        fSelectionXTable.setInput(fDataProvider.getDataDescriptors());
        fSelectionXTable.setFilters(new ViewerFilter[] { new CreatorXFilter() });
        fSelectionXTable.addSelectionChangedListener(new CreatorXSelectedEvent());

        /* Y axis table */
        TableViewerColumn creatorYColumn = new TableViewerColumn(fSelectionYTable, SWT.NONE);
        creatorYColumn.getColumn().setResizable(false);
        creatorYColumn.setLabelProvider(new DataDescriptorLabelProvider());

        TableColumnLayout creatorYLayout = new TableColumnLayout();
        creatorYLayout.setColumnData(creatorYColumn.getColumn(), new ColumnWeightData(100));

        Composite creatorYComposite = new Composite(creatorGroup, SWT.NONE);
        creatorYComposite.setLayout(creatorYLayout);
        creatorYComposite.setLayoutData(genericFillGridData);

        fSelectionYTable.getTable().setParent(creatorYComposite);
        fSelectionYTable.setContentProvider(ArrayContentProvider.getInstance());
        fSelectionYTable.setInput(fDataProvider.getDataDescriptors());
        fSelectionYTable.setFilters(new ViewerFilter[] { new CreatorYFilter() });
        fSelectionYTable.addCheckStateListener(new CreatorYSelectedEvent());

        /* Selected series warning */
        fWarningLabel.setParent(creatorGroup);
        fWarningLabel.setText(Messages.ChartMakerDialog_SelectionRestrictionWarning);
        fWarningLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        fWarningLabel.setVisible(false);

        /* Add button */
        GridData creatorButtonGridData = new GridData();
        creatorButtonGridData.horizontalAlignment = SWT.RIGHT;
        creatorButtonGridData.widthHint = 30;
        creatorButtonGridData.heightHint = 30;

        Image addImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD);
        fAddButton.setParent(creatorGroup);
        fAddButton.setLayoutData(creatorButtonGridData);
        fAddButton.setImage(addImage);
        fAddButton.addListener(SWT.Selection, new AddButtonClickedEvent());
    }

    private void createOptionsGroup() {
        GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 2;

        GridData configOptionsGridData = new GridData();
        configOptionsGridData.horizontalAlignment = SWT.FILL;
        configOptionsGridData.grabExcessHorizontalSpace = true;

        Group optionsGroup = new Group(fComposite, SWT.BORDER);
        optionsGroup.setText(Messages.ChartMakerDialog_Options);
        optionsGroup.setLayout(optionsLayout);
        optionsGroup.setLayoutData(configOptionsGridData);

        /* Checkboxes for logscale */
        fXLogscaleButton.setParent(optionsGroup);
        fXLogscaleButton.setText(Messages.ChartMakerDialog_LogScaleX);

        fYLogscaleButton.setParent(optionsGroup);
        fYLogscaleButton.setText(Messages.ChartMakerDialog_LogScaleY);
    }

    @Override
    public void okPressed() {
        /* Create the data series */
        fDataSeries = new ChartData(fDataProvider, fSelectedSeries);

        /* Create the data model */
        ChartType type = checkNotNull(checkNotNull(fType).getType());
        String title = fDataProvider.getName();
        boolean xlog = fXLogscaleButton.getSelection();
        boolean ylog = fYLogscaleButton.getSelection();
        fChartModel = new ChartModel(type, title, xlog, ylog);

        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    private boolean checkIfSeriesCompatible(IChartTypeDefinition typeA, IChartTypeDefinition typeB) {
        for (ChartSeries series : fSelectedSeries) {
            if (typeA.checkIfXDescriptorValid(series.getX(), null) != typeB.checkIfXDescriptorValid(series.getX(), null)) {
                return false;
            }

            if (typeA.checkIfYDescriptorValid(series.getY(), null) != typeB.checkIfYDescriptorValid(series.getY(), null)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfButtonReady() {
        if (fSelectionXTable.getSelection().isEmpty()) {
            return false;
        }

        if (fSelectionYTable.getCheckedElements().length == 0) {
            return false;
        }

        return true;
    }

    private boolean checkIfSeriesPresent(ChartSeries test) {
        for (ChartSeries series : fSelectedSeries) {
            if (series.getX() == test.getX() && series.getY() == test.getY()) {
                return true;
            }
        }

        return false;
    }

    private @Nullable ChartSeries findRemoveButtonOwner(Button button) {
        for (ChartSeries series : fSelectedSeries) {
            ChartSeriesDialog line = (ChartSeriesDialog) series;
            if (line.getButton() == button) {
                return line;
            }
        }

        return null;
    }

    private void removeIncompatibleSeries(IChartTypeDefinition type) {
        Iterator<ChartSeries> iterator = fSelectedSeries.iterator();

        while (iterator.hasNext()) {
            ChartSeriesDialog series = (ChartSeriesDialog) iterator.next();

            /* Check if the series is compatible, if not, remove it */
            if (!type.checkIfXDescriptorValid(series.getX(), null) || !type.checkIfYDescriptorValid(series.getY(), null)) {
                series.dispose();

                /* Remove the series of the series list */
                iterator.remove();
            }
        }
    }

    private void unselectIncompatibleSeries(IChartTypeDefinition type) {
        /* Check if X selected series is compatible */
        IDataChartDescriptor<?, ?> descriptorX = (IDataChartDescriptor<?, ?>) fSelectionXTable.getStructuredSelection().getFirstElement();
        if (descriptorX != null && !type.checkIfXDescriptorValid(descriptorX, fXFilter)) {
            fSelectionXTable.getTable().deselectAll();
        }

        /* Check if Y selected series are compatible */
        for (Object element : fSelectionYTable.getCheckedElements()) {
            IDataChartDescriptor<?, ?> descriptorY = (IDataChartDescriptor<?, ?>) checkNotNull(element);

            if (!type.checkIfYDescriptorValid(descriptorY, fYFilter)) {
                fSelectionYTable.setChecked(element, false);
            }
        }
    }

    private void configureLogscaleCheckboxes() {
        /* Enable X logscale checkbox if possible */
        if (checkNotNull(fType).checkIfXLogscalePossible(fXFilter)) {
            fXLogscaleButton.setEnabled(true);
        } else {
            fXLogscaleButton.setEnabled(false);
            fXLogscaleButton.setSelection(false);
        }

        /* Enable Y logscale checkbox if possible */
        if (checkNotNull(fType).checkIfYLogscalePossible(fYFilter)) {
            fYLogscaleButton.setEnabled(true);
        } else {
            fYLogscaleButton.setEnabled(false);
            fYLogscaleButton.setSelection(false);
        }
    }

    private boolean tryResetXFilter() {
        if (fSelectedSeries.size() != 0) {
            return false;
        }

        fXFilter = null;
        return true;
    }

    private boolean tryResetYFilter() {
        if (fSelectedSeries.size() != 0) {
            return false;
        }

        if (fSelectionYTable.getCheckedElements().length != 0) {
            return false;
        }

        fYFilter = null;
        return true;
    }

    // ------------------------------------------------------------------------
    // Listeners, Providers, etc
    // ------------------------------------------------------------------------

    /**
     * This class extension avoid the use of an hashmap for linking a series
     * with a button. Each button in the series table are linked to a series.
     */
    private class ChartSeriesDialog extends ChartSeries {
        private final Button fButton;

        private ChartSeriesDialog(IDataChartDescriptor<?, ?> descriptorX, IDataChartDescriptor<?, ?> descriptorY) {
            super(descriptorX, descriptorY);
            // Create the button for this series
            Button button = new Button((Composite) fSeriesTable.getControl(), SWT.PUSH);
            button.setImage(DELETE_IMAGE);
            button.addListener(SWT.Selection, new SeriesRemoveButtonEvent());
            fButton = button;
        }

        private Button getButton() {
            return fButton;
        }

        public void dispose() {
            fButton.dispose();
        }

    }

    /**
     * This provider provides the image in the chart type selection table.
     */
    private class TypeLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public Image getImage(@Nullable Object element) {
            IChartTypeDefinition type = checkNotNull((IChartTypeDefinition) element);
            return new Image(fComposite.getDisplay(), type.getImageData());
        }
    }

    /**
     * This listener handle the selection in the chart type selection table.
     */
    private class TypeSelectionListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            IStructuredSelection selection = fTypeTable.getStructuredSelection();
            IChartTypeDefinition type = (IChartTypeDefinition) selection.getFirstElement();

            if (type == null) {
                return;
            }

            /* Check if the series are compatible with the chart type */
            if (fSelectedSeries.size() != 0 && !checkIfSeriesCompatible(checkNotNull(fType), type)) {
                String warning = Messages.ChartMakerDialog_WarningConfirm;
                String message = String.format(Messages.ChartMakerDialog_WarningIncompatibleSeries,
                        type.getType().toString().toLowerCase());

                /* Ask the user if he wants to continue */
                boolean choice = MessageDialog.openConfirm(fComposite.getShell(), warning, message);
                if (!choice) {
                    fTypeTable.setSelection(new StructuredSelection(fType));
                    return;
                }

                removeIncompatibleSeries(type);
                fSeriesTable.refresh();
            }

            fType = type;

            /* Refresh controls */
            unselectIncompatibleSeries(fType);

            if (tryResetXFilter()) {
                fSelectionXTable.refresh();
            }

            if (tryResetYFilter()) {
                fSelectionYTable.refresh();
            }

            fAddButton.setEnabled(checkIfButtonReady());
            configureLogscaleCheckboxes();
        }
    }

    /**
     * This dummy provider is used as a workaround in a column's bug.
     */
    private class SeriesDummyLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }
    }

    /**
     * This provider provides the labels for the X column of the series table.
     */
    private class SeriesXLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getX().getLabel();
        }
    }

    /**
     * This provider provides the labels for the Y column of the series table.
     */
    private class SeriesYLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getY().getLabel();
        }
    }

    /**
     * This provider provides the buttons for removing a series in the series
     * table.
     */
    private class SeriesRemoveLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public void update(@Nullable ViewerCell cell) {
            if (cell == null) {
                return;
            }

            /* Create a button if it doesn't exist */
            ChartSeriesDialog series = (ChartSeriesDialog) cell.getViewerRow().getElement();
            Button button = series.getButton();

            /* Set the position of the button into the cell */
            TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(button, item, cell.getColumnIndex());
            editor.layout();
        }
    }

    /**
     * This listener handles the event when the button for removing a series is
     * clicked.
     */
    private class SeriesRemoveButtonEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            /* Dispose the button of the series */
            Button button = (Button) checkNotNull(event.widget);
            button.dispose();

            /* Remove the series from the list */
            ChartSeries series = findRemoveButtonOwner(button);
            fSelectedSeries.remove(series);
            fSeriesTable.refresh();

            /* Refresh controls */
            tryResetXFilter();
            fSelectionXTable.refresh();

            tryResetYFilter();
            fSelectionYTable.refresh();

            /* Disable OK button if no series are made */
            if (fSelectedSeries.size() == 0) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                fWarningLabel.setVisible(false);
            }

            configureLogscaleCheckboxes();
        }
    }

    /**
     * This listener resizes the height of each row of the series table.
     */
    private class SeriesRowResize implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            event.height = 27;
        }
    }

    /**
     * This provider provides labels for {@link DataChartDescriptor}.
     */
    private class DataDescriptorLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) checkNotNull(element);
            return descriptor.getLabel();
        }
    }

    /**
     * This filter is used for filtering labels in the X selection table.
     */
    private class CreatorXFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartTypeDefinition type = fType;

            if (type == null) {
                return false;
            }

            IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) checkNotNull(element);
            return type.checkIfXDescriptorValid(descriptor, fXFilter);
        }
    }

    /**
     * This filter is used for filtering labels in the Y selection table.
     */
    private class CreatorYFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartTypeDefinition type = fType;

            if (type == null) {
                return false;
            }

            IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) checkNotNull(element);
            return type.checkIfYDescriptorValid(descriptor, fYFilter);
        }
    }

    /**
     * This listener handles the event when a selection is made in the X
     * selection table.
     */
    private class CreatorXSelectedEvent implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            /* Enable button if possible */
            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    /**
     * This listener handles the event when a value is checked in the Y
     * selection table.
     */
    private class CreatorYSelectedEvent implements ICheckStateListener {
        @Override
        public void checkStateChanged(@Nullable CheckStateChangedEvent event) {
            if (event == null) {
                return;
            }

            /* Set Y filter if needed */
            if (event.getChecked()) {
                if (fYFilter == null) {
                    IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) event.getElement();
                    fYFilter = descriptor;
                }
            } else {
                tryResetYFilter();
            }

            /* Refresh controls */
            fSelectionYTable.refresh();
            fAddButton.setEnabled(checkIfButtonReady());

            configureLogscaleCheckboxes();
        }
    }

    /**
     * This listener handle the event when the add button of the series creator
     * is clicked.
     */
    private class AddButtonClickedEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            IDataChartDescriptor<?, ?> descriptorX = (IDataChartDescriptor<?, ?>) checkNotNull(fSelectionXTable.getStructuredSelection().getFirstElement());
            Object[] descriptorsY = fSelectionYTable.getCheckedElements();

            /* Create a series for each Y axis */
            for (int i = 0; i < descriptorsY.length; i++) {
                IDataChartDescriptor<?, ?> descriptorY = (IDataChartDescriptor<?, ?>) descriptorsY[i];
                ChartSeriesDialog series = new ChartSeriesDialog(descriptorX, checkNotNull(descriptorY));

                if (!checkIfSeriesPresent(series)) {
                    fSelectedSeries.add(series);
                }
            }

            /* Set the X filter */
            if (fXFilter == null) {
                fXFilter = descriptorX;
            }

            /* Refresh controls */
            // FIXME: The first refresh creates the buttons, the second makes
            // sure they show correctly, otherwise one button is displayed only
            // half his size until another call to refresh magically makes them
            // all show correctly
            fSeriesTable.refresh();
            Display.getDefault().asyncExec(() -> fSeriesTable.refresh());
            fSelectionXTable.refresh();

            /* Enable OK button */
            getButton(IDialogConstants.OK_ID).setEnabled(true);
            fWarningLabel.setVisible(true);

            configureLogscaleCheckboxes();
        }
    }

}
