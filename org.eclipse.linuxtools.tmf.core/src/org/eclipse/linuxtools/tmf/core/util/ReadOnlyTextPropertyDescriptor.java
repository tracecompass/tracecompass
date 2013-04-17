/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.util;

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
 * @since 2.0
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
