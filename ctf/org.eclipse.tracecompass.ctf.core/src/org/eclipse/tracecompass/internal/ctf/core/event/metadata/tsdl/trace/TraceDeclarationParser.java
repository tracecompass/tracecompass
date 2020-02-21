/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.trace;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.Messages;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.ByteOrderParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeSpecifierListParser;

/**
 *
 * A <em>trace<em> is divided into multiple event streams. Each event stream
 * contains a subset of the trace event types. <br>
 * The final output of the trace, after its generation and optional transport
 * over the network, is expected to be either on permanent or temporary storage
 * in a virtual file system. Because each event stream is appended to while a
 * trace is being recorded, each is associated with a distinct set of files for
 * output. Therefore, a stored trace can be represented as a directory
 * containing zero, one or more files per stream. <br>
 * Metadata description associated with the trace contains information on trace
 * event types expressed in the _Trace Stream Description Language_ (TSDL). This
 * language describes: <br>
 * <ul>
 * <li>Trace version</li>
 * <li>Types available</li>
 * <li>Per-trace event header description</li>
 * <li>Per-stream event header description</li>
 * <li>Per-stream event context description</li>
 * <li>Per-event
 * <ul>
 * <li>Event type to stream mapping</li>
 * <li>Event type to name mapping</li>
 * <li>Event type to ID mapping</li>
 * <li>Event context description</li>
 * <li>Event fields description</li>
 * </ul>
 * </ul>
 *
 * @author Matthew Khouzam
 *
 */
public final class TraceDeclarationParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {

        private final DeclarationScope fCurrentScope;
        private final CTFTrace fTrace;

        /**
         * Parameter Object
         *
         * @param trace
         *            the trace
         * @param currentScope
         *            the current scope
         */
        public Param(CTFTrace trace, DeclarationScope currentScope) {
            fTrace = trace;
            fCurrentScope = currentScope;
        }

    }

    /**
     * Parser instance
     */
    public static final TraceDeclarationParser INSTANCE = new TraceDeclarationParser();

    private TraceDeclarationParser() {
    }

    /**
     * Parse a trace AST node
     *
     * @param traceDecl
     *            trace AST node
     * @param param
     *            A parameter object of the type {@link Param}
     * @return a {@link CTFTrace} that is populated
     * @throws ParseException
     *             if the AST is malformed
     */
    @Override
    public CTFTrace parse(CommonTree traceDecl, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        CTFTrace trace = ((Param) param).fTrace;
        DeclarationScope scope = ((Param) param).fCurrentScope;

        /* There should be a left and right */
        CommonTree leftNode = (CommonTree) traceDecl.getChild(0);
        CommonTree rightNode = (CommonTree) traceDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException("Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        String left = concatenateUnaryStrings(leftStrings);

        if (left.equals(MetadataStrings.MAJOR)) {
            if (trace.majorIsSet()) {
                throw new ParseException("major is already set"); //$NON-NLS-1$
            }

            trace.setMajor(VersionNumberParser.INSTANCE.parse(rightNode, null));
        } else if (left.equals(MetadataStrings.MINOR)) {
            if (trace.minorIsSet()) {
                throw new ParseException("minor is already set"); //$NON-NLS-1$
            }

            trace.setMinor(VersionNumberParser.INSTANCE.parse(rightNode, null));
        } else if (left.equals(MetadataStrings.UUID_STRING)) {
            UUID uuid = UUIDParser.INSTANCE.parse(rightNode, null);

            /*
             * If uuid was already set by a metadata packet, compare it to see
             * if it matches
             */
            if (trace.uuidIsSet()) {
                if (trace.getUUID().compareTo(uuid) != 0) {
                    throw new ParseException("UUID mismatch. Packet says " //$NON-NLS-1$
                            + trace.getUUID() + " but metadata says " + uuid); //$NON-NLS-1$
                }
            } else {
                trace.setUUID(uuid);
            }

        } else if (left.equals(MetadataStrings.BYTE_ORDER)) {
            ByteOrder byteOrder = ByteOrderParser.INSTANCE.parse(rightNode, new ByteOrderParser.Param(trace));

            /*
             * If byte order was already set by a metadata packet, compare it to
             * see if it matches
             */
            if (trace.getByteOrder() != null) {
                if (trace.getByteOrder() != byteOrder) {
                    throw new ParseException(
                            "Endianness mismatch. Magic number says " //$NON-NLS-1$
                                    + trace.getByteOrder()
                                    + " but metadata says " + byteOrder); //$NON-NLS-1$
                }
            } else {
                trace.setByteOrder(byteOrder);

                final DeclarationScope currentScope = scope;
                for (String type : currentScope.getTypeNames()) {
                    IDeclaration d = currentScope.lookupType(type);
                    if (d instanceof IntegerDeclaration) {
                        addByteOrder(byteOrder, currentScope, type, (IntegerDeclaration) d);
                    } else if (d instanceof FloatDeclaration) {
                        addByteOrder(byteOrder, currentScope, type, (FloatDeclaration) d);
                    } else if (d instanceof EnumDeclaration) {
                        addByteOrder(byteOrder, currentScope, type, (EnumDeclaration) d);
                    } else if (d instanceof StructDeclaration) {
                        setAlign(currentScope, (StructDeclaration) d, byteOrder);
                    }
                }
            }
        } else if (left.equals(MetadataStrings.PACKET_HEADER)) {
            if (trace.packetHeaderIsSet()) {
                throw new ParseException("packet.header already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("packet.header expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration packetHeaderDecl = TypeSpecifierListParser.INSTANCE.parse(typeSpecifier, new TypeSpecifierListParser.Param(trace, null, null, scope));

            if (!(packetHeaderDecl instanceof StructDeclaration)) {
                throw new ParseException("packet.header expects a struct"); //$NON-NLS-1$
            }

            trace.setPacketHeader((StructDeclaration) packetHeaderDecl);
        } else {
            Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownTraceAttributeWarning + " " + left); //$NON-NLS-1$
        }
        return trace;
    }

    private static void addByteOrder(ByteOrder byteOrder,
            final DeclarationScope parentScope, String name,
            IntegerDeclaration decl) throws ParseException {

        if (!decl.isByteOrderSet()) {
            IntegerDeclaration newI;
            newI = IntegerDeclaration.createDeclaration(decl.getLength(), decl.isSigned(),
                    decl.getBase(), byteOrder, decl.getEncoding(),
                    decl.getClock(), decl.getAlignment());
            parentScope.replaceType(name, newI);
        }
    }

    private static void addByteOrder(ByteOrder byteOrder, DeclarationScope parentScope, String name, EnumDeclaration decl) throws ParseException {
        if (!decl.isByteOrderSet()) {
            final IntegerDeclaration containerType = decl.getContainerType();
            EnumDeclaration newEnum = new EnumDeclaration(IntegerDeclaration.createDeclaration(containerType.getLength(), containerType.isSigned(),
                    containerType.getBase(), byteOrder, containerType.getEncoding(),
                    containerType.getClock(), containerType.getAlignment()),
                    decl.getLookupTable());

            parentScope.replaceType(name, newEnum);
        }
    }

    private static void addByteOrder(ByteOrder byteOrder, DeclarationScope parentScope, String name, FloatDeclaration decl) throws ParseException {
        if (!decl.isByteOrderSet()) {
            FloatDeclaration newFloat = new FloatDeclaration(decl.getExponent(), decl.getMantissa(), byteOrder, decl.getAlignment());
            parentScope.replaceType(name, newFloat);
        }
    }

    private void setAlign(DeclarationScope parentScope, StructDeclaration sd,
            ByteOrder byteOrder) throws ParseException {

        for (String s : sd.getFieldsList()) {
            IDeclaration d = sd.getField(s);

            if (d instanceof StructDeclaration) {
                setAlign(parentScope, (StructDeclaration) d, byteOrder);

            } else if (d instanceof VariantDeclaration) {
                setAlign(parentScope, (VariantDeclaration) d, byteOrder);
            } else if (d instanceof IntegerDeclaration) {
                IntegerDeclaration decl = (IntegerDeclaration) d;
                if (decl.getByteOrder() != byteOrder) {
                    IntegerDeclaration newI;
                    newI = IntegerDeclaration.createDeclaration(decl.getLength(),
                            decl.isSigned(), decl.getBase(), byteOrder,
                            decl.getEncoding(), decl.getClock(),
                            decl.getAlignment());
                    sd.addField(s, newI);
                }
            }
        }
    }

    private void setAlign(DeclarationScope parentScope, VariantDeclaration vd,
            ByteOrder byteOrder) throws ParseException {

        for (String s : vd.getFields().keySet()) {
            IDeclaration d = vd.getFields().get(s);

            if (d instanceof StructDeclaration) {
                setAlign(parentScope, (StructDeclaration) d, byteOrder);

            } else if (d instanceof IntegerDeclaration) {
                IntegerDeclaration decl = (IntegerDeclaration) d;
                IntegerDeclaration newI;
                newI = IntegerDeclaration.createDeclaration(decl.getLength(),
                        decl.isSigned(), decl.getBase(), byteOrder,
                        decl.getEncoding(), decl.getClock(),
                        decl.getAlignment());
                vd.getFields().put(s, newI);
            }
        }
    }
}
