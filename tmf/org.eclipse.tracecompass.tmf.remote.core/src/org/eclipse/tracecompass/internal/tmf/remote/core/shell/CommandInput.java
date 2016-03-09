/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.shell;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;

import com.google.common.collect.ImmutableList;

/**
 * Class for defining command input for remote command execution.
 *
 * @author Bernd Hufmann
 */
public class CommandInput implements ICommandInput {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The input as list of Strings. */
    private final @NonNull List<@NonNull String> fInput = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public List<String> getInput() {
        return ImmutableList.copyOf(fInput);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void add(@Nullable String segment) {
        if (segment != null) {
            fInput.add(segment);
        }
    }

    @Override
    public void addAll(List<String> segments) {
        for (String segment : segments) {
            add(segment);
        }
    }

    /**
     * Creates a single command string from a command line list.
     *
     * @return single command string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String segment : getInput()) {
            builder.append(segment).append(' ');
        }
        return nullToEmptyString(builder.toString().trim());
    }
}