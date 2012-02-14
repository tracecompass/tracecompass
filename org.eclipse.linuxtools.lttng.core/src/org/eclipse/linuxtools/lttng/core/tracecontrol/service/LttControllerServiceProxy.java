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
package org.eclipse.linuxtools.lttng.core.tracecontrol.service;

import org.eclipse.linuxtools.lttng.core.LttngConstants;
import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * <b><u>LttControllerServiceProxy</u></b>
 * <p>
 * Provides the mplementation of the ILttControllerService interface for 
 * sending of commands to the remote system.
 * </p>
 */
public class LttControllerServiceProxy implements ILttControllerService {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final short INVALID_ARG_LENGTH = 2;
    
    private IChannel fProxychannel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public LttControllerServiceProxy(IChannel chan) {
        fProxychannel = chan;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.tm.tcf.protocol.IService#getName()
     * 
     * Return service name, as it appears on the wire - a TCF name of the service.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Sets the service channel.
     * @param channel
     */
    public void setChannel(IChannel channel) {
        fProxychannel = channel;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getProviders(org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetProviders)
     */
    @Override
    public IToken getProviders(final DoneGetProviders done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetProviders, new Object[] {}) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0]; 
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetProviders(token, error, strArray);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getTargets(java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetTargets)
     */
    @Override
    public IToken getTargets(String provider, final DoneGetTargets done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetTargets, new Object[] { provider }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0];
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str =  args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetTargets(token, error, strArray);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getMarkers(java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetMarkers)
     */
    @Override
    public IToken getMarkers(String provider, String target, final DoneGetMarkers done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetMarkers, new Object[] { provider, target }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0];
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetMarkers(token, error, strArray);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getTraces(java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetTraces)
     */
    @Override
    public IToken getTraces(String provider, String target, final DoneGetTraces done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetTraces, new Object[] { provider, target }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0];
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetTraces(token, error, strArray);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getActiveTraces(java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetActiveTraces)
     */
    @Override
    public IToken getActiveTraces(String provider, String target, final DoneGetActiveTraces done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetActiveTraces, new Object[] { provider, target }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0];
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetActiveTraces(token, error, strArray);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getActiveTraceInfo(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetActiveTraceInfo)
     */
    @Override
    public IToken getActiveTraceInfo(String provider, String target, String trace, final DoneGetActiveTraceInfo done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetActiveTraceInfo, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0];
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetActiveTraceInfo(token, error, strArray);
            }
        }.token;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getChannels(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetChannels)
     */
    @Override
    public IToken getChannels(String provider, String target, String trace, final DoneGetChannels done) {

        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetChannels, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] strArray = new String[0];
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    strArray = toStringArray(str.toString());
                }
                done.doneGetChannels(token, error, strArray);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setupTrace(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetupTrace)
     */
    @Override
    public IToken setupTrace(String provider, String target, String trace, final DoneSetupTrace done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetupTrace, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetupTrace(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setTraceTransport(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetTraceTransport)
     */
    @Override
    public IToken setTraceTransport(String provider, String target, String trace, String transport, final DoneSetTraceTransport done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetupTraceTransport, new Object[] { provider, target, trace, transport }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetTraceTransport(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#getMarkerInfo(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneGetMarkerInfo)
     */
    @Override
    public IToken getMarkerInfo(String provider, String target, String marker, final DoneGetMarkerInfo done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_GetMarkerInfo, new Object[] { provider, target, marker }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                String result = ""; //$NON-NLS-1$
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                    result = str.toString();
                    if (result.length() > 2) {
                        result = result.substring(1, result.length() - 1);
                    }
                }
                done.doneGetMarkerInfo(token, error, result);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setMarkerEnable(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetMarkerEnable)
     */
    @Override
    public IToken setMarkerEnable(String provider, String target, String marker, Boolean enable, final DoneSetMarkerEnable done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetMarkerEnable, new Object[] { provider, target, marker, enable }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetMarkerEnable(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setChannelEnable(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetChannelEnable)
     */
    @Override
    public IToken setChannelEnable(String provider, String target, String trace, String channel, Boolean enable, final DoneSetChannelEnable done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetChannelEnable, new Object[] { provider, target, trace, channel, enable }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetChannelEnable(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setChannelOverwrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetChannelOverwrite)
     */
    @Override
    public IToken setChannelOverwrite(String provider, String target, String trace, String channel, Boolean overwrite, final DoneSetChannelOverwrite done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetChannelOverwrite, new Object[] { provider, target, trace, channel, overwrite }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetChannelOverwrite(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setChannelTimer(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetChannelTimer)
     */
    @Override
    public IToken setChannelTimer(String provider, String target, String trace, String channel, long period, final DoneSetChannelTimer done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetChannelTimer, new Object[] { provider, target, trace, channel, period }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetChannelTimer(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setChannelSubbufNum(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetChannelSubbufNum)
     */
    @Override
    public IToken setChannelSubbufNum(String provider, String target, String trace, String channel, long subbufNum, final DoneSetChannelSubbufNum done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetChannelSubbufNum, new Object[] { provider, target, trace, channel, subbufNum }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetChannelSubbufNum(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#setChannelSubbufSize(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneSetChannelSubbufSize)
     */
    @Override
    public IToken setChannelSubbufSize(String provider, String target, String trace, String channel, long subbufSize, final DoneSetChannelSubbufSize done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_SetChannelSubbufSize, new Object[] { provider, target, trace, channel, subbufSize }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneSetChannelSubbufSize(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#allocTrace(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneAllocTrace)
     */
    @Override
    public IToken allocTrace(String provider, String target, String trace, final DoneAllocTrace done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_AllocTrace, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneAllocTrace(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#writeTraceLocal(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneWriteTraceLocal)
     */
    @Override
    public IToken writeTraceLocal(String provider, String target, String trace, String path, int numChannel, Boolean isAppend, Boolean isFlightRecorder, Boolean isNormalOnly, final DoneWriteTraceLocal done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_WriteTraceLocal, new Object[] { provider, target, trace, path, numChannel, isAppend, isFlightRecorder, isNormalOnly }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneWriteTraceLocal(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#writeTraceNetwork(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneWriteTraceNetwork)
     */
    @Override
    public IToken writeTraceNetwork(String provider, String target, String trace, String path, int numChannel, Boolean isAppend, Boolean isFlightRecorder, Boolean isNormalOnly, final DoneWriteTraceNetwork done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_WriteTraceNetwork, new Object[] { provider, target, trace, path, numChannel, isAppend, isFlightRecorder, isNormalOnly }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneWriteTraceNetwork(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#stopWriteTraceNetwork(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneWriteTraceNetwork)
     */
    @Override
    public IToken stopWriteTraceNetwork(String provider, String target, String trace, final DoneStopWriteTraceNetwork done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_StopWriteTraceNetwork, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneStopWriteTraceNetwork(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#startTrace(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneStartTrace)
     */
    @Override
    public IToken startTrace(String provider, String target, String trace, final DoneStartTrace done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_StartTrace, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneStartTrace(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#pauseTrace(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DonePauseTrace)
     */
    @Override
    public IToken pauseTrace(String provider, String target, String trace, final DonePauseTrace done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_PauseTrace, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.donePauseTrace(token, error, str);
            }
        }.token;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.rse.service.ILttControllerService#destroyTrace(java.lang.String, java.lang.String, java.lang.String, org.eclipse.linuxtools.lttng.rse.service.ILttControllerService.DoneDestroyTrace)
     */
    @Override
    public IToken destroyTrace(String provider, String target, String trace, final DoneDestroyTrace done) {
        return new Command(fProxychannel, this, LttngConstants.Lttng_Control_DestroyTrace, new Object[] { provider, target, trace }) {
            @Override
            public void done(Exception error, Object[] args) {
                Object str = null;
                if (error == null) {
                    assert args.length == INVALID_ARG_LENGTH;
                    error = toDetailedError(toError(args[0]), args[1]);
                    str = args[1];
                }
                done.doneDestroyTrace(token, error, str);
            }
        }.token;
    }

    /*
     * Converts comma separated string to String array.
     */
    private String[] toStringArray(String list) {
        if (list.length() > 2) {
            String temp = list.substring(1, list.length() - 1);
            String[] str = temp.split(LttngConstants.Lttng_Control_Separator);
            for (int i = 0; i < str.length; i++) {
                str[i] = str[i].trim();
            }
            return str;
        }
        return new String[0];
    }
    
    /*
     * Creates new error report using a given error and a detailed message.
     */
    private Exception toDetailedError(Exception error, Object detail) {
        if ((error != null) && (error instanceof ErrorReport) && (detail != null)) {
            error = new ErrorReport(error.getMessage() + "\nDetail: " + detail.toString(),  ((ErrorReport) error).getAttributes());  //$NON-NLS-1$
        }
        return error;
    }
}
