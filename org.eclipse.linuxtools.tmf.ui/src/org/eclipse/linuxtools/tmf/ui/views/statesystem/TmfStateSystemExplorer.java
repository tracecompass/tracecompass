/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Alexandre Montplaisir - Refactoring, performance tweaks
 *   Bernd Hufmann - Updated signal handling
 *   Marc-Andre Laperle - Add time zone preference
 *   Geneviève Bastien - Use a tree viewer instead of a tree
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statesystem;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

/**
 * Displays the State System at a current time.
 *
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfStateSystemExplorer extends TmfView {

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.ssview"; //$NON-NLS-1$

    private static final Image FILTER_IMAGE =
            Activator.getDefault().getImageFromPath( File.separator + "icons" +  File.separator + "elcl16" +  File.separator + "filter_items.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private TmfStateSystemViewer fViewer;

    /**
     * Default constructor
     */
    public TmfStateSystemExplorer() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {

        fViewer = new TmfStateSystemViewer(parent);

        fillToolBar() ;

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            fViewer.traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

    }

    // ------------------------------------------------------------------------
    // Part For Button Action
    // ------------------------------------------------------------------------

    private void fillToolBar() {
        Action fFilterAction = new FilterAction();
        fFilterAction.setImageDescriptor(ImageDescriptor.createFromImage(FILTER_IMAGE));
        fFilterAction.setToolTipText(Messages.FilterButton) ;
        fFilterAction.setChecked(false);

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(fFilterAction);
    }

    private class FilterAction extends Action {
        @Override
        public void run() {
            fViewer.changeFilterStatus();
        }
    }

    @Override
    public void setFocus() {
    }
}
