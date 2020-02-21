/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;

/**
 * Interface that represent a line in a virtual table.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface IVirtualTableLine {
    /**
     * Get the table line index
     *
     * @return The index of this line
     */
    long getIndex();

    /**
     * Get the table line
     *
     * @return A list of string that contain the data for this line
     */
    List<VirtualTableCell> getCells();
}
