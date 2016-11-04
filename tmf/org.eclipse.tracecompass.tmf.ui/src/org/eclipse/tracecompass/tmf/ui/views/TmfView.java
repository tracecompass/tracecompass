/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added possibility to pin view
 *   Marc-Andre Laperle - Support for view alignment
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.ui.views.TimeAlignViewsAction;
import org.eclipse.tracecompass.internal.tmf.ui.views.TmfAlignmentSynchronizer;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
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
    /** This allows us to keep track of the view sizes */
    private Composite fParentComposite;
    private ControlAdapter fControlListener;
    private static final TmfAlignmentSynchronizer TIME_ALIGNMENT_SYNCHRONIZER = new TmfAlignmentSynchronizer();

    /**
     * Action class for pinning of TmfView.
     */
    protected PinTmfViewAction fPinAction;
    private static TimeAlignViewsAction fAlignViewsAction;

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

        /* Workaround for Bug 490400: Clear the action bars */
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().removeAll();
        bars.getMenuManager().removeAll();

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

    @Override
    public void createPartControl(final Composite parent) {
        fParentComposite = parent;
        if (this instanceof ITmfTimeAligned) {
            contributeAlignViewsActionToToolbar();

            fControlListener = new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    /*
                     * When switching perspective, the view can be resized just
                     * before it is made visible. Queue the time alignment to
                     * ensure it occurs when the parent composite is visible.
                     */
                    e.display.asyncExec(() -> {
                        TIME_ALIGNMENT_SYNCHRONIZER.handleViewResized(TmfView.this);
                    });
                }
            };
            parent.addControlListener(fControlListener);

            getSite().getPage().addPartListener(new IPartListener() {
                @Override
                public void partOpened(IWorkbenchPart part) {
                    // do nothing
                }

                @Override
                public void partDeactivated(IWorkbenchPart part) {
                    // do nothing
                }

                @Override
                public void partClosed(IWorkbenchPart part) {
                    if (part == TmfView.this && fControlListener != null && !fParentComposite.isDisposed()) {
                        fParentComposite.removeControlListener(fControlListener);
                        fControlListener = null;
                        getSite().getPage().removePartListener(this);
                        TIME_ALIGNMENT_SYNCHRONIZER.handleViewClosed(TmfView.this);
                    }
                }

                @Override
                public void partBroughtToTop(IWorkbenchPart part) {
                    // do nothing
                }

                @Override
                public void partActivated(IWorkbenchPart part) {
                    // do nothing
                }
            });
        }
    }

    private void contributeAlignViewsActionToToolbar() {
        if (fAlignViewsAction == null) {
            fAlignViewsAction = new TimeAlignViewsAction();
        }

        IToolBarManager toolBarManager = getViewSite().getActionBars()
                .getToolBarManager();
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBarManager.add(fAlignViewsAction);
    }

    /**
     * Returns the parent control of the view
     *
     * @return the parent control
     *
     * @since 1.0
     */
    public Composite getParentComposite() {
        return fParentComposite;
    }

    /**
     * Return the Eclipse view ID in the format <Primary ID>:<Secondary ID> or
     * simply <Primary ID> if secondary ID is null
     *
     * @return This view's view ID
     * @since 2.2
     */
    protected String getViewId() {
        IViewSite viewSite = getViewSite();
        String secondaryId = viewSite.getSecondaryId();
        if (secondaryId == null) {
            return viewSite.getId();
        }
        return viewSite.getId() + ':' + secondaryId;
    }
}
