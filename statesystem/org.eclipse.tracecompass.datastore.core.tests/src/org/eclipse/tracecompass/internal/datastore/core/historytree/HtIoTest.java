/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.historytree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.internal.datastore.core.historytree.HtIo;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HistoryTreeStub;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HtTestUtils;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode.NodeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link HtIo} class
 *
 * @author Geneviève Bastien
 */
public class HtIoTest {

    private static final int BLOCKSIZE = HtTestUtils.BLOCKSIZE;
    private static final int NB_CHILDREN = 3;

    private @Nullable HtIo<HTInterval, HTNode<HTInterval>> fHtIo;
    private @Nullable File fStateFile;

    /**
     * Construct the tree IO Object
     *
     * @throws IOException
     *             Exception with the file
     */
    @Before
    public void setUp() throws IOException {
        File file = File.createTempFile("tmp", null);
        assertNotNull(file);
        fStateFile = file;

        fHtIo = new HtIo<>(file,
                HtTestUtils.BLOCKSIZE,
                NB_CHILDREN,
                true,
                HtTestUtils.READ_FACTORY,
                HistoryTreeStub.NODE_FACTORY);
    }

    /**
     * Delete the file after test
     */
    @After
    public void cleanUp() {
        HtIo<HTInterval, HTNode<HTInterval>> htIo = fHtIo;
        if (htIo != null) {
            htIo.deleteFile();
        }
    }

    private static HTNode<HTInterval> createCoreNode(int seqNum, int parentNum) {
        HTNode<HTInterval> node = HistoryTreeStub.NODE_FACTORY.createNode(NodeType.CORE,
                BLOCKSIZE, NB_CHILDREN, seqNum, parentNum, 0L);
        return node;
    }

    private static HTNode<HTInterval> createLeafNode(int seqNum, int parentNum) {
        HTNode<HTInterval> node = HistoryTreeStub.NODE_FACTORY.createNode(NodeType.LEAF,
                BLOCKSIZE, NB_CHILDREN, seqNum, parentNum, 0L);
        return node;
    }

    /**
     * Test reading and writing nodes
     *
     * @throws IOException
     *             Exception thrown by the file
     */
    @Test
    public void testReadWriteNode() throws IOException {
        HtIo<HTInterval, HTNode<HTInterval>> htio = fHtIo;
        assertNotNull(htio);

        int coreNodeSeqNum = 0;
        int leafNodeSeqNum = 1;

        // Add a core node and a leaf node
        HTNode<HTInterval> coreNode = createCoreNode(coreNodeSeqNum, -1);
        assertFalse(HtIo.isInCache(htio, coreNodeSeqNum));
        htio.writeNode(coreNode);
        assertTrue(HtIo.isInCache(htio, coreNodeSeqNum));

        HTNode<HTInterval> leafNode = createLeafNode(leafNodeSeqNum, coreNodeSeqNum);
        assertFalse(HtIo.isInCache(htio, leafNodeSeqNum));
        htio.writeNode(leafNode);
        assertTrue(HtIo.isInCache(htio, leafNodeSeqNum));

        // Now read the nodes from the same htio object, they should be in cache
        HTNode<HTInterval> coreRead = htio.readNode(coreNodeSeqNum);
        assertEquals(coreNode, coreRead);
        HTNode<HTInterval> leafRead = htio.readNode(leafNodeSeqNum);
        assertEquals(leafNode, leafRead);

        // Invalidate the cache
        HtIo.clearCache();

        // Re-read the nodes, they should now be read from disk and be in the
        // cache after
        assertFalse(HtIo.isInCache(htio, coreNodeSeqNum));
        coreRead = htio.readNode(coreNodeSeqNum);
        assertEquals(coreNode, coreRead);
        assertTrue(HtIo.isInCache(htio, coreNodeSeqNum));

        // Read the leaf node from disk
        assertFalse(HtIo.isInCache(htio, leafNodeSeqNum));
        leafRead = htio.readNode(leafNodeSeqNum);
        assertEquals(leafNode, leafRead);
        assertTrue(HtIo.isInCache(htio, leafNodeSeqNum));

        // Close the file and reopen a new htio object
        htio.closeFile();

        assertNotNull(fStateFile);
        htio = new HtIo<>(fStateFile,
                BLOCKSIZE,
                NB_CHILDREN,
                false,
                HtTestUtils.READ_FACTORY,
                HistoryTreeStub.NODE_FACTORY);

        fHtIo = htio;

        // Read the core node from the disk
        assertFalse(HtIo.isInCache(htio, coreNodeSeqNum));
        coreRead = htio.readNode(coreNodeSeqNum);
        assertEquals(coreNode, coreRead);

        // Read the leaf node from the disk
        assertFalse(HtIo.isInCache(htio, leafNodeSeqNum));
        leafRead = htio.readNode(leafNodeSeqNum);
        assertEquals(leafNode, leafRead);

        // Re-read the nodes, they should have been read from the cache
        assertTrue(HtIo.isInCache(htio, coreNodeSeqNum));
        coreRead = htio.readNode(coreNodeSeqNum);
        assertEquals(coreNode, coreRead);

        // Read the leaf node from cache
        assertTrue(HtIo.isInCache(htio, leafNodeSeqNum));
        leafRead = htio.readNode(leafNodeSeqNum);
        assertEquals(leafNode, leafRead);
    }

    /**
     * Test that the section at the end of the file where extra data can be
     * written works well
     *
     * @throws IOException
     *             Exception thrown by the file
     */
    @Test
    public void testExtraDataSave() throws IOException {
        writeBufferAtNodePos(2);
    }

    /**
     * Test that writing at the beginning of the file works well
     *
     * @throws IOException
     *             Exception thrown by the file
     */
    @Test
    public void testHeaderDataSave() throws IOException {
        writeBufferAtNodePos(-1);
    }

    /**
     * Test that writing data far beyond the node section end works.
     *
     * @throws IOException
     *             Exception thrown by the file
     */
    @Test
    public void testTooFarData() throws IOException {
        writeBufferAtNodePos(6);
    }

    private void writeBufferAtNodePos(int nodeOffset) throws IOException {
        HtIo<HTInterval, HTNode<HTInterval>> htio = fHtIo;
        assertNotNull(htio);

        int coreNodeSeqNum = 0;
        int leafNodeSeqNum = 1;

        // Add a core node and a leaf node
        HTNode<HTInterval> coreNode = createCoreNode(coreNodeSeqNum, -1);
        assertFalse(HtIo.isInCache(htio, coreNodeSeqNum));
        htio.writeNode(coreNode);
        assertTrue(HtIo.isInCache(htio, coreNodeSeqNum));

        HTNode<HTInterval> leafNode = createLeafNode(leafNodeSeqNum, coreNodeSeqNum);
        assertFalse(HtIo.isInCache(htio, leafNodeSeqNum));
        htio.writeNode(leafNode);
        assertTrue(HtIo.isInCache(htio, leafNodeSeqNum));

        // Write 3 integers at some position of the file
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(32);
        buffer.putInt(33);
        buffer.putInt(232);
        buffer.flip();

        try (FileOutputStream fcOut = htio.getFileWriter(nodeOffset)) {
            fcOut.write(buffer.array());
        }

        // Close the file and reopen a new htio object
        htio.closeFile();
        assertNotNull(fStateFile);
        htio = new HtIo<>(fStateFile,
                BLOCKSIZE,
                NB_CHILDREN,
                false,
                HtTestUtils.READ_FACTORY,
                HistoryTreeStub.NODE_FACTORY);

        fHtIo = htio;

        // Read the same 3 integers at the same position in the file
        byte[] bytes = new byte[12];
        htio.supplyATReader(nodeOffset).read(bytes);
        buffer = ByteBuffer.wrap(bytes);
        assertEquals(32, buffer.getInt());
        assertEquals(33, buffer.getInt());
        assertEquals(232, buffer.getInt());
    }
}
