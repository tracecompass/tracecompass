/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.statesystem.mipmap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * Interface to allow additional types of mipmaps to be added. Two functions
 * need to be implemented: {@link ITmfMipmapFeature#updateMipmap} and
 * {@link ITmfMipmapFeature#updateAndCloseMipmap}.
 *
 * @author Jean-Christian Kouamé
 *
 */
public interface ITmfMipmapFeature {

    /**
     * Update the mipmap with a new state value.
     *
     * @param value
     *            The new state value
     * @param ts
     *            The timestamp of the event
     */
    public void updateMipmap(@NonNull ITmfStateValue value, long ts);

    /**
     * Update the mipmap values at all levels before closing.
     */
    public void updateAndCloseMipmap();
}
