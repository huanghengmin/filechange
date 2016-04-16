package com.fartec.ichange.plugin.filechange.utils;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-3-16
 * Time: обнГ2:55
 * To change this template use File | Settings | File Templates.
 */

public class FileUtil {
    public static FileList ftpJdomFile(String dir){
        FileList fileList =new FileList();
        File file=new File(dir);
        if (file.isDirectory()){
            File[] files=file.listFiles();
            if (files==null){
                return null;
            }
            for (int i=0;i<files.length;i++){
                if (files[i].isDirectory()){
                    ftpJdomFile(FileFilter.getEndSubString(files[i].getAbsolutePath(),File.separator));
                }else {
                 fileList.addFileBean(FileBean.fileToBean(files[i],dir));
                }
            }
        }
         return fileList;
    }
}
