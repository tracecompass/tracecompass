/**********************************************************************
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
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.List;

/**
 * <p>
 * Interface for providing information about contexts to be added to channels/events.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IAddContextDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Sets the available contexts to choose from.
     * @param contexts - a list of available contexts.
     */
    void setAvalibleContexts(List<String> contexts);

    /**
     * @return array of contexts to be added
     */
    List<String> getContexts();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the open return value
     */
    int open();
}
