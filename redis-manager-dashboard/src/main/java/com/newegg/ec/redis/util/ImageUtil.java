package com.newegg.ec.redis.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author Jay.H.Zou
 * @date 2019/7/2
 */
public class ImageUtil {

    public static String IMAGE_SUFFIX_PNG = ".png";

    public static String IMAGE_SUFFIX_JPG = ".jpg";

    /**
     * 保存文件，直接以multipartFile形式
     *
     * @param multipartFile
     * @param path          文件保存绝对路径
     * @return 返回文件名
     * @throws IOException
     */
    public static void saveImage(MultipartFile multipartFile, String path, String siteName) throws Exception {
        if (!path.endsWith(SignUtil.SLASH)) {
            path += SignUtil.SLASH;
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileInputStream fileInputStream = (FileInputStream) multipartFile.getInputStream();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + File.separator + getImageName(siteName)));
        byte[] bs = new byte[1024];
        int len;
        while ((len = fileInputStream.read(bs)) != -1) {
            bos.write(bs, 0, len);
        }
        bos.flush();
        bos.close();
    }

    public static boolean existImage(String filePath, String siteName) {
        File file = new File(filePath + getImageName(siteName));
        return file.exists();
    }

    public static void updateImageName(String filePath, String oldSiteName, String newSiteName) {
        if (!filePath.endsWith(SignUtil.SLASH)) {
            filePath += SignUtil.SLASH;
        }
        File file = new File(filePath + getImageName(oldSiteName));
        // 判断原文件是否存在（防止文件名冲突）
        if (!file.exists()) {
            return;
        }
        newSiteName = newSiteName.trim();
        if (StringUtils.isBlank(newSiteName)) {
            return;
        }
        String newFilePath = filePath + getImageName(newSiteName);
        File newFile = new File(newFilePath);
        // 修改文件名
        file.renameTo(newFile);
    }

    public static String getImageName(String userName) {
        return userName.replaceAll(SignUtil.SPACE, SignUtil.MINUS) + IMAGE_SUFFIX_PNG;
    }

}
