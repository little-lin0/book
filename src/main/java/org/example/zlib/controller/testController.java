package org.example.zlib.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.RequestBody;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.zlib.util.EPUBUtil;
import org.example.zlib.util.PDFUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.pdfbox.cos.COSInteger.get;
import static org.example.zlib.util.DownloadUtil.*;
import static org.springframework.util.ResourceUtils.getFile;

/**
 * @author kit
 * @version 1.0
 * @description: TODO
 * @date 2024/12/12 9:23
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class testController {
    public static final String BASE_URL="https://zh.z-lib.gs";
    public static final Set<String> fileTypeSet=new HashSet<>();
    public static final Set<String> emailSet=new LinkedHashSet<>();
    public static  Iterator<String> emailIterator=null;
    public static  String login_email=null;
    static {
        emailSet.add("695072376@qq.com");
        emailSet.add("2103590261@qq.com");
        emailSet.add("1094667053@qq.com");
        emailIterator=emailSet.iterator();
        login_email=emailIterator.next();
        fileTypeSet.add(".pdf");
        fileTypeSet.add(".epub");
    }
    public static final String BASE_FILE_PATH="D:\\电子书";
    public static  JSONObject token=new JSONObject();

    public static String USER_HOME=System.getProperty("user.home");


    @PostMapping("")
    public static void getBookFileByName(@RequestParam String name, @RequestParam String authorName, HttpServletResponse response, PrintWriter writer) throws IOException {
        String realName = name.split("-")[1];
        log.warn("开始获取书籍：{}",realName);
        Document root = getSearchHtmlRoot(BASE_URL+"/s/"+realName+authorName);
        Element body = root.getElementsByTag("body").get(0);
        Element searchResultBox = body.getElementById("searchResultBox");
        String limitNum = body.getElementsByClass("caret-scroll__tile").get(0)
                .getElementsByClass("caret-scroll__title").text();
        log.warn("账号：{}已下载：{}",login_email,limitNum);
        if(limitNum.equals("10/10")){
            if(emailIterator.hasNext()){
                login_email=emailIterator.next();
                token=new JSONObject();
                getBookFileByName(name,authorName,null, writer);
                return;
            }else {
                throw  new RuntimeException("no account");
            }
        }
        Elements elements = searchResultBox.getAllElements();
        List<Element> bookCardList = elements.stream().filter(e -> e.tag().getName().equals("z-bookcard")).collect(Collectors.toList());
        for (Element bookCard : bookCardList) {
            Attributes attributes = bookCard.attributes();
            String bookId = attributes.get("id");
            String url = BASE_URL+attributes.get("href");
            String filesize = attributes.get("filesize");
            Elements title = bookCard.getElementsByAttributeValue("slot", "title");
            Elements author = bookCard.getElementsByAttributeValue("slot", "author");
            if(title.text().contains(realName)&&author.text().contains(authorName)){
               log.warn("找到 书名：{} 作者：{} 大小：{}",title.text(),author.text(),filesize);
                getFile(response,url,name,bookId,writer);
                bookList.add(name);
                return;
            }
        }
        log.warn("未找到 书名：{} 作者：{}",realName,authorName);
        noGetBookList.add(name);
    }
    @GetMapping("downLoad")
    public static void getFile(HttpServletResponse response, @RequestParam String url, @RequestParam String name, @RequestParam String bookId, PrintWriter writer) throws IOException {
        Document root = getDetailHtmlRoot(url);
        Element body = root.getElementsByTag("body").get(0);
//        Element button = body.get(0).getElementsByClass("book-actions-buttons").get(0);
        Element addDownloadedBook = body.getElementsByClass("addDownloadedBook").get(0);
        String fileUrl = BASE_URL + addDownloadedBook.attributes().get("href");
        String fileType = "."+addDownloadedBook.getElementsByClass("book-property__extension").get(0).text();
        Elements convertElements = body.getElementsByClass("converterLink no-js__unavailable");
        Map<String,String> convertUrlMap=new HashMap<>();
        for (Element convertElement : convertElements) {
            String convertType = convertElement.text().toLowerCase();
            convertUrlMap.put("."+ convertType,fileUrl+"?convertedTo="+convertType);
        }
        List<String> downLoadFileTypeList=new ArrayList<>(fileTypeSet);
        String filePath=BASE_FILE_PATH+ File.separator+name;
        File dir=new File(filePath);
        dir.mkdirs();
        String realName = name.substring(name.indexOf("-") + 1);
        if(fileTypeSet.contains(fileType)){
            downLoadFileTypeList.remove(fileType);
            downLoadFile(response,fileUrl,fileType,realName,filePath,writer);
        }
        downLoadExtraTypeFile(response,bookId,realName,downLoadFileTypeList,filePath,convertUrlMap,writer);
    }

    public static void downLoadExtraTypeFile(HttpServletResponse response, String bookId, String name, List<String> fileTypeList, String filePath, Map<String, String> convertUrlMap, PrintWriter writer) throws IOException {
        String extraFileUrl=BASE_URL+"/papi/book/"+bookId+"/formats";
        Map<String,String> fileTypeUrlMap=getExtraFileUrlList(extraFileUrl);
        if(MapUtil.isEmpty(fileTypeUrlMap)){
            if(CollectionUtils.isEmpty(convertUrlMap)){
                log.warn("书名：{}找不到其他格式",name);
                return;
            }
        }
        for (String fileType : fileTypeList) {
            String fileurl = convertUrlMap.get(fileType);
            if (Objects.nonNull(fileurl)) {
                log.warn("书名：{} 下载其他其他格式：{}",name,fileType);
                downLoadFile(response, fileurl, fileType, name, filePath, writer);
            } else {
                fileurl = fileTypeUrlMap.get(fileType);
                if (Objects.nonNull(fileurl)) {
                    fileurl = BASE_URL + fileurl;
                    log.warn("书名：{} 下载其他其他格式：{}",name,fileType);
                    downLoadFile(response, fileurl, fileType, name, filePath, writer);
                }
            }
        }
    }

    public static Map<String, String> getExtraFileUrlList(String extraFileUrl) throws IOException {
        Map<String, String> resultMap=new HashMap<>();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        JSONObject token = getToken(login_email);
        String cookie="remix_userkey="+token.getString("user_key")+";remix_userid="+token.getString("user_id")+";selectedSiteMode=books";
        Request request = new Request.Builder()
                .url(extraFileUrl)
                .method("GET", null)
                .addHeader("Cookie", cookie)
                .build();
        Response response = client.newCall(request).execute();
        JSONObject result = JSONObject.parseObject(response.body().string());
        JSONArray books = result.getJSONArray("books");
        if(CollectionUtils.isEmpty(books)){
            return resultMap;
        }
        for (Object o : books) {
            JSONObject book = (JSONObject) o;
            resultMap.put("."+book.getString("extension"),book.getString("href"));
        }
        return resultMap;
    }

    public static Document getDetailHtmlRoot(String url) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        JSONObject token = getToken(login_email);
        String cookie="remix_userkey="+token.getString("user_key")+";remix_userid="+token.getString("user_id");
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Cookie", cookie)
                .build();
        Response response = client.newCall(request).execute();
        String html = response.body().string();
        return Jsoup.parse(html);
    }

    public static Document  getSearchHtmlRoot(String url) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        JSONObject token = getToken(login_email);
        String cookie="remix_userkey="+token.getString("user_key")+";remix_userid="+token.getString("user_id")+";selectedSiteMode=books";
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie",cookie)
                .build();
        Response response = client.newCall(request).execute();
        return Jsoup.parse(response.body().string());
    }
    public static  void downLoadFile(HttpServletResponse httpServletResponse, String url, String fileType, String name, String filePath, PrintWriter writer) throws IOException {
        log.warn("开始下载：{} 格式：{} url：{}",name,fileType,url);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        JSONObject token = getToken(login_email);
        String cookie="remix_userkey="+token.getString("user_key")+";remix_userid="+token.getString("user_id")+";selectedSiteMode=books";
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Cookie", cookie)
                .build();
        Response response = client.newCall(request).execute();
        File file = new File(filePath, name + fileType);
        OutputStream outputStream = Files.newOutputStream(file.toPath());
        IOUtils.copy(response.body().byteStream(), outputStream);
        outputStream.flush();
        outputStream.close();
        log.warn("下载{} 格式：{} 结束 ",name,fileType);
        // 处理文件
        handleFile(file,fileType,writer);
    }

    private static void handleFile(File file, String fileType, PrintWriter writer) {
        log.error("开始修改：" + file.getName());
        try {
            if (".pdf".equals(fileType)) {
                PDFUtil.handlePDF(file.getAbsolutePath());
            } else if (".epub".equals(fileType)) {
                writer.println(file.getName());
                writer.flush();
                EPUBUtil.handleEPUB(file.getAbsolutePath(), writer);
                log.error("修改结束：" + file.getName());
            }
        } catch (Exception e) {
            log.error("修改异常：" + file.getName());
            exceptionBookList.add(file.getName());
        }
    }

    public static JSONObject getToken(String email) throws IOException {
        if(MapUtil.isNotEmpty(token))
            return token;
        JSONObject responseObject=login(email);
        JSONArray errors = responseObject.getJSONArray("errors");
        if(!CollectionUtils.isEmpty(errors)){
            throw new RuntimeException("登录账号异常"+errors);
        }
        token= responseObject.getJSONObject("response");
        return token;
    }

    public static JSONObject login(String email) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("isModal","true")
                .addFormDataPart("site_mode","books")
                .addFormDataPart("action","login")
                .addFormDataPart("redirectUrl","https://zh.z-lib.gs/?ts=1160")
                .addFormDataPart("gg_json_mode","1")
                .addFormDataPart("email",email)
                .addFormDataPart("password","sl230916")
                .build();
        Request request = new Request.Builder()
                .url("https://zh.z-lib.gs/rpc.php")
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        return JSONObject.parseObject(response.body().string());
    }

    public static JSONArray getDownLoadBookInfo() throws IOException {
        JSONArray bookInfo = new JSONArray();
        for (String email : emailSet) {
            JSONObject responseObject=login(email);
            JSONArray errors = responseObject.getJSONArray("errors");
            if(!CollectionUtils.isEmpty(errors)){
                continue;
            }
            JSONArray downLoadBookInfo=getDownLoadBook(responseObject.getJSONObject("response"));
            bookInfo.addAll(downLoadBookInfo);
        }
        return bookInfo;
    }

    private static JSONArray getDownLoadBook(JSONObject token) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        String cookie="remix_userkey="+token.getString("user_key")+";remix_userid="+token.getString("user_id");
        Request request = new Request.Builder()
                .url("https://zh.z-lib.gs/papi/user/related_books/download_history/200")
                .method("GET", null)
                .addHeader("Cookie", cookie)
                .build();
        Response response = client.newCall(request).execute();
        String resultString = response.body().string();
        return JSONObject.parseObject(resultString).getJSONArray("books");
    }

    private static JSONArray getRecommendBook(List<Long> bookIds) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client =  new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        JSONObject token=getToken(login_email);
        String cookie="remix_userkey="+token.getString("user_key")+";remix_userid="+token.getString("user_id");
        MediaType mediaType = MediaType.parse("application/json");
        Map<String,List<Long>> bodyParam=new HashMap<>();
        bodyParam.put("bookIds",bookIds);
        RequestBody body = RequestBody.create(JSONObject.toJSONString(bodyParam),mediaType);
        Request request = new Request.Builder()
                .url("https://zh.z-lib.gs/papi/book/recommended/mosaic/50")
                .method("POST", body)
                .addHeader("Cookie", cookie)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        String resultString = response.body().string();
        return JSONObject.parseObject(resultString).getJSONArray("books");
    }

    public static void main(String[] args) throws IOException {
        JSONArray downLoadBookInfo = getDownLoadBookInfo();
        List<Long> bookIds=new ArrayList<>();
        bookIds.add(23226896L);
        bookIds.add(17504981L);
        bookIds.add(5722826L);
        bookIds.add(44476075L);
        bookIds.add(115607430L);
        bookIds.add(15819626L);
        JSONArray recommendBook = getRecommendBook(bookIds);
        PrintWriter printWriter=new PrintWriter(USER_HOME+"\\DeskTop\\推荐电子书.txt");
        for (Object o : recommendBook) {
            JSONObject book = (JSONObject) o;
            if(Objects.isNull(book.get("cover"))){
                continue;
            }
            if(isDownLoad(downLoadBookInfo,book)){
                continue;
            }
            printWriter.println(book.getString("title")+"-"+book.getString("author"));
            printWriter.flush();
        }
        printWriter.close();
    }

    private static boolean isDownLoad(JSONArray downLoadBookInfo, JSONObject book) {
        for (Object o : downLoadBookInfo) {
            JSONObject downLoadBook = (JSONObject) o;
            if(downLoadBook.getLong("id").equals(book.getLong("id"))){
                return true;
            }
            if(book.getString("title").contains(downLoadBook.getString("title"))
                    &&book.getString("author").contains(downLoadBook.getString("author"))){
                return true;
            }
        }
        return false;
    }


}
