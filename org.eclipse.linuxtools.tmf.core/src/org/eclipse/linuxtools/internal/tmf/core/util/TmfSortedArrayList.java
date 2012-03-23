/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.util;

import java.util.ArrayList;

/**
 * <b><u>TmfSortedArrayList</u></b>
 * <p>
 */

public class TmfSortedArrayList<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public void insertSorted(T value) {
        add(value);
        Comparable<T> cmp = (Comparable<T>) value;
        for (int pos = size() - 1; pos > 0 && cmp.compareTo(get(pos - 1)) < 0; pos--) {
            T tmp = get(pos);
            set(pos, get(pos - 1));
            set(pos - 1, tmp);
        }
    }

}
