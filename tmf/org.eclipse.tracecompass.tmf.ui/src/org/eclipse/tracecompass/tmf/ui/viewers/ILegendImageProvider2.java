/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;

/**
 * Provides a legend image. With a name, desired height and width, an image is
 * created.
 *
 * @since 6.0
 */
public interface ILegendImageProvider2 {

    /**
     * Returns an image that represents the legend.
     *
     * @param imageHeight
     *            Desired image height
     * @param imageWidth
     *            Desired image width
     * @param id
     *            Id associate with a legend image
     * @return A legend image
     */
    Image getLegendImage(int imageHeight, int imageWidth, @NonNull Long id);
}
