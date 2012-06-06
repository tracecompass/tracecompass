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
 *
 */
public interface CTFStrings {

    /** None */
    public static final String NONE = "none"; //$NON-NLS-1$
    /** Ascii */
    public static final String ASCII = "ASCII"; //$NON-NLS-1$
    /** UTF8 */
    public static final String UTF8 = "UTF8"; //$NON-NLS-1$
    /** b (for binary like b11010010 */
    public static final String BIN = "b"; //$NON-NLS-1$
    /** Binary */
    public static final String BINARY = "binary"; //$NON-NLS-1$
    /** Octal like o177 */
    public static final String OCTAL_CTE = "o"; //$NON-NLS-1$
    /** Octal like oct177 */
    public static final String OCT = "oct"; //$NON-NLS-1$
    /** Octal like octal177 */
    public static final String OCTAL = "octal"; //$NON-NLS-1$
    /** Pointer (memory address for all the hardcore Java gurus out there)*/
    public static final String POINTER = "p"; //$NON-NLS-1$
    /** X for hex */
    public static final String X2 = "X"; //$NON-NLS-1$
    /** x for hex */
    public static final String X = "x"; //$NON-NLS-1$
    /** hex */
    public static final String HEX = "hex"; //$NON-NLS-1$
    /** Hexadecimal */
    public static final String HEXADECIMAL = "hexadecimal"; //$NON-NLS-1$
    /** unsigned like in 10000ul */
    public static final String UNSIGNED_CTE = "u"; //$NON-NLS-1$
    /** Decimal */
    public static final String DEC_CTE = "d"; //$NON-NLS-1$
    /** Integer like 1000i */
    public static final String INT_MOD = "i"; //$NON-NLS-1$
    /** Decimal */
    public static final String DEC = "dec"; //$NON-NLS-1$
    /** Decimal */
    public static final String DECIMAL = "decimal"; //$NON-NLS-1$
    /** native for byteorders*/
    public static final String NATIVE = "native"; //$NON-NLS-1$
    /** network for byteorders*/
    public static final String NETWORK = "network"; //$NON-NLS-1$
    /** Big endian */
    public static final String BE = "be"; //$NON-NLS-1$
    /** Little endian */
    public static final String LE = "le"; //$NON-NLS-1$
    /** Alignment of a field */
    public static final String ALIGN = "align"; //$NON-NLS-1$
    /** Mantissa digits */
    public static final String MANT_DIG = "mant_dig"; //$NON-NLS-1$
    /** Exponent digits */
    public static final String EXP_DIG = "exp_dig"; //$NON-NLS-1$
    /** Loglevel */
    public static final String LOGLEVEL2 = "loglevel"; //$NON-NLS-1$
    /** Name */
    public static final String NAME2 = "name"; //$NON-NLS-1$
    /** Event context */
    public static final String EVENT_CONTEXT = "event.context"; //$NON-NLS-1$
    /** Fields */
    public static final String FIELDS_STRING = "fields"; //$NON-NLS-1$
    /** context */
    public static final String CONTEXT = "context"; //$NON-NLS-1$
    /** Stream ID */
    public static final String STREAM_ID = "stream_id"; //$NON-NLS-1$
    /** Packet context */
    public static final String PACKET_CONTEXT = "packet.context"; //$NON-NLS-1$
    /** ID */
    public static final String ID = "id"; //$NON-NLS-1$
    /** Packet Header */
    public static final String PACKET_HEADER = "packet.header"; //$NON-NLS-1$
    /** Event Header */
    public static final String EVENT_HEADER = "event.header"; //$NON-NLS-1$
    /** Byte order */
    public static final String BYTE_ORDER = "byte_order"; //$NON-NLS-1$
    /** UUID */
    public static final String UUID_STRING = "uuid"; //$NON-NLS-1$
    /** False */
    public static final String FALSE2 = "FALSE"; //$NON-NLS-1$
    /** False */
    public static final String FALSE = "false"; //$NON-NLS-1$
    /** True */
    public static final String TRUE2 = "TRUE"; //$NON-NLS-1$
    /** True */
    public static final String TRUE = "true"; //$NON-NLS-1$
    /** Minor (Vresion)*/
    public static final String MINOR = "minor"; //$NON-NLS-1$
    /** Major (Vresion)*/
    public static final String MAJOR = "major"; //$NON-NLS-1$

}
