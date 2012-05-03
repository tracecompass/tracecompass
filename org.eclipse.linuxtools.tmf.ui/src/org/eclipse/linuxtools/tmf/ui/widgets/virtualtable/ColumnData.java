/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Khouzam - Extracted from TmfEventsView
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.virtualtable;

public class ColumnData {
    public final String header;
    public final int    width;
    public final int    alignment;

    public ColumnData(String h, int w, int a) {
        header = h;
        width = w;
        alignment = a;
    }

}
