package org.example.zlib.util;

import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileFilter;
@Slf4j
public class PDFUtil {
    public static void main(String[] args) {
//        File baseDir=new File(DownloadUtil.BASE_FILE_PATH);
//        File[] fileDirList = baseDir.listFiles();
//        for (File fileDir : fileDirList) {
//            File[] files = fileDir.listFiles();
//            for (File file : files) {
//                if(file.getName().contains("pdf")){
//                    log.error("开始修改："+file.getName());
//                    file.setWritable(true);
//                    handlePDF(file.getAbsolutePath());
//                }
//            }
//        }
//        handlePDF("D:\\浏览器下载\\陷入我们的热恋.pdf");

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
            doc.save(filePath);
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}
