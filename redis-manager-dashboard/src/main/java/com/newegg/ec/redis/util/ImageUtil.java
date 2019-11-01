package com.newegg.ec.redis.util;


import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/2
 */
public class ImageUtil {

    private static String IMAGE_SUFFIX_JPG = ".jpg";

    /**
     * 保存文件，直接以multipartFile形式
     *
     * @param multipartFile
     * @param path          文件保存绝对路径
     * @return 返回文件名
     * @throws IOException
     */
    public static void saveImage(MultipartFile multipartFile, String path, String userId) throws Exception {
        InputStream inputStream = multipartFile.getInputStream();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + File.separator + getImageName(userId)));
        byte[] bs = new byte[1024];
        int len;
        while ((len = inputStream.read(bs)) != -1) {
            bos.write(bs, 0, len);
        }
        bos.flush();
        bos.close();
    }

    public static List<File> getAllFiles(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            List<File> fileList = new ArrayList<>(Arrays.asList(files));
            return fileList;
        }
        return new ArrayList<>();
    }

    public static String getImageName(String userId) {
        return userId + IMAGE_SUFFIX_JPG;
    }

}
