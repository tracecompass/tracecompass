/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Schorn - Initial API and implementation
 *   Bernd Hufmann - Update for null safety
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LttngVersion;

class NullControlService implements ILttngControlService {

    @Override
    public LttngVersion getVersion() {
        return LttngVersion.NULL_VERSION;
    }

    @Override
    public String getVersionString() {
        return checkNotNull(LttngVersion.NULL_VERSION.toString());
    }

    @Override
    public boolean isVersionSupported(String version) {
        return false;
    }

    @Override
    public List<String> getSessionNames(IProgressMonitor monitor) throws ExecutionException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        return null;
    }

    @Override
    public ISnapshotInfo getSnapshotInfo(String sessionName, IProgressMonitor monitor) throws ExecutionException {
        return null;
    }

    @Override
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor) throws ExecutionException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<IUstProviderInfo> getUstProvider() throws ExecutionException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor) throws ExecutionException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public ISessionInfo createSession(ISessionInfo sessionInfo, IProgressMonitor monitor) throws ExecutionException {
        return null;
    }

    @Override
    public void destroySession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void startSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void stopSession(String sessionName, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void enableChannels(String sessionName, List<String> channelNames, TraceDomainType domain, IChannelInfo info, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void disableChannels(String sessionName, List<String> channelNames, TraceDomainType domain, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void enableEvents(String sessionName, String channelName, List<String> eventNames, TraceDomainType domain, String filterExpression, List<String> excludedEvents, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void enableSyscalls(String sessionName, String channelName, List<String> eventNames, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void enableProbe(String sessionName, String channelName, String eventName, boolean isFunction, String probe, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void enableLogLevel(String sessionName, String channelName, List<String> eventName, LogLevelType logLevelType, ITraceLogLevel level, String filterExpression, TraceDomainType domain, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void disableEvent(String sessionName, String channelName, List<String> eventNames, TraceDomainType domain, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public List<String> getContextList(IProgressMonitor monitor) throws ExecutionException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void addContexts(String sessionName, String channelName, String eventName, TraceDomainType domain, List<String> contexts, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void recordSnapshot(String sessionName, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void runCommands(IProgressMonitor monitor, List<String> commands) throws ExecutionException {
    }

    @Override
    public void loadSession(@Nullable String inputPath, boolean isForce, IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void saveSession(String session, String outputPath, boolean isForce, IProgressMonitor monitor) throws ExecutionException {
    }
}
