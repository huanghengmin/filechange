package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-5-9
 * Time: 下午6:32
 * To change this template use File | Settings | File Templates.
 */
public class WebdavTargetReceiveUtils {

    private WebDavUtils webDavUtils=new WebDavUtils();
    private Logger logger = Logger.getLogger(WebdavTargetReceiveUtils.class);
    private WebdavTargetTempFileUtils webdavTargetTempFileUtils = new WebdavTargetTempFileUtils();

    /**
     * 保存正常文件到目标端
     * @param fileBean   文件对象
     */
    public void putTempFileToTarget(FileBean fileBean,TargetFile targetFile){
        //构建目标端操作对象
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //目标端文件全名
        String path= webDavUtils.getTargetHostAndName(targetFile);
        if(fileBean.getFullname().startsWith("/")){
            path += webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()) + fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
        }else {
            path += webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()) + "/"+fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
        }
        File file = webdavTargetTempFileUtils.getTempFile(fileBean);         //得到临时文件
        if(file!=null){
            try{
                createTargetDirectory(sardine,fileBean, targetFile);//分级创建目录
                try {
                    sardine.put(path, new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    logger.info("***获取本地临时文件流上传到服务器出错!!!!***");
                }
                //判断是否保存成功
                if(existsTargetFile(targetFile,fileBean)){
                    file.delete();              //删除临时文件
                    webdavTargetTempFileUtils.deleteTempFileFolder(file.getAbsolutePath());
                    logger.info(Thread.currentThread().getName()+"*****文件*****"+webDavUtils.getTargetHostAndName(targetFile) +webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname()+"::同步完成！");
                }
            }catch (Exception e){
                logger.info(e.getMessage()+"***目标端服务器可能存在连接错误!!!***");
            }
        }
    }

    /**
     *  上传正常文件流到目标端！
     * @param fileBean    文件对象
     * @param in            输入流
     */
    public boolean putFile(FileBean fileBean,InputStream inputStream,TargetFile targetFile){
        boolean flag = false;
        if(fileBean.getFilepostlocation()==-2) {
            webdavTargetTempFileUtils.createNewNormalTempFile(fileBean);
        }else {
            if(fileBean.getFilepostlocation()==0){
                webdavTargetTempFileUtils.createNewNormalTempFile(fileBean);
            }
        }
        boolean exists=false;
        File tempFile= webdavTargetTempFileUtils.getTempFile(fileBean);    //得到临时文件
        if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileStart)){
            if(tempFile!=null)
                flag = webdavTargetTempFileUtils.createToTempFile(fileBean,inputStream) ;
            else
                return false;
            exists = webdavTargetTempFileUtils.existsTempFile(fileBean);  //查找是否存在临时文件
            if(exists)
                logger.info(fileBean+"***临时文件保存成功!!!!!***");
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.info("***关闭输入流出错!!!!***"+fileBean.getFullname());
            }
        }else {
            if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileEnd))
                if(tempFile!=null)
                    flag = webdavTargetTempFileUtils.createToTempFile(fileBean,inputStream);
                else
                    return false;
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.info("***关闭输入流出错!!!!***"+fileBean.getFullname()+e.getMessage());
            }
            exists = webdavTargetTempFileUtils.existsTempFile(fileBean);
            if(exists)
                logger.info(fileBean.getFullname()+"***临时文件保存成功!!!***");
        }
        if(exists){
            putTempFileToTarget(fileBean,targetFile);     //保存临时文件流到目标端
        }
        return flag;
    }

     /**
     * 如果保存成功上传临时文件 !!!
     * @param fileBean       文件对象
     * @param targetFile         目标端配置文件
     */
   public void putSyncIngFileToTarget(FileBean fileBean,TargetFile targetFile){
        //目标端文件全名
        String path=webDavUtils.getFilePath(fileBean,targetFile);
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(), targetFile.getPassword());
        File file = webdavTargetTempFileUtils.getTempFile(fileBean);
        if(file!=null){
            //构建文件夹
                 createTargetDirectory(sardine,fileBean,targetFile);
            //保存到目标端
            try {
                  sardine.put(path, new FileInputStream(file));
                } catch (IOException e) {
                   logger.info("***"+fileBean.getFullname()+"保存到目标端出错!!!****");
                }
                //查询目标端文件是否保存成功
            if(judgeNotFinishedFileBeanSaveSuccess(fileBean,targetFile)) {     //查找未完成文件是否存在
                  file.delete(); //删除临时文件
                  webdavTargetTempFileUtils.deleteTempFileFolder(file.getAbsolutePath());             //删除临时文件目录
                  logger.info(Thread.currentThread().getName()+"*****文件*****"+webDavUtils.getTargetHostAndName(targetFile) + webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname()+"::同步完成！");
            }
        }
    }

   /**
    *   发送未完成的文件
    * @param fileBean  文件对象
    * @param targetFile    目标配置文件
    * @param sourceFile     源端配置文件
    */
    public boolean putSyncIngFile(InputStream inputStream,FileBean fileBean,TargetFile targetFile){
        boolean flag=false;
        //保存到目标端
        try{
            boolean bool = false;  //查找临时文件是否保存成功
                if(fileBean.getFilepostlocation() == 0)
                     webdavTargetTempFileUtils.createNewNotFinishedFileTempFile(fileBean);        //构建未完成文件临时文件
            if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileStart)){
                if(webdavTargetTempFileUtils.getTempFile(fileBean)!=null)
                    flag = webdavTargetTempFileUtils.createToTempFile(fileBean,inputStream) ;
                else
                    return false;
                bool = webdavTargetTempFileUtils.existsTempFile(fileBean);
                if(bool)
                    logger.info(fileBean+"临时文件保存成功");
                    inputStream.close();
            }else {
                if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileEnd))
                    if(webdavTargetTempFileUtils.getTempFile(fileBean)!=null)
                        flag = webdavTargetTempFileUtils.createToTempFile(fileBean,inputStream);
                    else
                        return false;
                        inputStream.close();
                    bool = webdavTargetTempFileUtils.existsTempFile(fileBean);
                if(bool)
                    logger.info(fileBean+"临时文件保存成功");
            }
            if(bool){
                putSyncIngFileToTarget(fileBean,targetFile);   //保存到目标端并删除临时文件
            }
        }catch (Exception e){
            logger.info(e.getMessage()+"构建临时文件报错！！");
        }  finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.info(e.getMessage()+"获取流"+fileBean.getFullname()+"出错，可能文件被修改！！！");
            }
        }
        return flag;
    }

    /**
     * 转换后去目标端查找 !
     * @param fileBean   文件对象
     * @return            返回未完成文件是否保存成功
     */
    public boolean judgeNotFinishedFileBeanSaveSuccess(FileBean fileBean,TargetFile targetFile){
          String path = webDavUtils.getFilePath(fileBean,targetFile);
        //把文件全名字转换成完成后的文件名
        fileBean.setFullname(path.replace(webDavUtils.getTargetHostAndName(targetFile)+
        webDavUtils.urlDecoder(webDavUtils.judgeWorkDir(targetFile.getDir()),targetFile),""));
        //返回是否保存成功的标识
        return  existsTargetFile(targetFile,fileBean);
    }

    /**
     * 查询目标端的文件是否存在  正常文件！！！
     * @param targetFile           目标端配置文件
     * @param fileBean                 源端fileBean对象
     * @return                              判断目标端是否存在此文件，存在返回true不存在返回false
     */
    public boolean existsTargetFile(TargetFile targetFile,FileBean fileBean){
        boolean  flag=false;
        //转码后的文件全名
        String targetFileName=null;
        if(fileBean.getFullname().startsWith("/")){
             targetFileName =  webDavUtils.getTargetHostAndName(targetFile) +
                     webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+
                             fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
        }else {
             targetFileName =  webDavUtils.getTargetHostAndName(targetFile) +
                     webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+"/"+
                             fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
        }
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(), targetFile.getPassword());   //操作对象
        try {
            if(sardine.exists(targetFileName)){
                //列出文件
                List<DavResource> davResources=sardine.list(targetFileName);
                Iterator<DavResource> iterator=davResources.iterator();
                DavResource resource=iterator.next();
                String href= URLDecoder.decode(resource.getHref().toString().replaceAll("\\+","%2B"),targetFile.getCharset());
                String fileFullName=null;
                if(!href.contains("http://"))  {
                     fileFullName=href.replaceFirst(webDavUtils.judgeWorkDir(targetFile.getDir()),"");
                } else {
                     fileFullName=href.replaceFirst(webDavUtils.getTargetHostAndName(targetFile)+webDavUtils.judgeWorkDir(targetFile.getDir()),"");
                }
                if((resource.getContentLength()==fileBean.getFilesize())&& (fileFullName.equals(fileBean.getFullname()))){
                    flag=true;
                }
            }
        }catch (IOException e) {
            //如果不存在文件，则返回false
            return flag;
        }
        return flag;
    }

    /**
     * 得到目标端上传完好的文件  !
     * @param fileBean    文件对象
     * @return            返回目标端是否存在此文件
     */
    public boolean existsIspfNormalFile(FileBean fileBean,TargetFile targetFile){
        //文件对象
        FileBean newFileBean=new FileBean();
        //ispf文件后缀替换
        newFileBean.setFullname(webDavUtils.replaceEnd(fileBean.getFullname(),FileContext.Str_SyncFileSourceProcess_Flag));
        //文件大小不变
        newFileBean.setFilesize(fileBean.getFilesize());
        //文件名不变
        newFileBean.setName(fileBean.getName());
        //查找目标端是否存在此文件
        boolean  flag= existsTargetFile(targetFile,newFileBean);
        //返回是否存在标识
        return flag;
    }

    /**
     * 得到目标端ipse文件   如果存在返回true
     * @param fileBean        文件对象
     * @return                 文件是否存在
     */
    public boolean existsIspfIspeFile(FileBean fileBean,TargetFile targetFile){
        //文件对象
        FileBean newFileBean=new FileBean();
        //ispf文件后缀替换为ispe
        newFileBean.setFullname(
                webDavUtils.replaceOtherSeparator(fileBean.getFullname(),FileContext.Str_SyncFileSourceProcess_Flag,
                        FileContext.Str_SyncFileSourceProcess_End_Flag)) ;
        //文件大小不变
        newFileBean.setFilesize(fileBean.getFilesize());
        //文件名不变
        newFileBean.setName(fileBean.getName());
        //查找目标端是否存在此文件
        boolean  flag= existsTargetFile(targetFile,newFileBean);
        //返回是否存在标识
        return flag;
    }

    /**
     * 改名目标端已存在的ispe文件为正常文件
     * @param fileBean          源端传过来的ispf文件
     * @param targetFile           目标端配置文件
     */
    public void renameIspfIspeFileToNormal(FileBean fileBean,TargetFile targetFile){
        String path= webDavUtils.getTargetHostAndName(targetFile);
        if(fileBean.getFullname().startsWith("/")){
            path += webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(targetFile.getDir())+
                    webDavUtils.replaceOtherSeparator(fileBean.getFullname(),FileContext.Str_SyncFileSourceProcess_Flag,
                            FileContext.Str_SyncFileSourceProcess_End_Flag ))
                    ,targetFile).replaceAll("\\+","%20");
        } else {
            path += webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(targetFile.getDir())+
                    "/"+ webDavUtils.replaceOtherSeparator(fileBean.getFullname(),FileContext.Str_SyncFileSourceProcess_Flag,
                    FileContext.Str_SyncFileSourceProcess_End_Flag )),targetFile).replaceAll("\\+","%20");
        }
        Sardine sardine=SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        try {
            sardine.move(path,webDavUtils.replaceEnd(path,FileContext.Str_SyncFileSourceProcess_End_Flag));
        } catch (IOException e) {
            logger.info("移动目标端文件"+fileBean.getFullname()+"报错！！！");
        }
    }

    /**
     * 如果目标端为只增加时处理
     * @param fileBean   文件对象
     * @param  targetFile 目标端配置文件
     */
    public void onlyTargetAdd(FileBean fileBean,FileList resultFileList, TargetFile targetFile){
        String requestUrl= webDavUtils.getTargetHostAndName(targetFile)+
                webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
        Sardine sardine=SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        boolean  flag = false;
        try {
            if(sardine.exists(requestUrl)){
                flag = true;
            }
        } catch (IOException e) {
        }
        if(!flag){
            resultFileList.addFileBean(fileBean);
        }
    }

    /**
     * 原来的只增加模式
     * @param fileBean 文件对象
     * @param targetFile   目标端配置文件
     */
    public void originalTargetOnlyAdd(FileBean fileBean,TargetFile targetFile){
        String requestUrl= webDavUtils.getTargetHostAndName(targetFile)+
                webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
        Date newDate=new Date();
        SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy_MM_dd HH_mm_ss"); //加上时间
        String newDateFormat=sDateFormat.format(newDate);
        Sardine sardine=SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        try {
            if(sardine.exists(requestUrl)){
                try {
                    sardine.move(requestUrl, webDavUtils.getTargetHostAndName(targetFile)+
                            webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+
                                    webDavUtils.judgeWorkDir(webDavUtils.getFileDir(fileBean))+"/"+newDateFormat+fileBean.getName(),targetFile).replaceAll("\\+","%20"));
                }catch (IOException e) {
                    logger.info("只增加模式改目标端文件名报错！");
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     * 构建目标端文件夹
     * @param sardine   目标端操作对象
     * @param fileBean    文件对象
     * @param targetFile    目标端配置文件
     */
    public  void createTargetDirectory(Sardine sardine,FileBean fileBean, TargetFile targetFile){
        //源文件除去主机和端口工作空间后的路径
        String targetDir= webDavUtils.getFileDir(fileBean);
        //分别创建的目录
        String dirMin=null;
        if(!targetDir.equals("")&&targetDir!=null) {
            if(targetDir.startsWith("/")){
                dirMin= webDavUtils.judgeWorkDir(targetFile.getDir())+targetDir;
            }else {
                dirMin= webDavUtils.judgeWorkDir(targetFile.getDir())+"/"+targetDir;
            }
        }else{
            dirMin= webDavUtils.judgeWorkDir(targetFile.getDir())+"";
        }
        String[] dir=dirMin.split("/");
        String requestDir= webDavUtils.getTargetHostAndName(targetFile)+"/";
        for(String d:dir){
            if(!d.equals("")&&d!=null){
                try {
                    requestDir+= webDavUtils.urlEncoder(d,targetFile)+"/";
                    //隔多级目录不能自动创建
                    if(!sardine.exists(requestDir.replaceAll("\\+","%20"))) {
                        sardine.createDirectory(requestDir.replaceAll("\\+","%20"));
                    }
                } catch (IOException e) {
                    logger.info("**********目标端创建目录不成功!!!!**********");
                }
            }
        }
    }
}
