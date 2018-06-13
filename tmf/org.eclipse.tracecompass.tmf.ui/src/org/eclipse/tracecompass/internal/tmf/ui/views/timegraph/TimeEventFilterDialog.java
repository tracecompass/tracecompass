/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.views.timegraph;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This class implements a time event filter dialog.
 *
 * @author Jean-Christian Kouame
 */
public class TimeEventFilterDialog extends Dialog {

    /**
     * The empty string
     */
    public static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final int FIND_X_WIDTH_HINT = 200;

    private static final int MAX_FILTER_REGEX_SIZE = 4;

    private static final int Y_OFFSET = -15;

    /** The time event filter regex */
    private String fRegex;

    /** The time event filter regex */
    private Set<@NonNull String> fFilterRegexes = new LinkedHashSet<>(MAX_FILTER_REGEX_SIZE);

    private final TimeGraphControl fControl;

    private final AbstractTimeGraphView fView;

    private final ControlListener fControlListener = new ControlMovedListener();

    /**
     * Constructor
     *
     * @param parentShell
     *            The parent shell of the dialog
     * @param view
     *            The timegraph this dialog belongs to
     * @param control The timegraph control
     */
    public TimeEventFilterDialog(Shell parentShell, AbstractTimeGraphView view, TimeGraphControl control) {
        super(parentShell);
        fView = view;
        fControl = control;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.AbstractTimeGraphView_TimeEventFilterDialogTitle);

        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.horizontalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);

        Composite labels = createCLabelsArea(container);
        createFilterTextArea(parent, container,  labels);
        createCloseButton(container);

        // support close on escape button
        getShell().addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_ESCAPE) {
                clearFilter();
            }
        });

        for (String label : fFilterRegexes) {
            createCLabels(parent, labels, label);
        }

        fControl.addControlListener(fControlListener);
        fControl.getShell().addControlListener(fControlListener);

        return parent;
    }

    private static Composite createCLabelsArea(Composite container) {
        Composite labels = new Composite(container, SWT.NONE);
        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        labels.setLayoutData(gd);
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        rl.marginTop = 0;
        rl.marginBottom = 0;
        rl.marginLeft = 0;
        rl.marginRight = 0;
        labels.setLayout(rl);
        return labels;
    }

    private Text createFilterTextArea(Composite parent, Composite container, Composite labels) {
        Text filterText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = FIND_X_WIDTH_HINT;
        filterText.setLayoutData(gridData);
        if (fRegex != null) {
            filterText.setText(fRegex);
        }
        Color background = filterText.getBackground();
        filterText.addModifyListener(e -> {
            fRegex = filterText.getText();
            filterText.setBackground(background);
            fView.restartZoomThread();
        });

        filterText.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                // Do nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.CR) {
                    handleEnterPressed(parent, labels, filterText);
                    fView.restartZoomThread();
                }
            }
        });
        return filterText;
    }

    private void clearFilter() {
        fRegex = EMPTY_STRING;
        fFilterRegexes.clear();
        fView.restartZoomThread();
    }

    private void handleEnterPressed(Composite parent, Composite labels, Text filterText) {
        String currentRegex = filterText.getText();
        if (currentRegex.isEmpty() || fFilterRegexes.size() == MAX_FILTER_REGEX_SIZE || fFilterRegexes.contains(currentRegex)) {
            filterText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
            return;
        }
        boolean added = fFilterRegexes.add(currentRegex);
        if (added) {
            filterText.setText(EMPTY_STRING);
            fRegex = EMPTY_STRING;

            createCLabels(parent, labels, currentRegex);
        }
    }

    private Button createCloseButton(Composite composite) {
        Button closeButton = new Button(composite, SWT.NONE);
        closeButton.setToolTipText(Messages.TimeEventFilterDialog_CloseButton);
        closeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        closeButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
        closeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                clearFilter();
                close();
            }
        });
        return closeButton;
    }

    private void createCLabels(Composite parent, Composite labels, String currentRegex) {
        CLabel filter = new CLabel(labels, SWT.NONE);
        filter.setText(currentRegex);
        filter.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
        filter.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
        filter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e2) {
                deleteCLabel(parent, filter, e2);
            }
        });
        parent.layout();
        Rectangle bounds = parent.getShell().getBounds();
        Point size = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Rectangle trim = parent.getShell().computeTrim(0, 0, size.x, size.y);
        parent.getShell().setBounds(bounds.x + bounds.width - trim.width, bounds.y + bounds.height - trim.height, trim.width, trim.height);
    }

    private void deleteCLabel(Composite parent, CLabel cLabel, MouseEvent e) {
        Rectangle imageBounds;
        imageBounds = cLabel.getImage().getBounds();
        imageBounds.x += cLabel.getLeftMargin();
        imageBounds.y = (cLabel.getSize().y - imageBounds.height) / 2;
        if (imageBounds.contains(e.x, e.y)) {
            fFilterRegexes.removeIf(regex -> regex.equals(cLabel.getText()));
            e.widget.dispose();
            parent.layout();
            Rectangle bounds = parent.getShell().getBounds();
            Point size = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            Rectangle trim = parent.getShell().computeTrim(0, 0, size.x, size.y);
            int x = bounds.x + (bounds.width - trim.width);
            parent.getShell().setSize(trim.width, trim.height);
            Display.getDefault().asyncExec(() -> {
                parent.getShell().setLocation(x, bounds.y);
            });
            fView.restartZoomThread();
        }
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.NO_TRIM);
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.marginHeight = 0;
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        return getFilterLocation();
    }

    @Override
    public boolean close() {
        if (!fControl.isDisposed()) {
            fControl.removeControlListener(fControlListener);
            fControl.getShell().removeControlListener(fControlListener);
        }
        return super.close();
    }

    /**
     * Get the time event filter dialog location
     *
     * @return the filter dialog location
     */
    public Point getFilterLocation() {
        Rectangle bounds = fControl.getBounds();
        int width = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        return fControl.toDisplay(new Point(bounds.x + bounds.width - width, bounds.y + bounds.height + Y_OFFSET));
    }

    /**
     * Tells whether the dialog has active filters
     *
     * @return true if there is active filter, false otherwise
     */
    public boolean isFilterActive() {
        return !NonNullUtils.nullToEmptyString(fRegex).isEmpty() || hasActiveSavedFilters();
    }

    /**
     * Tells whether the dialog has active saved filters
     *
     * @return true if there is active saved filters, false otherwise
     */
    public boolean hasActiveSavedFilters() {
        return !fFilterRegexes.isEmpty();
    }

    /**
     * Get the dialog text box regex
     *
     * @return The current text box regex
     */
    public @NonNull String getTextBoxRegex() {
        return NonNullUtils.nullToEmptyString(fRegex);
    }

    /**
     * Get the set of saved filters
     *
     * @return the set of saved filters
     */
    public Set<@NonNull String> getSavedFilters() {
        return fFilterRegexes;
    }

    /**
     * Listener that handles move and resize events of the chart.
     */
    private class ControlMovedListener implements ControlListener {

        @Override
        public void controlMoved(@Nullable ControlEvent e) {
            resize();
        }

        @Override
        public void controlResized(@Nullable ControlEvent e) {
            resize();
        }

        private void resize() {
            if (getShell() == null) {
                return;
            }
            Point size = getInitialSize();
            Point location = getFilterLocation();
            getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x,
                    location.y, size.x, size.y)));
        }
    }

}
