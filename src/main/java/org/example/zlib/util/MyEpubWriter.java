package org.example.zlib.util;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.*;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/18 10:35
 */
public class MyEpubWriter{
    private static final Logger log = LoggerFactory.getLogger(MyEpubWriter.class);
    static final String EMPTY_NAMESPACE_PREFIX = "";
    private BookProcessor bookProcessor;

    public MyEpubWriter() {
        this(BookProcessor.IDENTITY_BOOKPROCESSOR);
    }

    public MyEpubWriter(BookProcessor bookProcessor) {
        this.bookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR;
        this.bookProcessor = bookProcessor;
    }

    public void write(Book book, OutputStream out) throws IOException {
        book = this.processBook(book);
        ZipOutputStream resultStream = new ZipOutputStream(out);
        this.writeMimeType(resultStream);
        this.writeContainer(resultStream);
        this.initTOCResource(book);
        this.writeResources(book, resultStream);
        this.writePackageDocument(book, resultStream);
        resultStream.close();
    }

    private Book processBook(Book book) {
        if (this.bookProcessor != null) {
            book = this.bookProcessor.processBook(book);
        }

        return book;
    }

    private void initTOCResource(Book book) {
        try {
            Resource tocResource = NCXDocument.createNCXResource(book);
            Resource currentTocResource = book.getSpine().getTocResource();
            if (currentTocResource != null) {
                book.getResources().remove(currentTocResource.getHref());
            }

            book.getSpine().setTocResource(tocResource);
            book.getResources().add(tocResource);
        } catch (Exception var4) {
            log.error("Error writing table of contents: " + var4.getClass().getName() + ": " + var4.getMessage());
        }

    }

    private void writeResources(Book book, ZipOutputStream resultStream) throws IOException {
        Iterator var3 = book.getResources().getAll().iterator();

        while(var3.hasNext()) {
            Resource resource = (Resource)var3.next();
            this.writeResource(resource, resultStream);
        }

    }

    private void writeResource(Resource resource, ZipOutputStream resultStream) throws IOException {
        if (resource != null) {
            try {
                resultStream.putNextEntry(new ZipEntry("EPUB/" + resource.getHref()));
                InputStream inputStream = resource.getInputStream();
                IOUtil.copy(inputStream, resultStream);
                inputStream.close();
            } catch (Exception var4) {
                log.error(var4.getMessage(), var4);
            }

        }
    }

    private void writePackageDocument(Book book, ZipOutputStream resultStream) throws IOException {
        resultStream.putNextEntry(new ZipEntry("EPUB/content.opf"));
        XmlSerializer xmlSerializer = EpubProcessorSupport.createXmlSerializer(resultStream);
        MyPackageDocumentWriter.write(this, xmlSerializer, book);
        xmlSerializer.flush();
    }

    private void writeContainer(ZipOutputStream resultStream) throws IOException {
        resultStream.putNextEntry(new ZipEntry("META-INF/container.xml"));
        Writer out = new OutputStreamWriter(resultStream);
        out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        out.write("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n");
        out.write("\t<rootfiles>\n");
        out.write("\t\t<rootfile full-path=\"EPUB/content.opf\" media-type=\"application/oebps-package+xml\"/>\n");
        out.write("\t</rootfiles>\n");
        out.write("</container>");
        out.flush();
    }

    private void writeMimeType(ZipOutputStream resultStream) throws IOException {
        ZipEntry mimetypeZipEntry = new ZipEntry("mimetype");
        mimetypeZipEntry.setMethod(0);
        byte[] mimetypeBytes = MediatypeService.EPUB.getName().getBytes();
        mimetypeZipEntry.setSize((long)mimetypeBytes.length);
        mimetypeZipEntry.setCrc(this.calculateCrc(mimetypeBytes));
        resultStream.putNextEntry(mimetypeZipEntry);
        resultStream.write(mimetypeBytes);
    }

    private long calculateCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    String getNcxId() {
        return "ncx";
    }

    String getNcxHref() {
        return "toc.ncx";
    }

    String getNcxMediaType() {
        return MediatypeService.NCX.getName();
    }

    public BookProcessor getBookProcessor() {
        return this.bookProcessor;
    }

    public void setBookProcessor(BookProcessor bookProcessor) {
        this.bookProcessor = bookProcessor;
    }


}
