/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.viewers.events;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;

/**
 * Header bar for the events table.
 *
 * @since 2.0
 */
public class TmfEventsTableHeader extends Composite {

    private static final Image COLLAPSED = Activator.getDefault().getImageFromPath("icons/ovr16/collapsed_ovr.gif"); //$NON-NLS-1$
    private static final Image EXPANDED = Activator.getDefault().getImageFromPath("icons/ovr16/expanded_ovr.gif"); //$NON-NLS-1$
    private static final Image DELETE = Activator.getDefault().getImageFromPath("icons/elcl16/delete_button.gif"); //$NON-NLS-1$
    private static final Image DELETE_SMALL = Activator.getDefault().getImageFromPath("icons/ovr16/delete_ovr.gif"); //$NON-NLS-1$
    private static final int DEFAULT_MARGIN = 3;
    private static final int COLLAPSED_IMAGE_MARGIN = 2;
    private static final int COLLAPSED_RIGHT_MARGIN = 32;
    private static final RGB LABEL_BACKGROUND = new RGB(255, 255, 192);
    private static final String TOOLTIP_KEY = "toolTip"; //$NON-NLS-1$

    /**
     * Interface for header bar call-backs.
     */
    public interface IEventsTableHeaderListener {
        /**
         * A filter has been selected.
         *
         * @param filter
         *            the selected filter
         */
        void filterSelected(ITmfFilter filter);

        /**
         * A filter has been removed.
         *
         * @param filter
         *            the removed filter
         */
        void filterRemoved(ITmfFilter filter);
    }

    private final IEventsTableHeaderListener fListener;
    private final RowLayout fLayout;
    private final Color fLabelBackground;
    private boolean fCollapsed = false;

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the style of widget to construct
     * @param listener
     *            the listener to the header bar events
     */
    public TmfEventsTableHeader(Composite parent, int style, IEventsTableHeaderListener listener) {
        super(parent, style);
        fListener = listener;
        fLayout = new RowLayout();
        fLayout.marginTop = 0;
        fLayout.marginBottom = 0;
        fLayout.marginLeft = EXPANDED.getBounds().width;
        setLayout(fLayout);
        fLabelBackground = new Color(getDisplay(), LABEL_BACKGROUND);
        getParent().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                getParent().layout();
            }
        });
        addPaintListener(e -> {
            if (fCollapsed) {
                e.gc.drawImage(COLLAPSED, 0, 0);
            } else {
                e.gc.drawImage(EXPANDED, 0, 0);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                toggle();
            }
        });
        addDisposeListener((e) -> {
            fLabelBackground.dispose();
        });
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int height = fCollapsed && getChildren().length > 0 ? EXPANDED.getBounds().height : hHint;
        return super.computeSize(getParent().getSize().x, height, changed);
    }

    /**
     * Add a filter to the header.
     *
     * @param filter
     *            the filter to add
     */
    public void addFilter(ITmfFilter filter) {
        if (filter instanceof TmfFilterRootNode) {
            TmfFilterRootNode parentFilter = (TmfFilterRootNode) filter;
            for (ITmfFilterTreeNode childFilter : parentFilter.getChildren()) {
                addNewFilter(childFilter);
            }
        } else {
            addNewFilter(filter);
        }
        fLayout.marginTop = 1;
        fLayout.marginBottom = 1;
        getParent().layout(true, true);
    }

    /**
     * Remove a filter from the header.
     *
     * @param filter
     *            the filter to remove
     */
    public void removeFilter(ITmfFilter filter) {
        for (Control control : getChildren()) {
            if (filter.equals(control.getData())) {
                control.dispose();
                break;
            }
        }
        if (getChildren().length == 0) {
            fLayout.marginTop = 0;
            fLayout.marginBottom = 0;
        }
        getParent().layout(true, true);
    }

    /**
     * Clear all filters in the header.
     */
    public void clearFilters() {
        for (Control control : getChildren()) {
            control.dispose();
        }
        fLayout.marginTop = 0;
        fLayout.marginBottom = 0;
        getParent().layout(true, true);
    }

    private void addNewFilter(ITmfFilter filter) {
        CLabel label = new CLabel(this, SWT.SHADOW_OUT);
        label.setBackground(fLabelBackground);
        String text;
        if (filter instanceof TmfFilterNode) {
            text = ((TmfFilterNode) filter).getFilterName();
            label.setData(TOOLTIP_KEY, filter.toString());
        } else {
            text = filter.toString();
        }
        if (fCollapsed) {
            label.setToolTipText(text);
            label.setTopMargin(0);
            label.setBottomMargin(0);
            label.setRightMargin(COLLAPSED_RIGHT_MARGIN);
        } else {
            label.setImage(DELETE);
            label.setText(text);
            label.setToolTipText((String) label.getData(TOOLTIP_KEY));
        }
        label.setData(filter);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                Rectangle bounds;
                if (fCollapsed) {
                    bounds = new Rectangle(0, 0, 2 * COLLAPSED_IMAGE_MARGIN + DELETE_SMALL.getBounds().width, label.getBounds().height);
                } else {
                    bounds = DELETE.getBounds();
                    bounds.x += label.getLeftMargin();
                    bounds.y = (label.getSize().y - bounds.height) / 2;
                }
                if (bounds.contains(e.x, e.y)) {
                    fListener.filterRemoved((ITmfFilter) label.getData());
                } else {
                    fListener.filterSelected((ITmfFilter) label.getData());
                    getParent().layout(true, true);
                }
            }
        });
        label.addPaintListener(e -> {
            if (fCollapsed) {
                e.gc.drawImage(DELETE_SMALL, COLLAPSED_IMAGE_MARGIN, COLLAPSED_IMAGE_MARGIN);
            }
        });
    }

    private void toggle() {
        fCollapsed = !fCollapsed;
        for (Control child : getChildren()) {
            if (child instanceof CLabel) {
                CLabel label = (CLabel) child;
                if (fCollapsed) {
                    label.setImage(null);
                    label.setToolTipText(label.getText());
                    label.setText(null);
                    label.setMargins(DEFAULT_MARGIN, 0, COLLAPSED_RIGHT_MARGIN, 0);
                } else {
                    label.setImage(DELETE);
                    label.setText(label.getToolTipText());
                    label.setToolTipText((String) label.getData(TOOLTIP_KEY));
                    label.setMargins(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN);
                }
            }
        }
        getParent().layout();
    }
}
