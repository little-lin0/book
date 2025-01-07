package org.example.zlib.util;

import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.*;
import nl.siegmann.epublib.epub.EpubProcessorSupport;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import org.apache.tika.parser.epub.EpubParser;
import org.eclipse.mylyn.docs.epub.core.EPUB;
import org.eclipse.mylyn.docs.epub.internal.EPUBFileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/13 16:16
 */
@Slf4j
public class EPUBUtil {
    public static void main(String[] args) throws Exception {
        handleEPUB("D:\\电子书\\40-小巷人家\\小巷人家.epub");
    }
    public static void handleEPUB(String filePath) throws Exception {
        EpubReader epubReader = new EpubReader();
        MyEpubWriter epubWriter =new MyEpubWriter();
        InputStream inputStream = Files.newInputStream(Paths.get(filePath));
        Book book = epubReader.readEpub(inputStream);
        List<Resource> resourceList = book.getContents();
        Resources resources = book.getResources();
//        Collection<Resource> resourceList = resources.getAll();
        Iterator<Resource> iterator = resourceList.iterator();
        List<String> removeList=new ArrayList<>();
        while (iterator.hasNext()) {
            Resource resource=iterator.next();
            Document document = Jsoup.parse(resource.getInputStream(), resource.getInputEncoding(), "");
            String text = document.text();
            if(text.contains("Z-Library")||text.contains("电子书")){
                System.out.println(document);
                removeList.add(resource.getHref());
            }
        }
        String[] split = filePath.split("\\\\");
        if(!CollectionUtils.isEmpty(removeList)){
            log.warn("文件：{} 推广信息:{}",split[split.length-1],removeList);
//            removeList.forEach(resources::remove);
        }

        epubWriter.write(book, Files.newOutputStream(Paths.get("D:\\电子书\\40-小巷人家\\123.epub")));
    }



}
