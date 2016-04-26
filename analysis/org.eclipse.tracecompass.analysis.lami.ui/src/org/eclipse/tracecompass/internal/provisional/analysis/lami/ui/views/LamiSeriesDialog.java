/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.ChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiXYSeriesDescription;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Series creation dialog
 *
 * @author Jonathan Rajotte-Julien
 */
public class LamiSeriesDialog extends SelectionDialog {

    private static final int MINIMUM_COLUMN_WIDTH = 30;
    private static final int MININAL_SERIES_TABLE_HEIGHT = 150;

    /* The root element to populate the viewer with */
    private final Object fXInputElement;
    private final Object fYInputElement;
    private final List<LamiXYSeriesDescription> series;

    /* Providers for populating dialog */
    private final ILabelProvider fXLabelProvider;
    private final IStructuredContentProvider fXContentProvider;
    private final ILabelProvider fYLabelProvider;
    private final IStructuredContentProvider fYContentProvider;
    private final IStructuredContentProvider fSeriesContentProvider;

    private final boolean fRestrictXSeriesNumbers;

    private final List<LamiAxisCheckBoxOption> fXCheckBoxOptions;
    private final List<LamiAxisCheckBoxOption> fYCheckBoxOptions;

    // the visual selection widget group
    private TableViewer fXTableViewer;
    private CheckboxTableViewer fYCheckBoxViewer;
    private TableViewer fSeriesListViewer;

    private Label fWarning;

    /**
     * @param parentShell
     *            The parent shell of the dialog
     * @param chartType
     *            The chart type for which the dialog construct series
     * @param xInput
     *            The possible X axis set of values
     * @param yInput
     *            The possible Y axis set of values
     * @param xContentProvider
     *            A content provider for the X axis set
     * @param xLabelProvider
     *            The label provider for the X axis set
     * @param yContentProvider
     *            The content provider for the Y axis set
     * @param yLabelProvider
     *            The label provider for the Y axis set
     */
    public LamiSeriesDialog(Shell parentShell, ChartType chartType, Object xInput,
            Object yInput,
            IStructuredContentProvider xContentProvider,
            ILabelProvider xLabelProvider,
            IStructuredContentProvider yContentProvider,
            ILabelProvider yLabelProvider) {
        super(parentShell);
        fXInputElement = xInput;
        fYInputElement = yInput;
        fXContentProvider = xContentProvider;
        fXLabelProvider = xLabelProvider;
        fYContentProvider = yContentProvider;
        fYLabelProvider = yLabelProvider;
        series = new ArrayList<>();
        fSeriesContentProvider = checkNotNull(ArrayContentProvider.getInstance());

        fXCheckBoxOptions = new ArrayList<>();
        fYCheckBoxOptions = new ArrayList<>();
        fSeriesListViewer = new TableViewer(parentShell);
        fXTableViewer = new TableViewer(parentShell);
        fYCheckBoxViewer = checkNotNull(CheckboxTableViewer.newCheckList(parentShell, SWT.NONE));

        /* Dynamic restriction per chart type */
        switch (chartType) {
        case XY_SCATTER:
            fRestrictXSeriesNumbers = false;
            break;
        case BAR_CHART:
        case PIE_CHART:
        default:
            fRestrictXSeriesNumbers = true;
            break;
        }

        this.fWarning = new Label(parentShell, SWT.NONE);
    }

    @Override
    protected Control createDialogArea(@Nullable Composite parent) {

        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);

        /* Base 3 column grid layout */
        GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 3;
        Group seriesGroup = new Group(composite, SWT.NONE);
        seriesGroup.setLayoutData(gridData);
        seriesGroup.setLayout(new GridLayout(3, false));
        seriesGroup.setText(Messages.LamiSeriesDialog_series);

        /*
         * New sub group for the series table.
         */
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        gridData.heightHint = MININAL_SERIES_TABLE_HEIGHT;
        Group seriesTableGroup = new Group(seriesGroup, SWT.NONE);
        seriesTableGroup.setLayoutData(gridData);
        TableColumnLayout layout = new TableColumnLayout();
        seriesTableGroup.setLayout(layout);

        /* Current series */
        fSeriesListViewer = new TableViewer(seriesTableGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        fSeriesListViewer.setContentProvider(fSeriesContentProvider);
        fSeriesListViewer.setInput(series);
        fSeriesListViewer.getTable().setHeaderVisible(true);
        fSeriesListViewer.getTable().setLinesVisible(true);
        TableViewerColumn column1 = createTableViewerColumn(fSeriesListViewer, Messages.LamiSeriesDialog_x_values, element -> element.getXAspect().getLabel());
        TableViewerColumn column2 = createTableViewerColumn(fSeriesListViewer, Messages.LamiSeriesDialog_y_values, element -> element.getYAspect().getLabel());
        layout.setColumnData(column1.getColumn(), new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
        layout.setColumnData(column2.getColumn(), new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));

        /* Delete series button */
        gridData = new GridData(GridData.CENTER);
        gridData.horizontalSpan = 1;
        Button deleteSeries = new Button(seriesGroup, SWT.PUSH);
        deleteSeries.setText(Messages.LamiSeriesDialog_delete);
        deleteSeries.setLayoutData(gridData);
        deleteSeries.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(@Nullable SelectionEvent e) {
                /* Remove the selectecd series */
                IStructuredSelection selections = (IStructuredSelection) fSeriesListViewer.getSelection();
                for (Object selection : selections.toList()) {
                    series.remove(selection);
                }
                /* When table is empty reset to initial state */
                if (series.isEmpty()) {
                    /* Make sure the OK button is disabled */
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                    /* Hide the selection warning */
                    fWarning.setVisible(false);

                    /*
                     * Reset the initial selection of the X axis selection table
                     */
                    fXTableViewer.refresh();
                    /* Reset check boxes options */
                    fXCheckBoxOptions.forEach(checkBox -> {
                        checkBox.setButtonEnabled(true);
                    });
                    fYCheckBoxOptions.forEach(checkBox -> {
                        checkBox.setButtonEnabled(true);
                    });
                }
                /* Refresh the series table to show the added series */
                fSeriesListViewer.refresh();
            }

            @Override
            public void widgetDefaultSelected(@Nullable SelectionEvent e) {
            }
        });

        /*
         * Series creator subgroup
         */
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 3;
        Group seriesCreatorGroup = new Group(composite, getShellStyle());
        seriesCreatorGroup.setLayoutData(gridData);
        seriesCreatorGroup.setLayout(new GridLayout(3, false));
        seriesCreatorGroup.setText(Messages.LamiSeriesDialog_serie_creator);

        /* X axis sash label */
        gridData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        Label xSeriesCreatorLabel = new Label(seriesCreatorGroup, SWT.CENTER);
        xSeriesCreatorLabel.setLayoutData(gridData);
        xSeriesCreatorLabel.setText(Messages.LamiSeriesDialog_x_axis);

        gridData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        Label ySeriesCreatorLabel = new Label(seriesCreatorGroup, SWT.CENTER);
        ySeriesCreatorLabel.setLayoutData(gridData);
        ySeriesCreatorLabel.setText(Messages.LamiSeriesDialog_y_axis);

        /* Empty label for grid layout */
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 1;
        Label emptyLabel = new Label(seriesCreatorGroup, SWT.CENTER);
        emptyLabel.setLayoutData(gridData);

        SashForm sash1 = new SashForm(seriesCreatorGroup, SWT.BORDER | SWT.HORIZONTAL);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        sash1.setLayoutData(gridData);
        sash1.setVisible(true);

        fXTableViewer = new TableViewer(sash1, getTableStyle());
        fXTableViewer.setContentProvider(fXContentProvider);
        fXTableViewer.setLabelProvider(fXLabelProvider);
        fXTableViewer.setInput(fXInputElement);

        fYCheckBoxViewer = checkNotNull(CheckboxTableViewer.newCheckList(sash1, SWT.BORDER));
        fYCheckBoxViewer.setLabelProvider(fYLabelProvider);
        fYCheckBoxViewer.setContentProvider(fYContentProvider);
        fYCheckBoxViewer.setInput(fYInputElement);

        gridData = new GridData(SWT.FILL, SWT.NONE, true, true);
        gridData.horizontalSpan = 1;
        Button button1 = new Button(seriesCreatorGroup, SWT.PUSH);
        button1.setText(Messages.LamiSeriesDialog_add);
        button1.setLayoutData(gridData);
        button1.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(@Nullable SelectionEvent e) {
                Object[] ySelections = fYCheckBoxViewer.getCheckedElements();
                IStructuredSelection xSelections = (IStructuredSelection) fXTableViewer.getSelection();
                @Nullable Object x = xSelections.getFirstElement();
                if (!(x instanceof LamiTableEntryAspect) || ySelections.length == 0) {
                    return;
                }

                /* Add selection to series if it doesn not already exist in the list */
                for (Object y : ySelections) {
                    if(!(y instanceof LamiTableEntryAspect)) {
                       continue;
                    }
                    LamiXYSeriesDescription serie = new LamiXYSeriesDescription((LamiTableEntryAspect) x, ((LamiTableEntryAspect) y));
                    if (!series.contains(serie)) {
                        series.add(serie);
                        fSeriesListViewer.refresh();
                    }
                }

                /* Set label warning visible and enable OK button */
                fWarning.setVisible(true);
                getButton(IDialogConstants.OK_ID).setEnabled(true);

                /* Update possible X selection based on current series */
                TableItem[] items = fXTableViewer.getTable().getItems();
                Arrays.stream(items).forEach(item -> {
                    LamiTableEntryAspect aspect = (LamiTableEntryAspect) item.getData();
                    if (!aspect.arePropertiesEqual(series.get(0).getXAspect())) {
                        fXTableViewer.remove(aspect);
                    }
                    if (fRestrictXSeriesNumbers && aspect != (series.get(0).getXAspect())) {
                        fXTableViewer.remove(aspect);
                    }
                });

                /*
                 * Disable all checkBox that do not apply to aspects series.
                 * Simply take the first one since all series should comply to
                 * the same restriction
                 */
                fXCheckBoxOptions.forEach(checkBox -> {
                    checkBox.setButtonEnabled(checkBox.getPredicate().test(series.get(0).getXAspect()));
                });
                fYCheckBoxOptions.forEach(checkBox -> {
                    checkBox.setButtonEnabled(checkBox.getPredicate().test(series.get(0).getYAspect()));
                });
            }

            @Override
            public void widgetDefaultSelected(@Nullable SelectionEvent e) {
            }
        });


        gridData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_END);
        gridData.horizontalSpan = 3;
        fWarning = new Label(seriesCreatorGroup, SWT.LEFT);
        fWarning.setLayoutData(gridData);
        fWarning.setText(Messages.LamiSeriesDialog_selectionRestrictionWarning);
        fWarning.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        fWarning.setVisible(false);

        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 3;
        Group optionGroups = new Group(composite, getShellStyle());
        optionGroups.setLayoutData(gridData);
        optionGroups.setLayout(new GridLayout(3, false));
        optionGroups.setText(Messages.LamiSeriesDialog_chart_options);

        for (LamiAxisCheckBoxOption checkBox : fXCheckBoxOptions) {
            Button button = new Button(optionGroups, SWT.CHECK);
            button.setSelection(checkBox.getDefaultValue());
            button.setText(checkBox.getName());
            checkBox.setButton(button);
        }

        for (LamiAxisCheckBoxOption checkBox : fYCheckBoxOptions) {
            Button button = new Button(optionGroups, SWT.CHECK);
            button.setSelection(checkBox.getDefaultValue());
            button.setText(checkBox.getName());
            checkBox.setButton(button);
        }

        fYCheckBoxViewer.getTable().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(@Nullable SelectionEvent e) {
                /* On check */
                if (e != null && e.detail == SWT.CHECK) {
                    /* Change possible selection */
                    IStructuredSelection selections = (IStructuredSelection) fYCheckBoxViewer.getSelection();
                    if (selections.getFirstElement() != null) {

                        boolean checked = fYCheckBoxViewer.getChecked(selections.getFirstElement());
                        /*
                         * If just selected look for stuff to disable. If not no
                         * need to look for stuff to disable since it was
                         * already done before.
                         */
                        if (checked) {
                            TableItem[] items = fYCheckBoxViewer.getTable().getItems();
                            Arrays.stream(items).forEach(item -> {
                                LamiTableEntryAspect aspect = (LamiTableEntryAspect) item.getData();
                                if (!aspect.arePropertiesEqual((LamiTableEntryAspect) checkNotNull(selections.getFirstElement()))) {
                                    fYCheckBoxViewer.remove(aspect);
                                }
                            });
                        } else if (!checked && fYCheckBoxViewer.getCheckedElements().length == 0 && fSeriesListViewer.getTable().getItemCount() == 0) {
                            fYCheckBoxViewer.refresh();
                        }
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(@Nullable SelectionEvent e) {
            }
        });

        Dialog.applyDialogFont(composite);
        return composite;
    }

    /*
     * Disable OK button on dialog creation.
     */
    @Override
    protected void createButtonsForButtonBar(@Nullable Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /**
     * Return the style flags for the table viewer.
     *
     * @return int
     */
    protected int getTableStyle() {
        return SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
    }

    /**
     * Add check box option for X series.
     *
     * @param name
     *            The name of the option. The actual text shown to the user.
     * @param defaultValue
     *            The default state of the check box option.
     * @param predicate
     *            The predicate to check if the option applies to the given
     *            aspect
     * @return The index of the option value in the result table.
     */
    public int addXCheckBoxOption(String name, boolean defaultValue, Predicate<LamiTableEntryAspect> predicate) {
        LamiAxisCheckBoxOption checkBox = new LamiAxisCheckBoxOption(name, defaultValue, predicate);
        fXCheckBoxOptions.add(checkBox);
        return fXCheckBoxOptions.size() - 1;
    }

    /**
     * Add check box option for Y series.
     *
     * @param name
     *            The name of the option. The actual text shown to the user.
     * @param defaultValue
     *            The default state of the check box option.
     * @param predicate
     *            The predicate to check if the option applies to the given
     *            aspect
     * @return The index of the option value in the result table.
     */
    public int addYCheckBoxOption(String name, boolean defaultValue, Predicate<LamiTableEntryAspect> predicate) {
        LamiAxisCheckBoxOption checkbox = new LamiAxisCheckBoxOption(name, defaultValue, predicate);
        fYCheckBoxOptions.add(checkbox);
        return fYCheckBoxOptions.size() - 1;
    }

    /**
     * @return The final values of X series check boxes.
     */
    public boolean[] getXCheckBoxOptionValues() {
        boolean[] selections = new boolean[fXCheckBoxOptions.size()];
        if (selections.length != 0) {
            IntStream.range(0, selections.length).forEach(i -> selections[i] = fXCheckBoxOptions.get(i).getValue());
        }
        return selections;
    }

    /**
     * @return The final values of Y series check boxes.
     */
    public boolean[] getYCheckBoxOptionValues() {
        boolean[] selections = new boolean[fYCheckBoxOptions.size()];
        if (selections.length != 0) {
            IntStream.range(0, selections.length).forEach(i -> selections[i] = fYCheckBoxOptions.get(i).getValue());
        }
        return selections;
    }

    @Override
    protected void okPressed() {
        for (LamiAxisCheckBoxOption checkBox : fXCheckBoxOptions) {
            checkBox.updateValue();
        }
        for (LamiAxisCheckBoxOption checkBox : fYCheckBoxOptions) {
            checkBox.updateValue();
        }
        super.okPressed();
    }

    @Override
    public Object[] getResult() {
        return series.toArray();
    }

    private static <T extends Comparable<T>> TableViewerColumn createTableViewerColumn(TableViewer viewer, String name,
            Function<LamiXYSeriesDescription, T> propertyFunction) {
        TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
        viewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public @Nullable String getText(@Nullable Object element) {
                if (element != null) {
                    return propertyFunction.apply((LamiXYSeriesDescription) element).toString();
                }
                return null;
            }
        });

        TableColumn column = viewerColumn.getColumn();
        column.setText(name);
        return viewerColumn;
    }

}
