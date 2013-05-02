/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;

/**
 * File and name internal helper class <br>
 * it has the file, a name to display, whether the name is conflicting and a
 * reference to the configuration element defining its trace type.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
class FileAndName implements Comparable<FileAndName> {

    final private File fFile;
    private String fTraceTypeId;
    private String fName;
    private boolean fConflict;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * A file and name
     *
     * @param f
     *            the file, can only be set here
     * @param n
     *            the name, can be renamed
     *
     */
    public FileAndName(File f, String n) {
        fFile = f;
        fName = n;
        fTraceTypeId = null;
    }

    // ------------------------------------------------------------------------
    // Getter / Setter
    // ------------------------------------------------------------------------

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return fName;
    }

    /**
     * Set the name
     *
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.fName = name;
    }

    /**
     * Sets the configuration element of the
     *
     * @param elem
     *            the element
     */
    public void setTraceTypeId(String elem) {
        fTraceTypeId = elem;
    }

    /**
     * Gets the configuration element canonical name
     *
     * @return gets the configuration element canonical name
     */
    public String getTraceTypeId() {
        return fTraceTypeId;
    }

    /**
     * Get the file
     *
     * @return the file
     */
    public File getFile() {
        return fFile;
    }

    /**
     * Set that the name is conflicting or not
     *
     * @param conflict
     *            if the name is conflicting or not
     */
    public void setConflictingName(boolean conflict) {
        fConflict = conflict;
    }

    /**
     * Is the name conflicting?
     *
     * @return is the name conflicting?
     */
    public boolean isConflictingName() {
        return fConflict;
    }

    // ------------------------------------------------------------------------
    // Comparator & Equals
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(FileAndName o) {
        int retVal = getFile().compareTo(o.getFile());
        if (retVal == 0) {
            if (getTraceTypeId() != null) {
                if (getTraceTypeId() != null) {
                    if (o.getTraceTypeId() != null) {
                        retVal = getTraceTypeId().compareTo(o.getTraceTypeId());
                    }
                }
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // do not take "name" into account since it can change on the fly.
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTraceTypeId == null) ? 0 : fTraceTypeId.hashCode());
        result = prime * result + ((fFile == null) ? 0 : fFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // do not take "name" into account since it can change on the fly.
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileAndName)) {
            return false;
        }
        FileAndName other = (FileAndName) obj;
        if (fTraceTypeId == null) {
            if (other.fTraceTypeId != null) {
                return false;
            }
        } else if (!fTraceTypeId.equals(other.fTraceTypeId)) {
            return false;
        }
        if (fFile == null) {
            if (other.fFile != null) {
                return false;
            }
        } else if (!fFile.equals(other.fFile)) {
            return false;
        }
        return true;
    }
}