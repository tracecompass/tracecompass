/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.xml;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * XML Utilities. Useful to avoid copy-pasting secure code generation. Utils
 * here should be OASP compliant.
 *
 * @author Matthew Khouzam
 * @since 3.2
 */
public final class XmlUtils {

    private XmlUtils() {
        // Do nothing
    }

    /**
     * <p>
     * Create a new <code>Transformer</code> that performs a copy of the
     * <code>Source</code> to the <code>Result</code>. i.e. the "<em>identity
     * transform</em>".
     * </p>
     * <p>
     * This is thread safe.
     * </p>
     * <p>
     * Use {@link XMLConstants#FEATURE_SECURE_PROCESSING} to ensure that the
     * transformer is secure.
     * </p>
     *
     * @return A Transformer object that may be used to perform a transformation
     *         in a single thread, never null.
     *
     * @throws TransformerConfigurationException
     *             When it is not possible to create a <code>Transformer</code>
     *             instance.
     */
    public static Transformer newSecureTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newTransformer();
    }
}
