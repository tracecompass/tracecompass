/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphTooltipHandler;

/**
 * Abstract tool tip handler.
 *
 * @since 3.2
 * @author Loic Prieur-Drevon - extracted from {@link TimeGraphTooltipHandler}
 */
public abstract class TmfAbstractToolTipHandler {
    private static final int OFFSET = 16;
    private Composite fTipComposite;
    private Shell fTipShell;

    /**
     * Callback for the mouse-over tooltip
     *
     * @param control
     *            The control object to use
     */
    public void activateHoverHelp(final Control control) {
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (fTipShell != null && !fTipShell.isDisposed()) {
                    fTipShell.dispose();
                }
            }
        });

        control.addMouseMoveListener(e -> {
            if (fTipShell != null && !fTipShell.isDisposed()) {
                fTipShell.dispose();
            }
        });

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                Point pt = control.toDisplay(e.x, e.y);
                if (fTipShell != null && !fTipShell.isDisposed()
                        && !fTipShell.getBounds().contains(pt)) {
                    fTipShell.dispose();
                }
            }

            @Override
            public void mouseHover(MouseEvent event) {
                if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
                    return;
                }
                Point pt = new Point(event.x, event.y);
                Control timeGraphControl = (Control) event.widget;
                createTooltipShell(timeGraphControl.getShell());
                for (Control child : fTipComposite.getChildren()) {
                    child.dispose();
                }
                fill(control, event, pt);
                fTipShell.pack();
                Point tipPosition = control.toDisplay(pt);
                fTipShell.pack();
                setHoverLocation(fTipShell, tipPosition);
                fTipShell.setVisible(true);
            }
        });
    }

    private void createTooltipShell(Shell parent) {
        final Display display = parent.getDisplay();
        if (fTipShell != null && !fTipShell.isDisposed()) {
            fTipShell.dispose();
        }
        fTipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 2;
        gridLayout.marginHeight = 2;
        fTipShell.setLayout(gridLayout);
        fTipShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        fTipComposite = new Composite(fTipShell, SWT.NONE);
        fTipComposite.setLayout(new GridLayout(3, false));
        setupControl(fTipComposite);
    }

    private static void setHoverLocation(Shell shell, Point position) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = shell.getBounds();
        if (position.x + shellBounds.width + OFFSET > displayBounds.width && position.x - shellBounds.width - OFFSET >= 0) {
            shellBounds.x = position.x - shellBounds.width - OFFSET;
        } else {
            shellBounds.x = Math.max(Math.min(position.x + OFFSET, displayBounds.width - shellBounds.width), 0);
        }
        if (position.y + shellBounds.height + OFFSET > displayBounds.height && position.y - shellBounds.height - OFFSET >= 0) {
            shellBounds.y = position.y - shellBounds.height - OFFSET;
        } else {
            shellBounds.y = Math.max(Math.min(position.y + OFFSET, displayBounds.height - shellBounds.height), 0);
        }
        shell.setBounds(shellBounds);
    }

    private void setupControl(Control control) {
        control.setForeground(fTipShell.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        control.setBackground(fTipShell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                fTipShell.dispose();
            }
        });

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                fTipShell.dispose();
            }
        });

        control.addMouseMoveListener(e -> fTipShell.dispose());
    }

    /**
     * Getter for the current underlying tip {@link Composite}
     *
     * @return the current underlying tip {@link Composite}
     */
    protected Composite getTipComposite() {
        return fTipComposite;
    }

    /**
     * Method to call to add tuples : name, value to the tooltip.
     *
     * @param name
     *            name of the line
     * @param value
     *            line value
     */
    protected void addItem(String name, String value) {
        Label nameLabel = new Label(fTipComposite, SWT.NO_FOCUS);
        nameLabel.setText(name);
        setupControl(nameLabel);
        Label separator = new Label(fTipComposite, SWT.NO_FOCUS | SWT.SEPARATOR | SWT.VERTICAL);
        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.heightHint = nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        separator.setLayoutData(gd);
        setupControl(separator);
        Label valueLabel = new Label(fTipComposite, SWT.NO_FOCUS);
        valueLabel.setText(value);
        setupControl(valueLabel);
    }

    /**
     * Abstract method to override within implementations. Call
     * {@link TmfAbstractToolTipHandler#addItem(String, String)} to populate the
     * tool tip.
     *
     * @param control
     *            the underlying control
     * @param event
     *            the mouse event to react to
     * @param pt
     *            the mouse hover position in the control's coordinates
     */
    protected abstract void fill(Control control, MouseEvent event, Point pt);

}
