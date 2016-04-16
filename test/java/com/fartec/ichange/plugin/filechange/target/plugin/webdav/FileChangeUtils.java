package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.source.plugin.webdav.WebDavUtils;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.TargetFile;
import com.inetec.common.exception.Ex;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-11
 * Time: 下午7:50
 * To change this template use File | Settings | File Templates.
 */
public class FileChangeUtils {

    private static WebDavUtils webDavUtils= new WebDavUtils();

    /**
     * 配置文件
     * @param sourceFile 源端配置
     * @param targetFile 目标端配置
     * @param sourceDir   源端工作空间
     * @param targetDir   目标端工作空间
     * @param sourceIp    源端地址
     * @param targetIp    目标地址
     * @param sourcePort   源端端口
     * @param targetPort   目标端口
     * @param sourceUser    源端用户名
     * @param sourcePwd     源端用户名密码
     * @param targetUser    目标端用户名
     * @param targetPwd     目标端用户名密码
     */
    public static  void  setConfig(SourceFile sourceFile,TargetFile targetFile,String sourceDir,String targetDir,
                                      String sourceIp,String targetIp, String sourcePort,String targetPort,
                                      String sourceUser,String sourcePwd,String targetUser,String targetPwd){
        //源端配置
        sourceFile.setPassword(sourcePwd);
        sourceFile.setUserName(sourceUser);
        sourceFile.setDir(sourceDir);
        try {
            sourceFile.setPort(sourcePort);
            sourceFile.setThreads("1");
        }catch (Ex ex) {
            System.out.println("源端设置端口号出错!");
            ex.printStackTrace();
        }
        sourceFile.setServerAddress(sourceIp);
        sourceFile.setCharset("utf-8");
        try {
            sourceFile.setDeletefile(String.valueOf(false));
        } catch (Ex ex) {
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            sourceFile.setInterval(String.valueOf(5*1000));
        } catch (Ex ex) {
            System.out.println("源端转换间隔时间出错!");
            ex.printStackTrace();
        }
        sourceFile.setProtocol(SourceFile.Str_Protocol_WebDAV);
        try {
            sourceFile.setIsincludesubdir(String.valueOf(true));
        } catch (Ex ex) {
            System.out.println("源端转换是否包含子目录出错!");
            ex.printStackTrace();
        }
        try {
            sourceFile.setIstwoway(String.valueOf(true));
        } catch (Ex ex) {
            System.out.println("源端转换同步是否双向出错!");
            ex.printStackTrace();
        }
        //能够过滤的类型
        sourceFile.setFiltertypes("*.*");
        //不能过滤的类型
        sourceFile.setNotfiltertypes("");
        //目标端配置
        try {
            targetFile.setDeletefile(String.valueOf(false));
        } catch (Ex ex) {
            System.out.println("目标端转换是否删除文件出错!");
            ex.printStackTrace();
        }
        targetFile.setPassword(targetPwd);
        targetFile.setUserName(targetUser);
        targetFile.setServerAddress(targetIp);
        try {
            targetFile.setPort(targetPort);
        } catch (Ex ex) {
            System.out.println("目标端设置端口号出错!");
            ex.printStackTrace();
        }
        targetFile.setDir(targetDir);
        try {
            targetFile.setThreads(String .valueOf(10));
        } catch (Ex ex) {
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        targetFile.setCharset("GBK");
        try {
            targetFile.setOnlyadd(String.valueOf(false));
        } catch (Ex ex) {
            System.out.println("目标端设置是否只增加文件出错!");
            ex.printStackTrace();
        }
       
    }

    /**
     * 上传本地文件到服务器
     * @param sourceFile 源端配置文件
     * @param localPath  本地目录
     */
    public static void  uploadToService(SourceFile sourceFile,String localPath){
        File file=new File(localPath);
        Sardine sardine= SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        if(file.isDirectory()) {
            File[] files=file.listFiles();
            int i=0;
            for (;i<files.length;i++){
                if( files[i].isFile()){
                    InputStream fis = null;
                    try {
                        fis = new FileInputStream(new File(files[i].getAbsolutePath()));
                        try {
                            String dir=files[i].getAbsolutePath().substring(files[i].getAbsolutePath().indexOf(":")+1,files[i].getAbsolutePath().length());
                            String uploadDir=dir.replaceAll("\\\\","/");
                            String requestUrl= webDavUtils.getSourceHostAndName(sourceFile)+
                                    webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+uploadDir,sourceFile);
                            if(!sardine.exists(requestUrl)) {
                                //源文件除去主机和端口工作空间后的路径
                                String targetDir=null;
                                if(uploadDir.contains("/")){
                                    //除去主机名，端口号，工作空间后的路径不包括文件名
                                    targetDir=uploadDir.substring(0,uploadDir.lastIndexOf("/"));
                                }else {
                                    targetDir="";
                                }
                                //分别创建的目录
                                String dirMin=null;
                                if(!targetDir.equals("")&&targetDir!=null) {
                                    dirMin=targetDir;
                                }else{
                                    dirMin="";
                                }
                                String[] dir2=dirMin.split("/");
                                String requestDir= webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(
                                        webDavUtils.judgeWorkDir(sourceFile.getDir()),sourceFile)+"/";
                                for(String d:dir2){
                                    if(!d.equals("")&&d!=null){
                                        try {
                                            requestDir+= webDavUtils.urlEncoder(d,sourceFile)+"/";
                                            //隔多级目录不能自动创建
                                            if(!sardine.exists(requestDir))
                                            {
                                                sardine.createDirectory(requestDir);
                                            }
                                        } catch (IOException e) {
                                            System.out.println("目标端创建目录不成功！");
                                        }
                                    }
                                }
                            }
                            sardine.put(webDavUtils.getSourceHostAndName(sourceFile)+
                                    webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+"/"+
                                            uploadDir,sourceFile), fis);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    uploadToService(sourceFile,files[i].getAbsolutePath());
                }
            }
        }
    }

    /**
     * 先删除本地目录下所有文件
     * @param file   文件对象
     */
    private static void deleteFile(File file){
        if(file.exists()){                    //判断文件是否存在
            if(file.isFile()){                    //判断是否是文件
                file.delete();                       //delete()方法 你应该知道 是删除的意思;
            }else if(file.isDirectory()){              //否则如果它是一个目录
                File files[] = file.listFiles();               //声明目录下所有的文件 files[];
                for(int i=0;i<files.length;i++){            //遍历目录下所有的文件
                    deleteFile(files[i]);             //把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }else{
        }
    }

    /**
     * 上传文件到源端服务器
     * @param fileCount    文件个数
     * @param fileType     文件类型
     * @param localPath    本地目录
     */
    public static void createToLocal(long  fileCount,String fileType,String localPath){
        File file=new File(localPath);
        if(file.exists()){
            if(file.isDirectory()){

                File files[] = file.listFiles();            //声明目录下所有的文件 files[];
                for(int i=0;i<files.length;i++){            //遍历目录下所有的文件
                deleteFile(files[i]);             //把每个文件 用这个方法进行迭代
            }
        }
        FileOutputStream fileOutputStream=null;
        BufferedOutputStream bufferedOutputStream=null;
        for (int i=0;i<fileCount;i++){
            if(i%10==0){
                File file1=new File(localPath+File.separator+"黄恒民"+i+i+i+i+i+i+i+i+File.separator+"黄恒民"+i+i+File.separator+"黄恒民"+i+i);
                if(!file1.exists()){
                file1.mkdirs();
                }
                try {
                    fileOutputStream=new FileOutputStream(new File(file1.getAbsolutePath()+File.separator+"11"+i+"."+fileType)) ;
                    bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
                    try {
                        bufferedOutputStream.write(new String("黄恒民"+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i).getBytes());
                        bufferedOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else {
            try {
                fileOutputStream=new FileOutputStream(new File(localPath+File.separator+"黄恒民"+i+"."+fileType)) ;
                bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
                try {
                    bufferedOutputStream.write(new String("黄恒民"+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i).getBytes());
                    bufferedOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
              }
            }
          }
        }
    }


}
