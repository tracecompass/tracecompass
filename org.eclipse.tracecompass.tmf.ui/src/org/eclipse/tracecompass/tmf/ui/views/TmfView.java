/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added possibility to pin view
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * Basic abstract TMF view class implementation.
 *
 * It registers any sub class to the signal manager for receiving and sending
 * TMF signals.
 *
 * @author Francois Chouinard
 */
public abstract class TmfView extends ViewPart implements ITmfComponent {

    private final String fName;

    /**
     * Action class for pinning of TmfView.
     */
    protected PinTmfViewAction fPinAction;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor. Creates a TMF view and registers to the signal manager.
     *
     * @param viewName
     *            A view name
     */
    public TmfView(String viewName) {
        super();
        fName = viewName;
        TmfSignalManager.register(this);
    }

    /**
     * Disposes this view and de-registers itself from the signal manager
     */
    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfComponent
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public void broadcast(TmfSignal signal) {
        TmfSignalManager.dispatchSignal(signal);
    }

    @Override
    public void broadcastAsync(TmfSignal signal) {
        TmfSignalManager.dispatchSignalAsync(signal);
    }

    // ------------------------------------------------------------------------
    // View pinning support
    // ------------------------------------------------------------------------

    /**
     * Returns whether the pin flag is set.
     * For example, this flag can be used to ignore time synchronization signals from other TmfViews.
     *
     * @return pin flag
     */
    public boolean isPinned() {
        return ((fPinAction != null) && (fPinAction.isChecked()));
    }

    /**
     * Method adds a pin action to the TmfView. The pin action allows to toggle the <code>fIsPinned</code> flag.
     * For example, this flag can be used to ignore time synchronization signals from other TmfViews.
     */
    protected void contributePinActionToToolBar() {
        if (fPinAction == null) {
            fPinAction = new PinTmfViewAction();

            IToolBarManager toolBarManager = getViewSite().getActionBars()
                    .getToolBarManager();
            toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            toolBarManager.add(fPinAction);
        }
    }
}
