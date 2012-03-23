/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ShowNodeStart.java,v 1.3 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.ui.IViewPart;

/**
 * @author sveyrier
 */
public class ShowNodeStart extends Action {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public ShowNodeStart() {
        this(null);
    }
    
    public ShowNodeStart(IViewPart _view) {
        super();
        if (_view instanceof SDView) {
            fView = (SDView)_view;
        }
        setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NODE_START));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
   @Override
    @SuppressWarnings("rawtypes")
    public void run() {
        if (fView == null)
            return;
        SDWidget sdWidget = fView.getSDWidget();

        if (sdWidget == null) {
            return;
        }
        
        ISelectionProvider selProvider = sdWidget.getSelectionProvider();
        ISelection sel = selProvider.getSelection();
        Object selectedNode = null;
        Iterator it = ((StructuredSelection) sel).iterator();
        while (it.hasNext())
            selectedNode = it.next();
        if (selectedNode != null) {
            GraphNode node = (GraphNode) selectedNode;
            if (node.getX() * sdWidget.getZoomFactor() < sdWidget.getContentsX() + sdWidget.getVisibleWidth() / 2)
                sdWidget.ensureVisible(Math.round(node.getX() * sdWidget.getZoomFactor() - sdWidget.getVisibleWidth() / (float) 2), Math.round(node.getY() * sdWidget.getZoomFactor()));
            else
                sdWidget.ensureVisible(Math.round(node.getX() * sdWidget.getZoomFactor() + sdWidget.getVisibleWidth() / (float) 2), Math.round(node.getY() * sdWidget.getZoomFactor()));
        }
    }

   public void setView(SDView view) {
       fView = view;
   }
}
