/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.parsers.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.parsers.custom.Messages;

/**
 * An aspect for a custom event's extra fields
 *
 * @author Geneviève Bastien
 */
public class CustomExtraFieldsAspect implements ITmfEventAspect<String> {

    /**
     * Constructor
     */
    public CustomExtraFieldsAspect() {
        // Do nothing
    }

    @Override
    public @NonNull String getName() {
        return NonNullUtils.nullToEmptyString(Messages.CustomExtraFieldsAspect_extraFieldsAspectName);
    }

    @Override
    public @NonNull String getHelpText() {
        return NonNullUtils.nullToEmptyString(Messages.CustomExtraFieldsAspect_extraFieldsAspectHelp);
    }

    @Override
    public @Nullable String resolve(@NonNull ITmfEvent event) {
        List<String> fields = new ArrayList<>();
        for (ITmfEventField field : event.getContent().getFields()) {
            // Add the fields that do not have another aspect associated
            if (field instanceof CustomExtraField) {
                fields.add(field.getName() + '=' + field.getValue());
            }
        }
        return String.join(", ", fields); //$NON-NLS-1$
    }

}
