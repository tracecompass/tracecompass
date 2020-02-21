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

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver;

/**
 * Abstract class implementing a mapper that returns string values.
 *
 * @param <T>
 *            The type of the input
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IStringResolver<T> extends IDataResolver<T, String> {

}
