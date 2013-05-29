/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;

/**
 * Action class implementation for zooming in, out or reset of zoom.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class Zoom extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The Action ID for zooming in.
     */
    public static final String ZOOM_IN_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ZoomInCoolBar"; //$NON-NLS-1$
    /**
     * The Action ID for zooming out.
     */
    public static final String ZOOM_OUT_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ZoomOutCoolBar"; //$NON-NLS-1$
    /**
     * The Action ID for reset zooming.
     */
    public static final String RESET_ZOOM_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ResetZoom"; //$NON-NLS-1$
    /**
     * The Action ID for no zoominf.
     */
    public static final String NO_ZOOM_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.NoZoom"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Flag to indicate last zoom in.
     */
    private boolean fLastZoomIn = false;
    /**
     * Flag to indicate last zoom out.
     */
    private boolean fLastZoomOut = false;
    /**
     * The cursor used when zooming in.
     */
    private final Cursor fZoomInCursor;
    /**
     * The cursor used when zooming out.
     */
    private final Cursor fZoomOutCursor;

    /**
     * The different zoom actions
     */
    public static enum ZoomType {
        /** No zoom information */
        ZOOM_NONE,
        /** Zoom in */
        ZOOM_IN,
        /** Zoom out */
        ZOOM_OUT,
        /** Reset to the default zoom level */
        ZOOM_RESET
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param view The view reference
     * @param type The type of zoom.
     */
    public Zoom(SDView view, ZoomType type) {
        super(view, "", AS_RADIO_BUTTON); //$NON-NLS-1$

        // Pre-create zooming cursors
        fZoomInCursor = new Cursor(Display.getCurrent(),
                Activator.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM_IN).getImageData(),
                Activator.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM).getImageData(), 0, 0);

        fZoomOutCursor = new Cursor(Display.getCurrent(),
                Activator.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM_OUT).getImageData(),
                Activator.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM).getImageData(), 0, 0);

        switch (type) {
        case ZOOM_IN:
            setText(Messages.SequenceDiagram_ZoomIn);
            setToolTipText(Messages.SequenceDiagram_ZoomInTheDiagram);
            setId(ZOOM_IN_ID);
            setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN_MENU));
            break;

        case ZOOM_OUT:
            setText(Messages.SequenceDiagram_ZoomOut);
            setToolTipText(Messages.SequenceDiagram_ZoomOutTheDiagram);
            setId(ZOOM_OUT_ID);
            setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT_MENU));
            break;

        case ZOOM_RESET:
            setText(Messages.SequenceDiagram_ResetZoomFactor);
            setToolTipText(Messages.SequenceDiagram_ResetZoomFactor);
            setId(RESET_ZOOM_ID);
            setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HOME_MENU));
            break;

        case ZOOM_NONE:
        default:
            setText(Messages.SequenceDiagram_Select);
            setToolTipText(Messages.SequenceDiagram_Select);
            setId(NO_ZOOM_ID);
            setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SELECT_MENU));
            break;
        }
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {

        if ((getView() == null) || (getView().getSDWidget() == null)) {
            return;
        }

        SDWidget viewer = getView().getSDWidget();

        if (getId().equals(ZOOM_OUT_ID)) {
            // Eclipse 3.0 M7 workaround
            if (fLastZoomOut == isChecked()) {
                setChecked(!isChecked());
            }

            viewer.setZoomOutMode(isChecked());
            fLastZoomOut = isChecked();
            if (isChecked()) {
                viewer.setCursor(fZoomOutCursor);
                setActionChecked(NO_ZOOM_ID, false);
            } else {
                viewer.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
                setActionChecked(NO_ZOOM_ID, true);
            }
        } else if (getId().equals(ZOOM_IN_ID)) {
            // Eclipse 3.0 M7 workaround
            if (fLastZoomIn == isChecked()) {
                setChecked(!isChecked());
            }

            viewer.setZoomInMode(isChecked());
            fLastZoomIn = isChecked();
            if (isChecked()) {
                viewer.setCursor(fZoomInCursor);
                setActionChecked(NO_ZOOM_ID, false);
            } else {
                viewer.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
                setActionChecked(NO_ZOOM_ID, true);
            }
        } else if (getId().equals(RESET_ZOOM_ID)) {
            viewer.resetZoomFactor();

            // The reset action is a radio button only to uncheck the zoom in and out button
            // when it is clicked. This avoid adding code to do it manually
            // We only have to force it to false every time
            setChecked(false);
            setActionChecked(NO_ZOOM_ID, true);
        } else if (getId().equals(NO_ZOOM_ID)) {
            setChecked(true);
            viewer.setZoomInMode(false);
            viewer.setZoomInMode(false);
            viewer.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
        }
    }

    /**
     * Set action check state of a view action for a given action ID.
     *
     * @param id The action ID
     * @param checked true to check the action, false to uncheck the action
     */
    protected void setActionChecked(String id, boolean checked) {
        if (getView() != null) {
            IActionBars bar = getView().getViewSite().getActionBars();
            if (bar == null) {
                return;
            }
            IToolBarManager barManager = bar.getToolBarManager();
            if (barManager == null) {
                return;
            }
            IContributionItem nextPage = barManager.find(id);
            if (nextPage instanceof ActionContributionItem) {
                IAction action = ((ActionContributionItem) nextPage).getAction();
                if (action != null) {
                    action.setChecked(checked);
                }
            }
        }
    }
}
