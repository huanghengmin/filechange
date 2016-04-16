package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-5-6
 * Time: 下午4:36
 * To change this template use File | Settings | File Templates.
 */
public class WebdavSourceTempFileUtils {
    private static final String tempFilePath = System.getProperty("ichange.home")+"/temp/webdav/";
    private Logger logger = Logger.getLogger(WebdavSourceTempFileUtils.class);
    private static WebdavSourceTempFileUtils webdavSourceTempFileUtils = null;
    private WebdavSourceTempFileUtils(){}
    public static WebdavSourceTempFileUtils getSingleWebdavSourceTempFile(){
        if(webdavSourceTempFileUtils == null){
            webdavSourceTempFileUtils = new WebdavSourceTempFileUtils();
        }
        return webdavSourceTempFileUtils;
    }
    /**
     * 创建目录
     * @param destDirName 目标目录名
     * @return 目录创建成功返回true，否则返回false
     */
    public void createDir(String destDirName) {
        File dir = new File(destDirName);
        if(!dir.exists()) {
            if(!destDirName.endsWith(File.separator))
                destDirName = destDirName + File.separator;
            // 创建单个目录
            if(dir.mkdirs()) {
                System.out.println("创建目录" + destDirName + "成功！");
            } else {
                System.out.println("创建目录" + destDirName + "成功！");
            }
        }
    }

    /**
     * 得到临时文件
     * @param fileBean 文件对象
     * @return    得到的临时文件
     */
    public File getFile(FileBean fileBean){
        logger.info(fileBean.getFullname()+"文件全名");
        String fileFullName = null;
        if(fileBean.getFullname().contains("/"))
            fileFullName =fileBean.getFullname().replace("/",File.separator);
        else
            fileFullName = fileBean.getFullname();
        String path = null;
        if(fileFullName.startsWith(File.separator))
            path = tempFilePath+fileFullName;
        else
            path = tempFilePath+File.separator+fileFullName;
        File file = new File(path);
        if(!file.exists()){
            String dir = null;
            if(path.contains(File.separator)){
                dir = path.substring(0,path.lastIndexOf(File.separator));
            } else {
                dir = path;
            }
            createDir(dir);
            try {
                file.createNewFile();
            } catch (IOException e) {
               logger.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return  file;
    }
    /**
     * 创建临时文件
     * @param prefix 临时文件的前缀
     * @param suffix 临时文件的后缀
     * @param dirName 临时文件所在的目录，如果输入null，则在用户的文档目录下创建临时文件
     * @return 临时文件创建成功返回抽象路径名的规范路径名字符串，否则返回null
     */
    public void createToTempFile(FileBean fileBean,InputStream in) {
        File file  =  getFile(fileBean);
        if(file.exists()){
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file,true);
                try {
                    fileOutputStream.write(IOUtils.toByteArray(in));
                } catch (IOException e) {
                  logger.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
                }
            } catch (FileNotFoundException e) {
                logger.info(e.getMessage()); //To change body of catch statement use File | Settings | File Templates.
            } finally {
                try {
                    if(in != null)
                        in.close();
                } catch (IOException e) {
                 logger.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                 logger.info(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
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
        File file = getFile(fileBean);
        if(file.exists()){
            if(fileBean.getFilesize() == file.length()){
                logger.info("临时文件保存成功");
                return  true;
            }
        }
        return false;
    }
}
