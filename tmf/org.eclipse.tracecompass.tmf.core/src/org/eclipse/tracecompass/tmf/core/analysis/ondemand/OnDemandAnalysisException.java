/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.ondemand;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Exceptions resulting from the execution of on-demand analyses.
 *
 * The suggested behavior is to display the reported messages to the end user,
 * so they know why execution did not end normally.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class OnDemandAnalysisException extends Exception {

    private static final long serialVersionUID = 7296987172562152876L;

    /**
     * Build a new exception. If the message is not null, it should be reported
     * to the user.
     *
     * @param message
     *            The message to display, if any
     */
    public OnDemandAnalysisException(@Nullable String message) {
        super(message);
    }

    @Override
    public @Nullable String getMessage() {
        return super.getMessage();
    }

}
