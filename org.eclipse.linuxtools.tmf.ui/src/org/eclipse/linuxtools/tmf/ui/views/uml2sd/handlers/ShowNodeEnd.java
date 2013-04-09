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

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.ui.IViewPart;

/**
 * Action class implementation to show end of a graph node.
 *
 * @version 1.0
 * @author sveyrier
 */
public class ShowNodeEnd extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram view reference
     */
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public ShowNodeEnd() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view The sequence diagram view reference
     */
    public ShowNodeEnd(IViewPart view) {
        super();
        if (view instanceof SDView) {
            fView = (SDView)view;
        }
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NODE_END));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("rawtypes")
    public void run() {
        if (fView == null) {
            return;
        }

        SDWidget sdWidget = fView.getSDWidget();

        if (sdWidget == null) {
            return;
        }

        ISelectionProvider selProvider = sdWidget.getSelectionProvider();
        ISelection sel = selProvider.getSelection();
        Object selectedNode = null;

        Iterator it = ((StructuredSelection) sel).iterator();
        while (it.hasNext()) {
            selectedNode = it.next();
        }

        if (selectedNode != null) {
            GraphNode node = (GraphNode) selectedNode;
            if ((node.getX() + node.getWidth()) * sdWidget.getZoomFactor() < sdWidget.getContentsX() + sdWidget.getVisibleWidth() / 2) {
                sdWidget.ensureVisible(Math.round((node.getX() + node.getWidth()) * sdWidget.getZoomFactor()) - sdWidget.getVisibleWidth() / 2, Math.round((node.getY() + node.getHeight()) * sdWidget.getZoomFactor()));
            } else {
                sdWidget.ensureVisible(Math.round((node.getX() + node.getWidth()) * sdWidget.getZoomFactor() + sdWidget.getVisibleWidth() / (float) 2), Math.round((node.getY() + node.getHeight()) * sdWidget.getZoomFactor()));
            }
        }
    }

    /**
     * Sets the active SD view.
     *
     * @param view The SD view.
     */
   public void setView(SDView view) {
        fView = view;
    }
}
