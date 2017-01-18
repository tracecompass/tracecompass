/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.ITracePackageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageBookmarkElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFileElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An operation that exports a trace package to an archive
 *
 * @author Marc-Andre Laperle
 */
@SuppressWarnings("restriction")
public class TracePackageExportOperation extends AbstractTracePackageOperation {

    private static final String TRACE_EXPORT_TEMP_FOLDER = ".traceExport"; //$NON-NLS-1$

    private final TracePackageTraceElement[] fTraceExportElements;
    private final boolean fUseCompression;
    private final boolean fUseTar;
    private final Set<IResource> fResources;
    private IFolder fExportFolder;

    /**
     * Constructs a new export operation
     *
     * @param traceExportElements
     *            the trace elements to be exported
     * @param useCompression
     *            whether or not to use compression
     * @param useTar
     *            use tar format or zip
     * @param fileName
     *            the output file name
     */
    public TracePackageExportOperation(TracePackageTraceElement[] traceExportElements, boolean useCompression, boolean useTar, String fileName) {
        super(fileName);
        fTraceExportElements = traceExportElements;
        fUseCompression = useCompression;
        fUseTar = useTar;
        fResources = new HashSet<>();
    }

    /**
     * Run the operation. The status (result) of the operation can be obtained
     * with {@link #getStatus}
     *
     * @param progressMonitor
     *            the progress monitor to use to display progress and receive
     *            requests for cancellation
     */
    @Override
    public void run(IProgressMonitor progressMonitor) {

        try {
            int totalWork = getNbCheckedElements(fTraceExportElements) * 2;
            progressMonitor.beginTask(Messages.TracePackageExportOperation_GeneratingPackage, totalWork);

            fExportFolder = createExportFolder(progressMonitor);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element createElement = doc.createElement(ITracePackageConstants.TMF_EXPORT_ELEMENT);
            Node tmfNode = doc.appendChild(createElement);

            for (TracePackageTraceElement tracePackageElement : fTraceExportElements) {
                if (!isFilesChecked(tracePackageElement)) {
                    continue;
                }

                exportTrace(progressMonitor, tmfNode, tracePackageElement);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
            DOMSource source = new DOMSource(doc);
            StringWriter buffer = new StringWriter();
            StreamResult result = new StreamResult(buffer);
            transformer.transform(source, result);
            String content = buffer.getBuffer().toString();

            ModalContext.checkCanceled(progressMonitor);

            exportManifest(content);

            setStatus(exportToArchive(progressMonitor, totalWork));

            fExportFolder.delete(true, SubMonitor.convert(progressMonitor));

            progressMonitor.done();

        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        } catch (Exception e) {
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e));
        }
    }

    private IFolder createExportFolder(IProgressMonitor monitor) throws CoreException {
        IFolder folder = fTraceExportElements[0].getTraceElement().getProject().getResource().getFolder(TRACE_EXPORT_TEMP_FOLDER);
        if (folder.exists()) {
            folder.delete(true, null);
        }
        folder.create(IResource.FORCE | IResource.HIDDEN, true, SubMonitor.convert(monitor));
        return folder;
    }

    private void exportTrace(IProgressMonitor monitor, Node tmfNode, TracePackageTraceElement tracePackageElement) throws InterruptedException, CoreException {
        TmfTraceElement traceElement = tracePackageElement.getTraceElement();
        Element traceXmlElement = tmfNode.getOwnerDocument().createElement(ITracePackageConstants.TRACE_ELEMENT);
        traceXmlElement.setAttribute(ITracePackageConstants.TRACE_NAME_ATTRIB, traceElement.getResource().getName());
        traceXmlElement.setAttribute(ITracePackageConstants.TRACE_TYPE_ATTRIB, traceElement.getTraceType());
        Node traceNode = tmfNode.appendChild(traceXmlElement);

        for (TracePackageElement element : tracePackageElement.getChildren()) {
            ModalContext.checkCanceled(monitor);
            if (!element.isChecked()) {
                continue;
            }

            if (element instanceof TracePackageSupplFilesElement) {
                exportSupplementaryFiles(monitor, traceNode, traceElement, (TracePackageSupplFilesElement) element);
            } else if (element instanceof TracePackageBookmarkElement) {
                exportBookmarks(monitor, traceNode, (TracePackageBookmarkElement) element);
            } else if (element instanceof TracePackageFilesElement) {
                exportTraceFiles(monitor, traceNode, (TracePackageFilesElement) element);
            }

            monitor.worked(1);
        }
    }

    private void exportSupplementaryFiles(IProgressMonitor monitor, Node traceNode, TmfTraceElement traceElement, TracePackageSupplFilesElement element) throws InterruptedException, CoreException {
        Document doc = traceNode.getOwnerDocument();
        if (element.getChildren().length > 0) {

            IPath projectPath = traceElement.getProject().getPath();

            for (TracePackageElement child : element.getChildren()) {
                TracePackageSupplFileElement supplFile = (TracePackageSupplFileElement) child;
                ModalContext.checkCanceled(monitor);
                IResource res = supplFile.getResource();
                // project/.tracing/A/B/statistics.ht -> .tracing/A/B/statistics.ht
                IPath relativeToExportFolder = res.getFullPath().makeRelativeTo(projectPath);

                // project/.traceExport/.tracing/A/B
                IFolder folder = fExportFolder.getFolder(relativeToExportFolder.removeLastSegments(1));
                TraceUtils.createFolder(folder, SubMonitor.convert(monitor));

                res.refreshLocal(0, SubMonitor.convert(monitor));
                createExportResource(folder, res);
                Element suppFileElement = doc.createElement(ITracePackageConstants.SUPPLEMENTARY_FILE_ELEMENT);

                suppFileElement.setAttribute(ITracePackageConstants.SUPPLEMENTARY_FILE_NAME_ATTRIB, relativeToExportFolder.toString());
                traceNode.appendChild(suppFileElement);
            }

            IFolder suppFilesFolder = fExportFolder.getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
            fResources.add(suppFilesFolder);
        }
    }

    private void exportTraceFiles(IProgressMonitor monitor, Node traceNode, TracePackageFilesElement element) throws CoreException {
        Document doc = traceNode.getOwnerDocument();
        TmfTraceElement traceElement = ((TracePackageTraceElement) element.getParent()).getTraceElement();
        IResource resource = traceElement.getResource();

        final TmfTraceFolder tracesFolder = traceElement.getProject().getTracesFolder();
        if (tracesFolder == null) {
            return;
        }
        IPath traceFolderPath = tracesFolder.getPath();

        // project/Traces/A/B/Kernel -> A/B/Kernel
        IPath relativeToExportFolder = resource.getFullPath().makeRelativeTo(traceFolderPath);

        // project/.traceExport/A/B
        IFolder folder = fExportFolder.getFolder(relativeToExportFolder.removeLastSegments(1));
        TraceUtils.createFolder(folder, SubMonitor.convert(monitor));

        createExportResource(folder, resource);
        Element fileElement = doc.createElement(ITracePackageConstants.TRACE_FILE_ELEMENT);

        fileElement.setAttribute(ITracePackageConstants.TRACE_FILE_NAME_ATTRIB, relativeToExportFolder.toString());
        traceNode.appendChild(fileElement);

        // Always export the top-most folder containing the trace or the trace itself
        IResource exportedResource = fExportFolder.findMember(relativeToExportFolder.segment(0));
        fResources.add(exportedResource);
    }

    /**
     * Creates a linked resource in the specified folder
     *
     * @param exportFolder the folder that will contain the linked resource
     * @param res the resource to export
     * @throws CoreException when createLink fails
     * @return the created linked resource
     */
    private static IResource createExportResource(IFolder exportFolder, IResource res) throws CoreException {
        IResource ret = null;
        // Note: The resources cannot be HIDDEN or else they are ignored by ArchiveFileExportOperation
        if (res instanceof IFolder) {
            IFolder folder = exportFolder.getFolder(res.getName());
            folder.createLink(res.getLocationURI(), IResource.NONE, null);
            ret = folder;
        } else if (res instanceof IFile) {
            IFile file = exportFolder.getFile(res.getName());
            file.createLink(res.getLocationURI(), IResource.NONE, null);
            ret = file;
        }
        return ret;
    }

    private static void exportBookmarks(IProgressMonitor monitor, Node traceNode, TracePackageBookmarkElement element) throws CoreException, InterruptedException {
        Document doc = traceNode.getOwnerDocument();
        IFile bookmarksFile = ((TracePackageTraceElement) element.getParent()).getTraceElement().getBookmarksFile();
        if (bookmarksFile != null && bookmarksFile.exists()) {
            IMarker[] findMarkers = bookmarksFile.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);
            if (findMarkers.length > 0) {
                Element bookmarksXmlElement = doc.createElement(ITracePackageConstants.BOOKMARKS_ELEMENT);
                Node bookmarksNode = traceNode.appendChild(bookmarksXmlElement);

                for (IMarker marker : findMarkers) {
                    ModalContext.checkCanceled(monitor);

                    Element singleBookmarkXmlElement = doc.createElement(ITracePackageConstants.BOOKMARK_ELEMENT);
                    for (String key : marker.getAttributes().keySet()) {
                        singleBookmarkXmlElement.setAttribute(key, marker.getAttribute(key).toString());
                    }

                    bookmarksNode.appendChild(singleBookmarkXmlElement);
                }
            }
        }
    }

    private void exportManifest(String content) throws CoreException {
        IFile file = fExportFolder.getFile(ITracePackageConstants.MANIFEST_FILENAME);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        if (file.exists()) {
            file.setContents(inputStream, IResource.FORCE, null);
        } else {
            file.create(inputStream, IResource.FORCE | IResource.HIDDEN, null);
        }
        fResources.add(file);
    }

    private IStatus exportToArchive(IProgressMonitor monitor, int totalWork) throws InvocationTargetException, InterruptedException {
        ArchiveFileExportOperation op = new ArchiveFileExportOperation(new ArrayList<>(fResources), getFileName());
        op.setCreateLeadupStructure(false);
        //FIXME: Remove use of reflection when support for Eclipse 4.5 is dropped
        try {
            Method method = op.getClass().getMethod("setIncludeLinkedResources", boolean.class); //$NON-NLS-1$
            method.invoke(op, true);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
            // Ignored, setIncludeLinkedResources doesn't exist in Eclipse < 4.6
        }
        op.setUseCompression(fUseCompression);
        op.setUseTarFormat(fUseTar);
        op.run(SubMonitor.convert(monitor).newChild(totalWork / 2));

        return op.getStatus();
    }
}
