/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.pcap.core.protocol;

/**
 * Interface that lists constants related to protocols/layers.
 *
 * See http://en.wikipedia.org/wiki/OSI_model#Description_of_OSI_layers.
 *
 * @author Vincent Perot
 */
public interface PcapProtocolValues {

    /**
     * Layer 0. This layer is not an OSI layer but is used as an helper to store
     * the pseudo-protocol PCAP.
     */
    int LAYER_0 = 0;

    /** Layer 1 of the OSI model */
    int LAYER_1 = 1;

    /** Layer 2 of the OSI model */
    int LAYER_2 = 2;

    /** Layer 3 of the OSI model */
    int LAYER_3 = 3;

    /** Layer 4 of the OSI model */
    int LAYER_4 = 4;

    /** Layer 5 of the OSI model */
    int LAYER_5 = 5;

    /** Layer 6 of the OSI model */
    int LAYER_6 = 6;

    /** Layer 7 of the OSI model */
    int LAYER_7 = 7;

}
