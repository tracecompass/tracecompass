/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TimeGraphLegend;

/**
 * Critical Path Legend
 *
 * @author Matthew Khouzam
 *
 */
public class CriticalPathLegend extends TimeGraphLegend {

    /**
     * Constructor
     *
     * @param parent
     *            the shell to draw on
     * @param provider
     *            the provider containing the states
     */
    public CriticalPathLegend(@Nullable Shell parent, ITimeGraphPresentationProvider provider) {
        super(parent, provider);
    }

    @Override
    protected void createStatesGroup(@Nullable Composite composite) {
        if (composite == null) {
            return;
        }
        StateItem[] stateItems = getPresentationProvider().getStateTable();

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);

        Composite innerComposite = new Composite(sc, SWT.NONE);
        sc.setLayout(GridLayoutFactory.swtDefaults().margins(20, 0).create());
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.setLayoutData(gd);
        innerComposite.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0).create());
        innerComposite.setLayoutData(gd);

        /*
         * Create a running group
         */
        Group running = new Group(innerComposite, SWT.NONE);
        running.setLayout(GridLayoutFactory.swtDefaults().create());
        running.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create());
        running.setText(Messages.CriticalPathLegend_running);

        /*
         * Add the running swatch
         */
        new LegendEntry(running, stateItems[0]);

        /*
         * Create a blocked group
         */
        Group blocked = new Group(innerComposite, SWT.NONE);
        blocked.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        blocked.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).create());
        blocked.setText(Messages.CriticalPathLegend_blocked);

        boolean isLeft = true;
        for (int i = 1; i <= stateItems.length / 2; i++) {
            // put the two columns
            if (!isLinkState(stateItems[i])) {
                LegendEntry lefty = new LegendEntry(blocked, stateItems[i]);
                setEntryLayout(lefty, isLeft);
                isLeft = !isLeft;
            }

            if (i + stateItems.length / 2 < stateItems.length && !isLinkState(stateItems[i + stateItems.length / 2])) {
                LegendEntry righty = new LegendEntry(blocked, stateItems[i + stateItems.length / 2]);
                setEntryLayout(righty, isLeft);
                isLeft = !isLeft;
            }
        }

        sc.setContent(innerComposite);
        sc.setMinSize(innerComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private static void setEntryLayout(LegendEntry entry, boolean leftColumn) {
        if (leftColumn) {
            entry.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).create());
        } else {
            entry.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).create());
        }
    }
}
