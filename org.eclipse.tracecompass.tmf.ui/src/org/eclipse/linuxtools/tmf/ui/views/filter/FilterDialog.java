/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.filter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

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
        getShell().setMinimumSize(getShell().computeSize(500, 200));
        Composite composite = (Composite) super.createDialogArea(parent);

        fViewer = new FilterViewer(composite, SWT.BORDER);
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
