/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.events;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the new events table messages.
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.messages"; //$NON-NLS-1$
    /**
     * The events table title
     */
    public static @Nullable String EventsTableDataProvider_Title;
    /**
     * The events table data provider help text
     */
    public static @Nullable String EventsTableDataProviderFactory_DescriptionText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Default constructor
    }
}
