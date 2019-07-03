/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

import com.google.common.net.MediaType;

/**
 * Helper to download traces from HTTP/HTTPS.
 *
 * @author Simon Delisle
 *
 */
public class DownloadTraceHttpHelper {

    private static final String CONTENT_DISPOSITION = "Content-Disposition"; //$NON-NLS-1$

    private DownloadTraceHttpHelper() {
        // Do nothing, private constructor
    }

    /**
     * Download trace from a HTTP/HTTPS source.
     *
     * @param traceUrl
     *            Trace url you want to download
     * @param destinationDir
     *            Directory where the trace will be saved
     * @return The downloaded trace or null if something went wrong
     */
    public static TraceDownloadStatus downloadTrace(String traceUrl, String destinationDir) {
        File destFile = null;

        try {
            URL url = new URL(traceUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(120000); // 2 minutes timeout
            int responseCode = httpConnection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                httpConnection.disconnect();
                return new TraceDownloadStatus(TraceDownloadStatus.ERROR, destFile, null);
            }

            String fileName = getFileName(httpConnection);
            destFile = new File(destinationDir, fileName);

            try {
                FileUtils.copyURLToFile(url, destFile);
            } catch (IOException e) {
                Activator.getDefault().logError("Unable to download from " + url.toString() + " to " + destFile.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                cleanDirectory(new File(destinationDir));
                return new TraceDownloadStatus(TraceDownloadStatus.ERROR, destFile, e);
            } finally {
                httpConnection.disconnect();
            }
        } catch (SocketTimeoutException e) {
            return new TraceDownloadStatus(TraceDownloadStatus.TIMEOUT, null, e);
        } catch (IOException e) {
            return new TraceDownloadStatus(TraceDownloadStatus.ERROR, null, e);
        }

        return new TraceDownloadStatus(TraceDownloadStatus.OK, destFile, null);
    }

    /**
     * Download multiple traces from a list of HTTP/HTTPS sources.
     *
     * @param tracesUrl
     *            Collection of trace url
     * @param destinationDir
     *            Directory where the traces will be saved
     * @return List that contains downloaded traces or null if something went
     *         wrong
     */
    public static TraceDownloadStatus downloadTraces(Collection<String> tracesUrl, String destinationDir) {
        TraceDownloadStatus status = new TraceDownloadStatus(TraceDownloadStatus.OK, null, null);
        for (String traceUrl : tracesUrl) {
            TraceDownloadStatus singleFileStatus = downloadTrace(traceUrl, destinationDir);
            status.add(singleFileStatus);
        }
        return status;
    }

    /**
     * Try to find the name of the file by using connection information.
     *
     * @param connection
     *            HTTP connection
     * @return File name
     */
    private static String getFileName(HttpURLConnection connection) {
        String fileName = getLastSegmentUrl(connection.getURL().toString());
        String contentType = connection.getContentType();

        if (contentType != null) {
            MediaType type = MediaType.parse(contentType);
            if (type.is(MediaType.ANY_APPLICATION_TYPE)) {
                String contentDisposition = connection.getHeaderField(CONTENT_DISPOSITION);
                if (contentDisposition != null) {
                    String[] content = contentDisposition.split(";"); //$NON-NLS-1$
                    for (String string : content) {
                        if (string.contains("filename=")) { //$NON-NLS-1$
                            int index = string.indexOf('"');
                            fileName = string.substring(index + 1, string.length() - 1);
                        }
                    }
                }
            }
        }

        return fileName;
    }

    /**
     * Get the last part of a specific url (after the last '/').
     *
     * @param url
     *            The url
     * @return The last segment of the url
     */
    private static String getLastSegmentUrl(String url) {
        int indexFile = url.lastIndexOf('/');
        return url.substring(indexFile + 1, url.length());
    }

    /**
     * Clean the destination directory in case something went wrong during the
     * download.
     *
     * @param dir
     *            Directory to delete
     */
    private static void cleanDirectory(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            Activator.getDefault().logError("Unable to delete: " + dir.toString(), e); //$NON-NLS-1$
        }
    }
}
