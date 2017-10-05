/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson and others
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

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.views.TimeAlignViewsAction;
import org.eclipse.tracecompass.internal.tmf.ui.views.TmfAlignmentSynchronizer;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Basic abstract TMF view class implementation.
 * <br>
 * It registers any sub class to the signal manager for receiving and sending
 * TMF signals.
 * <br>
 * Subclasses may optionally implement the {@link ITmfTimeAligned},
 * {@link ITmfAllowMultiple} and {@link ITmfPinnable} interfaces to enable
 * those features.
 *
 * @author Francois Chouinard
 */
public abstract class TmfView extends ViewPart implements ITmfComponent {

    private static final TmfAlignmentSynchronizer TIME_ALIGNMENT_SYNCHRONIZER = TmfAlignmentSynchronizer.getInstance();
    private final String fName;
    /** This allows us to keep track of the view sizes */
    private Composite fParentComposite;
    private ControlAdapter fControlListener;

    /**
     * Action class for pinning of TmfView.
     */
    protected PinTmfViewAction fPinAction;

    private static TimeAlignViewsAction fAlignViewsAction;

    /**
     * The separator used between the primary and secondary id of a view id.
     *
     * @since 3.2
     */
    public static final String VIEW_ID_SEPARATOR = ":"; //$NON-NLS-1$

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
     * Returns whether the view is pinned.
     *
     * @return if the view is pinned
     */
    public boolean isPinned() {
        return ((fPinAction != null) && (fPinAction.isPinned()));
    }

    /**
     * Method adds a pin action to the TmfView. For example, this action can be
     * used to ignore time synchronization signals from other TmfViews. <br>
     *
     * Uses {@link ITmfPinnable#setPinned(ITmfTrace)} to propagate the state of
     * the action button.
     */
    protected void contributePinActionToToolBar() {
        if (fPinAction == null && this instanceof ITmfPinnable) {
            fPinAction = new PinTmfViewAction((ITmfPinnable) this);

            IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
            toolBarManager.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
            toolBarManager.add(fPinAction);
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        fParentComposite = parent;

        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        if (this instanceof ITmfAllowMultiple) {
            contributeNewViewActionToLocalMenu(menuManager);
        }

        if (this instanceof ITmfTimeAligned) {
            contributeAlignViewsActionToLocalMenu(menuManager);

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

        if (!menuManager.isEmpty()) {
            menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        }

        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        /* Subclass tool bar contributions should be added to this group */

        if (this instanceof ITmfPinnable) {
            contributePinActionToToolBar();
        }
    }

    /**
     * Add the "New view" action to the view menu. This action spawns a new view
     * of the same type as the caller.
     */
    private void contributeNewViewActionToLocalMenu(IMenuManager menuManager) {
        if (!menuManager.isEmpty()) {
            menuManager.add(new Separator());
        }
        if (this instanceof ITmfPinnable) {
            MenuManager newViewMenu = new MenuManager(MessageFormat.format(Messages.TmfView_NewViewActionText, this.getTitle()));
            newViewMenu.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEW_VIEW));
            newViewMenu.setRemoveAllWhenShown(true);
            newViewMenu.addMenuListener(new IMenuListener2() {
                @Override
                public void menuAboutToShow(IMenuManager mgr) {
                    Set<@NonNull ITmfTrace> openedTraces = TmfTraceManager.getInstance().getOpenedTraces();
                    ITmfTrace activeTrace = ((ITmfPinnable) TmfView.this).getTrace();
                    if (activeTrace != null) {
                        mgr.add(new NewTmfViewAction(TmfView.this, activeTrace, true));
                        mgr.add(new Separator());
                    }
                    for (ITmfTrace trace : openedTraces) {
                        mgr.add(new NewTmfViewAction(TmfView.this, trace));
                    }
                    mgr.add(new Separator());
                    mgr.add(new NewTmfViewAction(TmfView.this, null));
                }

                @Override
                public void menuAboutToHide(IMenuManager manager) {
                    /* Clear menu to release action references to trace instances */
                    manager.removeAll();
                }
            });
            menuManager.add(newViewMenu);
        } else {
            NewTmfViewAction action = new NewTmfViewAction(TmfView.this);
            menuManager.add(action);
        }
    }

    private static void contributeAlignViewsActionToLocalMenu(IMenuManager menuManager) {
        if (fAlignViewsAction == null) {
            fAlignViewsAction = new TimeAlignViewsAction();
        }

        if (!menuManager.isEmpty()) {
            menuManager.add(new Separator());
        }
        menuManager.add(fAlignViewsAction);
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
     * Return the Eclipse view ID in the format 'Primary ID':'Secondary ID' or
     * simply 'Primary ID' if secondary ID is null
     *
     * @return This view's view ID
     * @since 2.2
     */
    protected @NonNull String getViewId() {
        IViewSite viewSite = getViewSite();
        String secondaryId = viewSite.getSecondaryId();
        if (secondaryId == null) {
            return String.valueOf(viewSite.getId());
        }
        return viewSite.getId() + VIEW_ID_SEPARATOR + secondaryId;
    }
}
