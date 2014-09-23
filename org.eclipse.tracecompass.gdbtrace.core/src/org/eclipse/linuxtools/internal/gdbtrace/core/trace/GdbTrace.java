/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Dumais - Initial implementation
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *   Matthew Khouzam - update validate
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.core.trace;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.gdbtrace.core.Activator;
import org.eclipse.linuxtools.internal.gdbtrace.core.GdbTraceCorePlugin;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.osgi.util.NLS;

/**
 * GDB Tracepoint extension of TmfTrace. This class implements the necessary
 * methods and functionalities so that a GDB tracepoint file can be used by the
 * TMF framework as a "tracer".
 * <p>
 *
 * @author Marc Dumais
 * @author Francois Chouinard
 * @author Matthew Khouzam
 */
@SuppressWarnings("restriction")
public class GdbTrace extends TmfTrace implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int CACHE_SIZE = 20;

    private static final byte[] HEADER = new byte[] {0x7f, 'T', 'R', 'A', 'C', 'E'};

    /** The qualified name for the 'executable' persistent property */
    public static final QualifiedName EXEC_KEY = new QualifiedName(GdbTraceCorePlugin.PLUGIN_ID, "executable"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Interface to access GDB Tracepoints
    private DsfGdbAdaptor fGdbTpRef;
    private long fNbFrames = 0;

    // The trace location
    long fLocation;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public GdbTrace() {
        setCacheSize(CACHE_SIZE);
    }

    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    NLS.bind(Messages.GdbTrace_FileNotFound, path));
        }
        if (!file.isFile()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    NLS.bind(Messages.GdbTrace_GdbTracesMustBeAFile, path));
        }
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[HEADER.length];
            int read = stream.read(buffer);
            if (read != HEADER.length || !Arrays.equals(buffer, HEADER)) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        NLS.bind(Messages.GdbTrace_NotGdbTraceFile, path));
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    NLS.bind(Messages.GdbTrace_IOException, path), e);
        }
        return new TraceValidationStatus(100, GdbTraceCorePlugin.PLUGIN_ID);
    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        try {
            String tracedExecutable = resource.getPersistentProperty(EXEC_KEY);
            if (tracedExecutable == null) {
                throw new TmfTraceException(Messages.GdbTrace_ExecutableNotSet);
            }

            String defaultGdbCommand = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                    IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND,
                    IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT, null);

            fGdbTpRef = new DsfGdbAdaptor(this, defaultGdbCommand, path, tracedExecutable);
            fNbFrames = getNbFrames();
        } catch (CoreException e) {
            throw new TmfTraceException(Messages.GdbTrace_FailedToInitializeTrace, e);
        }

        super.initTrace(resource, path, type);
    }

    @Override
    public synchronized void dispose() {
        if (fGdbTpRef != null) {
            fGdbTpRef.dispose();
        }
        super.dispose();
    }

    /**
     * @return GDB-DSF session id
     */
    public String getDsfSessionId() {
        return fGdbTpRef.getSessionId();
    }

    /**
     * @return the number of frames in current tp session
     */
    public synchronized long getNbFrames() {
        fNbFrames = fGdbTpRef.getNumberOfFrames();
        return fNbFrames;
    }

    // ------------------------------------------------------------------------
    // TmfTrace
    // ------------------------------------------------------------------------

    @Override
    public synchronized TmfContext seekEvent(ITmfLocation location) {
        fLocation = (location != null) ? ((Long) location.getLocationInfo()) : 0;
        return new TmfContext(new TmfLongLocation(fLocation), fLocation);
    }

    @Override
    public synchronized ITmfContext seekEvent(double ratio) {
        TmfContext context = seekEvent((long) ratio * getNbEvents());
        return context;
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        if (getNbEvents() > 0 && location instanceof TmfLongLocation) {
            return (double) ((TmfLongLocation) location).getLocationInfo() / getNbEvents();
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return new TmfLongLocation(fLocation);
    }

    @Override
    public GdbTraceEvent parseEvent(ITmfContext context) {
        if (context.getRank() >= fNbFrames) {
            return null;
        }
        // work-around to ensure that the select and parse of trace frame will
        // be atomic
        GdbTraceEvent event = fGdbTpRef.selectAndReadFrame(context.getRank());
        fLocation++;
        return event;
    }

    @Override
    public synchronized TmfContext seekEvent(ITmfTimestamp timestamp) {
        long rank = timestamp.getValue();
        return seekEvent(rank);
    }

    @Override
    public synchronized TmfContext seekEvent(long rank) {
        fLocation = rank;
        TmfContext context = new TmfContext(new TmfLongLocation(fLocation), rank);
        return context;
    }

    /**
     * Select a frame and update the visualization
     *
     * @param rank
     *            the rank
     */
    public synchronized void selectFrame(long rank) {
        fGdbTpRef.selectDataFrame(rank, true);
    }
}