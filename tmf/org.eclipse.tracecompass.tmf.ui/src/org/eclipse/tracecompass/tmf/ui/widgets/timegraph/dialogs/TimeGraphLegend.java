/*******************************************************************************
 * Copyright (c) 2009, 2019 Ericsson.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.tracecompass.internal.tmf.ui.util.TimeGraphStyleUtil;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;

import com.google.common.collect.Collections2;

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

        addStateGroups(composite);

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
    private void addStateGroups(Composite composite) {

        StateItem[] stateTable = fProvider.getStateTable();
        if (stateTable == null) {
            return;
        }
        List<StateItem> stateItems = Arrays.asList(stateTable);
        Collection<StateItem> linkStates = Collections2.filter(stateItems, TimeGraphLegend::isLinkState);
        int numColumn = linkStates.isEmpty() ? 1 : 2;

        ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
        Composite innerComposite = new Composite(sc, SWT.NONE);

        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

        sc.setLayout(GridLayoutFactory.swtDefaults().margins(20, 0).create());
        sc.setLayoutData(gd);

        GridLayout gridLayout = GridLayoutFactory.swtDefaults().margins(0, 0).create();
        gridLayout.numColumns = numColumn;
        gridLayout.makeColumnsEqualWidth = false;
        innerComposite.setLayout(gridLayout);
        innerComposite.setLayoutData(gd);

        sc.setContent(innerComposite);

        createStatesGroup(innerComposite);
        createLinkGroup(linkStates, innerComposite);

        sc.setMinSize(innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Creates a states group
     *
     * @param composite
     *            the parent composite
     * @since 3.3
     */
    protected void createStatesGroup(Composite composite) {
        Group gs = new Group(composite, SWT.NONE);
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

        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.TOP;
        gs.setLayoutData(gridData);

        // Go through all the defined pairs of state color and state name and
        // display them.
        StateItem[] stateTable = fProvider.getStateTable();
        List<StateItem> stateItems = stateTable != null ? Arrays.asList(stateTable) : Collections.emptyList();
        stateItems.forEach(si -> {
            if (!isLinkState(si)) {
                new LegendEntry(gs, si);
            }
        });
    }

    private void createLinkGroup(Collection<StateItem> linkStates, Composite innerComposite) {
        if (linkStates.isEmpty()) {
            return;
        }
        Group gs = new Group(innerComposite, SWT.NONE);
        gs.setText(fProvider.getLinkTypeName());

        GridLayout layout = new GridLayout();
        layout.marginWidth = 20;
        layout.marginBottom = 10;
        gs.setLayout(layout);

        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.TOP;
        gs.setLayoutData(gridData);

        // Go through all the defined pairs of state color and state name and
        // display them.
        linkStates.forEach(si -> new LegendEntry(gs, si));
    }

    /**
     * Test whether a state item is a link state or not
     *
     * @param si
     *            The state item
     * @return True if the state item is a link state, false otherwise
     * @since 4.0
     */
    protected static boolean isLinkState(StateItem si) {
        Object itemType = si.getStyleMap().getOrDefault(ITimeEventStyleStrings.itemTypeProperty(), ITimeEventStyleStrings.stateType());
        return itemType instanceof String && ((String) itemType).equals(ITimeEventStyleStrings.linkType());
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
            String fillColorKey = TimeGraphStyleUtil.getPreferenceName(fProvider, si, ITimeEventStyleStrings.fillColor());
            String heightFactorKey = TimeGraphStyleUtil.getPreferenceName(fProvider, si, ITimeEventStyleStrings.heightFactor());
            IPreferenceStore store = TimeGraphStyleUtil.getStore();
            TimeGraphStyleUtil.loadValue(fProvider, si);
            String name = si.getStateString();
            setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
            fBar = new Swatch(this, si.getStateColor());
            fBar.setToolTipText(Messages.TimeGraphLegend_swatchClick);
            fBar.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(MouseEvent e) {
                    Shell shell = new Shell();
                    ColorDialog cd = new ColorDialog(shell, SWT.NONE);
                    cd.setRGB(fBar.fColor.getRGB());
                    RGB color = cd.open();
                    if (color != null) {
                        store.setValue(fillColorKey, new RGBAColor(color.red, color.green, color.blue, 255).toString());
                        fBar.setColor(color);
                        si.setStateColor(color);
                        fProvider.refresh();
                        fReset.setEnabled(true);
                    }
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
            fScale.setSelection((int) (100 * si.getStateHeightFactor()));
            fScale.setToolTipText(Messages.TimeGraphLegend_widthTooltip);
            fScale.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    float newWidth = fScale.getSelection() * 0.01f;
                    store.setValue(heightFactorKey, newWidth);
                    si.getStyleMap().put(ITimeEventStyleStrings.heightFactor(), newWidth);
                    fProvider.refresh();
                    fReset.setEnabled(true);
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
                    store.setToDefault(heightFactorKey);
                    store.setToDefault(fillColorKey);
                    fBar.setColor(si.getStateColor());
                    fScale.setSelection((int) (100 * si.getStateHeightFactor()));
                    fProvider.refresh();
                    fReset.setEnabled(false);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // do nothing
                }
            });
            fReset.setToolTipText(Messages.TimeGraphLegend_resetTooltip);
            fReset.setImage(RESET_IMAGE.createImage());
            fReset.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create());
            if (store.getString(fillColorKey).equals(store.getDefaultString(fillColorKey)) &&
                    store.getFloat(heightFactorKey) == store.getDefaultFloat(heightFactorKey)) {
                fReset.setEnabled(false);
            }
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
