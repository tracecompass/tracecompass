/**********************************************************************
 * Copyright (c) 2021 Draeger, Auriga
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.statesystem.core.backend;

/**
 * A custom extension to {@link IStateHistoryBackend}.
 *
 * @author Ivan Grinenko
 * @since 5.1
 *
 */
public interface ICustomStateHistoryBackend extends IStateHistoryBackend {

    /**
     * Once a custom backend builds its storage and fills it with data it is
     * considered to be "built". It tells analysis that Trace Compass should not
     * run analysis again.
     *
     * @return {@code true} if backend is built already, {@code false}
     *             otherwise.
     */
    boolean isBuilt();

}
