package org.example.zlib.util;

import lombok.extern.slf4j.Slf4j;
import org.example.zlib.controller.testController;

import java.io.*;
import java.util.*;
import static org.example.zlib.controller.testController.USER_HOME;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/15 17:12
 */
@Slf4j
public class DownloadUtil {
    public static final String BASE_FILE_PATH="D:\\电子书";
    public static List<String> bookList=new ArrayList<>();
    public static List<String> noGetBookList=new ArrayList<>();
    public static List<String> exceptionBookList=new ArrayList<>();

    public static void main(String[] args){
//        1. 扫描文件夹、目录下为空进行获取文件
//        2. 记录操作账号、下载书籍计数，到达下载数（10）切换账号  caret-scroll__title
//        3. 日志记录书籍下载情况
//        4. 检测文件推广页删除
        try {
            Scanner scanner=new Scanner(new File(USER_HOME+"\\Desktop\\电子书.txt"));
            FileWriter fw = new FileWriter(USER_HOME + "\\Desktop\\电子书_检测.txt", true);
            PrintWriter writer = new PrintWriter(fw);
            boolean isChangeDir=false;
            int xh=238;
            while (scanner.hasNext()){
                String nextLine = scanner.nextLine();
//                if(nextLine.equals("----无")){
//                    continue;
//                }
//                if(nextLine.equals("---------新建")){
//                    isChangeDir=false;
//                    continue;
//                }
                String[] split = nextLine.split("-");
//                String fileDir = isChangeDir?nextLine.replace("-" + split[2], ""):nextLine;
                String fileDir=xh+"-"+nextLine;
                xh++;
                File file=new File(BASE_FILE_PATH,fileDir);
                if(file.exists()){
                    continue;
                }
                try {
                    testController.getBookFileByName(fileDir,split[1],null,writer);
                } catch (Exception e) {
                    e.printStackTrace();
                    if(e.getMessage().equals("no account")){
                        break;
                    }
                    log.error("下载书籍{}异常",nextLine);
                    noGetBookList.add(nextLine);
                }
            }
            log.info("已下载书籍列表"+bookList);
            File file=new File(USER_HOME+"\\Desktop\\电子书_下载失败.txt");
            file.createNewFile();
            PrintWriter failWriter=new PrintWriter(file);
            for (String book : noGetBookList) {
                failWriter.println(book);
            }
            failWriter.println("====文件异常====");
            for (String book : exceptionBookList) {
                failWriter.println(book);
            }
            failWriter.close();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
