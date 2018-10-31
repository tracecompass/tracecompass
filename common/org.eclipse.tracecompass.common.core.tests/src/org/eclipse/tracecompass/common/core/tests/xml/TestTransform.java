/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.xml;

import static org.junit.Assert.assertNotNull;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.eclipse.tracecompass.common.core.xml.XmlUtils;
import org.junit.Test;

/**
 * Test XmlUtils
 *
 * @author Matthew Khouzam
 */
public class TestTransform {

    /**
     * Test safe transformer creator
     *
     * @throws TransformerConfigurationException
     *             should not happen
     */
    @Test
    public void testNewSafeTransformer() throws TransformerConfigurationException {
        Transformer newSafeTransformer = XmlUtils.newSecureTransformer();
        assertNotNull(newSafeTransformer);
        // TODO: test with unsafe XML
    }

}
