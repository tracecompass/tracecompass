/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfThreadSelectedSignal;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Follow Thread Action, this action broadcasts a
 * {@link TmfThreadSelectedSignal} when run, it sends a thread id and a trace to
 * the signal.
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public class UnfollowThreadAction extends Action {

    private final TmfView fView;

    /**
     * Constructor
     *
     * @param source
     *            the view that is generating the signal, but also shall broadcast
     *            it
     */
    public UnfollowThreadAction(TmfView source) {
        fView = source;
    }

    @Override
    public String getText() {
        return Messages.FollowThreadAction_unfollow;
    }

    @Override
    public void run() {
        fView.broadcast(new TmfThreadSelectedSignal(fView, new HostThread("", -1))); //$NON-NLS-1$
        super.run();
    }

}
