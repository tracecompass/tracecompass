/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.core.tests.model.impl;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseLoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IFieldInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IProbeEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BaseLoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.DomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.EventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.FieldInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.LoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ProbeEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SnapshotInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.UstProviderInfo;

/**
 *  Test facility to constants across test case
 */
@SuppressWarnings("javadoc")
public class ModelImplFactory {

    private ISessionInfo fSessionInfo1 = null;
    private ISessionInfo fSessionInfo2 = null;
    private IDomainInfo fDomainInfo1 = null;
    private IDomainInfo fDomainInfo2 = null;
    private IChannelInfo fChannelInfo1 = null;
    private IChannelInfo fChannelInfo2 = null;
    private IEventInfo fEventInfo1 = null;
    private IEventInfo fEventInfo2 = null;
    private IEventInfo fEventInfo3 = null;
    private ILoggerInfo fLoggerInfo1 = null;
    private ILoggerInfo fLoggerInfo2 = null;
    private IFieldInfo fFieldInfo1 = null;
    private IFieldInfo fFieldInfo2 = null;
    private IBaseEventInfo fBaseEventInfo1 = null;
    private IBaseEventInfo fBaseEventInfo2 = null;
    private IBaseLoggerInfo fBaseLoggerInfo1 = null;
    private IBaseLoggerInfo fBaseLoggerInfo2 = null;
    private IUstProviderInfo fUstProviderInfo1 = null;
    private IUstProviderInfo fUstProviderInfo2 = null;
    private IProbeEventInfo fProbeEventInfo1 = null;
    private IProbeEventInfo fProbeEventInfo2 = null;
    private ISnapshotInfo fSnapshotInfo1 = null;
    private ISnapshotInfo fSnapshotInfo2 = null;

    public ModelImplFactory() {

        fFieldInfo1 = new FieldInfo("intfield");
        fFieldInfo1.setFieldType("int");
        fFieldInfo2 = new FieldInfo("stringfield");
        fFieldInfo2.setFieldType("string");

        fBaseEventInfo1 = new BaseEventInfo("event1");
        fBaseEventInfo1.setEventType(TraceEventType.UNKNOWN);
        fBaseEventInfo1.setLogLevel(TraceLogLevel.TRACE_ERR);
        fBaseEventInfo1.addField(fFieldInfo1);
        fBaseEventInfo1.addField(fFieldInfo2);
        fBaseEventInfo1.setFilterExpression("intField==10");

        fBaseEventInfo2 = new BaseEventInfo("event2");
        fBaseEventInfo2.setEventType(TraceEventType.TRACEPOINT);
        fBaseEventInfo2.setLogLevel(TraceLogLevel.TRACE_DEBUG);

        fBaseLoggerInfo1 = new BaseLoggerInfo("logger1");
        fBaseLoggerInfo1.setDomain(TraceDomainType.JUL);
        fBaseLoggerInfo1.setLogLevel(TraceJulLogLevel.JUL_FINE);

        fBaseLoggerInfo2 = new BaseLoggerInfo("logger2");
        fBaseLoggerInfo2.setDomain(TraceDomainType.LOG4J);
        fBaseLoggerInfo1.setLogLevel(TraceJulLogLevel.JUL_FINER);

        fEventInfo1 = new EventInfo("event1");
        fEventInfo1.setEventType(TraceEventType.TRACEPOINT);
        fEventInfo1.setLogLevelType("==");
        fEventInfo1.setLogLevel(TraceLogLevel.TRACE_DEBUG);
        fEventInfo1.setState(TraceEnablement.ENABLED);

        fEventInfo2 = new EventInfo("event2");
        fEventInfo2.setLogLevelType("<=");
        fEventInfo2.setLogLevel(TraceLogLevel.TRACE_INFO);
        fEventInfo2.setEventType(TraceEventType.UNKNOWN);
        fEventInfo2.setState(TraceEnablement.DISABLED);

        fEventInfo3 = new EventInfo("event3");
        fEventInfo3.setEventType(TraceEventType.TRACEPOINT);
        fEventInfo3.setState(TraceEnablement.DISABLED);

        fLoggerInfo1 = new LoggerInfo("logger1");
        fLoggerInfo1.setDomain(TraceDomainType.JUL);
        fLoggerInfo1.setLogLevel(TraceJulLogLevel.JUL_CONFIG);
        fLoggerInfo1.setState("false");

        fLoggerInfo2 = new LoggerInfo("logger2");
        fLoggerInfo2.setDomain(TraceDomainType.LOG4J);
        fLoggerInfo1.setLogLevel(TraceJulLogLevel.JUL_INFO);
        fLoggerInfo1.setState("true");

        fUstProviderInfo1 = new UstProviderInfo("myUST1");
        fUstProviderInfo1.setPid(1234);
        fUstProviderInfo1.addEvent(fBaseEventInfo1);
        fUstProviderInfo1.addLogger(fLoggerInfo1);

        fUstProviderInfo2 = new UstProviderInfo("myUST2");
        fUstProviderInfo2.setPid(2345);
        fUstProviderInfo2.addEvent(fBaseEventInfo1);
        fUstProviderInfo2.addEvent(fBaseEventInfo2);
        fUstProviderInfo2.addLogger(fLoggerInfo1);
        fUstProviderInfo2.addLogger(fLoggerInfo2);

        fChannelInfo1 = new ChannelInfo("channel1");
        fChannelInfo1.setSwitchTimer(10L);
        fChannelInfo1.setOverwriteMode(true);
        fChannelInfo1.setReadTimer(11L);
        fChannelInfo1.setState(TraceEnablement.DISABLED);
        fChannelInfo1.setNumberOfSubBuffers(12);
        fChannelInfo1.setOutputType("splice()");
        fChannelInfo1.setSubBufferSize(13L);
        fChannelInfo1.addEvent(fEventInfo1);
        fChannelInfo1.setNumberOfDiscardedEvents(14L);
        fChannelInfo1.setNumberOfLostPackets(15L);

        fChannelInfo2 = new ChannelInfo("channel2");
        fChannelInfo2.setSwitchTimer(1L);
        fChannelInfo2.setOverwriteMode(false);
        fChannelInfo2.setReadTimer(2L);
        fChannelInfo2.setState(TraceEnablement.ENABLED);
        fChannelInfo2.setNumberOfSubBuffers(3);
        fChannelInfo2.setOutputType("mmap()");
        fChannelInfo2.setSubBufferSize(4L);
        fChannelInfo2.addEvent(fEventInfo2);
        fChannelInfo2.addEvent(fEventInfo3);
        fChannelInfo2.setNumberOfDiscardedEvents(5L);
        fChannelInfo2.setNumberOfLostPackets(6L);

        fDomainInfo1 = new DomainInfo("test1");
        fDomainInfo1.addChannel(fChannelInfo1);

        fDomainInfo2 = new DomainInfo("test2");
        fDomainInfo2.addChannel(fChannelInfo1);
        fDomainInfo2.addChannel(fChannelInfo2);

        fSessionInfo1 = new SessionInfo("session1");
        fSessionInfo1.setSessionPath("/home/user");
        fSessionInfo1.setSessionState(TraceSessionState.ACTIVE);
        fSessionInfo1.addDomain(fDomainInfo1);

        fSessionInfo2 = new SessionInfo("session2");
        fSessionInfo2.setSessionPath("/home/user1");
        fSessionInfo2.setSessionState(TraceSessionState.INACTIVE);
        fSessionInfo2.addDomain(fDomainInfo1);
        fSessionInfo2.addDomain(fDomainInfo2);
        fSessionInfo2.setStreamedTrace(true);

        fProbeEventInfo1 = new ProbeEventInfo("probeEvent1");
        fProbeEventInfo1.setEventType(TraceEventType.TRACEPOINT);
        fProbeEventInfo1.setState(TraceEnablement.ENABLED);
        fProbeEventInfo1.setAddress("0xc1231234");

        fProbeEventInfo2 = new ProbeEventInfo("probeEvent2");
        fProbeEventInfo2.setEventType(TraceEventType.FUNCTION);
        fProbeEventInfo2.setState(TraceEnablement.DISABLED);
        fProbeEventInfo2.setOffset("0x100");
        fProbeEventInfo2.setSymbol("init_post");

        fSnapshotInfo1 = new SnapshotInfo("snapshot-1");
        fSnapshotInfo1.setId(1);
        fSnapshotInfo1.setSnapshotPath("/home/user/lttng-trace/mysession/");
        fSnapshotInfo2 = new SnapshotInfo("other-snapshot");
        fSnapshotInfo2.setId(1);
        fSnapshotInfo2.setSnapshotPath("net4://172.0.0.1:1234/");
        fSnapshotInfo2.setStreamedSnapshot(true);

        fSessionInfo1.setSnapshotInfo(fSnapshotInfo1);
    }

    public ISessionInfo getSessionInfo1() {
        return fSessionInfo1;
    }

    public ISessionInfo getSessionInfo2() {
        return fSessionInfo2;
    }

    public IDomainInfo getDomainInfo1() {
        return fDomainInfo1;
    }

    public IDomainInfo getDomainInfo2() {
        return fDomainInfo2;
    }

    public IChannelInfo getChannel1() {
        return fChannelInfo1;
    }

    public IChannelInfo getChannel2() {
        return fChannelInfo2;
    }

    public IEventInfo getEventInfo1() {
        return fEventInfo1;
    }

    public IEventInfo getEventInfo2() {
        return fEventInfo2;
    }

    public IEventInfo getEventInfo3() {
        return fEventInfo3;
    }

    public IBaseEventInfo getBaseEventInfo1() {
        return fBaseEventInfo1;
    }

    public IBaseEventInfo getBaseEventInfo2() {
        return fBaseEventInfo2;
    }

    public ILoggerInfo getLoggerInfo1() {
        return fLoggerInfo1;
    }

    public ILoggerInfo getLoggerInfo2() {
        return fLoggerInfo2;
    }

    public IBaseLoggerInfo getBaseLoggerInfo1() {
        return fBaseLoggerInfo1;
    }

    public IBaseLoggerInfo getBaseLoggerInfo2() {
        return fBaseLoggerInfo2;
    }

    public IUstProviderInfo getUstProviderInfo1() {
        return fUstProviderInfo1;
    }

    public IUstProviderInfo getUstProviderInfo2() {
        return fUstProviderInfo2;
    }

    public IProbeEventInfo getProbeEventInfo1() {
        return fProbeEventInfo1;
    }

    public IProbeEventInfo getProbeEventInfo2() {
        return fProbeEventInfo2;
    }

    public IFieldInfo getFieldInfo1() {
        return fFieldInfo1;
    }

    public IFieldInfo getFieldInfo2() {
        return fFieldInfo2;
    }

    public ISnapshotInfo getSnapshotInfo1() {
        return fSnapshotInfo1;
    }

    public ISnapshotInfo getSnapshotInfo2() {
        return fSnapshotInfo2;
    }
}
