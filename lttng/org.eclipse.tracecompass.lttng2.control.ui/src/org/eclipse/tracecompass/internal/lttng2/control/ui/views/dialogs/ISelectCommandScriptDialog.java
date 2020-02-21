/**********************************************************************
 * Copyright (c) 2014 Ericsson
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
 * Interface for dialog for selecting a command script.
 *
 * @author Bernd Hufmann
 *
 */
public interface ISelectCommandScriptDialog {
    /**
     * @return a list of command
     */
    List<String> getCommands();

    /**
     * @return the open return value
     */
    int open();
}
