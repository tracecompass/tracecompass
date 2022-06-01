/*******************************************************************************
 * Copyright (c) 2009, 2020 Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

import java.util.ArrayList;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.util.TimeGraphStyleUtil;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;

import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

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
    private Composite fInnerComposite;

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
        Composite composite = (Composite) super.createDialogArea(parent);

        addStateGroups(composite);

        setTitle(Messages.TmfTimeLegend_LEGEND);
        setDialogHelpAvailable(false);
        setHelpAvailable(false);

        // Set the minimum size to avoid 0-sized legends from user resize
        parent.getShell().setMinimumSize(150, 150);

        composite.addDisposeListener((e) -> {
            fResourceManager.dispose();
        });
        return composite;
    }

    @Override
    protected Point getInitialSize() {
        Point initialSize = super.getInitialSize();
        Composite innerComposite = fInnerComposite;
        /*
         * If shell initial size is taller than available area, use 2 columns.
         */
        if (initialSize.y > Display.getDefault().getClientArea().height && innerComposite != null) {
            // Needed to make sure resize listener gets the right shell size
            getShell().layout();
            getGridLayouts(innerComposite).forEach(gl -> gl.numColumns = 2);
            initialSize = super.getInitialSize();
            // Needed to make sure vertical scroll bar appears
            Display.getDefault().asyncExec(() -> innerComposite.getParent().layout());
        }
        return initialSize;
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

        ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
        sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.setLayout(GridLayoutFactory.swtDefaults().margins(200, 0).create());

        Composite innerComposite = new Composite(sc, SWT.NONE);
        fInnerComposite = innerComposite;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        innerComposite.setLayoutData(gd);
        innerComposite.setLayout(new GridLayout());
        innerComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                /*
                 * Find the highest number of columns that fits in the new width
                 */
                Point size = innerComposite.getSize();
                List<GridLayout> gridLayouts = getGridLayouts(innerComposite);
                Point minSize = new Point(0, 0);
                for (int columns = 8; columns > 0; columns--) {
                    final int numColumns = columns;
                    gridLayouts.forEach(gl -> gl.numColumns = numColumns);
                    minSize = innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    if (minSize.x <= size.x) {
                        break;
                    }
                }
                sc.setMinSize(0, minSize.y);
            }
        });

        sc.setContent(innerComposite);

        createStatesGroup(innerComposite);
        createLinkGroup(linkStates, innerComposite);

        sc.setMinSize(0, innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }

    /**
     * Creates a states group
     *
     * @param composite
     *            the parent composite
     * @since 3.3
     */
    protected void createStatesGroup(Composite composite) {
        String stateTypeName = fProvider.getStateTypeName();
        StringBuilder buffer = new StringBuilder();
        if (!stateTypeName.isEmpty()) {
            buffer.append(stateTypeName);
            buffer.append(" "); //$NON-NLS-1$
        }
        buffer.append(Messages.TmfTimeLegend_StateTypeName);

        StateItem[] stateTable = fProvider.getStateTable();
        List<StateItem> stateItems = stateTable != null ? Arrays.asList(stateTable) : Collections.emptyList();
        Multimap<String, StateItem> groupedStateItems = LinkedHashMultimap.create();
        for (StateItem stateItem : stateItems) {
            if (!isLinkState(stateItem)) {
                Object group = stateItem.getStyleMap().get(StyleProperties.STYLE_GROUP);
                if (group instanceof String) {
                    groupedStateItems.put((String) group, stateItem);
                } else {
                    groupedStateItems.put(buffer.toString(), stateItem);
                }
            }
        }

        for (String groupName : groupedStateItems.keySet()) {
            Collection<StateItem> groupItems = groupedStateItems.get(groupName);
            createGroup(composite, groupName, groupItems);
        }
    }

    private static List<GridLayout> getGridLayouts(Composite innerComposite) {
        List<GridLayout> gridLayouts = new ArrayList<>();
        if (innerComposite == null) {
            return gridLayouts;
        }
        Arrays.asList(innerComposite.getChildren()).forEach(control -> {
            if (control instanceof Composite) {
                Layout layout = ((Composite) control).getLayout();
                if (layout instanceof GridLayout) {
                    gridLayouts.add((GridLayout) layout);
                }
            }
        });
        return gridLayouts;
    }

    private void createLinkGroup(Collection<StateItem> linkStates, Composite composite) {
        if (linkStates.isEmpty()) {
            return;
        }
        createGroup(composite, fProvider.getLinkTypeName(), linkStates);
    }

    private void createGroup(Composite parent, String name, Collection<StateItem> stateItems) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(name);
        group.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 10;
        layout.marginBottom = 10;
        group.setLayout(layout);

        for (StateItem stateItem : stateItems) {
            new LegendEntry(group, stateItem);
        }
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
        Object itemType = si.getStyleMap().getOrDefault(StyleProperties.itemTypeProperty(), StyleProperties.stateType());
        return itemType instanceof String && ((String) itemType).equals(StyleProperties.linkType());
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
     * Widget for a legend entry has a color chooser, a label, a width and a
     * reset button
     *
     * @author Matthew Khouzam
     * @since 3.3
     */
    protected class LegendEntry extends Composite {
        /**
         * ID to identify a control as part of a given entry
         */
        private static final String LEGEND_ENTRY_KEY = "legend.entry.key"; //$NON-NLS-1$
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
            String bgColorKey = TimeGraphStyleUtil.getPreferenceName(fProvider, si, StyleProperties.BACKGROUND_COLOR);
            String heightFactorKey = TimeGraphStyleUtil.getPreferenceName(fProvider, si, StyleProperties.HEIGHT);
            String widthKey = TimeGraphStyleUtil.getPreferenceName(fProvider, si, StyleProperties.WIDTH);
            IPreferenceStore store = TimeGraphStyleUtil.getStore();
            TimeGraphStyleUtil.loadValue(fProvider, si);
            String name = si.getStateString();
            setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
            fBar = new Swatch(this, si.getStateColor());
            fBar.setData(LEGEND_ENTRY_KEY, name);
            fBar.setToolTipText(Messages.TimeGraphLegend_swatchClick);
            fBar.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(MouseEvent e) {
                    Shell shell = new Shell();
                    ColorDialog cd = new ColorDialog(shell, SWT.NONE);
                    cd.setRGB(fBar.fColor.getRGB());
                    RGB color = cd.open();
                    if (color != null) {
                        store.setValue(bgColorKey, ColorUtils.toHexColor(color.red, color.green, color.blue));
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
            label.setData(LEGEND_ENTRY_KEY, name);
            label.setText(name);
            label.setLayoutData(GridDataFactory.fillDefaults().hint(160, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).create());
            fScale = new Scale(this, SWT.NONE);
            if (si.getStyleMap().get(StyleProperties.WIDTH) instanceof Integer) {
                fScale.setMinimum(1);
                fScale.setMaximum(10);
                fScale.setSelection(si.getStateWidth());
            } else {
                fScale.setMinimum(1);
                fScale.setMaximum(100);
                fScale.setSelection((int) (100 * si.getStateHeightFactor()));
            }
            fScale.setToolTipText(Messages.TimeGraphLegend_widthTooltip);
            fScale.setData(LEGEND_ENTRY_KEY, name);
            fScale.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (si.getStyleMap().get(StyleProperties.WIDTH) instanceof Integer) {
                        int newWidth = fScale.getSelection();
                        store.setValue(widthKey, newWidth);
                        si.getStyleMap().put(StyleProperties.WIDTH, newWidth);
                    } else {
                        float newHeight = fScale.getSelection() * 0.01f;
                        store.setValue(heightFactorKey, newHeight);
                        si.getStyleMap().put(StyleProperties.HEIGHT, newHeight);
                    }
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
            fReset.setData(LEGEND_ENTRY_KEY, name);
            fReset.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    si.reset();
                    store.setToDefault(heightFactorKey);
                    store.setToDefault(widthKey);
                    store.setToDefault(bgColorKey);
                    fBar.setColor(si.getStateColor());
                    if (si.getStyleMap().get(StyleProperties.WIDTH) instanceof Integer) {
                        fScale.setSelection(si.getStateWidth());
                    } else {
                        fScale.setSelection((int) (100 * si.getStateHeightFactor()));
                    }
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
            if (store.getString(bgColorKey).equals(store.getDefaultString(bgColorKey)) &&
                    store.getFloat(heightFactorKey) == store.getDefaultFloat(heightFactorKey) &&
                    store.getInt(widthKey) == store.getDefaultInt(widthKey)) {
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
            addListener(SWT.Paint, event -> draw(event.gc));
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
