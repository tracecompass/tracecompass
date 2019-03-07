/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphTooltipHandler;

/**
 * Abstract tool tip handler.
 *
 * @since 3.2
 * @author Loic Prieur-Drevon - extracted from {@link TimeGraphTooltipHandler}
 */
public abstract class TmfAbstractToolTipHandler {
    private static final int MOUSE_DEADZONE = 5;
    private static final int OFFSET = 16;
    private Composite fTipComposite;
    private Shell fTipShell;
    private Rectangle fInitialDeadzone;

    /**
     * Important note: this is being added to a display filter, this may leak,
     * make sure it is removed when not needed.
     */
    private final Listener fListener = this::disposeIfExited;
    private final Listener fFocusLostListener = event -> {
        Shell tipShell = fTipShell;
        // Don't dispose if the tooltip is clicked.
        if (tipShell != null && event.display.getActiveShell() != tipShell) {
            tipShell.dispose();
        }
    };

    /**
     * Dispose the shell if we exit the range.
     *
     * @param e
     *            The event which occurred
     */
    private void disposeIfExited(Event e) {
        if (!(e.widget instanceof Control)) {
            return;
        }
        Control control = (Control) e.widget;
        if (control != null && !control.isDisposed()) {
            Point pt = control.toDisplay(e.x, e.y);
            Shell tipShell = fTipShell;
            if (tipShell != null && !tipShell.isDisposed()) {
                Rectangle bounds = tipShell.getBounds();
                bounds.x -= OFFSET;
                bounds.y -= OFFSET;
                bounds.height += 2 * OFFSET;
                bounds.width += 2 * OFFSET;
                if (!bounds.contains(pt) && !fInitialDeadzone.contains(pt)) {
                    tipShell.dispose();
                }
            }
        }
    }

    /**
     * Callback for the mouse-over tooltip
     *
     * @param control
     *            The control object to use
     */
    public void activateHoverHelp(final Control control) {

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseHover(MouseEvent event) {
                // Is application not in focus?
                // -OR- a mouse button is pressed
                if (Display.getDefault().getFocusControl() == null
                        || (event.stateMask & SWT.BUTTON_MASK) != 0
                        || (event.stateMask & SWT.KEY_MASK) != 0) {
                    return;
                }
                Point pt = new Point(event.x, event.y);
                Control timeGraphControl = (Control) event.widget;
                Point ptInDisplay = control.toDisplay(event.x, event.y);
                fInitialDeadzone = new Rectangle(ptInDisplay.x - MOUSE_DEADZONE, ptInDisplay.y - MOUSE_DEADZONE, 2 * MOUSE_DEADZONE, 2 * MOUSE_DEADZONE);
                createTooltipShell(timeGraphControl.getShell());
                for (Control child : fTipComposite.getChildren()) {
                    child.dispose();
                }
                fill(control, event, pt);
                if (fTipComposite.getChildren().length == 0) {
                    // avoid displaying empty tool tips.
                    return;
                }
                fTipShell.pack();
                Point tipPosition = control.toDisplay(pt);
                setHoverLocation(fTipShell, tipPosition);
                fTipShell.setVisible(true);
                // Register Display filters.
                Display display = Display.getDefault();
                display.addFilter(SWT.MouseMove, fListener);
                display.addFilter(SWT.FocusOut, fFocusLostListener);
            }
        });
    }

    private void createTooltipShell(Shell parent) {
        final Display display = parent.getDisplay();
        if (fTipShell != null && !fTipShell.isDisposed()) {
            fTipShell.dispose();
        }
        fTipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
        // Deregister display filters on dispose
        fTipShell.addDisposeListener(e -> e.display.removeFilter(SWT.MouseMove, fListener));
        fTipShell.addDisposeListener(e -> e.display.removeFilter(SWT.FocusOut, fFocusLostListener));
        fTipShell.addListener(SWT.Deactivate, e -> {
            if (!fTipShell.isDisposed()) {
                fTipShell.dispose();
            }
        });
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
