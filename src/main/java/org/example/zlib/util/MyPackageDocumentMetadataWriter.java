package org.example.zlib.util;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.util.StringUtil;
import org.xmlpull.v1.XmlSerializer;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2025/1/14 16:05
 */
public class MyPackageDocumentMetadataWriter {
    public MyPackageDocumentMetadataWriter() {
    }

    public static void writeMetaData(Book book, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("http://www.idpf.org/2007/opf", "metadata");
//        serializer.setPrefix("dc", "http://purl.org/dc/elements/1.1/");
//        serializer.setPrefix("opf", "http://www.idpf.org/2007/opf");
        writeIdentifiers(book.getMetadata().getIdentifiers(), serializer);
        writeSimpleMetdataElements("title", book.getMetadata().getTitles(), serializer);
        writeSimpleMetdataElements("subject", book.getMetadata().getSubjects(), serializer);
        writeSimpleMetdataElements("description", book.getMetadata().getDescriptions(), serializer);
        writeSimpleMetdataElements("publisher", book.getMetadata().getPublishers(), serializer);
        writeSimpleMetdataElements("type", book.getMetadata().getTypes(), serializer);
        writeSimpleMetdataElements("rights", book.getMetadata().getRights(), serializer);
        Iterator var2 = book.getMetadata().getAuthors().iterator();

        Author author;
        while(var2.hasNext()) {
            author = (Author)var2.next();
            serializer.startTag("http://purl.org/dc/elements/1.1/", "creator");
            serializer.attribute("http://www.idpf.org/2007/opf", "role", author.getRelator().getCode());
            serializer.attribute("http://www.idpf.org/2007/opf", "file-as", author.getLastname() + ", " + author.getFirstname());
            serializer.text(author.getFirstname() + " " + author.getLastname());
            serializer.endTag("http://purl.org/dc/elements/1.1/", "creator");
        }

        var2 = book.getMetadata().getContributors().iterator();

        while(var2.hasNext()) {
            author = (Author)var2.next();
            serializer.startTag("http://purl.org/dc/elements/1.1/", "contributor");
            serializer.attribute("http://www.idpf.org/2007/opf", "role", author.getRelator().getCode());
            serializer.attribute("http://www.idpf.org/2007/opf", "file-as", author.getLastname() + ", " + author.getFirstname());
            serializer.text(author.getFirstname() + " " + author.getLastname());
            serializer.endTag("http://purl.org/dc/elements/1.1/", "contributor");
        }

        var2 = book.getMetadata().getDates().iterator();

        while(var2.hasNext()) {
            Date date = (Date)var2.next();
            serializer.startTag("http://purl.org/dc/elements/1.1/", "date");
            if (date.getEvent() != null) {
                serializer.attribute("http://www.idpf.org/2007/opf", "event", date.getEvent().toString());
            }

            serializer.text(date.getValue());
            serializer.endTag("http://purl.org/dc/elements/1.1/", "date");
        }

        if (StringUtil.isNotBlank(book.getMetadata().getLanguage())) {
            serializer.startTag("http://purl.org/dc/elements/1.1/", "language");
            serializer.text(book.getMetadata().getLanguage());
            serializer.endTag("http://purl.org/dc/elements/1.1/", "language");
        }

        if (book.getMetadata().getOtherProperties() != null) {
            var2 = book.getMetadata().getOtherProperties().entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<QName, String> mapEntry = (Map.Entry)var2.next();
                serializer.startTag(((QName)mapEntry.getKey()).getNamespaceURI(), ((QName)mapEntry.getKey()).getLocalPart());
                serializer.text((String)mapEntry.getValue());
                serializer.endTag(((QName)mapEntry.getKey()).getNamespaceURI(), ((QName)mapEntry.getKey()).getLocalPart());
            }
        }

        if (book.getCoverImage() != null) {
            serializer.startTag("http://www.idpf.org/2007/opf", "meta");
            serializer.attribute("", "name", "cover");
            serializer.attribute("", "content", book.getCoverImage().getId());
            serializer.endTag("http://www.idpf.org/2007/opf", "meta");
        }

        serializer.startTag("http://www.idpf.org/2007/opf", "meta");
        serializer.attribute("", "name", "generator");
        serializer.attribute("", "content", "EPUBLib version 3.0");
        serializer.endTag("http://www.idpf.org/2007/opf", "meta");
        serializer.endTag("http://www.idpf.org/2007/opf", "metadata");
    }

    private static void writeSimpleMetdataElements(String tagName, List<String> values, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        Iterator var3 = values.iterator();

        while(var3.hasNext()) {
            String value = (String)var3.next();
            if (!StringUtil.isBlank(value)) {
                serializer.startTag("http://purl.org/dc/elements/1.1/", tagName);
                serializer.text(value);
                serializer.endTag("http://purl.org/dc/elements/1.1/", tagName);
            }
        }

    }

    private static void writeIdentifiers(List<Identifier> identifiers, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        Identifier bookIdIdentifier = Identifier.getBookIdIdentifier(identifiers);
        if (bookIdIdentifier != null) {
            serializer.startTag("http://purl.org/dc/elements/1.1/", "identifier");
            serializer.attribute("", "id", "id");
            serializer.attribute("http://www.idpf.org/2007/opf", "scheme", bookIdIdentifier.getScheme());
            serializer.text(bookIdIdentifier.getValue());
            serializer.endTag("http://purl.org/dc/elements/1.1/", "identifier");
            Iterator var3 = identifiers.subList(1, identifiers.size()).iterator();

            while(var3.hasNext()) {
                Identifier identifier = (Identifier)var3.next();
                if (identifier != bookIdIdentifier) {
                    serializer.startTag("http://purl.org/dc/elements/1.1/", "identifier");
                    serializer.attribute("http://www.idpf.org/2007/opf", "scheme", identifier.getScheme());
                    serializer.text(identifier.getValue());
                    serializer.endTag("http://purl.org/dc/elements/1.1/", "identifier");
                }
            }

        }
    }
}
