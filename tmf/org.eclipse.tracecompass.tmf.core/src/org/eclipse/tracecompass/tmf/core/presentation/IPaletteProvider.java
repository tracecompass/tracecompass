/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Color palette provider. Returns a list of colors. Use the {@link #get()}
 * method to get the colors
 *
 * @author Matthew Khouzam
 * @since 4.0
 */
public interface IPaletteProvider extends Supplier<@NonNull List<@NonNull RGBAColor>> {

    @Override
    @NonNull List<@NonNull RGBAColor> get();

}
