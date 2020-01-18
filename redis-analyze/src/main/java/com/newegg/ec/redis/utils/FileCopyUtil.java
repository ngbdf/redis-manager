/**
 * 
 */
package com.newegg.ec.redis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Hulva Luva.H
 * @since 2018年4月11日
 *
 */
public class FileCopyUtil {

	/**
	 * 文件拷贝
	 * 
	 * @param source 源文件
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		try (OutputStream os = new FileOutputStream(dest);) {
			is = new FileInputStream(source);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static void main(String[] args) {
		try {
			// check destination folder exist
			File des = new File("rdbtemp/dump.rdb");
			System.out.println(des.getAbsolutePath());
			FileCopyUtil.copyFileUsingStream(new File("D:\\temp\\redis\\prd\\dump.rdb"), des);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
