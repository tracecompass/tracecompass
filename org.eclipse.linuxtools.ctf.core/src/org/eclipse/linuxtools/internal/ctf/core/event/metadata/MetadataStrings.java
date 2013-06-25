/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata;

/**
 * Strings generated from the TSDL grammar. Note that they are static final so
 * they get quarked. See CTF specs for more details
 *
 * @author Matthew Khouzam and All
 */
@SuppressWarnings("nls")
public interface MetadataStrings {

    /** None */

    static final String NONE = "none";
    /** Ascii */
    static final String ASCII = "ASCII";
    /** UTF8 */
    static final String UTF8 = "UTF8";
    /** b (for binary like b11010010 */
    static final String BIN = "b";
    /** Binary */
    static final String BINARY = "binary";
    /** Octal like o177 */
    static final String OCTAL_CTE = "o";
    /** Octal like oct177 */
    static final String OCT = "oct";
    /** Octal like octal177 */
    static final String OCTAL = "octal";
    /** Pointer (memory address for all the hardcore Java gurus out there)*/
    static final String POINTER = "p";
    /** X for hex */
    static final String X2 = "X";
    /** x for hex */
    static final String X = "x";
    /** hex */
    static final String HEX = "hex";
    /** Hexadecimal */
    static final String HEXADECIMAL = "hexadecimal";
    /** unsigned like in 10000ul */
    static final String UNSIGNED_CTE = "u";
    /** Decimal */
    static final String DEC_CTE = "d";
    /** Integer like 1000i */
    static final String INT_MOD = "i";
    /** Decimal */
    static final String DEC = "dec";
    /** Decimal */
    static final String DECIMAL = "decimal";
    /** native for byteorders*/
    static final String NATIVE = "native";
    /** network for byteorders*/
    static final String NETWORK = "network";
    /** Big endian */
    static final String BE = "be";
    /** Little endian */
    static final String LE = "le";
    /** Alignment of a field */
    static final String ALIGN = "align";
    /** Mantissa digits */
    static final String MANT_DIG = "mant_dig";
    /** Exponent digits */
    static final String EXP_DIG = "exp_dig";
    /** Loglevel */
    static final String LOGLEVEL2 = "loglevel";
    /** Name */
    static final String NAME2 = "name";
    /** Event context */
    static final String EVENT_CONTEXT = "event.context";
    /** Fields */
    static final String FIELDS_STRING = "fields";
    /** context */
    static final String CONTEXT = "context";
    /** Stream ID */
    static final String STREAM_ID = "stream_id";
    /** Packet context */
    static final String PACKET_CONTEXT = "packet.context";
    /** ID */
    static final String ID = "id";
    /** Packet Header */
    static final String PACKET_HEADER = "packet.header";
    /** Event Header */
    static final String EVENT_HEADER = "event.header";
    /** Byte order */
    static final String BYTE_ORDER = "byte_order";
    /** UUID */
    static final String UUID_STRING = "uuid";
    /** False */
    static final String FALSE2 = "FALSE";
    /** False */
    static final String FALSE = "false";
    /** True */
    static final String TRUE2 = "TRUE";
    /** True */
    static final String TRUE = "true";
    /** Minor (Vresion)*/
    static final String MINOR = "minor";
    /** Major (Vresion)*/
    static final String MAJOR = "major";

}
