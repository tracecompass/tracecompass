/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog box for entering minimum and maximum time range for time compression bar. 
 * 
 * @version 1.0
 * @author sveyrier
 * @author Bernd Hufmann
 * 
 */
public class MinMaxDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Label for minimum. 
     */
    protected Label minLabel;
    /**
     * Label for maximum.
     */
    protected Label maxLabel;
    /**
     * Label for scale
     */
    protected Label scaleLabel;
    /**
     * Label for precision.
     */
    protected Label precisionLabel;
    /**
     * Text field for minimum. 
     */
    protected Text minText;
    /**
     * Text field for maximum. 
     */
    protected Text maxText;
    /**
     * Text field for scale. 
     */
    protected Text scaleText;
    /**
     * Text field for precision. 
     */
    protected Text precisionText;
    /**
     * The sequence diagram widget reference.
     */
    protected SDWidget sdWidget;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Standard constructor.
     * @param shell The shell
     * @param viewer The sequence diagram widget reference. 
     */
    public MinMaxDialog(Shell shell, SDWidget viewer) {
        super(shell);
        sdWidget = viewer;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Method to create a grid data base on horizontal span.
     * @param span The horizontal span
     * @return a grid data object
     */
    protected GridData newGridData(int span) {
        GridData data = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = span;
        return data;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite p) {
        p.getShell().setText(SDMessages._123);
        Composite parent = (Composite) super.createDialogArea(p);

        GridLayout parentLayout = new GridLayout();
        parentLayout.numColumns = 6;
        parent.setLayout(parentLayout);

        Group g1 = new Group(parent, SWT.SHADOW_NONE);
        g1.setLayoutData(newGridData(3));
        GridLayout g1layout = new GridLayout();
        g1layout.numColumns = 3;
        g1.setLayout(g1layout);

        minLabel = new Label(g1, SWT.RADIO);
        minLabel.setText(SDMessages._124);
        minLabel.setLayoutData(newGridData(1));

        minText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        minText.setLayoutData(newGridData(2));
        minText.setText(String.valueOf(sdWidget.getFrame().getMinTime().getValue()));

        maxLabel = new Label(g1, SWT.RADIO);
        maxLabel.setText(SDMessages._125);
        maxLabel.setLayoutData(newGridData(1));

        maxText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        maxText.setLayoutData(newGridData(2));
        maxText.setText(String.valueOf(sdWidget.getFrame().getMaxTime().getValue()));

        scaleLabel = new Label(g1, SWT.RADIO);
        scaleLabel.setText(SDMessages._136);
        scaleLabel.setLayoutData(newGridData(1));

        scaleText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        scaleText.setLayoutData(newGridData(2));
        scaleText.setText(String.valueOf(sdWidget.getFrame().getMinTime().getScale()));

        
        precisionLabel = new Label(g1, SWT.RADIO);
        precisionLabel.setText(SDMessages._137);
        precisionLabel.setLayoutData(newGridData(1));

        precisionText = new Text(g1, SWT.SINGLE | SWT.BORDER);
        precisionText.setLayoutData(newGridData(2));
        precisionText.setText(String.valueOf(sdWidget.getFrame().getMinTime().getPrecision()));

        return parent;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        long min = 0;
        long max = 0;
        int scale = 0;
        int precision = 0;
        try {
            min = Long.parseLong(minText.getText());
            max = Long.parseLong(maxText.getText());
            scale = Integer.parseInt(scaleText.getText());
            precision = Integer.parseInt(precisionText.getText());

            sdWidget.getFrame().setMax(new TmfTimestamp(max, scale, precision));
            sdWidget.getFrame().setMin(new TmfTimestamp(min, scale, precision));

            sdWidget.redraw();

            super.okPressed();
        } catch (Exception e) {
            MessageDialog.openError(getShell(), SDMessages._98, SDMessages._99);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        createButton(parent, IDialogConstants.CLIENT_ID, SDMessages._126, false);
        getButton(IDialogConstants.CLIENT_ID).addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                sdWidget.getFrame().resetCustomMinMax();
                minText.setText(String.valueOf(sdWidget.getFrame().getMinTime().getValue()));
                maxText.setText(String.valueOf(sdWidget.getFrame().getMaxTime().getValue()));
                scaleText.setText(String.valueOf(sdWidget.getFrame().getMinTime().getScale()));
                precisionText.setText(String.valueOf(sdWidget.getFrame().getMinTime().getPrecision()));
                maxText.getParent().layout(true);
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
    }
}
