/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Add UST callstack state system
 *   Marc-Andre Laperle - Handle BufferOverflowException (Bug 420203)
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.trace;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.common.core.trace.ILttngTrace;
import org.eclipse.tracecompass.internal.lttng2.ust.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.ContextVpidAspect;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.ContextVtidAspect;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.DefaultUstEventLayout;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst20EventLayout;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst27EventLayout;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst28EventLayout;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst29EventLayout;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryAspect;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoFunctionAspect;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoSourceAspect;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * Class to contain LTTng-UST traces
 *
 * @author Matthew Khouzam
 */
public class LttngUstTrace extends CtfTmfTrace implements ILttngTrace{

    /**
     * Name of the tracer that generates this trace type, as found in the CTF
     * metadata.
     *
     * @since 2.0
     */
    public static final String TRACER_NAME = "lttng-ust"; //$NON-NLS-1$

    private static final int CONFIDENCE = 100;

    private static final @NonNull Collection<ITmfEventAspect<?>> LTTNG_UST_ASPECTS;

    static {
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(CtfTmfTrace.CTF_ASPECTS);
        builder.add(UstDebugInfoBinaryAspect.INSTANCE);
        builder.add(UstDebugInfoFunctionAspect.INSTANCE);
        builder.add(UstDebugInfoSourceAspect.INSTANCE);
        LTTNG_UST_ASPECTS = builder.build();
    }

    /** Default collections of aspects */
    private @NonNull Collection<ITmfEventAspect<?>> fUstTraceAspects = ImmutableSet.copyOf(LTTNG_UST_ASPECTS);

    private @Nullable ILttngUstEventLayout fLayout = null;

    /**
     * Default constructor
     */
    public LttngUstTrace() {
        super(LttngUstEventFactory.instance());
    }

    /**
     * Protected constructor for child classes. Classes extending this one may
     * have extra fields coming from the event itself and may pass their own
     * event factory.
     *
     * @param factory
     *            The event factory for this specific trace
     * @since 3.0
     */
    protected LttngUstTrace(@NonNull CtfTmfEventFactory factory) {
        super(factory);
    }

    /**
     * Get the event layout to use with this trace. This normally depends on the
     * tracer's version.
     *
     * @return The event layout
     * @since 2.0
     */
    public @NonNull ILttngUstEventLayout getEventLayout() {
        ILttngUstEventLayout layout = fLayout;
        if (layout == null) {
            throw new IllegalStateException("Cannot get the layout of a non-initialized trace!"); //$NON-NLS-1$
        }
        return layout;
    }

    @Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);

        /* Determine the event layout to use from the tracer's version */
        ILttngUstEventLayout layout = getLayoutFromEnv();
        fLayout = layout;

        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(LTTNG_UST_ASPECTS);
        if (checkFieldPresent(layout.contextVtid())) {
            builder.add(new ContextVtidAspect(layout));
        }
        if (checkFieldPresent(layout.contextVpid())) {
            builder.add(new ContextVpidAspect(layout));
        }
        builder.addAll(createCounterAspects(this));
        fUstTraceAspects = builder.build();
    }

    private boolean checkFieldPresent(@NonNull String field) {
        final Multimap<@NonNull String, @NonNull String> traceEvents = TmfEventTypeCollectionHelper.getEventFieldNames((getContainedEventTypes()));

        return traceEvents.containsValue(field);
    }

    private @NonNull ILttngUstEventLayout getLayoutFromEnv() {
        String tracerName = CtfUtils.getTracerName(this);
        int tracerMajor = CtfUtils.getTracerMajorVersion(this);
        int tracerMinor = CtfUtils.getTracerMinorVersion(this);

        if (TRACER_NAME.equals(tracerName)) {
            if (tracerMajor >= 2) {
                if (tracerMinor >= 9) {
                    return LttngUst29EventLayout.getInstance();
                } else if (tracerMinor >= 8) {
                    return LttngUst28EventLayout.getInstance();
                } else if (tracerMinor >= 7) {
                    return LttngUst27EventLayout.getInstance();
                }
                return LttngUst20EventLayout.getInstance();
            }
        }

        /* Fallback to the Default layout and hope for the best */
        return DefaultUstEventLayout.getInstance();
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return fUstTraceAspects;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "ust" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        IStatus status = super.validate(project, path);
        if (status instanceof CtfTraceValidationStatus) {
            Map<String, String> environment = ((CtfTraceValidationStatus) status).getEnvironment();
            /* Make sure the domain is "ust" in the trace's env vars */
            String domain = environment.get("domain"); //$NON-NLS-1$
            if (domain == null || !domain.equals("\"ust\"")) { //$NON-NLS-1$
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngUstTrace_DomainError);
            }
            return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
        }
        return status;
    }

    // ------------------------------------------------------------------------
    // Fields/methods bridging the Debug-info symbol provider
    // ------------------------------------------------------------------------

    /*
     * FIXME Once the symbol provider is split in core/ui components, the
     * UstDebugInfoSymbolProvider should be moved to the core plugin, and this
     * here can be removed.
     */

    /**
     * Configuration of the symbol provider.
     *
     * @since 2.0
     */
    public static class SymbolProviderConfig {

        private final boolean fUseCustomRootDir;
        private final @NonNull String fCustomRootDirPath;

        /**
         * Constructor
         *
         * Note that a path can be specified even if 'useCustomRootDir' is
         * false. This will keep the setting in the text field even when it is
         * grayed out.
         *
         * @param useCustomRootDir
         *            Should a custom directory be used
         * @param rootDirPath
         *            Custom directory path
         */
        public SymbolProviderConfig(boolean useCustomRootDir, @NonNull String rootDirPath) {
            fUseCustomRootDir = useCustomRootDir;
            fCustomRootDirPath = rootDirPath;
        }

        /**
         * @return Should a custom directory be used
         */
        public boolean useCustomRootDir() {
            return fUseCustomRootDir;
        }

        /**
         * @return The configured root directory
         */
        public String getCustomRootDirPath() {
            return fCustomRootDirPath;
        }

        /**
         * Return the "real" path to use for symbol resolution. This is a
         * convenience method that avoids having to check the state of
         * {@link #useCustomRootDir()} separately.
         *
         * @return The actual root directory to use
         */
        public String getActualRootDirPath() {
            if (fUseCustomRootDir) {
                return fCustomRootDirPath;
            }
            return ""; //$NON-NLS-1$
        }
    }

    private @NonNull SymbolProviderConfig fCurrentProviderConfig =
            /* Default settings for new traces */
            new SymbolProviderConfig(false, ""); //$NON-NLS-1$


    /**
     * Get the current symbol provider configuration for this trace.
     *
     * @return The current symbol provider configuration
     * @since 2.0
     */
    public @NonNull SymbolProviderConfig getSymbolProviderConfig() {
        return fCurrentProviderConfig;
    }

    /**
     * Set the symbol provider configuration for this trace.
     *
     * @param config
     *            The new symbol provider configuration to use
     * @since 2.0
     */
    public void setSymbolProviderConfig(@NonNull SymbolProviderConfig config) {
        fCurrentProviderConfig = config;
        UstDebugInfoBinaryAspect.invalidateSymbolCache();
    }
}
