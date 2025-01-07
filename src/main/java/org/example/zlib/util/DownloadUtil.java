package org.example.zlib.util;

import lombok.extern.slf4j.Slf4j;
import org.example.zlib.controller.testController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/15 17:12
 */
@Slf4j
public class DownloadUtil {
    public static final String BASE_FILE_PATH="D:\\电子书";
    public static void main(String[] args){
//        1. 扫描文件夹、目录下为空进行获取文件
//        2. 记录操作账号、下载书籍计数，到达下载数（10）切换账号  caret-scroll__title
//        3. 日志记录书籍下载情况
//        4. 检测文件推广页删除
        try {
            Scanner scanner=new Scanner(new File("C:\\Users\\69507\\Desktop\\电子书_待下载.txt"));
//        ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(3,3,0L, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
            List<String> bookList=new ArrayList<>();
            List<String> noGetBookList=new ArrayList<>();
            boolean isChangeDir=true;
            while (scanner.hasNext()){
                String nextLine = scanner.nextLine();
                if(nextLine.equals("----无")){
                    continue;
                }
                if(nextLine.equals("---------新建")){
                    isChangeDir=false;
                    continue;
                }
                String[] split = nextLine.split("-");
                String fileDir = isChangeDir?nextLine.replace("-" + split[2], ""):nextLine;
                File file=new File(BASE_FILE_PATH,fileDir);
                if(file.exists()){
                    continue;
                }
                try {
                    testController.getBookFileByName(fileDir,split[2],null,bookList,noGetBookList);
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
            File file=new File("C:\\Users\\69507\\Desktop\\电子书_下载失败.txt");
            file.createNewFile();
            PrintWriter writer=new PrintWriter(file);
            for (String book : noGetBookList) {
                writer.println(book);
            }
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
