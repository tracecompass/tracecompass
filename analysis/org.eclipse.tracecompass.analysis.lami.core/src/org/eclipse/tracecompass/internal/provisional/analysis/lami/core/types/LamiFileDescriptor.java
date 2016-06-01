/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

class LamiFileDescriptor extends LamiData {

    private final int fFd;

    public LamiFileDescriptor(int fd) {
        fFd = fd;
    }

    public int getId() {
        return fFd;
    }

    @Override
    public @Nullable String toString() {
        return Integer.toString(fFd);
    }
}
