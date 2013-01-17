/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * Class to fetch and save synchronization information between traces
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class SynchronizationBackend {

    private static final int SYNC_FILE_MAGIC_NUMBER = 0x0DECAF00;

    private static final int FILE_VERSION = 1;

    private static final int HEADER_SIZE = 20;

    private final File fSyncFile;

    /**
     * Constructor
     *
     * @param syncFile
     *            The file containing synchronization information
     * @throws IOException
     *             If the file couldn't be opened for some reason
     */
    public SynchronizationBackend(File syncFile) throws IOException {
        this(syncFile, true);
    }

    /**
     * Constructor with possibility to tell whether to throw errors on exception
     * or not
     *
     * @param syncFile
     *            The file containing synchronization information
     * @param throwErrors
     *            Whether to throw exceptions or not
     * @throws IOException
     *             If the file couldn't be opened for some reason
     */
    public SynchronizationBackend(File syncFile, boolean throwErrors) throws IOException {
        /*
         * Open the file ourselves, get the header information we need, then
         * pass on the descriptor.
         */
        int res;

        fSyncFile = syncFile;

        if (syncFile == null) {
            return;
        }

        if (!syncFile.exists()) {
            if (throwErrors) {
                throw new IOException("Selected synchronization file does not exist"); //$NON-NLS-1$
            }
            return;
        }
        if (syncFile.length() <= 0) {
            if (throwErrors) {
                throw new IOException("Invalid synchronization file selected, " + //$NON-NLS-1$
                        "target file is empty"); //$NON-NLS-1$
            }
            return;
        }

        FileInputStream fis = new FileInputStream(syncFile);
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        FileChannel fc = fis.getChannel();
        buffer.clear();
        fc.read(buffer);
        buffer.flip();

        /*
         * Check the magic number,to make sure we're opening the right type of
         * file
         */
        res = buffer.getInt();
        if (res != SYNC_FILE_MAGIC_NUMBER) {
            fc.close();
            fis.close();
            throw new IOException("Selected file does not" + //$NON-NLS-1$
                    "look like a synchronization file"); //$NON-NLS-1$
        }

        res = buffer.getInt(); /* Major version number */
        if (res != FILE_VERSION) {
            fc.close();
            fis.close();
            throw new IOException("Select synchronization file is of an older " //$NON-NLS-1$
                    + "format. Synchronization will have to be computed again."); //$NON-NLS-1$
        }

        res = buffer.getInt(); /* Minor version number */

        fc.close();
        fis.close();
    }

    /**
     * Opens an existing synchronization file
     *
     * @return The synchronization algorithm contained in the file
     * @throws IOException
     *             Exception returned file functions
     */
    public SynchronizationAlgorithm openExistingSync() throws IOException {

        if (fSyncFile == null) {
            return null;
        }

        /* Set the position after the header */
        FileInputStream fis = new FileInputStream(fSyncFile);
        FileChannel fc = fis.getChannel().position(HEADER_SIZE);

        /* Read the input stream */
        ObjectInputStream ois = new ObjectInputStream(fis);
        SyncAlgorithmFullyIncremental syncAlgo = null;
        try {
            syncAlgo = (SyncAlgorithmFullyIncremental) ois.readObject();
        } catch (ClassNotFoundException e) {

        }
        ois.close();
        fc.close();

        fis.close();

        return syncAlgo;
    }

    /**
     * Saves the synchronization algorithm object to file
     *
     * @param syncAlgo
     *            The algorithm to save
     * @throws FileNotFoundException
     *             propagate callee's exceptions
     */
    public void saveSync(SynchronizationAlgorithm syncAlgo) throws FileNotFoundException {

        if (fSyncFile == null) {
            return;
        }

        FileChannel fc;
        FileOutputStream fos;
        ObjectOutputStream oos;
        ByteBuffer buffer;
        int res;

        fos = new FileOutputStream(fSyncFile, false);
        fc = fos.getChannel();

        buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.clear();

        /* Save the header of the file */
        try {
            fc.position(0);

            buffer.putInt(SYNC_FILE_MAGIC_NUMBER);

            buffer.putInt(FILE_VERSION);

            buffer.flip();
            res = fc.write(buffer);
            assert (res <= HEADER_SIZE);
            /* done writing the file header */

            fc.position(HEADER_SIZE);

            oos = new ObjectOutputStream(fos);
            oos.writeObject(syncAlgo);
            oos.close();

        } catch (IOException e) {
            /* We should not have any problems at this point... */
            Activator.logError("Error saving trace synchronization data", e); //$NON-NLS-1$
        } finally {
            try {
                fc.close();
                fos.close();
            } catch (IOException e) {
                Activator.logError("Error closing synchronization file", e); //$NON-NLS-1$
            }
        }
        return;

    }

}
