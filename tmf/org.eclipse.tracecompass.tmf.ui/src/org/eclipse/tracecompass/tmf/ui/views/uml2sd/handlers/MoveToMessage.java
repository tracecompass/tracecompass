/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDView;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.SyncMessageReturn;

/**
 * Action Class implementation to move to selected message
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class MoveToMessage extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.GoToMessage"; //$NON-NLS-1$

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
        super(view);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH_MATCH));
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }

        SDWidget sdWidget = getView().getSDWidget();

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
            sdWidget.redraw();
        } else if (selectedNode instanceof SyncMessage) {
            GraphNode node = ((SyncMessage) selectedNode).getMessageReturn();
            sdWidget.clearSelection();
            sdWidget.addSelection(node);
            sdWidget.ensureVisible(node);
            sdWidget.redraw();
        }
    }
}
