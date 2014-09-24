/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;

/**
 * Property sheet page with empty sorter
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 2.0
 */
public class UnsortedPropertySheetPage extends PropertySheetPage {

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        // Override for unsorted property sheet page
        // See bug 1883 comment 43 and bug 109617
        setSorter(new PropertySheetSorter() {
            @Override
            public void sort(IPropertySheetEntry[] entries) {
            }
        });
    }

}
