package com.fartec.ichange.plugin.filechange.utils;

import com.inetec.common.config.nodes.SourceFile;
import org.apache.commons.net.ftp.FTPFile;
import sun.net.ftp.FtpClient;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-3-27
 * Time: 下午12:43
 * To change this template use File | Settings | File Templates.
 */
public class FtpFileCompare {
    /**
     * ftptile 转换成filebean  对象
     * @param file      ftp文件类型
     * @param workdir    工作路径
     * @param sourceFile   源端文件参数
     * @return
     */

    public static FileBean _FTPFileToBean(FTPFile file,String workdir,SourceFile sourceFile){
        FileBean result = new FileBean();
//        String[] st=workdir.split("/");
        String strdir=null;
        if(workdir.equalsIgnoreCase(sourceFile.getDir())){     //如果工作路径等于源端匹配的根目录的
            workdir="";
            strdir=(workdir+file.getName()).trim();                          //工作目录去掉源端根目录
        }else {
            if(sourceFile.getDir().equals("/")){
                strdir=workdir+"/"+file.getName();
            }
            else if(sourceFile.getDir().endsWith("/")){
                strdir=FileFilter.getSubString(workdir,sourceFile.getDir())+"/"+file.getName();
            }else {
                strdir=FileFilter.getSubString(workdir,sourceFile.getDir()+"/")+"/"+file.getName();
            }
        }
        result.setFullname(strdir);
        result.setName(file.getName());
        result.setTime(file.getTimestamp().getTimeInMillis());
        result.setFilesize(file.getSize());
        return result;
    }
}

