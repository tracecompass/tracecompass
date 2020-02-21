/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;

/**
 * The dialog for user-defined filters.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class FilterDialog extends Dialog {

    TmfFilterNode fRoot;
    FilterViewer fViewer;

    /**
     * Constructor.
     *
     * @param shell
     *            The shell to which this dialog is attached
     */
    public FilterDialog(Shell shell) {
        super(shell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.FilterDialog_FilterDialogTitle);
        getShell().setMinimumSize(getShell().computeSize(550, 250));
        Composite composite = (Composite) super.createDialogArea(parent);

        fViewer = new FilterViewer(composite, SWT.BORDER, true);
        fViewer.setInput(fRoot);
        return composite;
    }

    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter(ITmfFilterTreeNode filter) {
        fRoot = new TmfFilterNode(null);
        if (filter != null) {
            fRoot.addChild(filter.clone());
        }
        if (fViewer != null) {
            fViewer.setInput(fRoot);
        }
    }

    /**
     * @return the filter
     */
    public ITmfFilterTreeNode getFilter() {
        if (fRoot != null && fRoot.hasChildren()) {
            return fRoot.getChild(0).clone();
        }
        return null;
    }

}
