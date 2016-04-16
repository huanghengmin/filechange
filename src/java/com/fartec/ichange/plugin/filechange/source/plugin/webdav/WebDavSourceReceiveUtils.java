package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-19
 * Time: 下午3:25
 * To change this template use File | Settings | File Templates.
 */
public class WebDavSourceReceiveUtils {
    private Logger logger = Logger.getLogger(WebDavSourceReceiveUtils.class);
    private static WebDavSourceReceiveUtils webdavSourceReceiveUtils =null;
    private WebDavUtils webDavUtils = WebDavUtils.getSingleWebDavUtil();
    private WebdavSourceTempFileUtils webdavSourceTempFileUtils = WebdavSourceTempFileUtils.getSingleWebdavSourceTempFile();
    private WebDavSourceReceiveUtils(){}
    public static WebDavSourceReceiveUtils getSingleWebDavSourceUtil(){
        if(webdavSourceReceiveUtils == null){
            webdavSourceReceiveUtils = new WebDavSourceReceiveUtils();
        }
            return webdavSourceReceiveUtils;
    }

    /**
     * 查询源端的文件是否存在
     * @param targetFile      目标端配置文件
     * @param fileBean         源端fileBean对象
     * @return         判断源端是不存在此文件
     */
    public boolean existsSourceFile(SourceFile sourceFile,FileBean fileBean){
        boolean  flag = false;
        //转码后的文件全名
        String targetFileName = webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname(),sourceFile);
        //操作对象
        Sardine sardine= SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        try {
            if(sardine.exists(targetFileName)){
                List<DavResource> davResources = sardine.list(targetFileName);
                Iterator<DavResource> iterator = davResources.iterator();
                DavResource resource = iterator.next();
                if((resource.getContentLength() == fileBean.getFilesize())&&(webDavUtils.urlDecoder(resource.getPath().toString(),sourceFile).replace(webDavUtils.judgeWorkDir(sourceFile.getDir()),"").equals(fileBean.getFullname()))){
                    flag = true;
                }
            }
        }catch (IOException e) {
            return flag;
        }
        return flag;
    }


    /**
     *发送未完成的文件
     * @param fileBean  文件对象
     * @param targetFile    目标配置文件
     * @param sourceFile     源端配置文件
     */
    public boolean processNotFinishedFileBean(InputStream inputStream,FileBean fileBean,SourceFile sourceFile,WebDavSourceReceiveUtils webDavReceiveUtils){
        boolean flag = false;
        //目标端文件全名
        String path= webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+
                fileBean.getFullname().replace(FileContext.Str_SyncFileSourceProcess_Flag,FileContext.Str_SyncFileSourceProcess_End_Flag),sourceFile);

        Sardine sardine = SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        try {
            if(sardine.exists(path))  {
                sardine.delete(path);
            }
        } catch (IOException e) {
            logger.info("删除文件"+path+"出错！！！");
        }
        //目标端未完成文件全名
        File file = null;
        try{
            boolean bool = false;
            if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileStart)){
                webdavSourceTempFileUtils.createToTempFile(fileBean,inputStream) ;
                inputStream.close();
            }else {
                if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileEnd))
                    webdavSourceTempFileUtils.createToTempFile(fileBean,inputStream);
                inputStream.close();
                bool = webdavSourceTempFileUtils.existsTempFile(fileBean);
                if(bool){
                    logger.info(fileBean+"临时文件保存成功");
                }
            }
            if(bool){
                file = webdavSourceTempFileUtils.getFile(fileBean);
                 putStream(new FileInputStream(file) ,fileBean,sardine,path,sourceFile);//保存到目标端
                //保存目标端的名字
                 putRename(fileBean,sardine,path,sourceFile);
            }
        }catch (Exception e){
            logger.info("更改文件名出错"+fileBean.getFullname());
        }  finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.info("关闭文件流报错！！！");
            }
        }
        //判断是否保存成功
        if(webDavReceiveUtils.JudgeNotFinishedFileBeanSaveSuccess(fileBean,sourceFile)){
            flag = true;
            file.delete();
            logger.info(Thread.currentThread().getName()+"*****文件*****"+webDavUtils.getSourceHostAndName(sourceFile) +webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"::同步完成！");
        }
        return flag;
    }

    /**
     *  上传到源端
     * @param inputStream 输入流对象
     * @param fileBean     文件对象
     * @param sardine       源端操作对象
     * @param targetFullName  上传的全名
     */
    public void putStream(InputStream inputStream,FileBean fileBean,Sardine sardine,String targetFullName,SourceFile sourceFile){
        try {
            if(!sardine.exists(targetFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag)&&inputStream != null){
                //上传到源端服务器
                sardine.put(targetFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag,inputStream);
            }
        }catch (IOException e) {
            logger.info("目标端创建文件"+ webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"失败");
        }
    }

    /**
     *源端改回原名操作
     * @param fileBean   文件对象
     * @param sardine          源端操作对象
     * @param targetFullName      文件全名
     */
    public void putRename(FileBean fileBean,Sardine sardine,String targetFileBeanFullName,SourceFile sourceFile){
        try {
            if(sardine.exists(targetFileBeanFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag)){
                //改名操作
                sardine.move(targetFileBeanFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag,targetFileBeanFullName);
            }
        } catch (IOException e) {
            logger.info("******目标端文件"+ webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"改名不成功*****");
        }
    }

    /**
     * 转换后去目标端查找
     * @param fileBean    文件对象
     * @return     转换后去源端查找文件是否存在，是否保存成功
     */
    public boolean  JudgeNotFinishedFileBeanSaveSuccess(FileBean fileBean,SourceFile sourceFile){
        fileBean.setFullname(fileBean.getFullname().replace(FileContext.Str_SyncFileTargetProcess_Flag,""));
        return  existsSourceFile(sourceFile, fileBean);
    }

    /**
     * 源端构建文件夹
     * @param sardine      源端操作对象
     * @param fileBean          文件对象
     * @param sourceFile           源端配置文件
     */
    public synchronized void createSourceDirectory(Sardine sardine,FileBean fileBean, SourceFile sourceFile){
        //源文件除去主机和端口工作空间后的路径
        String targetDir = webDavUtils.getFileDir(fileBean);
        //分别创建的目录
        String dirMin = null;
        if(!targetDir.equals("")&&targetDir!=null) {
            dirMin = sourceFile.getDir()+targetDir;
        }else{
            dirMin = sourceFile.getDir()+"";
        }
        String[] dir = dirMin.split("/");
        String requestDir = webDavUtils.getSourceHostAndName(sourceFile)+"/";
        for(String d:dir){
            if(!d.equals("")&&d != null){
                try {
                    requestDir += webDavUtils.urlEncoder(d,sourceFile)+"/";
                    //隔多级目录不能自动创建
                    if(!sardine.exists(requestDir)) {
                        sardine.createDirectory(requestDir);
                    }
                } catch (IOException e) {
                    logger.error("*****源端"+webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"创建目录不成功!***");
                }
            }
        }
    }


}
