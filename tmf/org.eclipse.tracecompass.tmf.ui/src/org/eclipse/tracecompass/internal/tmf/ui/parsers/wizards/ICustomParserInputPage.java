/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;

/**
 * Custom Parser input page API needed for output page
 *
 * @author Matthew Khouzam
 */
interface ICustomParserInputPage {

    /**
     * Get the global list of inputs.
     *
     * @return The list of inputs
     */
    List<Entry<Tag, String>> getInputs();

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    CustomTraceDefinition getDefinition();

    /**
     * Get the raw text input.
     *
     * @return The raw text input.
     */
    char[] getInputText();

    /**
     * Returns this wizard page's title.
     *
     * @return the title of this wizard page, or <code>null</code> if none
     */
    String getTitle();

}