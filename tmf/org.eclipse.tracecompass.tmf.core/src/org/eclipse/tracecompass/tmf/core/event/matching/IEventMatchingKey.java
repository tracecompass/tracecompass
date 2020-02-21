/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

/**
 * Interface that classes describing event matching keys should implement. An
 * event matching key is a key obtained from an event's data/fields and that
 * will be used to match events together.
 *
 * If you implement this interface, make sure to consider implementing the
 * {@link Object#hashCode()} and {@link Object#equals(Object)} as they will be
 * used to match 2 keys together. Keys will come from different events and 2
 * keys that are identical using the {@link Object#equals(Object)} method will
 * create a match.
 *
 * @author Geneviève Bastien
 */
public interface IEventMatchingKey {

}
