/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *     Bernd Hufmann - Updated for source and model lookup interfaces
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.tmf.core.event.ITmfCustomAttributes;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.CtfConstants;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * A wrapper class around CTF's Event Definition/Declaration that maps all types
 * of Declaration to native Java types.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class CtfTmfEvent extends TmfEvent
        implements ITmfModelLookup, ITmfCustomAttributes {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String EMPTY_CTF_EVENT_NAME = "Empty CTF event"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Support attributes
    // Not part of this event's "definition", but used to populate lazy-loaded
    // fields.
    // ------------------------------------------------------------------------

    private final @Nullable IEventDeclaration fEventDeclaration;
    private final IEventDefinition fEvent;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /* Fields that are introduced by and part of this event's definition. */
    private final int fSourceCpu;
    private final String fChannel;

    /**
     * Field to override {@link TmfEvent#getName()}, to bypass the type-getting
     */
    private final String fEventName;

    /** Lazy-loaded field containing the event's payload */
    private transient @Nullable ITmfEventField fContent;

    /** Lazy-loaded field for the type, overriding TmfEvent's field */
    private transient @Nullable CtfTmfEventType fEventType;

    private final @Nullable ICTFStream fStream;
    private final Map<String, Object> fPacketAttributes;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor, used by {@link CtfTmfEventFactory#createEvent}.
     *
     * Only subclasses should call this. It is imperative that the subclass also
     * has a constructor with the EXACT same parameter signature, because the
     * factory will look for a constructor with the same arguments.
     *
     * @param trace
     *            The trace to which this event belongs
     * @param rank
     *            The rank of the event
     * @param timestamp
     *            The timestamp
     * @param channel
     *            The CTF channel of this event
     * @param cpu
     *            The event's CPU
     * @param declaration
     *            The event declaration
     * @param eventDefinition
     *            The event definition
     * @since 2.0
     */
    protected CtfTmfEvent(CtfTmfTrace trace,
            long rank,
            TmfNanoTimestamp timestamp,
            String channel,
            int cpu,
            IEventDeclaration declaration,
            IEventDefinition eventDefinition) {
        super(trace,
                rank,
                timestamp,
                /*
                 * Event type. We don't use TmfEvent's field here, we
                 * re-implement getType().
                 */
                null,
                /*
                 * Content handled with a lazy-loaded field re-implemented in
                 * getContent().
                 */
                null);

        fEventDeclaration = declaration;
        fSourceCpu = cpu;
        fEventName = checkNotNull(declaration.getName());
        fEvent = eventDefinition;
        fChannel = channel;
        fStream = fEvent.getDeclaration().getStream();
        fPacketAttributes = eventDefinition.getPacketAttributes();
    }

    /**
     * Inner constructor to create "null" events. Don't use this directly in
     * normal usage, use {@link CtfTmfEventFactory#getNullEvent(CtfTmfTrace)} to
     * get an instance of an empty event.
     *
     * There is no need to give higher visibility to this method than package
     * visible.
     *
     * @param trace
     *            The trace associated with this event
     */
    CtfTmfEvent(CtfTmfTrace trace) {
        super(trace,
                ITmfContext.UNKNOWN_RANK,
                new TmfNanoTimestamp(-1),
                null,
                new TmfEventField("", null, new CtfTmfEventField[0])); //$NON-NLS-1$
        fSourceCpu = -1;
        fEventName = EMPTY_CTF_EVENT_NAME;
        fEventDeclaration = null;
        fEvent = NullEventDefinition.INSTANCE;
        fChannel = ""; //$NON-NLS-1$
        fStream = null;
        fPacketAttributes = Collections.EMPTY_MAP;
    }

    /**
     * Default constructor. Do not use directly, but it needs to be present
     * because it's used in extension points, and the framework will use this
     * constructor to get the class type.
     *
     * @deprecated Should not be called by normal code
     */
    @Deprecated
    public CtfTmfEvent() {
        super();
        fSourceCpu = -1;
        fEventName = EMPTY_CTF_EVENT_NAME;
        fEventDeclaration = null;
        fEvent = NullEventDefinition.INSTANCE;
        fChannel = ""; //$NON-NLS-1$
        fStream = null;
        fPacketAttributes = Collections.EMPTY_MAP;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the cpu core the event was recorded on.
     *
     * @return The cpu id for a given source. In lttng it's from CPUINFO
     */
    public int getCPU() {
        return fSourceCpu;
    }

    /**
     * Return the CTF trace's channel from which this event originates.
     *
     * @return The event's channel
     * @since 2.0
     */
    public String getChannel() {
        return fChannel;
    }

    /**
     * Return this event's reference.
     *
     * @return The event's reference
     * @deprecated This method was replaced by {@link #getChannel()}.
     */
    @Deprecated
    public String getReference() {
        return getChannel();
    }

    /**
     * Get the stream Id
     *
     * @return the stream ID or -1 if the stream is null
     * @since 2.0
     */
    public long getStreamId() {
        ICTFStream stream = fStream;
        if (stream == null) {
            return -1;
        }
        return stream.getId();
    }

    // ------------------------------------------------------------------------
    // TmfEvent
    // ------------------------------------------------------------------------

    @Override
    public CtfTmfTrace getTrace() {
        /*
         * Should be of the right type, since we take a CtfTmfTrace at the
         * constructor
         */
        return (CtfTmfTrace) super.getTrace();
    }

    @Override
    public synchronized ITmfEventType getType() {
        CtfTmfEventType type = fEventType;
        if (type == null) {
            type = new CtfTmfEventType(fEventName, getContent());

            /*
             * Register the event type in the owning trace, but only if there is
             * one
             */
            getTrace().registerEventType(type);
            fEventType = type;
        }
        return type;
    }

    @Override
    public String getName() {
        return fEventName;
    }

    @Override
    public synchronized ITmfEventField getContent() {
        ITmfEventField content = fContent;
        if (content == null) {
            content = new TmfEventField(
                    ITmfEventField.ROOT_FIELD_ID, null, parseFields(fEvent));
            fContent = content;
        }
        return content;
    }

    /**
     * Extract the field information from the structDefinition haze-inducing
     * mess, and put them into something ITmfEventField can cope with.
     */
    private static CtfTmfEventField[] parseFields(IEventDefinition eventDef) {
        List<CtfTmfEventField> fields = new ArrayList<>();

        ICompositeDefinition structFields = eventDef.getFields();
        if (structFields != null) {
            if (structFields.getFieldNames() != null) {
                for (String fn : structFields.getFieldNames()) {
                    fields.add(CtfTmfEventField.parseField((IDefinition) structFields.getDefinition(fn), fn));
                }
            }
        }
        /* Add context information as CtfTmfEventField */
        ICompositeDefinition structContext = eventDef.getContext();
        if (structContext != null) {
            for (String contextName : structContext.getFieldNames()) {
                /* Prefix field name */
                String curContextName = CtfConstants.CONTEXT_FIELD_PREFIX + contextName;
                fields.add(CtfTmfEventField.parseField((IDefinition) structContext.getDefinition(contextName), curContextName));
            }
        }

        return fields.toArray(new @NonNull CtfTmfEventField[fields.size()]);
    }

    // ------------------------------------------------------------------------
    // ITmfCustomAttributes
    // ------------------------------------------------------------------------

    @Override
    public Set<String> listCustomAttributes() {
        IEventDeclaration declaration = fEventDeclaration;
        if (declaration == null) {
            return new HashSet<>();
        }
        return declaration.getCustomAttributes();
    }

    @Override
    public @Nullable String getCustomAttribute(@Nullable String name) {
        IEventDeclaration declaration = fEventDeclaration;
        if (declaration == null) {
            return null;
        }
        return declaration.getCustomAttribute(name);
    }

    @Override
    public @Nullable String getModelUri() {
        return getCustomAttribute(CtfConstants.MODEL_URI_KEY);
    }

    /**
     * Gets the packet attributes. The result is an instance of one of the
     * following classes: <code>Entry<String, Long></code>, <code>Long</code>,
     * <code>String</code> or <code>Double</code>. The map contains pairs of key
     * and values where the key and value can never be null.
     *
     * @return gets the packet attributes
     * @since 2.0
     */
    public Map<String, Object> getPacketAttributes() {
        return fPacketAttributes;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + getCPU();
        result = prime * result + getChannel().hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        /* super.equals() checks that the classes are the same */
        CtfTmfEvent other = checkNotNull((CtfTmfEvent) obj);
        if (getCPU() != other.getCPU()) {
            return false;
        }
        if (!getChannel().equals(other.getChannel())) {
            return false;
        }
        return true;
    }

}
