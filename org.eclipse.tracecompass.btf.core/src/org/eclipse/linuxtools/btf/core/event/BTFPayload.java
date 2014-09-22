/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core.event;

import java.util.Map;

import org.eclipse.linuxtools.btf.core.Messages;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * BTF event information (Table 7 and 8 from the spec)
 *
 * @author Matthew Khouzam
 */
public class BTFPayload {

    private static final Map<String, String> EVENT_DESCRIPTIONS;
    private static final String EMPTY = "TBA"; //$NON-NLS-1$
    private static final Map<String, TmfEventField[]> FIELDS;
    private static final TmfEventField[] TBA_FIELD = new TmfEventField[] { new TmfEventField(EMPTY, EMPTY, new TmfEventField[] { new TmfEventField(EMPTY, EMPTY, null) }) };

    static {
        ImmutableMap.Builder<String, String> builder = new Builder<>();
        builder.put("activate", Messages.BTFPayload_Activate); //$NON-NLS-1$
        builder.put("start", Messages.BTFPayload_Start); //$NON-NLS-1$
        builder.put("preempt", Messages.BTFPayload_Preempt); //$NON-NLS-1$
        builder.put("resume", Messages.BTFPayload_Resume); //$NON-NLS-1$
        builder.put("terminate", Messages.BTFPayload_Terminate); //$NON-NLS-1$
        builder.put("poll", Messages.BTFPayload_Poll); //$NON-NLS-1$
        builder.put("run", Messages.BTFPayload_Run); //$NON-NLS-1$
        builder.put("park", Messages.BTFPayload_Park); //$NON-NLS-1$
        builder.put("poll_parking", Messages.BTFPayload_PollParking); //$NON-NLS-1$
        builder.put("release_parking", Messages.BTFPayload_ReleaseParking); //$NON-NLS-1$
        builder.put("wait", Messages.BTFPayload_Wait); //$NON-NLS-1$
        builder.put("release", Messages.BTFPayload_Release); //$NON-NLS-1$
        builder.put("deadline", EMPTY); //$NON-NLS-1$
        builder.put("mpalimitexceeded", Messages.BTFPayload_MapLimitExceeded); //$NON-NLS-1$
        builder.put("boundedmigration", Messages.BTFPayload_BoundedMigration); //$NON-NLS-1$
        builder.put("phasemigration", Messages.BTFPayload_PhaseMigration); //$NON-NLS-1$
        builder.put("fullmigration", Messages.BTFPayload_FullMigration); //$NON-NLS-1$
        builder.put("enforcedmigration", Messages.BTFPayload_EnforcedMigration); //$NON-NLS-1$
        // not yet defined
        builder.put("execute_idle", EMPTY); //$NON-NLS-1$
        builder.put("processterminate", EMPTY); //$NON-NLS-1$
        builder.put("idle", EMPTY); //$NON-NLS-1$
        builder.put("schedule", EMPTY); //$NON-NLS-1$
        builder.put("trigger", EMPTY); //$NON-NLS-1$
        builder.put("schedulepoint", EMPTY); //$NON-NLS-1$
        builder.put("requestsemaphore", EMPTY); //$NON-NLS-1$
        builder.put("queued", EMPTY); //$NON-NLS-1$
        builder.put("lock", EMPTY); //$NON-NLS-1$
        builder.put("assigned", EMPTY); //$NON-NLS-1$
        builder.put("unlock", EMPTY); //$NON-NLS-1$
        builder.put("released", EMPTY); //$NON-NLS-1$
        builder.put("ready", EMPTY); //$NON-NLS-1$
        builder.put("free", EMPTY); //$NON-NLS-1$
        builder.put("set_frequence", EMPTY); //$NON-NLS-1$
        builder.put("processactivate", EMPTY); //$NON-NLS-1$
        builder.put("execute", EMPTY); //$NON-NLS-1$
        builder.put("idle_execution", EMPTY); //$NON-NLS-1$
        builder.put("suspend", EMPTY); //$NON-NLS-1$
        builder.put("read", EMPTY); //$NON-NLS-1$
        builder.put("write", EMPTY); //$NON-NLS-1$
        builder.put("processpolling", EMPTY); //$NON-NLS-1$
        builder.put("execute_waiting", EMPTY); //$NON-NLS-1$
        builder.put("wait_postexecution", EMPTY); //$NON-NLS-1$
        builder.put("waiting", EMPTY); //$NON-NLS-1$
        builder.put("overfull", EMPTY); //$NON-NLS-1$
        builder.put("full", EMPTY); //$NON-NLS-1$

        EVENT_DESCRIPTIONS = builder.build();
        ImmutableMap.Builder<String, TmfEventField[]> fieldBuilder = new Builder<>();
        for (String key : EVENT_DESCRIPTIONS.keySet()) {
            fieldBuilder.put(key, new TmfEventField[] { new TmfEventField("description", EVENT_DESCRIPTIONS.get(key), null) }); //$NON-NLS-1$
        }
        FIELDS = fieldBuilder.build();
    }

    /**
     * gets the description of a field
     *
     * @param key
     *            the field name
     * @return the description
     */
    public static TmfEventField[] getFieldDescription(String key) {
        String shortKey = key.split(",", 2)[0]; //$NON-NLS-1$
        TmfEventField[] retVal = FIELDS.get(shortKey);
        return (retVal == null) ? TBA_FIELD : retVal;
    }

}
