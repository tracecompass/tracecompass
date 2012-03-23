/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Zoom.java,v 1.3 2008/01/24 02:28:52 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;

/**
 * @author sveyrier
 * 
 */
public class Zoom extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    public final static String ZOOM_IN_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ZoomInCoolBar"; //$NON-NLS-1$
    public final static String ZOOM_OUT_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ZoomOutCoolBar"; //$NON-NLS-1$
    public final static String RESET_ZOOM_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ResetZoom"; //$NON-NLS-1$
    public final static String NO_ZOOM_ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.NoZoom"; //$NON-NLS-1$
    
    protected SDView fView = null;
    protected boolean fLastZoomIn = false;
    protected boolean fLastZoomOut = false;
    protected boolean fLastZoom = true;

    private static Cursor fZoomInCursor;
    private static Cursor fZoomOutCursor;

    public static enum ZoomType {
        ZOOM_NONE, ZOOM_IN, ZOOM_OUT, ZOOM_RESET
    };

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public Zoom(SDView _view, ZoomType type) {
        super("", AS_RADIO_BUTTON);//$NON-NLS-1$

        fView = _view;

        if ((fZoomInCursor != null) && (!fZoomInCursor.isDisposed())) {
            fZoomInCursor.dispose();
        }
        if ((fZoomOutCursor != null) && (!fZoomOutCursor.isDisposed())) {
            fZoomOutCursor.dispose();
        }

        // Pre-create zooming cursors
        fZoomInCursor = new Cursor(Display.getCurrent(), 
                TmfUiPlugin.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM_IN).getImageData(), 
                TmfUiPlugin.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM).getImageData(), 0, 0);
        
        fZoomOutCursor = new Cursor(Display.getCurrent(), 
                TmfUiPlugin.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM_OUT).getImageData(),
                TmfUiPlugin.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_ZOOM).getImageData(), 0, 0);

        switch (type) {
        case ZOOM_IN:
            setText(SDMessages._47);
            setToolTipText(SDMessages._48);
            setId(ZOOM_IN_ID);
            setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN_MENU));
            break;

        case ZOOM_OUT:
            setText(SDMessages._51);
            setToolTipText(SDMessages._52);
            setId(ZOOM_OUT_ID);
            setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT_MENU));
            break;

        case ZOOM_RESET:
            setText(SDMessages._49);
            setToolTipText(SDMessages._50);
            setId(RESET_ZOOM_ID);
            setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HOME_MENU));
            break;

        default:
            // ZoomType.NONE
            setText(SDMessages._53);
            setToolTipText(SDMessages._54);
            setId(NO_ZOOM_ID); 
            setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SELECT_MENU));
            break;
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        
        if ((fView == null) || (fView.getSDWidget() == null)) {
            return;
        }

        SDWidget viewer = fView.getSDWidget();

        if (getId().equals(ZOOM_OUT_ID)) {
            // Eclipse 3.0 M7 workaround
            if (fLastZoomOut == isChecked()) {
                setChecked(!isChecked());
            }

            viewer.setZoomOutMode(isChecked());
            fLastZoomOut = isChecked();
            fLastZoom = false;
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
            fLastZoom = false;
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
            fLastZoom = false;
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
     * @param id The action ID
     * @param checked true to check the action, false to uncheck the action
     */
    protected void setActionChecked(String id, boolean checked) {
        if (fView != null) {
            IActionBars bar = fView.getViewSite().getActionBars();
            if (bar == null) {
                return;
            }
            IToolBarManager barManager = bar.getToolBarManager();
            if (barManager == null) {
                return;
            }
            IContributionItem nextPage = barManager.find(id);
            if (nextPage != null && nextPage instanceof ActionContributionItem) {
                IAction action = ((ActionContributionItem) nextPage).getAction();
                if (action != null) {
                    action.setChecked(checked);
                }
            }
        }
    }
}
