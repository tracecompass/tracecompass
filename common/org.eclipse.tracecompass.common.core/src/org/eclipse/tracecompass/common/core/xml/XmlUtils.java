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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.eclipse.tracecompass.internal.common.core.Activator;

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

    /**
     * Create a document builder factory that is safe according to the OWASP
     * injection prevention cheat sheet.
     *
     * @return the documentBuilderFactory
     * @since 4.1
     */
    public static DocumentBuilderFactory newSafeDocumentBuilderFactory() {
        String feature = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // This one is from Sonar (squid:S2755)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed,
            // almost all
            // XML entity attacks are prevented
            // Xerces 2 only -
            // http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl"; //$NON-NLS-1$
            dbf.setFeature(feature, true);

            // If you can't completely disable DTDs, then at least do the
            // following:
            // Xerces 1 -
            // http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 -
            // http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities"; //$NON-NLS-1$
            dbf.setFeature(feature, false);

            // Xerces 1 -
            // http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 -
            // http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities"; //$NON-NLS-1$
            dbf.setFeature(feature, false);

            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd"; //$NON-NLS-1$
            dbf.setFeature(feature, false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema,
            // DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            // And, per Timothy Morgan:
            // "If for some reason support for inline  DOCTYPEs are a requirement, then  ensure
            // the entity settings are disabled (as shown above) and beware that SSRF attacks
            // (http://cwe.mitre.org/data/definitions/918.html)
            // and denial of service attacks (such as billion laughs or decompression bombs via
            // "jar:") are a risk."

        } catch (ParserConfigurationException e) {
            // This should catch a failed setFeature feature
            Activator.instance().logInfo("ParserConfigurationException was thrown. The feature '" + feature //$NON-NLS-1$
                    + "' is probably not supported by your XML processor.", e); //$NON-NLS-1$
        }
        return dbf;
    }
}
