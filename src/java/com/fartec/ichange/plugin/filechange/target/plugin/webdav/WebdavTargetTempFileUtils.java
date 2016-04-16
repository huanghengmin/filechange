package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-5-6
 * Time: 下午4:23
 * To change this template use File | Settings | File Templates.
 */
public class WebdavTargetTempFileUtils {

    private Logger logger=Logger.getLogger(WebdavTargetTempFileUtils.class);
    private static final String tempFilePath = System.getProperty("ichange.home")+"/temp/webdav";

    /**
     * 创建目录
     * @param destDirName 目标目录名
     * @return 目录创建成功返回true，否则返回false
     */
    public void createTempFileDir(String destDirName) {
        File dir = new File(destDirName);
        if(!dir.exists()) {
            if(!destDirName.endsWith("/"))
                destDirName = destDirName + "/";
            // 创建单个目录
            if(dir.mkdirs()) {
                 System.out.println("**创建目录" + destDirName + "成功!!!**");
            } else {
                 System.out.println("**创建目录" + destDirName + "失败!!!**");
            }
        }
    }

    /**
     * 删除临时文件文件夹
     * @param file 文件对象
     */
    public  void deleteTempFileFolder(String path){
         if(path.contains(File.separator)){
                 path = path.replace(File.separator,"\\/");
                 path = path.substring(0,path.lastIndexOf("\\/"));
             if(!path.equals(tempFilePath)){
                 File file=new File(path);
                 if(file.listFiles().length<=0){
                          file.delete();
                          deleteTempFileFolder(file.getAbsolutePath());
                     }
             }
                 
         }
    }

    /**
     * 查找临时 文件
     * @param fileBean 文件对象
     * @return    返回文件是否存在
     */
    public boolean existsTempFile(FileBean fileBean){
        File file = getTempFile(fileBean);
        if(file!=null){
            if(file.exists()){
                if(fileBean.getFilesize() == file.length()){
                    return  true;
                }  else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean existsSyncIngTempFile(FileBean fileBean){
        File file = getTempFile(fileBean);
        if(file!=null){
            if(file.exists()){
                if(file.length()>fileBean.getFilesize()){
                    file.delete();
                    deleteTempFileFolder(file.getAbsolutePath());
                }
                return  true;
                }  else {
                    return false;
                }
        }
        return false;
    }

    /**
     * 构建到临时文件
     * @param fileBean 文件对象
     * @param in            文件流
     * @return
     */
    public boolean createToTempFile(FileBean fileBean,InputStream in) {
        logger.info("目标端配置临时文件路径"+tempFilePath);
        File file  =  getTempFile(fileBean);
        if(file!=null){
            if(file.exists()){
                FileOutputStream fileOutputStream=null;
                try {
                    fileOutputStream = new FileOutputStream(file,true);
                    try {
                        fileOutputStream.write(IOUtils.toByteArray(in));
                    } catch (IOException e) {
                        logger.info(e.getMessage());
                    }
                } catch (FileNotFoundException e) {
                    logger.info(e.getMessage());
                } finally {
                    try {
                        if(in!=null)
                            in.close();
                    } catch (IOException e) {
                       logger.info(e.getMessage());
                        return false;
                    }
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        logger.info(e.getMessage());
                        return false;
                    }
                }
            }
        }
        if(file.length()>fileBean.getFilesize()){
            file.delete();
            deleteTempFileFolder(file.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     *  得到临时文件
     * @param fileBean 文件对象
     * @return        临时文件
     */
     public File getTempFile(FileBean fileBean){
        String path = getTempFilePath(fileBean);
        File file = new File(path);
        if(file.exists()){
            return file;
        }
        return null;
    }

    /**
     * 得到未完成文件大小
     * @param fileBean       文件对象
     * @return    目标端临时文件的大小
     */
     public long getTempFileContentLength(FileBean fileBean){
        long  contentLength = 0;
        String path = getTempFilePath(fileBean);
        File file = new File(path);
        if(file.exists()){
            long  fileLength = file.length();
           if(fileLength>fileBean.getFilesize()){
                file.delete();
                deleteTempFileFolder(file.getAbsolutePath());
                return  0;
           } else {
                contentLength=file.length();
           }
        }
        return  contentLength;
    }

    /**
     * 得到文件路径
     * @param fileBean  文件对象
     * @return
     */
    public String getTempFilePath(FileBean fileBean){
        String path =null;
        logger.info(fileBean.getFullname()+"文件全名");
        String fileFullName=null;
        if(fileBean.getFullname().contains("/"))
            fileFullName = fileBean.getFullname().replace("/",File.separator);
        else
            fileFullName = fileBean.getFullname();

        if(fileFullName.startsWith(File.separator))
               path = tempFilePath+fileFullName;
        else
            path = tempFilePath + File.separator + fileFullName;
            path =  path.replace(File.separator,"/");
        return  path;
    }
    
    /**
     *  构建ispf未完成文件
     * @param fileBean       构建ispf未完成文件
     * @return                    构建成功返回true否则返回false
     */
     public boolean createNewNotFinishedFileTempFile(FileBean fileBean){
        boolean  flag = false;
        String path = getTempFilePath(fileBean);
        File file = new File(path);
        if(!file.exists()){
            try {
                String dir=null;
                if(path.contains("/")){
                    dir=path.substring(0,path.lastIndexOf("/"));
                } else {
                    dir=path;
                }
                createTempFileDir(dir);   //构建目录
                flag = file.createNewFile();
            } catch (IOException e) {
                logger.info("构建新文件出错！！！！");
            }
        }
        return flag;
    }

    /**
     *    构建临时文件
     * @param fileBean 文件对象
     * @return    返回构建文件是否成功
     */
     public boolean createNewNormalTempFile(FileBean fileBean){
          boolean  flag=false;
          String path = getTempFilePath(fileBean);
          File file=new File(path);
          if(file.exists()){
             file.delete();
          } else {
              try {
                  String dir=null;
                  if(path.contains("/")){
                      dir = path.substring(0,path.lastIndexOf("/"));
                  } else {
                      dir = path;
                  }
                   createTempFileDir(dir);     //构建目录
                   flag = file.createNewFile();
              } catch (IOException e) {
                  logger.info("**构建新文件出错!!!!!**");
              }
          }
          return flag;
      }
}
