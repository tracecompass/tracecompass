/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson.
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

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Legend for the colors used in the time graph view
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphLegend extends TitleAreaDialog {

    private final ITimeGraphPresentationProvider provider;
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
        this.provider = provider;
        this.setShellStyle(getShellStyle() | SWT.RESIZE);
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

        return composite;
    }

    private void createStatesGroup(Composite composite) {
        ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL|SWT.H_SCROLL);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        Group gs = new Group(sc, SWT.H_SCROLL);
        sc.setContent(gs);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        sc.setLayoutData(gd);

        String stateTypeName = provider.getStateTypeName();
        StringBuffer buffer = new StringBuffer();
        if (!stateTypeName.isEmpty()) {
            buffer.append(stateTypeName);
            buffer.append(" "); //$NON-NLS-1$
        }
        buffer.append(Messages.TmfTimeLegend_StateTypeName);
        gs.setText(buffer.toString());

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 20;
        layout.marginBottom = 10;
        gs.setLayout(layout);

        // Go through all the defined pairs of state color and state name and display them.
        StateItem[] stateItems = provider.getStateTable();
        for (int i = 0; i < stateItems.length; i++) {
            //Get the color related to the index
            RGB rgb = stateItems[i].getStateColor();

            //Get the given name, provided by the interface to the application
            String stateName = stateItems[i].getStateString();

            // draw color with name
            Bar bar = new Bar(gs, rgb);
            gd = new GridData();
            gd.widthHint = 40;
            gd.heightHint = 20;
            gd.verticalIndent = 8;
            bar.setLayoutData(gd);
            Label name = new Label(gs, SWT.NONE);
            name.setText(stateName);
            gd = new GridData();
            gd.horizontalIndent = 10;
            gd.verticalIndent = 8;
            name.setLayoutData(gd);
        }
        sc.setMinSize(gs.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.TmfTimeLegend_TRACE_STATES_TITLE);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    class Bar extends Canvas {
        private final Color color;

        public Bar(Composite parent, RGB rgb) {
            super(parent, SWT.NONE);

            color = fResourceManager.createColor(rgb);
            addListener(SWT.Paint, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    draw(event.gc);
                }
            });
        }

        private void draw(GC gc) {
            Rectangle r = getClientArea();
            gc.setBackground(color);
            gc.fillRectangle(r);
            gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
            gc.drawRectangle(0, 0, r.width - 1, r.height - 1);
        }

        @Override
        public void dispose() {
            super.dispose();
            color.dispose();
        }

    }

}
