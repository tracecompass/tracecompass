/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.resources.IFolder;

/**
 *  Class to build project configuration.
 *
 * @author Bernd Hufmann
 */
class TmfProjectConfig {
    private IFolder fTracesFolder;
    private IFolder fExperimentsFolder;
    private IFolder fSupplementaryFolder;

    private TmfProjectConfig(Builder builder) {
        this.fTracesFolder = builder.fTracesFolder;
        this.fExperimentsFolder = builder.fExperimentsFolder;
        this.fSupplementaryFolder = builder.fSupplementaryFolder;
    }

    public IFolder getTracesFolder() {
        return fTracesFolder;
    }
    public IFolder getExperimentsFolder() {
        return fExperimentsFolder;
    }
    public IFolder getSupplementaryFolder() {
        return fSupplementaryFolder;
    }

    static class Builder {
        private IFolder fTracesFolder;
        private IFolder fExperimentsFolder;
        private IFolder fSupplementaryFolder;

        Builder() {
            // do nothing
        }

        public Builder setTracesFolder(IFolder tracesFolder) {
            fTracesFolder = tracesFolder;
            return this;
        }

        public Builder setExperimentsFolder(IFolder experimentsFolder) {
            fExperimentsFolder = experimentsFolder;
            return this;
        }

        public Builder setSupplementaryFolder(IFolder supplementaryFolder) {
            fSupplementaryFolder = supplementaryFolder;
            return this;
        }

        public TmfProjectConfig build() {
            return new TmfProjectConfig(this);
        }
    }

}