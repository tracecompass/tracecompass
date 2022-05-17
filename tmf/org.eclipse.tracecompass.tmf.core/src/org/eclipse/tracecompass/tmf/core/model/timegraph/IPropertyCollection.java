/**********************************************************************
 * Copyright (c) 2018, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.tracecompass.tmf.core.model.ICorePropertyCollection;

/**
 * Interface to get and set properties. This represents a group of items known
 * as properties. It provides a caching method for filtering elements, storing
 * the results as properties.
 * <p>
 * A developer should use this to store the results of tests to be able to
 * exchange these with another process that can act upon this "report".
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 * @deprecated As {@link ICorePropertyCollection} is to be used instead.
 */
@Deprecated
public interface IPropertyCollection extends ICorePropertyCollection {
}
