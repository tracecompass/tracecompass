/**********************************************************************
 * Copyright (c) 2019 Draeger, Auriga
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.AxisRange;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.swtchart.Range;

/**
 * Dialog box for entering minimum and maximum range for Y axis of
 * {@link TmfChartView}.
 *
 * @author Ivan Grinenko
 * @deprecated use {@link org.eclipse.tracecompass.internal.tmf.ui.views.xychart.LockRangeDialog}
 *
 */
@Deprecated
public class LockRangeDialog extends Dialog {
    /**
     * Checkbox to lock or unlock the axis.
     */
    private Button fCheck;
    /**
     * Text field for minimum.
     */
    private Text fMinText;
    /**
     * Text field for maximum.
     */
    private Text fMaxText;
    /**
     * Viewer with Y axis to lock.
     */
    private final TmfXYChartViewer fChartViewer;
    /**
     * ModifyListener for the text inputs.
     */
    private ModifyListener fModifyListener = e -> validateInputs();

    private static boolean isValidDouble(String input) {
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Constructor.
     *
     * @param parentShell
     *            parent shell
     * @param chartViewer
     *            viewer with Y axis to lock
     */
    public LockRangeDialog(Shell parentShell, TmfXYChartViewer chartViewer) {
        super(parentShell);
        fChartViewer = chartViewer;
    }

    @Override
    protected Control createDialogArea(Composite p) {
        p.getShell().setText(Messages.TmfChartView_LockYAxis);
        Composite parent = (Composite) super.createDialogArea(p);

        GridLayout parentLayout = new GridLayout(2, false);
        parent.setLayout(parentLayout);

        Range range = fChartViewer.getSwtChart().getAxisSet().getYAxis(0).getRange();

        fCheck = new Button(parent, SWT.CHECK);
        fCheck.setText(Messages.TmfChartView_LockButton);
        fCheck.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        fCheck.setSelection(true);
        fCheck.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fMinText.setEnabled(fCheck.getSelection());
                fMaxText.setEnabled(fCheck.getSelection());
                validateInputs();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        Label minLabel = new Label(parent, SWT.RADIO);
        minLabel.setText(Messages.TmfChartView_LowerYAxisRange);
        minLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        fMinText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        fMinText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fMinText.setText(String.valueOf(range.lower));
        fMinText.addModifyListener(fModifyListener);

        Label maxLabel = new Label(parent, SWT.RADIO);
        maxLabel.setText(Messages.TmfChartView_UpperYAxisRange);
        maxLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        fMaxText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        fMaxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fMaxText.setText(String.valueOf(range.upper));
        fMaxText.addModifyListener(fModifyListener);

        return parent;
    }

    @Override
    protected void okPressed() {
        if (fCheck.getSelection()) {
            lockAxis();
        } else {
            fChartViewer.setFixedYRange(null);
        }
        super.okPressed();
    }

    private void lockAxis() {
        double min = 0;
        double max = 0;
        try {
            min = Double.parseDouble(fMinText.getText());
            max = Double.parseDouble(fMaxText.getText());
            fChartViewer.setFixedYRange(new AxisRange(min, max));
        } catch (NumberFormatException e) {
            // Suppose values are already validated
        }
    }

    /**
     * OK is enabled only if both of text inputs contain valid doubles or the
     * checkbox is unchecked.
     */
    private void validateInputs() {
        boolean isValidInput = isValidDouble(fMaxText.getText()) && isValidDouble(fMinText.getText());
        getButton(IDialogConstants.OK_ID).setEnabled(isValidInput || !fCheck.getSelection());
    }

}
