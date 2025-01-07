package org.example.zlib.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/15 16:09
 */
public class FileUtil {
    public static void main(String[] args) throws Exception {
        Scanner scanner=new Scanner(new File("C:\\Users\\69507\\Desktop\\电子书_文件目录_20241215160640.txt"));
        File file=new File("C:\\Users\\69507\\Desktop\\电子书_待下载.txt");
        file.createNewFile();
        PrintWriter writer=new PrintWriter(file);
        Set<String> book=new HashSet<>();
        while (scanner.hasNext()){
            String dirPath = scanner.nextLine();
            String[] split = dirPath.split("/");
            if(split.length==3){
                String bookName = split[2];
                if(bookName.contains("-")){
                    book.add(bookName);
                }
            }
            if(split.length==4){
                String bookName = split[2];
                book.remove(bookName);
            }
        }
        scanner.close();
        List<String> bookList = book.stream().sorted(Comparator.comparing(bookDir -> Integer.valueOf(bookDir.split("-")[0]))).collect(Collectors.toList());
        for (String bookName : bookList) {
            writer.println(bookName);
        }
        writer.close();
    }
}
