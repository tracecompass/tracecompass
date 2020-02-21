/**********************************************************************
 * Copyright (c) 2015 Ericsson
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
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.tmf.remote.core.shell;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for creating a command input to executued in a
 * {@link ICommandShell} implementation.
 *
 * @author Bernd Hufmann
 */
public interface ICommandInput {

    /**
     * Return the command output.
     *
     * It should not be null, but could be empty.
     * It should return an immutable list.
     *
     * @return the command output.
     */
    List<String> getInput();

    /**
     * Adds a command segment to the command
     *
     * @param segment
     *            the command segment to add. Ignored if null.
     */
    void add(@Nullable String segment);

    /**
     * Adds a command segments to the command
     *
     * @param segments
     *            the command segments to add. Ignored if null.
     *            Any null segment in list will be ignored too.
     */
    void addAll(List<String> segments);
}
