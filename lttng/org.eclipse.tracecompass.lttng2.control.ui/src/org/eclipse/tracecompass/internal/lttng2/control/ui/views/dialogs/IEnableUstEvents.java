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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.List;

/**
 * Interface for providing information about UST events to be enabled.
 *
 * @author Bernd Hufmann
 */
public interface IEnableUstEvents  extends IBaseEnableUstEvents {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return a flag whether the tracepoints shall be configured.
     */
    boolean isTracepoints();

    /**
     * @return a flag whether events using wildcards should be enabled
     */
    boolean isWildcard();

    /**
     * @return a wildcard
     */
    String getWildcard();

    /**
     * @return a filter expression
     */
    String getFilterExpression();

    /**
     * @return a list of events to exclude
     */
    List<String> getExcludedEvents();

}
