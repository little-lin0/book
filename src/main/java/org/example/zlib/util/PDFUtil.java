package org.example.zlib.util;

import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.zlib.controller.testController;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class PDFUtil {
    public static final String BASE_FILE_PATH="D:\\电子书";
    public static void main(String[] args) {
//        File baseDir=new File(BASE_FILE_PATH);
//        List<File> fileDirList = Arrays.stream(baseDir.listFiles()).filter(file->Integer.valueOf(file.getName().split("-")[0])>0).sorted(Comparator.comparing(file->Integer.valueOf(file.getName().split("-")[0]))).collect(Collectors.toList());
//        for (File fileDir : fileDirList) {
//
//            File[] files = fileDir.listFiles();
////            Optional<File> edit = Arrays.stream(files).filter(file -> file.getName().contains("已修改")).findFirst();
////            if(!edit.isPresent()){
////                continue;
////            }
////            if(Integer.valueOf(fileDir.getName().split("-")[0])>67){
////                continue;
////            }
//            if(Integer.valueOf(fileDir.getName().split("-")[0])<161){
//                continue;
//            }
//            for (File file : files) {
////                try {
////                    PDDocument  doc = PDDocument.load(file);
////                } catch (IOException e) {
////                    log.error("加载："+file.getName()+"异常");
////                }
//                if(file.getName().contains("pdf")){
//                    log.error("开始修改："+file.getName());
//
////                    if(file.getName().contains("(已修改)")){
////                boolean rename = file.renameTo(new File(file.getAbsolutePath().replace("(已修改)", "")));
////                log.error("rename:"+rename);
////                    }
////                    else {
////                        file.delete();
////                    }
////                    file.setWritable(true);
//                    try {
//                        handlePDF(file.getAbsolutePath());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        log.error("修改："+file.getName()+"异常");
//                    }
//                }
//            }
//        }
//        handlePDF("D:\\电子书\\188-不原谅也没关系\\不原谅也没关系.pdf");

    }
    public static void handlePDF (String filePath) {
        PDDocument doc = null;
        String content = "";
        try {
            //加载一个pdf对象
            doc = PDDocument.load(new File(filePath));
            //获取一个PDFTextStripper文本剥离对象
            int numberOfPages = doc.getNumberOfPages();
            PDPageTree pages = doc.getPages();
            PDFTextStripper textStripper = new PDFTextStripper();
//            for (int i = 0; i <= numberOfPages; i++) {
                textStripper.setStartPage(numberOfPages);
                textStripper.setEndPage(numberOfPages);
                content = textStripper.getText(doc);
                if(content.contains("Z-Library")||content.contains("book")){
                    System.out.println(content);
//                    doc.removePage(1);
//                    doc.removePage(1);
//                                doc.removePage(66);
                doc.removePage(numberOfPages-1);

                }
//            }
//            File fileDir = new File(DownloadUtil.BASE_FILE_PATH, "已处理pdf");
//            fileDir.mkdirs();
//            File file=new File(fileDir,"123.pdf");
//            file.createNewFile();
            doc.save(filePath.replace(".pdf","(已修改).pdf"));
            doc.close();
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
