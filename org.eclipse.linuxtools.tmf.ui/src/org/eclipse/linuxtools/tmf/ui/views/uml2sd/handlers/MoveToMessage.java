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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessageReturn;

/**
 * Action Class implementation to move to selected message
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class MoveToMessage extends Action {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public final static String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.GoToMessage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The sequence diagram view reference.
     */
    protected SDView fView = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default Constructor
     */
    public MoveToMessage() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view a sequence diagram view reference
     */
    public MoveToMessage(SDView view) {
        super();
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH_MATCH));
        fView = view;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
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
        Iterator<Object> it = ((StructuredSelection) sel).iterator();
        while (it.hasNext()) {
            Object node = it.next();
            if (node instanceof BaseMessage) {
                selectedNode = node;
            }
        }

        if (selectedNode == null) {
            return;
        }

        if (selectedNode instanceof SyncMessageReturn) {
            GraphNode node = ((SyncMessageReturn) selectedNode).getMessage();
            sdWidget.clearSelection();
            sdWidget.addSelection(node);
            sdWidget.ensureVisible(node);
            // sdWidget.setFocusNode(node);
            sdWidget.redraw();
        } else if (selectedNode instanceof SyncMessage) {
            GraphNode node = ((SyncMessage) selectedNode).getMessageReturn();
            sdWidget.clearSelection();
            sdWidget.addSelection(node);
            sdWidget.ensureVisible(node);
            // sdWidget.setFocusNode(node);
            sdWidget.redraw();
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
