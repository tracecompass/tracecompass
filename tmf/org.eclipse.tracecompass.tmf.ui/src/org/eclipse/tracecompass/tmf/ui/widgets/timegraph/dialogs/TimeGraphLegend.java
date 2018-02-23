/*******************************************************************************
 * Copyright (c) 2009, 2018 Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;

/**
 * Legend for the colors used in the time graph view
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphLegend extends TitleAreaDialog {

    private static final ImageDescriptor RESET_IMAGE = Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_RESET_BUTTON);
    private final ITimeGraphPresentationProvider fProvider;
    private final LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());

    /**
     * Open the time graph legend window
     *
     * @param parent
     *            The parent shell
     * @param provider
     *            The presentation provider
     */
    public static void open(Shell parent, ITimeGraphPresentationProvider provider) {
        (new TimeGraphLegend(parent, provider)).open();
    }

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent shell
     * @param provider
     *            The presentation provider
     */
    public TimeGraphLegend(Shell parent, ITimeGraphPresentationProvider provider) {
        super(parent);
        fProvider = provider;
        this.setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    /**
     * Gets the Presentation Provider
     *
     * @return the presentation provider
     * @since 3.3
     */
    protected final ITimeGraphPresentationProvider getPresentationProvider() {
        return fProvider;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dlgArea = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(dlgArea, SWT.NONE);

        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        createStatesGroup(composite);

        setTitle(Messages.TmfTimeLegend_LEGEND);
        setDialogHelpAvailable(false);
        setHelpAvailable(false);

        // Set the minimum size to avoid 0 sized legends from user resize
        dlgArea.getShell().setMinimumSize(150, 150);

        composite.addDisposeListener((e) -> {
            fResourceManager.dispose();
        });
        return composite;
    }

    /**
     * Creates a states group
     *
     * @param composite
     *            the parent composite
     * @since 3.3
     */
    protected void createStatesGroup(Composite composite) {
        ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        Group gs = new Group(sc, SWT.H_SCROLL);
        sc.setContent(gs);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        sc.setLayoutData(gd);

        String stateTypeName = fProvider.getStateTypeName();
        StringBuilder buffer = new StringBuilder();
        if (!stateTypeName.isEmpty()) {
            buffer.append(stateTypeName);
            buffer.append(" "); //$NON-NLS-1$
        }
        buffer.append(Messages.TmfTimeLegend_StateTypeName);
        gs.setText(buffer.toString());

        GridLayout layout = new GridLayout();
        layout.marginWidth = 20;
        layout.marginBottom = 10;
        gs.setLayout(layout);

        // Go through all the defined pairs of state color and state name and
        // display them.
        StateItem[] stateItems = fProvider.getStateTable();
        for (int i = 0; i < stateItems.length; i++) {

            // draw color with name
            new LegendEntry(gs, stateItems[i]);
        }
        sc.setMinSize(gs.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.TmfTimeLegend_LEGEND);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    /**
     * Widget for a legend entry has a color chooser, a label, a width and a reset
     * button
     *
     * @author Matthew Khouzam
     * @since 3.3
     */
    protected class LegendEntry extends Composite {
        private final Swatch fBar;
        private final Scale fScale;
        private final Button fReset;

        /**
         * Constructor
         *
         * @param parent
         *            parent composite
         * @param si
         *            the state item
         */
        public LegendEntry(Composite parent, StateItem si) {
            super(parent, SWT.NONE);
            RGB rgb = si.getStateColor();
            String name = si.getStateString();
            float width = (float) si.getStyleMap().getOrDefault(ITimeEventStyleStrings.heightFactor(), 1.0f);
            setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
            fBar = new Swatch(this, rgb);
            fBar.setToolTipText(Messages.TimeGraphLegend_swatchClick);
            fBar.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(MouseEvent e) {
                    Shell shell = new Shell();
                    ColorDialog cd = new ColorDialog(shell, SWT.NONE);
                    cd.setRGB(fBar.fColor.getRGB());
                    RGB color = cd.open();
                    fBar.setColor(color);
                    si.setStateColor(color);
                    fProvider.refresh();
                }
            });
            fBar.addMouseTrackListener(new MouseTrackListener() {

                @Override
                public void mouseHover(MouseEvent e) {
                    // Do nothing
                }

                @Override
                public void mouseExit(MouseEvent e) {
                    Shell shell = parent.getShell();
                    Cursor old = shell.getCursor();
                    shell.setCursor(new Cursor(e.display, SWT.CURSOR_ARROW));
                    if (old != null) {
                        old.dispose();
                    }
                }

                @Override
                public void mouseEnter(MouseEvent e) {
                    Shell shell = parent.getShell();
                    Cursor old = shell.getCursor();
                    shell.setCursor(new Cursor(e.display, SWT.CURSOR_HAND));
                    if (old != null) {
                        old.dispose();
                    }
                }
            });

            fBar.setLayoutData(GridDataFactory.swtDefaults().hint(30, 20).create());
            CLabel label = new CLabel(this, SWT.NONE) {
                @Override
                protected String shortenText(GC gc, String t, int w) {
                    String text = super.shortenText(gc, t, w);
                    setToolTipText(t.equals(text) ? null : t);
                    return text;
                }
            };
            label.setText(name);
            label.setLayoutData(GridDataFactory.fillDefaults().hint(160, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).create());
            fScale = new Scale(this, SWT.NONE);
            fScale.setMaximum(100);
            fScale.setMinimum(1);
            fScale.setSelection((int) (100 * width));
            fScale.setToolTipText(Messages.TimeGraphLegend_widthTooltip);
            fScale.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    si.getStyleMap().put(ITimeEventStyleStrings.heightFactor(), fScale.getSelection() * 0.01f);
                    fProvider.refresh();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // do nothing
                }
            });
            fScale.setLayoutData(GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).create());
            fReset = new Button(this, SWT.FLAT);
            fReset.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    si.reset();
                    fBar.setColor(si.getStateColor());
                    fScale.setSelection((int) (100 * (float) si.getStyleMap().getOrDefault(ITimeEventStyleStrings.heightFactor(), 1.0f)));
                    fProvider.refresh();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // do nothing
                }
            });
            fReset.setToolTipText(Messages.TimeGraphLegend_resetTooltip);
            fReset.setImage(RESET_IMAGE.createImage());
            fReset.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create());
        }

        @Override
        public void dispose() {
            fReset.getImage().dispose();
            super.dispose();
        }
    }

    private class Swatch extends Canvas {
        private Color fColor;

        public Swatch(Composite parent, RGB rgb) {
            super(parent, SWT.FLAT);

            fColor = fResourceManager.createColor(rgb);
            setForeground(fColor);
            addListener(SWT.Paint, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    draw(event.gc);
                }
            });
        }

        public void setColor(RGB rgb) {
            if (rgb != null) {
                fColor = fResourceManager.createColor(rgb);
                setForeground(fColor);
                redraw();
            }
        }

        private void draw(GC gc) {
            Rectangle r = getClientArea();
            gc.setBackground(fColor);
            gc.fillRectangle(r);
            gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
            gc.setLineWidth(2);
            gc.drawRectangle(1, 1, r.width - 2, r.height - 2);
        }
    }
}
