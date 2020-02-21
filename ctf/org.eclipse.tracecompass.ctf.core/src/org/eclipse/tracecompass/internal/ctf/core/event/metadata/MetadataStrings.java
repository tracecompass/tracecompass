/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strings generated from the TSDL grammar. Note that they are static final so
 * they get quarked. See CTF specs for more details
 *
 * @author Matthew Khouzam and All
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface MetadataStrings {

    /** None */

    String NONE = "none";
    /** Ascii */
    String ASCII = "ASCII";
    /** UTF8 */
    String UTF8 = "UTF8";
    /** b (for binary like b11010010 */
    String BIN = "b";
    /** Binary */
    String BINARY = "binary";
    /** Octal like o177 */
    String OCTAL_CTE = "o";
    /** Octal like oct177 */
    String OCT = "oct";
    /** Octal like octal177 */
    String OCTAL = "octal";
    /** Pointer (memory address for all the hardcore Java gurus out there) */
    String POINTER = "p";
    /** X for hex */
    String X2 = "X";
    /** x for hex */
    String X = "x";
    /** hex */
    String HEX = "hex";
    /** Hexadecimal */
    String HEXADECIMAL = "hexadecimal";
    /** unsigned like in 10000ul */
    String UNSIGNED_CTE = "u";
    /** Decimal */
    String DEC_CTE = "d";
    /** Integer like 1000i */
    String INT_MOD = "i";
    /** Decimal */
    String DEC = "dec";
    /** Decimal */
    String DECIMAL = "decimal";
    /** native for byteorders */
    String NATIVE = "native";
    /** network for byteorders */
    String NETWORK = "network";
    /** Big endian */
    String BE = "be";
    /** Little endian */
    String LE = "le";
    /** Alignment of a field */
    String ALIGN = "align";
    /** Mantissa digits */
    String MANT_DIG = "mant_dig";
    /** Exponent digits */
    String EXP_DIG = "exp_dig";
    /** Loglevel */
    String LOGLEVEL2 = "loglevel";
    /** Name */
    String NAME2 = "name";
    /** Event context */
    String EVENT_CONTEXT = "event.context";
    /** Fields */
    String FIELDS_STRING = "fields";
    /** context */
    String CONTEXT = "context";
    /** Stream ID */
    String STREAM_ID = "stream_id";
    /** Packet context */
    String PACKET_CONTEXT = "packet.context";
    /** ID */
    String ID = "id";
    /** Packet Header */
    String PACKET_HEADER = "packet.header";
    /** Event Header */
    String EVENT_HEADER = "event.header";
    /** Byte order */
    String BYTE_ORDER = "byte_order";
    /** UUID */
    String UUID_STRING = "uuid";
    /** False */
    String FALSE2 = "FALSE";
    /** False */
    String FALSE = "false";
    /** True */
    String TRUE2 = "TRUE";
    /** True */
    String TRUE = "true";
    /** Minor (Version) */
    String MINOR = "minor";
    /** Major (Version) */
    String MAJOR = "major";
    /** event */
    String EVENT = "event";
    /** trace */
    String TRACE = "trace";
    /** stream */
    String STREAM = "stream";
    /** struct */
    String STRUCT = "struct";
    /** variant */
    String VARIANT = "variant";
    /** enum */
    String ENUM = "enum";

}
