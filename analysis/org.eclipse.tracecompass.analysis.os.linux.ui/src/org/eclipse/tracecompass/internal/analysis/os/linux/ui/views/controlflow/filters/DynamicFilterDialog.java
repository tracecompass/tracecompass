/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.Range;

/**
 * The Dynamic Filters configuration dialog.
 *
 * @author Jonathan Rajotte Julien
 */
public class DynamicFilterDialog extends TitleAreaDialog {

    /** Pattern for CPUS ranges e.g.: 1,1-200,2,3 */
    private static final Pattern CPU_RANGE = Pattern.compile("^((\\d+(\\-\\d+)?, ?)*(\\d+(\\-\\d+)?))+$"); //$NON-NLS-1$
    private static final @NonNull String INTERNAL_RANGE_SEPARATOR = "-"; //$NON-NLS-1$
    private static final @NonNull String RANGES_DELIMITER = ","; //$NON-NLS-1$

    /** The internal ActiveThreadsFilter result */
    private @NonNull ActiveThreadsFilter fInternalActiveThreadsFilter;
    private final @Nullable ITmfTrace fTrace;

    private Button fActiveThreadEnabledButton;
    private Button fAllActiveThreadsRadionButton;
    private Button fCpuRangesRadioButton;
    private Text fCpuRangesField;

    /**
     * Constructor to create a DynamicFilterDialog
     *
     * @param parentShell
     *            The parent shell
     * @param filter
     *            An
     *            {@link org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.ActiveThreadsFilter
     *            ActiveThreadFilter} instance.
     * @param trace
     *            The relevant trace
     */
    public DynamicFilterDialog(Shell parentShell, @NonNull ActiveThreadsFilter filter, @Nullable ITmfTrace trace) {
        super(parentShell);
        fInternalActiveThreadsFilter = filter;
        fTrace = trace;
    }

    @Override
    public void create() {
        super.create();
        setTitle(Messages.DynamicFilterDialog_Title);
        this.getShell().setText(Messages.DynamicFilterDialog_Title);
    }

    private static boolean validateCpuRange(final String newString) {
        return CPU_RANGE.matcher(newString).matches();
    }

    private void createActiveThreadSection(Composite parent) {
        boolean usesCpuRanges = false;
        boolean filterActive = false;

        ActiveThreadsFilter filter = fInternalActiveThreadsFilter;

        filterActive = fInternalActiveThreadsFilter.isEnabled();
        usesCpuRanges = filter.isCpuRangesBased();

        GridData gd;
        GridLayout gl;
        Group activeThreadGroup = new Group(parent, SWT.SHADOW_NONE | SWT.BORDER);
        activeThreadGroup.setText(Messages.DynamicFilterDialog_ActiveThreadsFilterName);

        activeThreadGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        gl = new GridLayout(1, true);
        gl.marginLeft = gl.marginRight = 0;
        activeThreadGroup.setLayout(gl);

        fActiveThreadEnabledButton = new Button(activeThreadGroup, SWT.CHECK);
        fActiveThreadEnabledButton.setText(Messages.DynamicFilterDialog_ActiveThreadsFilterName);
        fActiveThreadEnabledButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /* Cpu selection */
        Group cpuSelectionGroup = new Group(activeThreadGroup, SWT.SHADOW_NONE);
        cpuSelectionGroup.setText(Messages.DynamicFilterDialog_OptionsGroupLabel);
        cpuSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        cpuSelectionGroup.setLayout(new GridLayout(2, false));

        fAllActiveThreadsRadionButton = new Button(cpuSelectionGroup, SWT.RADIO);
        fAllActiveThreadsRadionButton.setText(Messages.DynamicFilterDialog_RadioButtonAllActiveThreads);
        fAllActiveThreadsRadionButton.setToolTipText(Messages.DynamicFilterDialog_RadioButtonAllActiveThreadsToolTip);

        fAllActiveThreadsRadionButton.setSelection(!usesCpuRanges);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        fAllActiveThreadsRadionButton.setLayoutData(gd);

        fCpuRangesRadioButton = new Button(cpuSelectionGroup, SWT.RADIO);
        fCpuRangesRadioButton.setText(Messages.DynamicFilterDialog_CpuRangesLabel);
        fCpuRangesRadioButton.setToolTipText(Messages.DynamicFilterDialog_CpuRangesTooltip);

        fCpuRangesRadioButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

        fCpuRangesField = new Text(cpuSelectionGroup, SWT.SINGLE | SWT.BORDER);
        fCpuRangesField.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
        fCpuRangesField.setMessage(Messages.DynamicFilterDialog_CpuRangesExamples);

        fCpuRangesRadioButton.setToolTipText(Messages.DynamicFilterDialog_CpuRangesTooltip);

        /* Attach an automatic validation to the field */
        fCpuRangesField.addVerifyListener(e -> {
            /* Reconstruct the string */
            final String oldString = fCpuRangesField.getText();
            final String newString = oldString.substring(0, e.start) + e.text + oldString.substring(e.end);

            /* Validate the string */
            boolean valid = validateCpuRange(newString);

            Button okButton = getButton(IDialogConstants.OK_ID);
            if (okButton != null) {
                getButton(IDialogConstants.OK_ID).setEnabled(valid);
            }
            if (valid) {
                setErrorMessage(null);
            } else {
                setErrorMessage(Messages.DynamicFilterDialog_InvalidRangesErrorMsg);
            }
        });

        fAllActiveThreadsRadionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                boolean selected = ((Button) e.widget).getSelection();
                if (!selected) {
                    return;
                }
                setErrorMessage(null);
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }
        });

        fCpuRangesRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                boolean selected = ((Button) e.widget).getSelection();
                fCpuRangesField.setEnabled(selected);
                validateCpuRange(fCpuRangesField.getText());
            }
        });

        fActiveThreadEnabledButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                boolean selected = ((Button) e.widget).getSelection();
                cpuSelectionGroup.setEnabled(selected);
                fAllActiveThreadsRadionButton.setEnabled(selected);
                fCpuRangesRadioButton.setEnabled(selected);
                fCpuRangesField.setEnabled(selected && fCpuRangesRadioButton.getSelection());
            }
        });

        /* Set the base state for the ui control */
        fActiveThreadEnabledButton.setSelection(filterActive);
        cpuSelectionGroup.setEnabled(filterActive);
        fCpuRangesRadioButton.setEnabled(filterActive);
        fAllActiveThreadsRadionButton.setEnabled(filterActive);

        fAllActiveThreadsRadionButton.setSelection(!usesCpuRanges);
        fCpuRangesRadioButton.setSelection(usesCpuRanges);

        fCpuRangesField.setEnabled(filterActive && usesCpuRanges);

        /* Populate the CPU ranges fields */
        if (!filter.getCpuRanges().isEmpty()) {
            StringJoiner joiner = new StringJoiner(RANGES_DELIMITER);
            for (Range<Long> range : filter.getCpuRanges()) {
                String rangeString = range.lowerEndpoint().toString();
                if (range.lowerEndpoint() != range.upperEndpoint()) {
                    rangeString = rangeString.concat(INTERNAL_RANGE_SEPARATOR + range.upperEndpoint());
                }
                joiner.add(rangeString);
            }
            fCpuRangesField.setText(joiner.toString());
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, true);
        container.setLayout(layout);
        createActiveThreadSection(container);
        return area;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private void saveInput() {
        if (!(fAllActiveThreadsRadionButton.getSelection() || fCpuRangesRadioButton.getSelection())
                || (fAllActiveThreadsRadionButton.getSelection() && fCpuRangesRadioButton.getSelection())) {
            throw new IllegalStateException(Messages.DynamicFilterDialog_InvalidRadioButtonState);
        }

        List<Range<Long>> ranges = null;
        ranges = parseCpuRangesText(fCpuRangesField.getText());

        fInternalActiveThreadsFilter = new ActiveThreadsFilter(ranges, fCpuRangesRadioButton.getSelection(), fTrace);
        fInternalActiveThreadsFilter.setEnabled(fActiveThreadEnabledButton.getSelection());
    }

    private static List<Range<Long>> parseCpuRangesText(final String string) {
        List<Range<Long>> results = new ArrayList<>();
        if (validateCpuRange(string)) {
            string.split(RANGES_DELIMITER);
            for (String range : string.split(RANGES_DELIMITER)) {
                if (range.contains(INTERNAL_RANGE_SEPARATOR)) {
                    /* Parse as a range */
                    String[] split = range.split(INTERNAL_RANGE_SEPARATOR);
                    if (split.length != 2) {
                        /* Invalid range */
                        continue;
                    }

                    long[] sorted = new long[split.length];
                    Arrays.setAll(sorted, i -> Long.parseLong(split[i]));
                    Arrays.sort(sorted);
                    results.add(Range.closed(sorted[0], sorted[1]));
                } else {
                    /* Parse as an individual number */
                    Long value = Long.parseLong(range);
                    results.add(Range.closed(value, value));
                }
            }
        }
        return results;
    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    @Override
    public boolean isHelpAvailable() {
        return false;
    }

    /**
     * Get the resulting ActiveThreadsFilter
     *
     * @return The configured
     *         {@link org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.ActiveThreadsFilter
     *         ActiveThreadFilter} instance.
     */
    public @NonNull ActiveThreadsFilter getActiveThreadsResult() {
        return fInternalActiveThreadsFilter;
    }
}
