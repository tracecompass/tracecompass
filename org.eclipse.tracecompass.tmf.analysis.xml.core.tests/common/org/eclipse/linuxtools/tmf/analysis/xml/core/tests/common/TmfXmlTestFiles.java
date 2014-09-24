/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.tests.common;

import java.io.File;

/**
 * Provides some test XML files to use
 *
 * @author Geneviève Bastien
 */
public enum TmfXmlTestFiles {
    /** A valid XML test file */
    VALID_FILE("../org.eclipse.linuxtools.tmf.analysis.xml.core.tests/test_xml_files/test_valid/test_valid.xml"),
    /** An invalid test file */
    INVALID_FILE("../org.eclipse.linuxtools.tmf.analysis.xml.core.tests/test_xml_files/test_invalid/test_invalid.xml");

    private final String fPath;

    private TmfXmlTestFiles(String file) {
        fPath = file;
    }

    /**
     * Get the file name part of the file
     *
     * @return The path of this test file
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Returns the file object corresponding to the test XML file
     *
     * @return The file object for this test file
     */
    public File getFile() {
        return new File(fPath);
    }

}
