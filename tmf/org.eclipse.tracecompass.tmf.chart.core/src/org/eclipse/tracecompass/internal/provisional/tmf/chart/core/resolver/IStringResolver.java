/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
