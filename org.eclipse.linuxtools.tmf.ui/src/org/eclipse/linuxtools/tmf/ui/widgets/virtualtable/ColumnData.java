/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
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
/**
 * ColumnData
 * @author Matthew Khouzam
 * @version 1.0
 *
 */
public class ColumnData {
    /**
     * The title of the column
     */
    public final String header;
    /**
     * the width of the column in pixels
     */
    public final int    width;
    /**
     * the alignment of the column
     */
    public final int    alignment;

    /**
     * Constructor
     * @param h header (title)
     * @param w width
     * @param a alignment
     */
    public ColumnData(String h, int w, int a) {
        header = h;
        width = w;
        alignment = a;
    }

}
