/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 243347 TarFile should not throw NPE in finalize()
 * Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Bug 463633
 * Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Copied to Trace Compass to work around bug 501379
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;


/**
 * Reads a .tar or .tar.gz archive file, providing an index enumeration
 * and allows for accessing an InputStream for arbitrary files in the
 * archive.
 */
public class TarFile {
	private File file;
	private TarInputStream entryEnumerationStream;
	private TarEntry curEntry;
	private TarInputStream entryStream;

	private InputStream internalEntryStream;
    // This field is just to prevent try with resources error and keep the code
    // similar to the original
    private InputStream fInputStream;

	/**
	 * Create a new TarFile for the given file.
	 *
	 * @param file the file
	 * @throws TarException on Tar error (bad format, etc)
	 * @throws IOException on i/o error
	 */
	public TarFile(File file) throws TarException, IOException {
		this.file = file;

		fInputStream = new FileInputStream(file);
		// First, check if it's a GZIPInputStream.
		try {
			fInputStream = new GZIPInputStream(fInputStream);
		} catch(IOException e) {
			//If it is not compressed we close
			//the old one and recreate
			fInputStream.close();
			fInputStream = new FileInputStream(file);
		}
		try {
			entryEnumerationStream = new TarInputStream(fInputStream);
		} catch (TarException | IOException ex) {
			fInputStream.close();
			throw ex;
		}
		curEntry = entryEnumerationStream.getNextEntry();
	}

	/**
	 * Close the tar file input stream.
	 *
	 * @throws IOException if the file cannot be successfully closed
	 */
	public void close() throws IOException {
		if (entryEnumerationStream != null) {
            entryEnumerationStream.close();
        }
		if (internalEntryStream != null) {
            internalEntryStream.close();
        }
	}

	/**
	 * Create a new TarFile for the given path name.
	 *
	 * @param filename the file name to create the TarFile from
     * @throws TarException on Tar error (bad format, etc)
     * @throws IOException on i/o error
	 */
	public TarFile(String filename) throws TarException, IOException {
		this(new File(filename));
	}

	/**
	 * Returns an enumeration cataloguing the tar archive.
	 *
	 * @return enumeration of all files in the archive
	 */
	public Enumeration<TarEntry> entries() {
		return new Enumeration<TarEntry>() {
			@Override
			public boolean hasMoreElements() {
				return (curEntry != null);
			}

			@Override
			public TarEntry nextElement() {
				TarEntry oldEntry = curEntry;
				try {
					curEntry = entryEnumerationStream.getNextEntry();
				} catch(TarException e) {
					curEntry = null;
				} catch(IOException e) {
					curEntry = null;
				}
				return oldEntry;
			}
		};
	}

	/**
	 * Returns a new InputStream for the given file in the tar archive.
	 *
	 * @param entry the entry to get the InputStream from
	 * @return an input stream for the given file
     * @throws TarException on Tar error (bad format, etc)
     * @throws IOException on i/o error
	 */
	public InputStream getInputStream(TarEntry entry) throws TarException, IOException {
		if(entryStream == null || !entryStream.skipToEntry(entry)) {
			if (internalEntryStream != null) {
				internalEntryStream.close();
			}
			internalEntryStream = new FileInputStream(file);
			// First, check if it's a GZIPInputStream.
			try {
				internalEntryStream = new GZIPInputStream(internalEntryStream);
			} catch(IOException e) {
				//If it is not compressed we close
				//the old one and recreate
				internalEntryStream.close();
				internalEntryStream = new FileInputStream(file);
			}
			entryStream = new TarInputStream(internalEntryStream, entry) {
				@Override
				public void close() {
					// Ignore close() since we want to reuse the stream.
				}
			};
		}
		return entryStream;
	}

	/**
	 * Returns the path name of the file this archive represents.
	 *
	 * @return path
	 */
	public String getName() {
		return file.getPath();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}
}
