package org.example.zlib.util;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Guide;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.PackageDocumentBase;
import nl.siegmann.epublib.epub.PackageDocumentMetadataWriter;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.StringUtil;
import org.example.zlib.util.MyEpubWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlSerializer;

public class MyPackageDocumentWriter extends PackageDocumentBase {
    private static final Logger log = LoggerFactory.getLogger(MyPackageDocumentWriter.class);

    public MyPackageDocumentWriter() {
    }

    public static void write(MyEpubWriter epubWriter, XmlSerializer serializer, Book book) throws IOException {
        try {
            serializer.startDocument("UTF-8", false);
            serializer.setPrefix("opf", "http://www.idpf.org/2007/opf");
            serializer.setPrefix("dc", "http://purl.org/dc/elements/1.1/");
            serializer.startTag("http://www.idpf.org/2007/opf", "package");
            serializer.attribute("", "version", "2.0");
            serializer.attribute("", "unique-identifier", "BookId");
            PackageDocumentMetadataWriter.writeMetaData(book, serializer);
            writeManifest(book, epubWriter, serializer);
            writeSpine(book, epubWriter, serializer);
            writeGuide(book, epubWriter, serializer);
            serializer.endTag("http://www.idpf.org/2007/opf", "package");
            serializer.endDocument();
            serializer.flush();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    private static void writeSpine(Book book, MyEpubWriter epubWriter, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("http://www.idpf.org/2007/opf", "spine");
        serializer.attribute("", "toc", book.getSpine().getTocResource().getId());
        if (book.getCoverPage() != null && book.getSpine().findFirstResourceById(book.getCoverPage().getId()) < 0) {
            serializer.startTag("http://www.idpf.org/2007/opf", "itemref");
            serializer.attribute("", "idref", book.getCoverPage().getId());
            serializer.attribute("", "linear", "no");
            serializer.endTag("http://www.idpf.org/2007/opf", "itemref");
        }

        writeSpineItems(book.getSpine(), serializer);
        serializer.endTag("http://www.idpf.org/2007/opf", "spine");
    }

    private static void writeManifest(Book book, MyEpubWriter epubWriter, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("http://www.idpf.org/2007/opf", "manifest");
        serializer.startTag("http://www.idpf.org/2007/opf", "item");
        serializer.attribute("", "id", epubWriter.getNcxId());
        serializer.attribute("", "href", epubWriter.getNcxHref());
        serializer.attribute("", "media-type", epubWriter.getNcxMediaType());
        serializer.endTag("http://www.idpf.org/2007/opf", "item");
        Iterator var3 = getAllResourcesSortById(book).iterator();

        while(var3.hasNext()) {
            Resource resource = (Resource)var3.next();
            writeItem(book, resource, serializer);
        }

        serializer.endTag("http://www.idpf.org/2007/opf", "manifest");
    }

    private static List<Resource> getAllResourcesSortById(Book book) {
        List<Resource> allResources = new ArrayList(book.getResources().getAll());
        Collections.sort(allResources, new Comparator<Resource>() {
            public int compare(Resource resource1, Resource resource2) {
                return resource1.getId().compareToIgnoreCase(resource2.getId());
            }
        });
        return allResources;
    }

    private static void writeItem(Book book, Resource resource, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        if (resource != null && (resource.getMediaType() != MediatypeService.NCX || book.getSpine().getTocResource() == null)) {
            if (StringUtil.isBlank(resource.getId())) {
                log.error("resource id must not be empty (href: " + resource.getHref() + ", mediatype:" + resource.getMediaType() + ")");
            } else if (StringUtil.isBlank(resource.getHref())) {
                log.error("resource href must not be empty (id: " + resource.getId() + ", mediatype:" + resource.getMediaType() + ")");
            } else if (resource.getMediaType() == null) {
                log.error("resource mediatype must not be empty (id: " + resource.getId() + ", href:" + resource.getHref() + ")");
            } else {
                serializer.startTag("http://www.idpf.org/2007/opf", "item");
                serializer.attribute("", "id", resource.getId());
                serializer.attribute("", "href", resource.getHref());
                serializer.attribute("", "media-type", resource.getMediaType().getName());
                serializer.endTag("http://www.idpf.org/2007/opf", "item");
            }
        }
    }

    private static void writeSpineItems(Spine spine, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        for(Iterator var2 = spine.getSpineReferences().iterator(); var2.hasNext(); serializer.endTag("http://www.idpf.org/2007/opf", "itemref")) {
            SpineReference spineReference = (SpineReference)var2.next();
            serializer.startTag("http://www.idpf.org/2007/opf", "itemref");
            serializer.attribute("", "idref", spineReference.getResourceId());
            if (!spineReference.isLinear()) {
                serializer.attribute("", "linear", "no");
            }
        }

    }

    private static void writeGuide(Book book, MyEpubWriter epubWriter, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("http://www.idpf.org/2007/opf", "guide");
        ensureCoverPageGuideReferenceWritten(book.getGuide(), epubWriter, serializer);
        Iterator var3 = book.getGuide().getReferences().iterator();

        while(var3.hasNext()) {
            GuideReference reference = (GuideReference)var3.next();
            writeGuideReference(reference, serializer);
        }

        serializer.endTag("http://www.idpf.org/2007/opf", "guide");
    }

    private static void ensureCoverPageGuideReferenceWritten(Guide guide, MyEpubWriter epubWriter, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        if (guide.getGuideReferencesByType(GuideReference.COVER).isEmpty()) {
            Resource coverPage = guide.getCoverPage();
            if (coverPage != null) {
                writeGuideReference(new GuideReference(guide.getCoverPage(), GuideReference.COVER, GuideReference.COVER), serializer);
            }

        }
    }

    private static void writeGuideReference(GuideReference reference, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        if (reference != null) {
            serializer.startTag("http://www.idpf.org/2007/opf", "reference");
            serializer.attribute("", "type", reference.getType());
            serializer.attribute("", "href", reference.getCompleteHref());
            if (StringUtil.isNotBlank(reference.getTitle())) {
                serializer.attribute("", "title", reference.getTitle());
            }

            serializer.endTag("http://www.idpf.org/2007/opf", "reference");
        }
    }
}
