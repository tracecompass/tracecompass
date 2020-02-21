/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A uneditable version of a {@link TextPropertyDescriptor}.
 *
 * @author Alexandre Montplaisir
 */
public class ReadOnlyTextPropertyDescriptor extends PropertyDescriptor {

    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            The id of the property
     * @param displayName
     *            The name to display for the property
     */
    public ReadOnlyTextPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new TextCellEditor(parent, SWT.READ_ONLY);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

}
