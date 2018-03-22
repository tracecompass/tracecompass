/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Color palette provider. Returns a list of colors. Use the {@link #get()}
 * method to get the colors
 *
 * @author Matthew Khouzam
 */
public interface IPaletteProvider extends Supplier<@NonNull List<@NonNull RGBAColor>> {

}
