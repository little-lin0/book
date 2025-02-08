package org.example.zlib.util;

import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.*;
import nl.siegmann.epublib.epub.EpubReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.zlib.controller.testController.USER_HOME;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/13 16:16
 */
@Slf4j
public class EPUBUtil {
    public static final String BASE_FILE_PATH=USER_HOME+"\\Desktop\\电子书";
    public static void main(String[] args) throws Exception {
        FileWriter fw = new FileWriter(USER_HOME + "\\Desktop\\电子书_检测.txt", true);
        PrintWriter writer = new PrintWriter(fw);
        //        File baseDir=new File(BASE_FILE_PATH);
//        List<File> fileDirList = Arrays.stream(baseDir.listFiles()).filter(file->Integer.valueOf(file.getName().split("-")[0])>0).sorted(Comparator.comparing(file->Integer.valueOf(file.getName().split("-")[0]))).collect(Collectors.toList());
//        PrintWriter writer=new PrintWriter(new File("C:\\Users\\69507\\Desktop\\电子书_检测.txt"));
//        for (File fileDir : fileDirList) {
//            File[] files = fileDir.listFiles();
//            for (File file : files) {
//                if(file.getName().contains(".epub")){
//                    log.error("开始检测："+fileDir.getName());
//                    try {
//                        writer.println(fileDir.getName());
//                        writer.flush();
//                        handleEPUB(file.getAbsolutePath(),writer);
//                    } catch (Exception e) {
//                        log.error("检测："+fileDir.getName()+" 异常");
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
        handleEPUB("D:\\电子书\\132-拖延心理学\\拖延心理学.epub", writer);
        writer.close();
    }
    public static void handleEPUB(String filePath, PrintWriter writer) throws Exception {
        EpubReader epubReader = new EpubReader();
        MyEpubWriter epubWriter =new MyEpubWriter();
        InputStream inputStream = Files.newInputStream(Paths.get(filePath));
        Book book = epubReader.readEpub(inputStream);
//        List<Resource> resourceList = book.getContents();
        Resources resources = book.getResources();
        Collection<Resource> resourceList = resources.getAll();
        Iterator<Resource> iterator = resourceList.iterator();
        Set<String> removeList=new HashSet<>();
        while (iterator.hasNext()) {
            Resource resource=iterator.next();
            Document document = Jsoup.parse(resource.getInputStream(), resource.getInputEncoding(), "");
            for (Element element : document.getAllElements()) {
                String text = element.text();
                if(text.contains("Z-Library")||text.contains("电子书")){
//                    System.out.println(document);
                    removeList.add(resource.getHref());
                }
            }
        }
        String[] split = filePath.split("\\\\");
        if(!CollectionUtils.isEmpty(removeList)){
            log.warn("文件：{} 推广信息:{}",split[split.length-1],removeList);
            writer.println(removeList);
//            removeList.forEach(resources::remove);
        }

//        epubWriter.write(book, Files.newOutputStream(Paths.get("D:\\电子书\\40-小巷人家\\123.epub")));
    }



}
