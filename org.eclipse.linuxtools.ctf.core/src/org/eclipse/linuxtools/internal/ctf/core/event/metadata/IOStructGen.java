/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial Design and Grammar
 *     Francis Giraldeau - Initial API and implementation
 *     Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.ctf.core.event.CTFClock;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * IOStructGen
 */
public class IOStructGen {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    /**
     * The trace
     */
    private final CTFTrace trace;
    private final CommonTree tree;

    /**
     * The current declaration scope.
     */
    private DeclarationScope scope = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tree
     *            the tree (ANTLR generated) with the parsed TSDL data.
     * @param trace
     *            the trace containing the places to put all the read metadata
     */
    public IOStructGen(CommonTree tree, CTFTrace trace) {
        this.trace = trace;
        this.tree = tree;
    }

    /**
     * Parse the tree and populate the trace defined in the constructor.
     *
     * @throws ParseException
     *             If there was a problem parsing the metadata
     */
    public void generate() throws ParseException {
        parseRoot(tree);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Parse the root node.
     *
     * @param root
     *            A ROOT node.
     * @throws ParseException
     */
    private void parseRoot(CommonTree root) throws ParseException {

        List<CommonTree> children = root.getChildren();
        java.io.FileOutputStream fos = null;
        java.io.OutputStreamWriter out = null;
        if (DEBUG) {
            try {
                fos = new java.io.FileOutputStream("/tmp/astInfo.txt"); //$NON-NLS-1$
                out = new java.io.OutputStreamWriter(fos, "UTF-8"); //$NON-NLS-1$
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return;
            }
        }

        CommonTree traceNode = null;
        List<CommonTree> streams = new ArrayList<CommonTree>();
        List<CommonTree> events = new ArrayList<CommonTree>();
        List<CommonTree> declarations = new ArrayList<CommonTree>();
        List<CommonTree> environments = new ArrayList<CommonTree>();
        List<CommonTree> clocks = new ArrayList<CommonTree>();
        List<CommonTree> callsites = new ArrayList<CommonTree>();
        /* Create a new declaration scope with no parent. */
        pushScope();

        try {
            for (CommonTree child : children) {
                final int type = child.getType();
                if (DEBUG) {
                    out.write(child.toString()
                            + " -> " + type + '\n'); //$NON-NLS-1$
                }
                switch (type) {
                case CTFParser.DECLARATION:
                    declarations.add(child);
                    break;
                case CTFParser.TRACE:
                    if (traceNode != null) {
                        throw new ParseException(
                                "Only one trace block is allowed"); //$NON-NLS-1$
                    }
                    traceNode = child;
                    break;
                case CTFParser.STREAM:
                    streams.add(child);
                    break;
                case CTFParser.EVENT:
                    events.add(child);
                    break;
                case CTFParser.CLOCK:
                    clocks.add(child);
                    break;
                case CTFParser.ENV:
                    environments.add(child);
                    break;
                case CTFParser.CALLSITE:
                    callsites.add(child);
                    break;
                default:
                    childTypeError(child);
                }
            }
            if (DEBUG) {
                out.write("Declarations\n"); //$NON-NLS-1$
            }
            for (CommonTree decl : declarations) {
                if (DEBUG) {
                    out.write(decl.toString() + '\n');
                }
                parseRootDeclaration(decl);
            }
            if (traceNode == null) {
                throw new ParseException("Missing trace block"); //$NON-NLS-1$
            }

            parseTrace(traceNode);

            if (DEBUG) {
                out.write("Environments\n"); //$NON-NLS-1$
            }
            for (CommonTree environment : environments) {
                parseEnvironment(environment);
            }
            if (DEBUG) {
                out.write("Clocks\n"); //$NON-NLS-1$
            }
            for (CommonTree clock : clocks) {
                parseClock(clock);
            }
            if (DEBUG) {
                out.write("Callsites\n"); //$NON-NLS-1$
            }
            for (CommonTree callsite : callsites) {
                parseCallsite(callsite);
            }

            if (DEBUG) {
                out.write("Streams\n"); //$NON-NLS-1$
            }
            if (streams.size() > 0) {
                for (CommonTree stream : streams) {
                    if (DEBUG) {
                        try {
                            out.write(stream.toString() + '\n');
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    parseStream(stream);
                }
            } else {
                /* Add an empty stream that will have a null id */
                trace.addStream(new Stream(trace));
            }

            if (DEBUG) {
                out.write("Events\n"); //$NON-NLS-1$
            }
            for (CommonTree event : events) {
                parseEvent(event);
                if (DEBUG) {
                    CommonTree name = (CommonTree) event.getChild(0).getChild(1).getChild(0).getChild(0);
                    CommonTree id = (CommonTree) event.getChild(1).getChild(1).getChild(0).getChild(0);
                    out.write("Name = " + name + " Id = " + id + '\n'); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            if (DEBUG) {
                out.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        popScope();
    }

    private void parseCallsite(CommonTree callsite) {

        List<CommonTree> children = callsite.getChildren();
        String name = null;
        String funcName = null;
        long lineNumber = -1;
        long ip = -1;
        String fileName = null;

        for (CommonTree child : children) {
            String left;
            /* this is a regex to find the leading and trailing quotes */
            final String regex = "^\"|\"$"; //$NON-NLS-1$
            /* this is to replace the previous quotes with nothing... effectively deleting them */
            final String nullString = ""; //$NON-NLS-1$
            left = child.getChild(0).getChild(0).getChild(0).getText();
            if (left.equals("name")) { //$NON-NLS-1$
                name = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
            } else if (left.equals("func")) { //$NON-NLS-1$
                funcName = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
            } else if (left.equals("ip")) { //$NON-NLS-1$
                ip = Long.parseLong(child.getChild(1).getChild(0).getChild(0).getText().substring(2), 16); // trim the 0x
            } else if (left.equals("file")) { //$NON-NLS-1$
                fileName = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
            } else if (left.equals("line")) { //$NON-NLS-1$
                lineNumber = Long.parseLong(child.getChild(1).getChild(0).getChild(0).getText());
            }
        }
        trace.addCallsite(name, funcName, ip,fileName, lineNumber);
    }

    private void parseEnvironment(CommonTree environment) {
        List<CommonTree> children = environment.getChildren();
        for (CommonTree child : children) {
            String left;
            String right;
            left = child.getChild(0).getChild(0).getChild(0).getText();
            right = child.getChild(1).getChild(0).getChild(0).getText();
            trace.addEnvironmentVar(left, right);
        }
    }

    private void parseClock(CommonTree clock) {
        List<CommonTree> children = clock.getChildren();
        CTFClock ctfClock = new CTFClock();
        for (CommonTree child : children) {
            final String key = child.getChild(0).getChild(0).getChild(0).getText();
            final CommonTree value = (CommonTree) child.getChild(1).getChild(0).getChild(0);
            final int type = value.getType();
            switch (type) {
            case CTFParser.INTEGER:
            case CTFParser.DECIMAL_LITERAL:
                /*
                 * Not a pretty hack, this is to make sure that there is no
                 * number overflow due to 63 bit integers. The offset should
                 * only really be an issue in the year 2262. the tracer in C/ASM
                 * can write an offset in an unsigned 64 bit long. In java, the
                 * last bit, being set to 1 will be read as a negative number,
                 * but since it is too big a positive it will throw an
                 * exception. this will happen in 2^63 ns from 1970. Therefore
                 * 293 years from 1970
                 */
                Long numValue;
                try {
                    numValue = Long.parseLong(value.getText());
                } catch (Exception e) {
                    numValue = 1330938566783103277L;
                }
                ctfClock.addAttribute(key, numValue);
                break;
            default:
                ctfClock.addAttribute(key, value.getText());
            }

        }
        String nameValue = ctfClock.getName();
        trace.addClock(nameValue, ctfClock);
    }

    private void parseTrace(CommonTree traceNode) throws ParseException {

        List<CommonTree> children = traceNode.getChildren();
        if (children == null) {
            throw new ParseException("Trace block is empty"); //$NON-NLS-1$
        }

        pushScope();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                parseTraceDeclaration(child);
                break;
            default:
                childTypeError(child);
                break;
            }
        }

        /*
         * If trace byte order was not specified and not using packet based
         * metadata
         */
        if (trace.getByteOrder() == null) {
            throw new ParseException("Trace byte order not set"); //$NON-NLS-1$
        }

        popScope();
    }

    private void parseTraceDeclaration(CommonTree traceDecl)
            throws ParseException {

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) traceDecl.getChild(0);
        CommonTree rightNode = (CommonTree) traceDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException(
                    "Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        String left = concatenateUnaryStrings(leftStrings);

        if (left.equals(MetadataStrings.MAJOR)) {
            if (trace.majorIsSet()) {
                throw new ParseException("major is already set"); //$NON-NLS-1$
            }

            trace.setMajor(getMajorOrMinor(rightNode));
        } else if (left.equals(MetadataStrings.MINOR)) {
            if (trace.minorIsSet()) {
                throw new ParseException("minor is already set"); //$NON-NLS-1$
            }

            trace.setMinor(getMajorOrMinor(rightNode));
        } else if (left.equals(MetadataStrings.UUID_STRING)) {
            UUID uuid = getUUID(rightNode);

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
            ByteOrder byteOrder = getByteOrder(rightNode);

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
                final DeclarationScope parentScope = scope.getParentScope();
                String types[] = parentScope.getTypeNames();

                for (String type : types) {
                    IDeclaration d = parentScope.lookupType(type);
                    if (d instanceof IntegerDeclaration) {
                        addByteOrder(byteOrder, parentScope, type, (IntegerDeclaration) d);
                    } else if (d instanceof StructDeclaration) {
                        setAlign(parentScope, (StructDeclaration) d, byteOrder);
                    }
                }
            }
        } else if (left.equals(MetadataStrings.PACKET_HEADER)) {
            if (trace.packetHeaderIsSet()) {
                throw new ParseException("packet.header already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException(
                        "packet.header expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration packetHeaderDecl = parseTypeSpecifierList(
                    typeSpecifier, null);

            if (!(packetHeaderDecl instanceof StructDeclaration)) {
                throw new ParseException("packet.header expects a struct"); //$NON-NLS-1$
            }

            trace.setPacketHeader((StructDeclaration) packetHeaderDecl);
        } else {
            Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownTraceAttributeWarning + " " + left); //$NON-NLS-1$
        }
    }

    private static void addByteOrder(ByteOrder byteOrder,
            final DeclarationScope parentScope, String name,
            IntegerDeclaration decl) throws ParseException {

        if (decl.getByteOrder() == null) {
            IntegerDeclaration newI;
            newI = new IntegerDeclaration(decl.getLength(), decl.isSigned(),
                    decl.getBase(), byteOrder, decl.getEncoding(),
                    decl.getClock(), decl.getAlignment());
            parentScope.replaceType(name, newI);
        }
    }

    private void setAlign(DeclarationScope parentScope, StructDeclaration sd,
            ByteOrder byteOrder) throws ParseException {

        for (String s : sd.getFieldsList()) {
            IDeclaration d = sd.getFields().get(s);

            if (d instanceof StructDeclaration) {
                setAlign(parentScope, (StructDeclaration) d, byteOrder);

            } else if (d instanceof VariantDeclaration) {
                setAlign(parentScope, (VariantDeclaration) d, byteOrder);

            } else if (d instanceof IntegerDeclaration) {
                IntegerDeclaration decl = (IntegerDeclaration) d;
                if (decl.getByteOrder() == null) {
                    IntegerDeclaration newI;
                    newI = new IntegerDeclaration(decl.getLength(),
                            decl.isSigned(), decl.getBase(), byteOrder,
                            decl.getEncoding(), decl.getClock(),
                            decl.getAlignment());
                    sd.getFields().put(s, newI);
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
                newI = new IntegerDeclaration(decl.getLength(),
                        decl.isSigned(), decl.getBase(), byteOrder,
                        decl.getEncoding(), decl.getClock(),
                        decl.getAlignment());
                vd.getFields().put(s, newI);
            }
        }
    }

    private void parseStream(CommonTree streamNode) throws ParseException {

        Stream stream = new Stream(trace);

        List<CommonTree> children = streamNode.getChildren();
        if (children == null) {
            throw new ParseException("Empty stream block"); //$NON-NLS-1$
        }

        pushScope();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                parseStreamDeclaration(child, stream);
                break;
            default:
                childTypeError(child);
                break;
            }
        }

        if (stream.isIdSet()) {
            if (!trace.packetHeaderIsSet()
                    || !trace.getPacketHeader().hasField(MetadataStrings.STREAM_ID)) {
                throw new ParseException(
                        "Stream has an ID, but there is no stream_id field in packet header."); //$NON-NLS-1$
            }
        }

        trace.addStream(stream);

        popScope();
    }

    private void parseStreamDeclaration(CommonTree streamDecl, Stream stream)
            throws ParseException {

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) streamDecl.getChild(0);
        CommonTree rightNode = (CommonTree) streamDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException(
                    "Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        String left = concatenateUnaryStrings(leftStrings);

        if (left.equals(MetadataStrings.ID)) {
            if (stream.isIdSet()) {
                throw new ParseException("stream id already defined"); //$NON-NLS-1$
            }

            long streamID = getStreamID(rightNode);

            stream.setId(streamID);
        } else if (left.equals(MetadataStrings.EVENT_HEADER)) {
            if (stream.isEventHeaderSet()) {
                throw new ParseException("event.header already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException(
                        "event.header expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration eventHeaderDecl = parseTypeSpecifierList(
                    typeSpecifier, null);

            if (!(eventHeaderDecl instanceof StructDeclaration)) {
                throw new ParseException("event.header expects a struct"); //$NON-NLS-1$
            }

            stream.setEventHeader((StructDeclaration) eventHeaderDecl);
        } else if (left.equals(MetadataStrings.EVENT_CONTEXT)) {
            if (stream.isEventContextSet()) {
                throw new ParseException("event.context already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException(
                        "event.context expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration eventContextDecl = parseTypeSpecifierList(
                    typeSpecifier, null);

            if (!(eventContextDecl instanceof StructDeclaration)) {
                throw new ParseException("event.context expects a struct"); //$NON-NLS-1$
            }

            stream.setEventContext((StructDeclaration) eventContextDecl);
        } else if (left.equals(MetadataStrings.PACKET_CONTEXT)) {
            if (stream.isPacketContextSet()) {
                throw new ParseException("packet.context already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException(
                        "packet.context expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration packetContextDecl = parseTypeSpecifierList(
                    typeSpecifier, null);

            if (!(packetContextDecl instanceof StructDeclaration)) {
                throw new ParseException("packet.context expects a struct"); //$NON-NLS-1$
            }

            stream.setPacketContext((StructDeclaration) packetContextDecl);
        } else {
            Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownStreamAttributeWarning + " " + left); //$NON-NLS-1$
        }
    }

    private void parseEvent(CommonTree eventNode) throws ParseException {

        List<CommonTree> children = eventNode.getChildren();
        if (children == null) {
            throw new ParseException("Empty event block"); //$NON-NLS-1$
        }

        EventDeclaration event = new EventDeclaration();

        pushScope();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                parseEventDeclaration(child, event);
                break;
            default:
                childTypeError(child);
                break;
            }
        }

        if (!event.nameIsSet()) {
            throw new ParseException("Event name not set"); //$NON-NLS-1$
        }

        /*
         * If the event did not specify a stream, then the trace must be single
         * stream
         */
        if (!event.streamIsSet()) {
            if (trace.nbStreams() > 1) {
                throw new ParseException(
                        "Event without stream_id with more than one stream"); //$NON-NLS-1$
            }

            /*
             * If the event did not specify a stream, the only existing stream
             * must not have an id. Note: That behavior could be changed, it
             * could be possible to just get the only existing stream, whatever
             * is its id.
             */
            Stream stream = trace.getStream(null);

            if (stream != null) {
                event.setStream(stream);
            } else {
                throw new ParseException(
                        "Event without stream_id, but there is no stream without id"); //$NON-NLS-1$
            }
        }

        /*
         * Add the event to the stream.
         */
        event.getStream().addEvent(event);

        popScope();
    }

    private void parseEventDeclaration(CommonTree eventDecl,
            EventDeclaration event) throws ParseException {

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) eventDecl.getChild(0);
        CommonTree rightNode = (CommonTree) eventDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException(
                    "Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        String left = concatenateUnaryStrings(leftStrings);

        if (left.equals(MetadataStrings.NAME2)) {
            if (event.nameIsSet()) {
                throw new ParseException("name already defined"); //$NON-NLS-1$
            }

            String name = getEventName(rightNode);

            event.setName(name);
        } else if (left.equals(MetadataStrings.ID)) {
            if (event.idIsSet()) {
                throw new ParseException("id already defined"); //$NON-NLS-1$
            }

            long id = getEventID(rightNode);

            event.setId(id);
        } else if (left.equals(MetadataStrings.STREAM_ID)) {
            if (event.streamIsSet()) {
                throw new ParseException("stream id already defined"); //$NON-NLS-1$
            }

            long streamId = getStreamID(rightNode);

            Stream stream = trace.getStream(streamId);

            if (stream == null) {
                throw new ParseException("Stream " + streamId + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            event.setStream(stream);
        } else if (left.equals(MetadataStrings.CONTEXT)) {
            if (event.contextIsSet()) {
                throw new ParseException("context already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("context expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration contextDecl = parseTypeSpecifierList(typeSpecifier,
                    null);

            if (!(contextDecl instanceof StructDeclaration)) {
                throw new ParseException("context expects a struct"); //$NON-NLS-1$
            }

            event.setContext((StructDeclaration) contextDecl);
        } else if (left.equals(MetadataStrings.FIELDS_STRING)) {
            if (event.fieldsIsSet()) {
                throw new ParseException("fields already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("fields expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration fieldsDecl;
            fieldsDecl = parseTypeSpecifierList(typeSpecifier, null);

            if (!(fieldsDecl instanceof StructDeclaration)) {
                throw new ParseException("fields expects a struct"); //$NON-NLS-1$
            }
            /*
             * The underscores in the event names. These underscores were added
             * by the LTTng tracer.
             */
            final StructDeclaration fields = (StructDeclaration) fieldsDecl;
            event.setFields(fields);
        } else if (left.equals(MetadataStrings.LOGLEVEL2)) {
            long logLevel = parseUnaryInteger((CommonTree) rightNode.getChild(0));
            event.setLogLevel(logLevel);
        } else {
            /* Custom event attribute, we'll add it to the attributes map */
            String right = parseUnaryString((CommonTree) rightNode.getChild(0));
            event.setCustomAttribute(left, right);
        }
    }

    /**
     * Parses a declaration at the root level.
     *
     * @param declaration
     *            The declaration subtree.
     * @throws ParseException
     */
    private void parseRootDeclaration(CommonTree declaration)
            throws ParseException {

        List<CommonTree> children = declaration.getChildren();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPE_SPECIFIER_LIST:
                parseTypeSpecifierList(child, null);
                break;
            default:
                childTypeError(child);
            }
        }
    }

    /**
     * Parses a typealias node. It parses the target, the alias, and registers
     * the type in the current scope.
     *
     * @param typealias
     *            A TYPEALIAS node.
     * @throws ParseException
     */
    private void parseTypealias(CommonTree typealias) throws ParseException {

        List<CommonTree> children = typealias.getChildren();

        CommonTree target = null;
        CommonTree alias = null;

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS_TARGET:
                target = child;
                break;
            case CTFParser.TYPEALIAS_ALIAS:
                alias = child;
                break;
            default:
                childTypeError(child);
                break;
            }
        }


        IDeclaration targetDeclaration = parseTypealiasTarget(target);

        if ((targetDeclaration instanceof VariantDeclaration)
                && ((VariantDeclaration) targetDeclaration).isTagged()) {
            throw new ParseException("Typealias of untagged variant is not permitted"); //$NON-NLS-1$
        }

        String aliasString = parseTypealiasAlias(alias);

        getCurrentScope().registerType(aliasString, targetDeclaration);
    }

    /**
     * Parses the target part of a typealias and gets the corresponding
     * declaration.
     *
     * @param target
     *            A TYPEALIAS_TARGET node.
     * @return The corresponding declaration.
     * @throws ParseException
     */
    private IDeclaration parseTypealiasTarget(CommonTree target)
            throws ParseException {

        List<CommonTree> children = target.getChildren();

        CommonTree typeSpecifierList = null;
        CommonTree typeDeclaratorList = null;
        CommonTree typeDeclarator = null;
        StringBuilder identifierSB = new StringBuilder();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPE_SPECIFIER_LIST:
                typeSpecifierList = child;
                break;
            case CTFParser.TYPE_DECLARATOR_LIST:
                typeDeclaratorList = child;
                break;
            default:
                childTypeError(child);
                break;
            }
        }


        if (typeDeclaratorList != null) {
            /*
             * Only allow one declarator
             *
             * eg: "typealias uint8_t *, ** := puint8_t;" is not permitted,
             * otherwise the new type puint8_t would maps to two different
             * types.
             */
            if (typeDeclaratorList.getChildCount() != 1) {
                throw new ParseException(
                        "Only one type declarator is allowed in the typealias target"); //$NON-NLS-1$
            }

            typeDeclarator = (CommonTree) typeDeclaratorList.getChild(0);
        }

        /* Parse the target type and get the declaration */
        IDeclaration targetDeclaration = parseTypeDeclarator(typeDeclarator,
                typeSpecifierList, identifierSB);

        /*
         * We don't allow identifier in the target
         *
         * eg: "typealias uint8_t* hello := puint8_t;", the "hello" is not
         * permitted
         */
        if (identifierSB.length() > 0) {
            throw new ParseException("Identifier (" + identifierSB.toString() //$NON-NLS-1$
                    + ") not expected in the typealias target"); //$NON-NLS-1$
        }

        return targetDeclaration;
    }

    /**
     * Parses the alias part of a typealias. It parses the underlying specifier
     * list and declarator and creates the string representation that will be
     * used to register the type.
     *
     * @param alias
     *            A TYPEALIAS_ALIAS node.
     * @return The string representation of the alias.
     * @throws ParseException
     */
    private static String parseTypealiasAlias(CommonTree alias)
            throws ParseException {

        List<CommonTree> children = alias.getChildren();

        CommonTree typeSpecifierList = null;
        CommonTree typeDeclaratorList = null;
        CommonTree typeDeclarator = null;
        List<CommonTree> pointers = new LinkedList<CommonTree>();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPE_SPECIFIER_LIST:
                typeSpecifierList = child;
                break;
            case CTFParser.TYPE_DECLARATOR_LIST:
                typeDeclaratorList = child;
                break;
            default:
                childTypeError(child);
                break;
            }
        }

        /* If there is a type declarator list, extract the pointers */
        if (typeDeclaratorList != null) {
            /*
             * Only allow one declarator
             *
             * eg: "typealias uint8_t := puint8_t *, **;" is not permitted.
             */
            if (typeDeclaratorList.getChildCount() != 1) {
                throw new ParseException(
                        "Only one type declarator is allowed in the typealias alias"); //$NON-NLS-1$
            }

            typeDeclarator = (CommonTree) typeDeclaratorList.getChild(0);

            List<CommonTree> typeDeclaratorChildren = typeDeclarator.getChildren();

            for (CommonTree child : typeDeclaratorChildren) {
                switch (child.getType()) {
                case CTFParser.POINTER:
                    pointers.add(child);
                    break;
                case CTFParser.IDENTIFIER:
                    throw new ParseException("Identifier (" + child.getText() //$NON-NLS-1$
                            + ") not expected in the typealias target"); //$NON-NLS-1$
                default:
                    childTypeError(child);
                    break;
                }
            }
        }

        return createTypeDeclarationString(typeSpecifierList, pointers);
    }

    /**
     * Parses a typedef node. This creates and registers a new declaration for
     * each declarator found in the typedef.
     *
     * @param typedef
     *            A TYPEDEF node.
     * @throws ParseException
     *             If there is an error creating the declaration.
     */
    private void parseTypedef(CommonTree typedef) throws ParseException {

        CommonTree typeDeclaratorListNode = (CommonTree) typedef.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);

        CommonTree typeSpecifierListNode = (CommonTree) typedef.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);

        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {
            StringBuilder identifierSB = new StringBuilder();

            IDeclaration typeDeclaration = parseTypeDeclarator(
                    typeDeclaratorNode, typeSpecifierListNode, identifierSB);

            if ((typeDeclaration instanceof VariantDeclaration)
                && ((VariantDeclaration) typeDeclaration).isTagged()) {
                throw new ParseException(
                        "Typealias of untagged variant is not permitted"); //$NON-NLS-1$
            }

            getCurrentScope().registerType(identifierSB.toString(),
                    typeDeclaration);
        }
    }

    /**
     * Parses a pair type declarator / type specifier list and returns the
     * corresponding declaration. If it is present, it also writes the
     * identifier of the declarator in the given {@link StringBuilder}.
     *
     * @param typeDeclarator
     *            A TYPE_DECLARATOR node.
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param identifierSB
     *            A StringBuilder that will receive the identifier found in the
     *            declarator.
     * @return The corresponding declaration.
     * @throws ParseException
     *             If there is an error finding or creating the declaration.
     */
    private IDeclaration parseTypeDeclarator(CommonTree typeDeclarator,
            CommonTree typeSpecifierList, StringBuilder identifierSB)
            throws ParseException {

        IDeclaration declaration = null;
        List<CommonTree> children = null;
        List<CommonTree> pointers = new LinkedList<CommonTree>();
        List<CommonTree> lengths = new LinkedList<CommonTree>();
        CommonTree identifier = null;

        /* Separate the tokens by type */
        if (typeDeclarator != null) {
            children = typeDeclarator.getChildren();
            for (CommonTree child : children) {

                switch (child.getType()) {
                case CTFParser.POINTER:
                    pointers.add(child);
                    break;
                case CTFParser.IDENTIFIER:
                    identifier = child;
                    break;
                case CTFParser.LENGTH:
                    lengths.add(child);
                    break;
                default:
                    childTypeError(child);
                    break;
                }
            }

        }

        /*
         * Parse the type specifier list, which is the "base" type. For example,
         * it would be int in int a[3][len].
         */
        declaration = parseTypeSpecifierList(typeSpecifierList, pointers);

        /*
         * Each length subscript means that we must create a nested array or
         * sequence. For example, int a[3][len] means that we have an array of 3
         * (sequences of length 'len' of (int)).
         */
        if (lengths.size() > 0) {
            /* We begin at the end */
            Collections.reverse(lengths);

            for (CommonTree length : lengths) {
                /*
                 * By looking at the first expression, we can determine whether
                 * it is an array or a sequence.
                 */
                List<CommonTree> lengthChildren = length.getChildren();

                CommonTree first = lengthChildren.get(0);
                if (isUnaryInteger(first)) {
                    /* Array */
                    int arrayLength = (int) parseUnaryInteger(first);

                    if (arrayLength < 1) {
                        throw new ParseException("Array length is negative"); //$NON-NLS-1$
                    }

                    /* Create the array declaration. */
                    declaration = new ArrayDeclaration(arrayLength, declaration);
                } else if (isAnyUnaryString(first)) {
                    /* Sequence */
                    String lengthName = concatenateUnaryStrings(lengthChildren);

                    /* Create the sequence declaration. */
                    declaration = new SequenceDeclaration(lengthName,
                            declaration);
                } else {
                    childTypeError(first);
                }
            }
        }

        if (identifier != null) {
            identifierSB.append(identifier.getText());
        }

        return declaration;
    }

    /**
     * Parses a type specifier list and returns the corresponding declaration.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param pointerList
     *            A list of POINTER nodes that apply to the specified type.
     * @return The corresponding declaration.
     * @throws ParseException
     *             If the type has not been defined or if there is an error
     *             creating the declaration.
     */
    private IDeclaration parseTypeSpecifierList(CommonTree typeSpecifierList,
            List<CommonTree> pointerList) throws ParseException {
        IDeclaration declaration = null;

        /*
         * By looking at the first element of the type specifier list, we can
         * determine which type it belongs to.
         */
        CommonTree firstChild = (CommonTree) typeSpecifierList.getChild(0);

        switch (firstChild.getType()) {
        case CTFParser.FLOATING_POINT:
            declaration = parseFloat(firstChild);
            break;
        case CTFParser.INTEGER:
            declaration = parseInteger(firstChild);
            break;
        case CTFParser.STRING:
            declaration = parseString(firstChild);
            break;
        case CTFParser.STRUCT:
            declaration = parseStruct(firstChild);
            break;
        case CTFParser.VARIANT:
            declaration = parseVariant(firstChild);
            break;
        case CTFParser.ENUM:
            declaration = parseEnum(firstChild);
            break;
        case CTFParser.IDENTIFIER:
        case CTFParser.FLOATTOK:
        case CTFParser.INTTOK:
        case CTFParser.LONGTOK:
        case CTFParser.SHORTTOK:
        case CTFParser.SIGNEDTOK:
        case CTFParser.UNSIGNEDTOK:
        case CTFParser.CHARTOK:
        case CTFParser.DOUBLETOK:
        case CTFParser.VOIDTOK:
        case CTFParser.BOOLTOK:
        case CTFParser.COMPLEXTOK:
        case CTFParser.IMAGINARYTOK:
            declaration = parseTypeDeclaration(typeSpecifierList, pointerList);
            break;
        default:
            childTypeError(firstChild);
        }

        return declaration;
    }

    private IDeclaration parseFloat(CommonTree floatingPoint)
            throws ParseException {

        List<CommonTree> children = floatingPoint.getChildren();

        /*
         * If the integer has no attributes, then it is missing the size
         * attribute which is required
         */
        if (children == null) {
            throw new ParseException("float: missing size attribute"); //$NON-NLS-1$
        }

        /* The return value */
        FloatDeclaration floatDeclaration = null;
        ByteOrder byteOrder = trace.getByteOrder();
        long alignment = 0;
        int exponent = 8;
        int mantissa = 24;

        /* Iterate on all integer children */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.CTF_EXPRESSION_VAL:
                /*
                 * An assignment expression must have 2 children, left and right
                 */

                CommonTree leftNode = (CommonTree) child.getChild(0);
                CommonTree rightNode = (CommonTree) child.getChild(1);

                List<CommonTree> leftStrings = leftNode.getChildren();

                if (!isAnyUnaryString(leftStrings.get(0))) {
                    throw new ParseException(
                            "Left side of ctf expression must be a string"); //$NON-NLS-1$
                }
                String left = concatenateUnaryStrings(leftStrings);

                if (left.equals(MetadataStrings.EXP_DIG)) {
                    exponent = (int) parseUnaryInteger((CommonTree) rightNode.getChild(0));
                } else if (left.equals(MetadataStrings.BYTE_ORDER)) {
                    byteOrder = getByteOrder(rightNode);
                } else if (left.equals(MetadataStrings.MANT_DIG)) {
                    mantissa = (int) parseUnaryInteger((CommonTree) rightNode.getChild(0));
                } else if (left.equals(MetadataStrings.ALIGN)) {
                    alignment = getAlignment(rightNode);
                } else {
                    throw new ParseException("Float: unknown attribute " + left); //$NON-NLS-1$
                }

                break;
            default:
                childTypeError(child);
                break;
            }
        }
        int size = mantissa + exponent;
        if (size == 0) {
            throw new ParseException("Float missing size attribute"); //$NON-NLS-1$
        }

        if (alignment == 0) {
            if ((size % 8) == 0) {
                alignment = 1;
            } else {
                alignment = 8;
            }
        }

        floatDeclaration = new FloatDeclaration(exponent, mantissa, byteOrder, alignment);

        return floatDeclaration;

    }

    /**
     * Parses a type specifier list as a user-declared type.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node containing a user-declared type.
     * @param pointerList
     *            A list of POINTER nodes that apply to the type specified in
     *            typeSpecifierList.
     * @return The corresponding declaration.
     * @throws ParseException
     *             If the type does not exist (has not been found).
     */
    private IDeclaration parseTypeDeclaration(CommonTree typeSpecifierList,
            List<CommonTree> pointerList) throws ParseException {
        /* Create the string representation of the type declaration */
        String typeStringRepresentation = createTypeDeclarationString(
                typeSpecifierList, pointerList);

        /* Use the string representation to search the type in the current scope */
        IDeclaration decl = getCurrentScope().rlookupType(
                typeStringRepresentation);

        if (decl == null) {
            throw new ParseException("Type " + typeStringRepresentation //$NON-NLS-1$
                    + " has not been defined."); //$NON-NLS-1$
        }

        return decl;
    }

    /**
     * Parses an integer declaration node.
     *
     * @param integer
     *            An INTEGER node.
     * @return The corresponding integer declaration.
     * @throws ParseException
     */
    private IntegerDeclaration parseInteger(CommonTree integer)
            throws ParseException {

        List<CommonTree> children = integer.getChildren();

        /*
         * If the integer has no attributes, then it is missing the size
         * attribute which is required
         */
        if (children == null) {
            throw new ParseException("integer: missing size attribute"); //$NON-NLS-1$
        }

        /* The return value */
        IntegerDeclaration integerDeclaration = null;
        boolean signed = false;
        ByteOrder byteOrder = trace.getByteOrder();
        long size = 0;
        long alignment = 0;
        int base = 10;
        String clock = null;

        Encoding encoding = Encoding.NONE;

        /* Iterate on all integer children */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.CTF_EXPRESSION_VAL:
                /*
                 * An assignment expression must have 2 children, left and right
                 */

                CommonTree leftNode = (CommonTree) child.getChild(0);
                CommonTree rightNode = (CommonTree) child.getChild(1);

                List<CommonTree> leftStrings = leftNode.getChildren();

                if (!isAnyUnaryString(leftStrings.get(0))) {
                    throw new ParseException(
                            "Left side of ctf expression must be a string"); //$NON-NLS-1$
                }
                String left = concatenateUnaryStrings(leftStrings);

                if (left.equals("signed")) { //$NON-NLS-1$
                    signed = getSigned(rightNode);
                } else if (left.equals(MetadataStrings.BYTE_ORDER)) {
                    byteOrder = getByteOrder(rightNode);
                } else if (left.equals("size")) { //$NON-NLS-1$
                    size = getSize(rightNode);
                } else if (left.equals(MetadataStrings.ALIGN)) {
                    alignment = getAlignment(rightNode);
                } else if (left.equals("base")) { //$NON-NLS-1$
                    base = getBase(rightNode);
                } else if (left.equals("encoding")) { //$NON-NLS-1$
                    encoding = getEncoding(rightNode);
                } else if (left.equals("map")) { //$NON-NLS-1$
                    clock = getClock(rightNode);
                } else {
                    Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownIntegerAttributeWarning + " " + left); //$NON-NLS-1$
                }

                break;
            default:
                childTypeError(child);
                break;
            }
        }

        if (size == 0) {
            throw new ParseException("Integer missing size attribute"); //$NON-NLS-1$
        }

        if (alignment == 0) {
            if ((size % 8) == 0) {
                alignment = 1;
            } else {
                alignment = 8;
            }
        }

        integerDeclaration = new IntegerDeclaration((int) size, signed, base,
                byteOrder, encoding, clock, alignment);

        return integerDeclaration;
    }

    private static String getClock(CommonTree rightNode) {
        return rightNode.getChild(1).getChild(0).getChild(0).getText();
    }

    private static StringDeclaration parseString(CommonTree string)
            throws ParseException {

        List<CommonTree> children = string.getChildren();
        StringDeclaration stringDeclaration = null;

        if (children == null) {
            stringDeclaration = new StringDeclaration();
        } else {
            Encoding encoding = Encoding.UTF8;
            for (CommonTree child : children) {
                switch (child.getType()) {
                case CTFParser.CTF_EXPRESSION_VAL:
                    /*
                     * An assignment expression must have 2 children, left and
                     * right
                     */

                    CommonTree leftNode = (CommonTree) child.getChild(0);
                    CommonTree rightNode = (CommonTree) child.getChild(1);

                    List<CommonTree> leftStrings = leftNode.getChildren();

                    if (!isAnyUnaryString(leftStrings.get(0))) {
                        throw new ParseException(
                                "Left side of ctf expression must be a string"); //$NON-NLS-1$
                    }
                    String left = concatenateUnaryStrings(leftStrings);

                    if (left.equals("encoding")) { //$NON-NLS-1$
                        encoding = getEncoding(rightNode);
                    } else {
                        throw new ParseException("String: unknown attribute " //$NON-NLS-1$
                                + left);
                    }

                    break;
                default:
                    childTypeError(child);
                    break;
                }
            }

            stringDeclaration = new StringDeclaration(encoding);
        }

        return stringDeclaration;
    }

    /**
     * Parses a struct declaration and returns the corresponding declaration.
     *
     * @param struct
     *            An STRUCT node.
     * @return The corresponding struct declaration.
     * @throws ParseException
     */
    private StructDeclaration parseStruct(CommonTree struct)
            throws ParseException {

        List<CommonTree> children = struct.getChildren();

        /* The return value */
        StructDeclaration structDeclaration = null;

        /* Name */
        String structName = null;
        boolean hasName = false;

        /* Body */
        CommonTree structBody = null;
        boolean hasBody = false;

        /* Align */
        long structAlign = 0;

        /* Loop on all children and identify what we have to work with. */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.STRUCT_NAME: {
                hasName = true;

                CommonTree structNameIdentifier = (CommonTree) child.getChild(0);

                structName = structNameIdentifier.getText();

                break;
            }
            case CTFParser.STRUCT_BODY: {
                hasBody = true;

                structBody = child;

                break;
            }
            case CTFParser.ALIGN: {
                CommonTree structAlignExpression = (CommonTree) child.getChild(0);

                structAlign = getAlignment(structAlignExpression);

                break;
            }
            default:
                childTypeError(child);

                break;
            }
        }

        /*
         * If a struct has just a body and no name (just like the song,
         * "A Struct With No Name" by America (sorry for that...)), it's a
         * definition of a new type, so we create the type declaration and
         * return it. We can't add it to the declaration scope since there is no
         * name, but that's what we want because it won't be possible to use it
         * again to declare another field.
         *
         * If it has just a name, we look it up in the declaration scope and
         * return the associated declaration. If it is not found in the
         * declaration scope, it means that a struct with that name has not been
         * declared, which is an error.
         *
         * If it has both, then we create the type declaration and register it
         * to the current scope.
         *
         * If it has none, then what are we doing here ?
         */
        if (hasBody) {
            /*
             * If struct has a name, check if already defined in the current
             * scope.
             */
            if (hasName && (getCurrentScope().lookupStruct(structName) != null)) {
                throw new ParseException("struct " + structName //$NON-NLS-1$
                        + " already defined."); //$NON-NLS-1$
            }
            /* Create the declaration */
            structDeclaration = new StructDeclaration(structAlign);

            /* Parse the body */
            parseStructBody(structBody, structDeclaration);

            /* If struct has name, add it to the current scope. */
            if (hasName) {
                getCurrentScope().registerStruct(structName, structDeclaration);
            }
        } else /* !hasBody */{
            if (hasName) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                structDeclaration = getCurrentScope().rlookupStruct(structName);

                /*
                 * If not found, it means that a struct with such name has not
                 * been defined
                 */
                if (structDeclaration == null) {
                    throw new ParseException("struct " + structName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */

                /* We can't do anything with that. */
                throw new ParseException("struct with no name and no body"); //$NON-NLS-1$
            }
        }

        return structDeclaration;
    }

    /**
     * Parses a struct body, adding the fields to specified structure
     * declaration.
     *
     * @param structBody
     *            A STRUCT_BODY node.
     * @param structDeclaration
     *            The struct declaration.
     * @throws ParseException
     */
    private void parseStructBody(CommonTree structBody,
            StructDeclaration structDeclaration) throws ParseException {

        List<CommonTree> structDeclarations = structBody.getChildren();

        /*
         * If structDeclaration is null, structBody has no children and the
         * struct body is empty.
         */
        if (structDeclarations != null) {
            pushScope();

            for (CommonTree declarationNode : structDeclarations) {
                switch (declarationNode.getType()) {
                case CTFParser.TYPEALIAS:
                    parseTypealias(declarationNode);
                    break;
                case CTFParser.TYPEDEF:
                    parseTypedef(declarationNode);
                    break;
                case CTFParser.SV_DECLARATION:
                    parseStructDeclaration(declarationNode, structDeclaration);
                    break;
                default:
                    childTypeError(declarationNode);
                    break;
                }
            }
            popScope();
        }
    }

    /**
     * Parses a declaration found in a struct.
     *
     * @param declaration
     *            A SV_DECLARATION node.
     * @param struct
     *            A struct declaration. (I know, little name clash here...)
     * @throws ParseException
     */
    private void parseStructDeclaration(CommonTree declaration,
            StructDeclaration struct) throws ParseException {


        /* Get the type specifier list node */
        CommonTree typeSpecifierListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);

        /* Get the type declarator list node */
        CommonTree typeDeclaratorListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);

        /* Get the type declarator list */
        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        /*
         * For each type declarator, parse the declaration and add a field to
         * the struct
         */
        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {

            StringBuilder identifierSB = new StringBuilder();

            IDeclaration decl = parseTypeDeclarator(typeDeclaratorNode,
                    typeSpecifierListNode, identifierSB);
            String fieldName = identifierSB.toString();

            if (struct.hasField(fieldName)) {
                throw new ParseException("struct: duplicate field " //$NON-NLS-1$
                        + fieldName);
            }

            struct.addField(fieldName, decl);

        }
    }

    /**
     * Parses an enum declaration and returns the corresponding declaration.
     *
     * @param theEnum
     *            An ENUM node.
     * @return The corresponding enum declaration.
     * @throws ParseException
     */
    private EnumDeclaration parseEnum(CommonTree theEnum) throws ParseException {

        List<CommonTree> children = theEnum.getChildren();

        /* The return value */
        EnumDeclaration enumDeclaration = null;

        /* Name */
        String enumName = null;

        /* Body */
        CommonTree enumBody = null;

        /* Container type */
        IntegerDeclaration containerTypeDeclaration = null;

        /* Loop on all children and identify what we have to work with. */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.ENUM_NAME: {
                CommonTree enumNameIdentifier = (CommonTree) child.getChild(0);
                enumName = enumNameIdentifier.getText();
                break;
            }
            case CTFParser.ENUM_BODY: {
                enumBody = child;
                break;
            }
            case CTFParser.ENUM_CONTAINER_TYPE: {
                containerTypeDeclaration = parseEnumContainerType(child);
                break;
            }
            default:
                childTypeError(child);
                break;
            }
        }

        /*
         * If the container type has not been defined explicitly, we assume it
         * is "int".
         */
        if (containerTypeDeclaration == null) {
            IDeclaration enumDecl;
            /*
             * it could be because the enum was already declared.
             */
            if (enumName != null) {
                enumDecl = getCurrentScope().rlookupEnum(enumName);
                if (enumDecl != null) {
                    return (EnumDeclaration) enumDecl;
                }
            }

            IDeclaration decl = getCurrentScope().rlookupType("int"); //$NON-NLS-1$

            if (decl == null) {
                throw new ParseException(
                        "enum container type implicit and type int not defined"); //$NON-NLS-1$
            } else if (!(decl instanceof IntegerDeclaration)) {
                throw new ParseException(
                        "enum container type implicit and type int not an integer"); //$NON-NLS-1$
            }

            containerTypeDeclaration = (IntegerDeclaration) decl;
        }

        /*
         * If it has a body, it's a new declaration, otherwise it's a reference
         * to an existing declaration. Same logic as struct.
         */
        if (enumBody != null) {
            /*
             * If enum has a name, check if already defined in the current
             * scope.
             */
            if ((enumName != null)
                    && (getCurrentScope().lookupEnum(enumName) != null)) {
                throw new ParseException("enum " + enumName //$NON-NLS-1$
                        + " already defined"); //$NON-NLS-1$
            }

            /* Create the declaration */
            enumDeclaration = new EnumDeclaration(containerTypeDeclaration);

            /* Parse the body */
            parseEnumBody(enumBody, enumDeclaration);

            /* If the enum has name, add it to the current scope. */
            if (enumName != null) {
                getCurrentScope().registerEnum(enumName, enumDeclaration);
            }
        } else {
            if (enumName != null) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                enumDeclaration = getCurrentScope().rlookupEnum(enumName);

                /*
                 * If not found, it means that an enum with such name has not
                 * been defined
                 */
                if (enumDeclaration == null) {
                    throw new ParseException("enum " + enumName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */
                throw new ParseException("enum with no name and no body"); //$NON-NLS-1$
            }
        }

        return enumDeclaration;

    }

    /**
     * Parses an enum body, adding the enumerators to the specified enum
     * declaration.
     *
     * @param enumBody
     *            An ENUM_BODY node.
     * @param enumDeclaration
     *            The enum declaration.
     * @throws ParseException
     */
    private void parseEnumBody(CommonTree enumBody,
            EnumDeclaration enumDeclaration) throws ParseException {

        List<CommonTree> enumerators = enumBody.getChildren();
        /* enum body can't be empty (unlike struct). */

        pushScope();

        /*
         * Start at -1, so that if the first enumrator has no explicit value, it
         * will choose 0
         */
        long lastHigh = -1;

        for (CommonTree enumerator : enumerators) {
            lastHigh = parseEnumEnumerator(enumerator, enumDeclaration,
                    lastHigh);
        }

        popScope();

    }

    /**
     * Parses an enumerator node and adds an enumerator declaration to an
     * enumeration declaration.
     *
     * The high value of the range of the last enumerator is needed in case the
     * current enumerator does not specify its value.
     *
     * @param enumerator
     *            An ENUM_ENUMERATOR node.
     * @param enumDeclaration
     *            en enumeration declaration to which will be added the
     *            enumerator.
     * @param lastHigh
     *            The high value of the range of the last enumerator
     * @return The high value of the value range of the current enumerator.
     * @throws ParseException
     */
    private static long parseEnumEnumerator(CommonTree enumerator,
            EnumDeclaration enumDeclaration, long lastHigh)
            throws ParseException {

        List<CommonTree> children = enumerator.getChildren();

        long low = 0, high = 0;
        boolean valueSpecified = false;
        String label = null;

        for (CommonTree child : children) {
            if (isAnyUnaryString(child)) {
                label = parseUnaryString(child);
            } else if (child.getType() == CTFParser.ENUM_VALUE) {

                valueSpecified = true;

                low = parseUnaryInteger((CommonTree) child.getChild(0));
                high = low;
            } else if (child.getType() == CTFParser.ENUM_VALUE_RANGE) {

                valueSpecified = true;

                low = parseUnaryInteger((CommonTree) child.getChild(0));
                high = parseUnaryInteger((CommonTree) child.getChild(1));
            } else {
                childTypeError(child);
            }
        }

        if (!valueSpecified) {
            low = lastHigh + 1;
            high = low;
        }

        if (low > high) {
            throw new ParseException("enum low value greater than high value"); //$NON-NLS-1$
        }

        if (!enumDeclaration.add(low, high, label)) {
            throw new ParseException("enum declarator values overlap."); //$NON-NLS-1$
        }

        if (valueSpecified && (BigInteger.valueOf(low).compareTo(enumDeclaration.getContainerType().getMinValue()) == -1 ||
                BigInteger.valueOf(high).compareTo(enumDeclaration.getContainerType().getMaxValue()) == 1)) {
            throw new ParseException("enum value is not in range"); //$NON-NLS-1$
        }

        return high;
    }

    /**
     * Parses an enum container type node and returns the corresponding integer
     * type.
     *
     * @param enumContainerType
     *            An ENUM_CONTAINER_TYPE node.
     * @return An integer declaration corresponding to the container type.
     * @throws ParseException
     *             If the type does not parse correctly or if it is not an
     *             integer type.
     */
    private IntegerDeclaration parseEnumContainerType(
            CommonTree enumContainerType) throws ParseException {

        /* Get the child, which should be a type specifier list */
        CommonTree typeSpecifierList = (CommonTree) enumContainerType.getChild(0);

        /* Parse it and get the corresponding declaration */
        IDeclaration decl = parseTypeSpecifierList(typeSpecifierList, null);

        /* If is is an integer, return it, else throw an error */
        if (decl instanceof IntegerDeclaration) {
            return (IntegerDeclaration) decl;
        }
        throw new ParseException("enum container type must be an integer"); //$NON-NLS-1$
    }

    private VariantDeclaration parseVariant(CommonTree variant)
            throws ParseException {

        List<CommonTree> children = variant.getChildren();
        VariantDeclaration variantDeclaration = null;

        boolean hasName = false;
        String variantName = null;

        boolean hasBody = false;
        CommonTree variantBody = null;

        boolean hasTag = false;
        String variantTag = null;

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.VARIANT_NAME:

                hasName = true;

                CommonTree variantNameIdentifier = (CommonTree) child.getChild(0);

                variantName = variantNameIdentifier.getText();

                break;
            case CTFParser.VARIANT_TAG:

                hasTag = true;

                CommonTree variantTagIdentifier = (CommonTree) child.getChild(0);

                variantTag = variantTagIdentifier.getText();

                break;
            case CTFParser.VARIANT_BODY:

                hasBody = true;

                variantBody = child;

                break;
            default:
                childTypeError(child);
                break;
            }
        }

        if (hasBody) {
            /*
             * If variant has a name, check if already defined in the current
             * scope.
             */
            if (hasName
                    && (getCurrentScope().lookupVariant(variantName) != null)) {
                throw new ParseException("variant " + variantName //$NON-NLS-1$
                        + " already defined."); //$NON-NLS-1$
            }

            /* Create the declaration */
            variantDeclaration = new VariantDeclaration();

            /* Parse the body */
            parseVariantBody(variantBody, variantDeclaration);

            /* If variant has name, add it to the current scope. */
            if (hasName) {
                getCurrentScope().registerVariant(variantName,
                        variantDeclaration);
            }
        } else /* !hasBody */{
            if (hasName) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                variantDeclaration = getCurrentScope().rlookupVariant(
                        variantName);

                /*
                 * If not found, it means that a struct with such name has not
                 * been defined
                 */
                if (variantDeclaration == null) {
                    throw new ParseException("variant " + variantName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */

                /* We can't do anything with that. */
                throw new ParseException("variant with no name and no body"); //$NON-NLS-1$
            }
        }

        if (hasTag) {
            variantDeclaration.setTag(variantTag);
        }

        return variantDeclaration;
    }

    private void parseVariantBody(CommonTree variantBody,
            VariantDeclaration variantDeclaration) throws ParseException {

        List<CommonTree> variantDeclarations = variantBody.getChildren();

        pushScope();

        for (CommonTree declarationNode : variantDeclarations) {
            switch (declarationNode.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(declarationNode);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(declarationNode);
                break;
            case CTFParser.SV_DECLARATION:
                parseVariantDeclaration(declarationNode, variantDeclaration);
                break;
            default:
                childTypeError(declarationNode);
                break;
            }
        }

        popScope();
    }

    private void parseVariantDeclaration(CommonTree declaration,
            VariantDeclaration variant) throws ParseException {

        /* Get the type specifier list node */
        CommonTree typeSpecifierListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);

        /* Get the type declarator list node */
        CommonTree typeDeclaratorListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);

        /* Get the type declarator list */
        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        /*
         * For each type declarator, parse the declaration and add a field to
         * the variant
         */
        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {

            StringBuilder identifierSB = new StringBuilder();

            IDeclaration decl = parseTypeDeclarator(typeDeclaratorNode,
                    typeSpecifierListNode, identifierSB);

            if (variant.hasField(identifierSB.toString())) {
                throw new ParseException("variant: duplicate field " //$NON-NLS-1$
                        + identifierSB.toString());
            }

            variant.addField(identifierSB.toString(), decl);
        }
    }

    /**
     * Creates the string representation of a type declaration (type specifier
     * list + pointers).
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param pointers
     *            A list of POINTER nodes.
     * @return The string representation.
     * @throws ParseException
     */
    private static String createTypeDeclarationString(
            CommonTree typeSpecifierList, List<CommonTree> pointers)
            throws ParseException {
        StringBuilder sb = new StringBuilder();

        createTypeSpecifierListString(typeSpecifierList, sb);
        createPointerListString(pointers, sb);

        return sb.toString();
    }

    /**
     * Creates the string representation of a list of type specifiers.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param sb
     *            A StringBuilder to which will be appended the string.
     * @throws ParseException
     */
    private static void createTypeSpecifierListString(
            CommonTree typeSpecifierList, StringBuilder sb)
            throws ParseException {

        List<CommonTree> children = typeSpecifierList.getChildren();

        boolean firstItem = true;

        for (CommonTree child : children) {
            if (!firstItem) {
                sb.append(' ');

            }

            firstItem = false;

            /* Append the string that represents this type specifier. */
            createTypeSpecifierString(child, sb);
        }
    }

    /**
     * Creates the string representation of a type specifier.
     *
     * @param typeSpecifier
     *            A TYPE_SPECIFIER node.
     * @param sb
     *            A StringBuilder to which will be appended the string.
     * @throws ParseException
     */
    private static void createTypeSpecifierString(CommonTree typeSpecifier,
            StringBuilder sb) throws ParseException {
        switch (typeSpecifier.getType()) {
        case CTFParser.FLOATTOK:
        case CTFParser.INTTOK:
        case CTFParser.LONGTOK:
        case CTFParser.SHORTTOK:
        case CTFParser.SIGNEDTOK:
        case CTFParser.UNSIGNEDTOK:
        case CTFParser.CHARTOK:
        case CTFParser.DOUBLETOK:
        case CTFParser.VOIDTOK:
        case CTFParser.BOOLTOK:
        case CTFParser.COMPLEXTOK:
        case CTFParser.IMAGINARYTOK:
        case CTFParser.CONSTTOK:
        case CTFParser.IDENTIFIER:
            sb.append(typeSpecifier.getText());
            break;
        case CTFParser.STRUCT: {
            CommonTree structName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.STRUCT_NAME);
            if (structName == null) {
                throw new ParseException(
                        "nameless struct found in createTypeSpecifierString"); //$NON-NLS-1$
            }

            CommonTree structNameIdentifier = (CommonTree) structName.getChild(0);

            sb.append(structNameIdentifier.getText());
            break;
        }
        case CTFParser.VARIANT: {
            CommonTree variantName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.VARIANT_NAME);
            if (variantName == null) {
                throw new ParseException(
                        "nameless variant found in createTypeSpecifierString"); //$NON-NLS-1$
            }

            CommonTree variantNameIdentifier = (CommonTree) variantName.getChild(0);

            sb.append(variantNameIdentifier.getText());
            break;
        }
        case CTFParser.ENUM: {
            CommonTree enumName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.ENUM_NAME);
            if (enumName == null) {
                throw new ParseException(
                        "nameless enum found in createTypeSpecifierString"); //$NON-NLS-1$
            }

            CommonTree enumNameIdentifier = (CommonTree) enumName.getChild(0);

            sb.append(enumNameIdentifier.getText());
            break;
        }
        case CTFParser.FLOATING_POINT:
        case CTFParser.INTEGER:
        case CTFParser.STRING:
            throw new ParseException(
                    "CTF type found in createTypeSpecifierString"); //$NON-NLS-1$
        default:
            childTypeError(typeSpecifier);
            break;
        }
    }

    /**
     * Creates the string representation of a list of pointers.
     *
     * @param pointerList
     *            A list of pointer nodes. If pointerList is null, this function
     *            does nothing.
     * @param sb
     *            A stringbuilder to which will be appended the string.
     */
    private static void createPointerListString(List<CommonTree> pointerList,
            StringBuilder sb) {
        if (pointerList == null) {
            return;
        }

        for (CommonTree pointer : pointerList) {

            sb.append(" *"); //$NON-NLS-1$
            if (pointer.getChildCount() > 0) {

                sb.append(" const"); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param node
     *            The node to check.
     * @return True if the given node is an unary string.
     */
    private static boolean isUnaryString(CommonTree node) {
        return ((node.getType() == CTFParser.UNARY_EXPRESSION_STRING));
    }

    /**
     * @param node
     *            The node to check.
     * @return True if the given node is any type of unary string (no quotes, quotes, etc).
     */
    private static boolean isAnyUnaryString(CommonTree node) {
        return ((node.getType() == CTFParser.UNARY_EXPRESSION_STRING) ||
                (node.getType() == CTFParser.UNARY_EXPRESSION_STRING_QUOTES));
    }

    /**
     * @param node
     *            The node to check.
     * @return True if the given node is an unary integer.
     */
    private static boolean isUnaryInteger(CommonTree node) {
        return ((node.getType() == CTFParser.UNARY_EXPRESSION_DEC) ||
                (node.getType() == CTFParser.UNARY_EXPRESSION_HEX) ||
                (node.getType() == CTFParser.UNARY_EXPRESSION_OCT));
    }

    /**
     * Parses a unary string node and return the string value.
     *
     * @param unaryString
     *            The unary string node to parse (type UNARY_EXPRESSION_STRING
     *            or UNARY_EXPRESSION_STRING_QUOTES).
     * @return The string value.
     */
    /*
     * It would be really nice to remove the quotes earlier, such as in the
     * parser.
     */
    private static String parseUnaryString(CommonTree unaryString) {

        CommonTree value = (CommonTree) unaryString.getChild(0);
        String strval = value.getText();

        /* Remove quotes */
        if (unaryString.getType() == CTFParser.UNARY_EXPRESSION_STRING_QUOTES) {
            strval = strval.substring(1, strval.length() - 1);
        }

        return strval;
    }

    /**
     * Parses an unary integer (dec, hex or oct).
     *
     * @param unaryInteger
     *            An unary integer node.
     * @return The integer value.
     * @throws CTFReaderException
     */
    private static long parseUnaryInteger(CommonTree unaryInteger) throws ParseException {

        List<CommonTree> children = unaryInteger.getChildren();
        CommonTree value = children.get(0);
        String strval = value.getText();

        long intval;
        try {
            intval = Long.decode(strval);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid integer format: " + strval); //$NON-NLS-1$
        }

        /* The rest of children are sign */
        if ((children.size() % 2) == 0) {
            return -intval;
        }
        return intval;
    }

    private static long getMajorOrMinor(CommonTree rightNode)
            throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("Invalid value for major/minor"); //$NON-NLS-1$
            }

            long m = parseUnaryInteger(firstChild);

            if (m < 0) {
                throw new ParseException("Invalid value for major/minor"); //$NON-NLS-1$
            }

            return m;
        }
        throw new ParseException("Invalid value for major/minor"); //$NON-NLS-1$
    }

    private static UUID getUUID(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isAnyUnaryString(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("Invalid value for UUID"); //$NON-NLS-1$
            }

            String uuidstr = parseUnaryString(firstChild);

            try {
                return UUID.fromString(uuidstr);
            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid format for UUID"); //$NON-NLS-1$
            }
        }
        throw new ParseException("Invalid value for UUID"); //$NON-NLS-1$
    }

    /**
     * Gets the value of a "signed" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "signed" value as a boolean.
     * @throws ParseException
     */
    private static boolean getSigned(CommonTree rightNode)
            throws ParseException {

        boolean ret = false;
        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(rightNode.getChildren());

            if (strval.equals(MetadataStrings.TRUE)
                    || strval.equals(MetadataStrings.TRUE2)) {
                ret = true;
            } else if (strval.equals(MetadataStrings.FALSE)
                    || strval.equals(MetadataStrings.FALSE2)) {
                ret = false;
            } else {
                throw new ParseException("Invalid boolean value " //$NON-NLS-1$
                        + firstChild.getChild(0).getText());
            }
        } else if (isUnaryInteger(firstChild)) {
            /* Happens if the value is something like "1234.hello" */
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("Invalid boolean value"); //$NON-NLS-1$
            }

            long intval = parseUnaryInteger(firstChild);

            if (intval == 1) {
                ret = true;
            } else if (intval == 0) {
                ret = false;
            } else {
                throw new ParseException("Invalid boolean value " //$NON-NLS-1$
                        + firstChild.getChild(0).getText());
            }
        } else {
            throw new ParseException();
        }

        return ret;
    }

    /**
     * Gets the value of a "byte_order" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "byte_order" value.
     * @throws ParseException
     */
    private ByteOrder getByteOrder(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(rightNode.getChildren());

            if (strval.equals(MetadataStrings.LE)) {
                return ByteOrder.LITTLE_ENDIAN;
            } else if (strval.equals(MetadataStrings.BE)
                    || strval.equals(MetadataStrings.NETWORK)) {
                return ByteOrder.BIG_ENDIAN;
            } else if (strval.equals(MetadataStrings.NATIVE)) {
                return trace.getByteOrder();
            } else {
                throw new ParseException("Invalid value for byte order"); //$NON-NLS-1$
            }
        }
        throw new ParseException("Invalid value for byte order"); //$NON-NLS-1$
    }

    /**
     * Determines if the given value is a valid alignment value.
     *
     * @param alignment
     *            The value to check.
     * @return True if it is valid.
     */
    private static boolean isValidAlignment(long alignment) {
        return !((alignment <= 0) || ((alignment & (alignment - 1)) != 0));
    }

    /**
     * Gets the value of a "size" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "size" value.
     * @throws ParseException
     */
    private static long getSize(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("Invalid value for size"); //$NON-NLS-1$
            }

            long size = parseUnaryInteger(firstChild);

            if (size < 1) {
                throw new ParseException("Invalid value for size"); //$NON-NLS-1$
            }

            return size;
        }
        throw new ParseException("Invalid value for size"); //$NON-NLS-1$
    }

    /**
     * Gets the value of a "align" integer or struct attribute.
     *
     * @param node
     *            A CTF_RIGHT node or directly an unary integer.
     * @return The align value.
     * @throws ParseException
     */
    private static long getAlignment(CommonTree node) throws ParseException {

        /*
         * If a CTF_RIGHT node was passed, call getAlignment with the first
         * child
         */
        if (node.getType() == CTFParser.CTF_RIGHT) {
            if (node.getChildCount() > 1) {
                throw new ParseException("Invalid alignment value"); //$NON-NLS-1$
            }

            return getAlignment((CommonTree) node.getChild(0));
        } else if (isUnaryInteger(node)) {
            long alignment = parseUnaryInteger(node);

            if (!isValidAlignment(alignment)) {
                throw new ParseException("Invalid value for alignment : " //$NON-NLS-1$
                        + alignment);
            }

            return alignment;
        }
        throw new ParseException("Invalid value for alignment"); //$NON-NLS-1$
    }

    /**
     * Gets the value of a "base" integer attribute.
     *
     * @param rightNode
     *            An CTF_RIGHT node.
     * @return The "base" value.
     * @throws ParseException
     */
    private static int getBase(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("invalid base value"); //$NON-NLS-1$
            }

            long intval = parseUnaryInteger(firstChild);
            if ((intval == 2) || (intval == 8) || (intval == 10)
                    || (intval == 16)) {
                return (int) intval;
            }
            throw new ParseException("Invalid value for base"); //$NON-NLS-1$
        } else if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(rightNode.getChildren());

            if (strval.equals(MetadataStrings.DECIMAL)
                    || strval.equals(MetadataStrings.DEC)
                    || strval.equals(MetadataStrings.DEC_CTE)
                    || strval.equals(MetadataStrings.INT_MOD)
                    || strval.equals(MetadataStrings.UNSIGNED_CTE)) {
                return 10;
            } else if (strval.equals(MetadataStrings.HEXADECIMAL)
                    || strval.equals(MetadataStrings.HEX)
                    || strval.equals(MetadataStrings.X)
                    || strval.equals(MetadataStrings.X2)
                    || strval.equals(MetadataStrings.POINTER)) {
                return 16;
            } else if (strval.equals(MetadataStrings.OCTAL)
                    || strval.equals(MetadataStrings.OCT)
                    || strval.equals(MetadataStrings.OCTAL_CTE)) {
                return 8;
            } else if (strval.equals(MetadataStrings.BINARY)
                    || strval.equals(MetadataStrings.BIN)) {
                return 2;
            } else {
                throw new ParseException("Invalid value for base"); //$NON-NLS-1$
            }
        } else {
            throw new ParseException("invalid value for base"); //$NON-NLS-1$
        }
    }

    /**
     * Gets the value of an "encoding" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "encoding" value.
     * @throws ParseException
     */
    private static Encoding getEncoding(CommonTree rightNode)
            throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryString(firstChild)) {
            String strval = concatenateUnaryStrings(rightNode.getChildren());

            if (strval.equals(MetadataStrings.UTF8)) {
                return Encoding.UTF8;
            } else if (strval.equals(MetadataStrings.ASCII)) {
                return Encoding.ASCII;
            } else if (strval.equals(MetadataStrings.NONE)) {
                return Encoding.NONE;
            } else {
                throw new ParseException("Invalid value for encoding"); //$NON-NLS-1$
            }
        }
        throw new ParseException("Invalid value for encoding"); //$NON-NLS-1$
    }

    private static long getStreamID(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("invalid value for stream id"); //$NON-NLS-1$
            }

            long intval = parseUnaryInteger(firstChild);

            return intval;
        }
        throw new ParseException("invalid value for stream id"); //$NON-NLS-1$
    }

    private static String getEventName(CommonTree rightNode)
            throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isAnyUnaryString(firstChild)) {
            String str = concatenateUnaryStrings(rightNode.getChildren());

            return str;
        }
        throw new ParseException("invalid value for event name"); //$NON-NLS-1$
    }

    private static long getEventID(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("invalid value for event id"); //$NON-NLS-1$
            }

            long intval = parseUnaryInteger(firstChild);

            return intval;
        }
        throw new ParseException("invalid value for event id"); //$NON-NLS-1$
    }

    /**
     * Concatenates a list of unary strings separated by arrows (->) or dots.
     *
     * @param strings
     *            A list, first element being an unary string, subsequent
     *            elements being ARROW or DOT nodes with unary strings as child.
     * @return The string representation of the unary string chain.
     */
    private static String concatenateUnaryStrings(List<CommonTree> strings) {

        StringBuilder sb = new StringBuilder();

        CommonTree first = strings.get(0);
        sb.append(parseUnaryString(first));

        boolean isFirst = true;

        for (CommonTree ref : strings) {
            if (isFirst) {
                isFirst = false;
                continue;
            }


            CommonTree id = (CommonTree) ref.getChild(0);

            if (ref.getType() == CTFParser.ARROW) {
                sb.append("->"); //$NON-NLS-1$
            } else { /* DOT */
                sb.append('.');
            }

            sb.append(parseUnaryString(id));
        }

        return sb.toString();
    }

    /**
     * Throws a ParseException stating that the parent-child relation between
     * the given node and its parent is not valid. It means that the shape of
     * the AST is unexpected.
     *
     * @param child
     *            The invalid child node.
     * @throws ParseException
     */
    private static void childTypeError(CommonTree child) throws ParseException {
        CommonTree parent = (CommonTree) child.getParent();
        String error = "Parent " + CTFParser.tokenNames[parent.getType()] //$NON-NLS-1$
                + " can't have a child of type " //$NON-NLS-1$
                + CTFParser.tokenNames[child.getType()] + "."; //$NON-NLS-1$

        throw new ParseException(error);
    }

    // ------------------------------------------------------------------------
    // Scope management
    // ------------------------------------------------------------------------

    /**
     * Adds a new declaration scope on the top of the scope stack.
     */
    private void pushScope() {
        scope = new DeclarationScope(scope);
    }

    /**
     * Removes the top declaration scope from the scope stack.
     */
    private void popScope() {
        scope = scope.getParentScope();
    }

    /**
     * Returns the current declaration scope.
     *
     * @return The current declaration scope.
     */
    private DeclarationScope getCurrentScope() {
        return scope;
    }

}
