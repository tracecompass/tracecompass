/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;

/**
 * Property sheet page with empty sorter
 *
 * @author Patrick Tasse
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
                // Empty sorter, do nothing
            }
        });
    }

}
