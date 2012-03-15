/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.tracecontrol.service;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

public interface ILttControllerService extends IService {

    /**
     * This service name, as it appears on the wire - a TCF name of the service.
     */
    public static final String NAME = LttngConstants.Lttng_Control_Command;

    IToken getProviders(DoneGetProviders done);

    interface DoneGetProviders {
        /**
         * This method is called when getProviders() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetProviders(IToken token, Exception error, String str[]);
    }

    IToken getTargets(String provider, DoneGetTargets done);

    interface DoneGetTargets {
        /**
         * This method is called when getTargets() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetTargets(IToken token, Exception error, String str[]);
    }

    IToken getMarkers(String provider, String target, DoneGetMarkers done);

    interface DoneGetMarkers {
        /**
         * This method is called when getMarkers() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetMarkers(IToken token, Exception error, String str[]);
    }

    IToken getTraces(String provider, String target, DoneGetTraces done);

    interface DoneGetTraces {
        /**
         * This method is called when getTraces() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetTraces(IToken token, Exception error, String str[]);
    }

    IToken getActiveTraces(String provider, String target, DoneGetActiveTraces done);

    interface DoneGetActiveTraces {
        /**
         * This method is called when getTraces() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetActiveTraces(IToken token, Exception error, String str[]);
    }
    
    IToken getActiveTraceInfo(String provider, String target, String trace, DoneGetActiveTraceInfo done);

    interface DoneGetActiveTraceInfo {
        /**
         * This method is called when getTraces() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetActiveTraceInfo(IToken token, Exception error, String str[]);
    }
    
    
    
    IToken getChannels(String provider, String target, String trace, DoneGetChannels done);

    interface DoneGetChannels {
        /**
         * This method is called when getChannels() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetChannels(IToken token, Exception error, String str[]);
    }

    IToken setupTrace(String provider, String target, String trace, DoneSetupTrace done);

    interface DoneSetupTrace {
        /**
         * This method is called when setupTrace() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetupTrace(IToken token, Exception error, Object str);
    }

    IToken setTraceTransport(String provider, String target, String trace, String transport, DoneSetTraceTransport done);

    interface DoneSetTraceTransport {
        /**
         * This method is called when setTraceTransport() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetTraceTransport(IToken token, Exception error, Object str);
    }

    IToken getMarkerInfo(String provider, String target, String marker, DoneGetMarkerInfo done);

    interface DoneGetMarkerInfo {
        /**
         * This method is called when getMarkerInfo() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneGetMarkerInfo(IToken token, Exception error, String str);
    }

    IToken setMarkerEnable(String provider, String target, String marker, Boolean enable, DoneSetMarkerEnable done);

    interface DoneSetMarkerEnable {
        /**
         * This method is called when setMarkerEnable() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetMarkerEnable(IToken token, Exception error, Object str);
    }

    IToken writeTraceLocal(String provider, String target, String trace, String path, int numChannel, Boolean isAppend, Boolean isFlightRecorder, Boolean isNormalOnly, DoneWriteTraceLocal done);

    interface DoneWriteTraceLocal {
        /**
         * This method is called when writeTraceLocal() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneWriteTraceLocal(IToken token, Exception error, Object str);
    }

    IToken writeTraceNetwork(String provider, String target, String trace, String path, int numChannel, Boolean isAppend, Boolean isFlightRecorder, Boolean isNormalOnly, DoneWriteTraceNetwork done);

    interface DoneWriteTraceNetwork {
        /**
         * This method is called when writeTraceNetwork() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneWriteTraceNetwork(IToken token, Exception error, Object str);
    }

    IToken stopWriteTraceNetwork(String provider, String target, String trace, DoneStopWriteTraceNetwork done);

    interface DoneStopWriteTraceNetwork {
        /**
         * This method is called when stopWriteTraceNetwork() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneStopWriteTraceNetwork(IToken token, Exception error, Object str);
    }

    IToken allocTrace(String provider, String target, String trace, DoneAllocTrace done);

    interface DoneAllocTrace {
        /**
         * This method is called when allocTrace() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneAllocTrace(IToken token, Exception error, Object str);
    }

    IToken setChannelEnable(String provider, String target, String trace, String channel, Boolean enable, DoneSetChannelEnable done);

    interface DoneSetChannelEnable {
        /**
         * This method is called when setChannelEnable() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetChannelEnable(IToken token, Exception error, Object str);
    }

    IToken setChannelOverwrite(String provider, String target, String trace, String channel, Boolean overwrite, DoneSetChannelOverwrite done);

    interface DoneSetChannelOverwrite {
        /**
         * This method is called when setChannelOverwrite() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetChannelOverwrite(IToken token, Exception error, Object str);
    }

    IToken setChannelTimer(String provider, String target, String trace, String channel, long period, DoneSetChannelTimer done);

    interface DoneSetChannelTimer {
        /**
         * This method is called when setChannelTimer() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetChannelTimer(IToken token, Exception error, Object str);
    }

    IToken setChannelSubbufNum(String provider, String target, String trace, String channel, long subbufNum, DoneSetChannelSubbufNum done);

    interface DoneSetChannelSubbufNum {
        /**
         * This method is called when setChannelSubbufNum() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetChannelSubbufNum(IToken token, Exception error, Object str);
    }

    IToken setChannelSubbufSize(String provider, String target, String trace, String channel, long subbufSize, DoneSetChannelSubbufSize done);

    interface DoneSetChannelSubbufSize {
        /**
         * This method is called when setChannelSubbufSize() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneSetChannelSubbufSize(IToken token, Exception error, Object str);
    }

    IToken startTrace(String provider, String target, String trace, DoneStartTrace done);

    interface DoneStartTrace {
        /**
         * This method is called when startTrace() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneStartTrace(IToken token, Exception error, Object str);
    }

    IToken pauseTrace(String provider, String target, String trace, DonePauseTrace done);

    interface DonePauseTrace {
        /**
         * This method is called when pauseTrace() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void donePauseTrace(IToken token, Exception error, Object str);
    }

    IToken destroyTrace(String provider, String target, String trace, DoneDestroyTrace done);

    interface DoneDestroyTrace {
        /**
         * This method is called when destroyTrace() command is completed.
         * 
         * @param token - pending command handle.
         * @param error - null if the command is successful.
         * @param str - response of the agent
         */
        void doneDestroyTrace(IToken token, Exception error, Object str);
    }
}
