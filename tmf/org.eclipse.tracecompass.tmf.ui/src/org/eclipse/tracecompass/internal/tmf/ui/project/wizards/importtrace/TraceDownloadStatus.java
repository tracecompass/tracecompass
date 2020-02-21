/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Status of a trace download operation. Can contain multiple status to track
 * mutliple download in the same operation.
 *
 * @author Simon Delisle
 *
 */
public class TraceDownloadStatus {

    /**
     * Status type severity indicating this status represents the normal case.
     */
    public static final int OK = 0;

    /**
     * Status type severity indicating this status represents a http/https
     * connection timeout.
     */
    public static final int TIMEOUT = 0x01;

    /**
     * Status type severity indicating this status represents an error.
     */
    public static final int ERROR = 0x02;

    private File fDownloadedFile;
    private Throwable fException;
    private int fSeverity;
    private List<TraceDownloadStatus> fMultipleStatus;

    /**
     * Constructor.
     *
     * @param severity
     *            Status severity
     * @param file
     *            Downloaded file
     * @param exception
     *            The exception that describe the problem
     */
    public TraceDownloadStatus(int severity, File file, Throwable exception) {
        fSeverity = severity;
        fDownloadedFile = file;
        fException = exception;
        fMultipleStatus = new ArrayList<>();
    }

    /**
     * Get the downloaded file if the operation was successful. Can be null if
     * something went wrong during the download.
     *
     * @return The downloaded file
     */
    public File getDownloadedFile() {
        return fDownloadedFile;
    }

    /**
     * Get the wrapped exception.
     *
     * @return The exception that describe the problem
     */
    public Throwable getException() {
        return fException;
    }

    /**
     * Get the status severity (OK, TIMEOUT or ERROR).
     *
     * @return The status severity
     */
    public int getSeverity() {
        return fSeverity;
    }

    /**
     * Set the downloaded file.
     *
     * @param downloadedFile
     *            File to wrap in this status
     */
    public void setDownloadedFile(File downloadedFile) {
        fDownloadedFile = downloadedFile;
    }

    /**
     * Set the exception for this status.
     *
     * @param exception
     *            Exception to wrap in this status
     */
    public void setException(Throwable exception) {
        fException = exception;
    }

    /**
     * Set the status severity (OK, TIMEOUT or ERROR).
     *
     * @param severity
     *            The status severity
     */
    public void setSeverity(int severity) {
        fSeverity = severity;
    }

    /**
     * Check if the status as a severity of OK
     *
     * @return True if the status is OK
     */
    public boolean isOk() {
        return fSeverity == OK;
    }

    /**
     * Check if the status as a severity of TIMEOUT
     *
     * @return True if the status is a TIMEOUT
     */
    public boolean isTimeout() {
        return fSeverity == TIMEOUT;
    }

    /**
     * Get a list of children status in case it's a multiple status or an empty
     * list if it's not the case.
     *
     * @return All the children status of this status
     */
    public List<TraceDownloadStatus> getChildren() {
        return fMultipleStatus;
    }

    /**
     * Add a status to this status and update the severity using the given
     * status severity
     *
     * @param status
     *            Status to add
     */
    public void add(TraceDownloadStatus status) {
        fMultipleStatus.add(status);
        if (status.getSeverity() == TIMEOUT) {
            fSeverity = TIMEOUT;
            fException = status.getException();
        } else if (fSeverity == OK && !status.isOk()) {
            fSeverity = status.getSeverity();
        }
    }
}
