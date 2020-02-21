/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IEnableEventsDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;

/**
 * Enable events dialog stub implementation.
 */
@SuppressWarnings("javadoc")
public class EnableEventsDialogStub implements IEnableEventsDialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private boolean fIsTracePoints;
    private boolean fIsAllEvents;
    private boolean fIsAllTracePoints;
    private boolean fIsSysCalls;
    private boolean fIsDynamicProbe;
    private String fProbeEventName;
    private String fDynamicProbe;
    private boolean fIsFunctionProbe;
    private String fFunctionEventName;
    private String fFunctionProbe;
    private boolean fIsWildcard;
    private String fWildcard;
    private boolean fIsLogLevel;
    private TraceLogLevel fLogLevel;
    private LogLevelType fLogLevelType;
    private String fFilter;
    private List<String> fNames = new ArrayList<>();
    private List<String> fExcludedEvents;
    private TraceDomainType fDomain;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    public void setDomain(TraceDomainType domain) {
        fDomain = domain;
    }

    public void setIsAllEvents(boolean isAllEvents) {
        fIsAllEvents = isAllEvents;
    }

    public void setIsTracePoints(boolean isTracePoints) {
        fIsTracePoints = isTracePoints;
    }

    public void setIsAllTracePoints(boolean isAllTracePoints) {
        fIsAllTracePoints = isAllTracePoints;
    }

    public void setIsSysCalls(boolean isSysCalls) {
        this.fIsSysCalls = isSysCalls;
    }

    public void setIsDynamicProbe(boolean isDynamicProbe) {
        fIsDynamicProbe = isDynamicProbe;
    }

    public void setProbeEventName(String probeEventName) {
        fProbeEventName = probeEventName;
    }

    public void setDynamicProbe(String dynamicProbe) {
        fDynamicProbe = dynamicProbe;
    }

    public void setIsFunctionProbe(boolean isFunctionProbe) {
        fIsFunctionProbe = isFunctionProbe;
    }

    public void setFunctionEventName(String functionEventName) {
        fFunctionEventName = functionEventName;
    }

    public void setFunctionProbe(String functionProbe) {
        fFunctionProbe = functionProbe;
    }

    public void setIsWildcard(boolean isWildcard) {
        fIsWildcard = isWildcard;
    }

    public void setWildcard(String wildcard) {
        fWildcard = wildcard;
    }

    public void setIsLogLevel(boolean isLogLevel) {
        fIsLogLevel = isLogLevel;
    }

    public void setLogLevel(TraceLogLevel logLevel) {
        fLogLevel = logLevel;
    }

    public void setLogLevelType(LogLevelType logLevelType) {
        fLogLevelType = logLevelType;
    }

    public void setNames(List<String> names) {
        fNames = names;
    }

    public void setFilterExpression(String filter) {
        fFilter = filter;
    }

    @Override
    public boolean isAllEvents() {
        return fIsAllEvents;
    }

    @Override
    public boolean isTracepoints() {
        return fIsTracePoints;
    }

    @Override
    public boolean isAllTracePoints() {
        return fIsAllTracePoints;
    }

    @Override
    public boolean isSyscalls() {
        return fIsSysCalls;
    }

    @Override
    public boolean isAllSyscalls() {
        return fIsSysCalls;
    }

    @Override
    public List<String> getEventNames() {
        return fNames;
    }

    @Override
    public boolean isDynamicProbe() {
        return fIsDynamicProbe;
    }

    @Override
    public String getProbeEventName() {
        return fProbeEventName;
    }

    @Override
    public String getProbeName() {
        return fDynamicProbe;
    }

    @Override
    public boolean isDynamicFunctionProbe() {
        return fIsFunctionProbe;
    }

    @Override
    public String getFunctionEventName() {
        return fFunctionEventName;
    }

    @Override
    public String getFunction() {
        return fFunctionProbe;
    }

    @Override
    public boolean isWildcard() {
        return fIsWildcard;
    }

    @Override
    public String getWildcard() {
        return fWildcard;
    }

    @Override
    public boolean isLogLevel() {
        return fIsLogLevel;
    }

    @Override
    public LogLevelType getLogLevelType() {
        return fLogLevelType;
    }

    @Override
    public TraceLogLevel getLogLevel() {
        return fLogLevel;
    }

    @Override
    public TraceDomainType getDomain() {
        return fDomain;
    }

    @Override
    public void setTraceProviderGroup(TraceProviderGroup providerGroup) {
    }

    @Override
    public void setTraceDomainComponent(TraceDomainComponent domain) {
    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public String getFilterExpression() {
        return fFilter;
    }

    @Override
    public List<String> getExcludedEvents() {
        return fExcludedEvents;
    }

}