/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

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
