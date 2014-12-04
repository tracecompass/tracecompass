/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview.messages"; //$NON-NLS-1$

    public static @Nullable String VmView_threads;
    public static @Nullable String VmView_stateTypeName;
    public static @Nullable String VmView_multipleStates;
    public static @Nullable String VmView_nextResourceActionNameText;
    public static @Nullable String VmView_nextResourceActionToolTipText;
    public static @Nullable String VmView_previousResourceActionNameText;
    public static @Nullable String VmView_previousResourceActionToolTipText;
    public static @Nullable String VmView_VCpu;
    public static @Nullable String VmView_virtualMachine;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
