/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.dialogs;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;

/**
 * A dialog for soliciting a bookmark description and color from the user.
 *
 * @since 2.0
 */
public class AddBookmarkDialog extends MultiLineInputDialog {

    private ColorSelector fColorSelector;
    private Scale fAlphaScale;
    private Label fAlphaLabel;
    private int fAlpha = 128;

    /**
     * Constructor
     *
     * @param parentShell
     *            the parent shell
     * @param initialValue
     *            the initial input value, or <code>null</code> if none (equivalent to the empty string)
     */
    public AddBookmarkDialog(Shell parentShell, String initialValue) {
        super(parentShell, Messages.AddBookmarkDialog_Title, Messages.AddBookmarkDialog_Message, initialValue);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite areaComposite = (Composite) super.createDialogArea(parent);
        Composite colorComposite = new Composite(areaComposite, SWT.NONE);
        RowLayout layout = new RowLayout();
        layout.center = true;
        colorComposite.setLayout(layout);
        colorComposite.moveBelow(getText());
        Label colorLabel = new Label(colorComposite, SWT.NONE);
        colorLabel.setText(Messages.AddBookmarkDialog_Color);
        fColorSelector = new ColorSelector(colorComposite);
        fColorSelector.setColorValue(new RGB(255, 0, 0));
        Label alphaLabel = new Label(colorComposite, SWT.NONE);
        alphaLabel.setText(Messages.AddBookmarkDialog_Alpha);
        fAlphaScale = new Scale(colorComposite, SWT.NONE);
        fAlphaScale.setMaximum(255);
        fAlphaScale.setSelection(fAlpha);
        fAlphaScale.setIncrement(1);
        fAlphaScale.setPageIncrement(16);
        fAlphaScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fAlpha = fAlphaScale.getSelection();
                fAlphaLabel.setText(Integer.toString(fAlpha));
            }
        });
        fAlphaLabel = new Label(colorComposite, SWT.NONE);
        fAlphaLabel.setText(Integer.toString(fAlpha));
        return areaComposite;
    }

    /**
     * Returns the color selected in this dialog.
     *
     * @return the color RGBA value
     */
    public RGBA getColorValue() {
        RGB rgb = fColorSelector.getColorValue();
        return new RGBA(rgb.red, rgb.green, rgb.blue, fAlpha);
    }
}
