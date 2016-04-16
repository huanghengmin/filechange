package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-5-8
 * Time: 下午2:49
 * To change this template use File | Settings | File Templates.
 */
public class WebDavSourceSendUtils {
    private final int MB = 1024*1024;
    private Logger logger = Logger.getLogger(WebDavSourceSendUtils.class);
    private WebDavUtils webDavUtils = new WebDavUtils();
    private WebDavFilterUtils webDavFilterUtils = new WebDavFilterUtils();

    /**
     * 发送标识文件列表
     * @param fileList 文件列表对象
     * @param sourceFile   源端配置文件
     * @param sourceOperation     传送到目标端操作对象
     */
    public void syncIngFileList(FileList fileList,SourceFile sourceFile,SourceOperation sourceOperation){
       if(fileList.size()>0){
           Iterator<FileBean> iterator =  fileList.iterable();
           while (iterator.hasNext()){
               FileBean fileBean = iterator.next();
                processSyncIngFile(fileBean,sourceFile,sourceOperation);
           }
       }
    }

    /**
     * 发送未完成文件到目标端
     * @param fileBean 文件对象
     * @param sardine 文件操作对象
     * @param path 文件操作路径
     * @param sourceFile 源端配置文件
     * @param sourceOperation 目标端操作对象
     */
    public void processSyncIngFile(FileBean fileBean,SourceFile sourceFile,SourceOperation sourceOperation){
        //转码后的文件全名
        String path = webDavUtils.getSourceHostAndName(sourceFile);
        if(fileBean.getFullname().startsWith("/")){
            path += webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(sourceFile.getDir()) + fileBean.getFullname()),sourceFile).replaceAll("\\+", "%20");
        }else {
            path += webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(sourceFile.getDir()) + "/" + fileBean.getFullname()),sourceFile).replaceAll("\\+", "%20");
        }
        Sardine sardine = SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        InputStream inputStream = null;
        if(fileBean.getFilepostlocation()==-2){
            try {
                inputStream = sardine.get(path);
            } catch (IOException e) {
                logger.info("***获取"+fileBean.getFullname()+"文件流不成功,文件可能被移动!!!!***");
            }
            if(inputStream != null){
                boolean  flag = processSyncIngBytes(inputStream,fileBean,sourceFile,sourceOperation);
                if(flag){
                    try{
                        webDavUtils.deleteLinkFile(fileBean,sardine,sourceFile);//删除标识文件
                        if(sourceFile.isDeletefile()){
                            //如果源端删除，直接删除
                            sardine.delete(path);
                            //删除源端文件文件夹
                            deleteSourceDirectory(fileBean, sardine, sourceFile);
                        }
                    }catch (IOException e){
                        logger.error(fileBean.getFullname()+"***改回原名不成功!!!文件可能被修改或移动或修改!!!***"+e.getMessage());
                        return;
                    }
                }
            }else {
                logger.info("***获取文件流"+fileBean.getFullname()+"出错!!!可能文件已修改!!!***");
                return;
            }

        } else {
        try {
            inputStream = sardine.get(path);
        } catch (IOException e) {
            logger.info("***获取"+fileBean.getFullname()+"文件流不成功,文件可能被移动!!!!***");
        }
        if(inputStream != null){
            try {
                inputStream.skip(fileBean.getFilepostlocation());
            } catch (IOException e) {
               logger.info("跳过目标端文件长度报错!!");
            }
            boolean  flag = processSyncIngBytes(inputStream,fileBean,sourceFile,sourceOperation);
            if(flag){
                try{
                    webDavUtils.deleteLinkFile(fileBean,sardine,sourceFile);//删除标识文件
                    if(sourceFile.isDeletefile()){
                        //如果源端删除，直接删除
                        sardine.delete(path);
                        //删除源端文件文件夹
                        deleteSourceDirectory(fileBean, sardine, sourceFile);
                    }
                }catch (IOException e){
                    logger.error(fileBean.getFullname()+"***改回原名不成功!!!文件可能被修改或移动或修改!!!***"+e.getMessage());
                    return;
                }
            }
        }else {
            logger.info("***获取文件流"+fileBean.getFullname()+"出错!!!可能文件已修改!!!***");
            return;
        }
        }
    }

    /**
     * 构建文件对象
     * @param resource  资源对象
     * @param sourceFile     源端配置文件
     * @return                     返回构建好的文件对象
     */
    public FileBean buildFileBean(DavResource resource,SourceFile sourceFile){
         FileBean fileBean=new FileBean();
         fileBean.setFilesize(resource.getContentLength());
        try{
            //中文的转码后的href，可能含有主机名和端口号,可能没有
            String href= URLDecoder.decode(resource.getHref().toString().replaceAll("\\+","%2B"),sourceFile.getCharset());
            if(!href.contains("http://"))  {
                fileBean.setName(href.substring(href.lastIndexOf("/")+1,href.length()));
                fileBean.setFullname(href.replaceFirst(webDavUtils.judgeWorkDir(sourceFile.getDir()),""));
            } else {
                fileBean.setName(href.substring(href.lastIndexOf("/")+1,href.length()));
                fileBean.setFullname(href.replaceFirst(webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir()),""));
            }
        }catch (UnsupportedEncodingException e) {
            logger.info("****服务器不支持URL转码操作!!!!!****");
        }
            fileBean.setTime(resource.getModified().getTime());
            return  fileBean;
    }

    /**
     *
     * @param sourceFile
     * @return
     */
    public List<DavResource> ergodicSourceSyncIng(SourceFile sourceFile){
        //保存源端同步过的所有文件
        List<DavResource>  syncIngFileList =new ArrayList<DavResource>();
        //保存的目录列表
        List<DavResource> folder = new ArrayList<DavResource>();
        //初始请求路径
        String sourceRequestUrl = webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir()),sourceFile);
        //构建操作对象
        Sardine sardine = SardineFactory.begin(sourceFile.getUserName(), sourceFile.getPassword());
        //webDAV资源
        List<DavResource> sourceDavResource=null;
        try {
            if(sardine.exists(sourceRequestUrl)){
                sourceDavResource = sardine.list(sourceRequestUrl);
            }
        }catch (IOException e){
            logger.info(e.getMessage()+"***遍历服务器目录出错，连接失败或服务器目录设置出错!!!***");
            return null;
        }
        if(!sourceDavResource.isEmpty()){
            Iterator<DavResource> sourceIterator = sourceDavResource.iterator();      //遍历
            sourceIterator.next();   //跳过第一个指向父目录的资源
            while (sourceIterator.hasNext()){
                DavResource resource = sourceIterator.next();
                if(resource.isDirectory()){
                    folder.add(resource);
                }else{
                        String fileName = webDavUtils.getDavResourceName(resource,sourceFile); //得到资源名称
                    if(fileName.startsWith(FileContext.Str_Sync)&&fileName.endsWith(FileContext.Str_Lnk)){
                        String name = webDavUtils.getSyncIngFileName(resource,sourceFile);
                        boolean  flag = webDavFilterUtils.fileBeanFilter(name,sourceFile);
                        if(flag){
                          syncIngFileList.add(resource);  //添加到同步文件列表中
                        }
                    }
                }
            }
        }
        if(!folder.isEmpty()){
            ergodicSourceFolderSyncIng(sourceFile,folder,sardine,syncIngFileList);      //遍历下层目录
        }
        return syncIngFileList;
    }

    /**
     *
     * @param sourceFile
     * @param sourceFolder
     * @param sardine
     * @param syncIngFileList
     */
    public void  ergodicSourceFolderSyncIng(SourceFile sourceFile,List<DavResource> sourceFolder,Sardine sardine,List<DavResource> syncIngFileList){
        if(sourceFile.isIsincludesubdir()){
            //文件夹列表
            List<DavResource> folder = new ArrayList<DavResource>();
            //遍历传过来的文件夹集合
            if(!sourceFolder.isEmpty()){
                Iterator<DavResource> soIterator = sourceFolder.iterator();
                while (soIterator.hasNext()){
                    DavResource davResource = soIterator.next();
                    String requestUrl = null;
                    if(davResource.getHref().toString().contains("http://")) {
                        String decoder = webDavUtils.urlDecoder(davResource.getHref().toString(),sourceFile).replaceFirst(webDavUtils.getSourceHostAndName(sourceFile),"");
                        requestUrl =  webDavUtils.getSourceHostAndName(sourceFile)+
                                webDavUtils.urlEncoder(webDavUtils.urlDecoder(decoder,sourceFile),sourceFile).replaceAll("\\+", "%20");
                    }else {
                        requestUrl = webDavUtils.getSourceHostAndName(sourceFile) +
                                webDavUtils.urlEncoder(webDavUtils.urlDecoder(davResource.getHref().toString(),sourceFile),sourceFile).replaceAll("\\+", "%20");
                    }
                    //得到资源
                    List<DavResource> davResources = null;
                    try {
                        if(sardine.exists(requestUrl)){
                            davResources = sardine.list(requestUrl);
                        }
                    } catch (IOException e) {
                        logger.info(e.getMessage()+"***Error!!!文件夹可能移动或修改!!!***");
                        return;
                    }
                    if(davResources!=null){
                        if(davResources.size()>0){
                            Iterator<DavResource> iterator = davResources.iterator();
                            //跳过父目录
                            iterator.next();
                            while (iterator.hasNext()){
                                DavResource resource = iterator.next();
                                if(resource.isDirectory()){
                                    folder.add(resource); //下层文件夹加入些对象
                                }else{
                                        String fileName = webDavUtils.getDavResourceName(resource,sourceFile);   //得到资源名称
                                    if(fileName.startsWith(FileContext.Str_Sync)&&fileName.endsWith(FileContext.Str_Lnk)){
                                        String name = webDavUtils.getSyncIngFileName(resource,sourceFile);
                                        boolean  flag = webDavFilterUtils.fileBeanFilter(name,sourceFile);
                                        if(flag){
                                            syncIngFileList.add(resource);  //添加到同步文件列表中
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(folder.size() > 0 ){
                ergodicSourceFolderSyncIng(sourceFile,folder,sardine,syncIngFileList); //下层遍历
            }
        }
    }

    /**
     * 发送Bytes数组到目标端
     * @param in 输入流对象
     * @param fileBean 文件对象
     * @param sourceFile 源端配置文件
     * @param sourceOperation 目标端操作对象
     * @return
     */
    public boolean processSyncIngBytes(InputStream inputStream,FileBean fileBean,SourceFile sourceFile,SourceOperation sourceOperation){
        boolean flag = false;
        int length = -1;
        long readLength = 0;
        byte[] bytes = new byte[sourceFile.getPacketsize()*MB];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
                 while ((length = inputStream.read(bytes)) != -1){
                      if(readLength >= sourceFile.getPacketsize()*MB){
                            fileBean.setSyncflag(FileContext.Str_SyncFileStart);
                            flag  =  sourceOperation.process(out.toByteArray(),fileBean);
                            fileBean.setFilepostlocation(3);
                            if(flag == false) {
                                readLength = -1;
                                return false;
                            }
                            readLength = 0;
                            out.reset();
                            out.write(bytes, 0, length);
                            readLength += length;
                      } else {
                            out.write(bytes, 0, length);
                            readLength += length;
                      }
                 }
                 if(readLength >= 0){
                     fileBean.setSyncflag(FileContext.Str_SyncFileEnd);
                     flag = sourceOperation.process(out.toByteArray(),fileBean);
                     fileBean.setFilepostlocation(3);
                     out.reset();
                     readLength = 0;
                 }
        } catch (IOException e) {
            logger.info("***目标端发送文件"+fileBean.getFullname()+"数据报错!!!***");
            return false;
        }finally {
            try {
                inputStream.close();
                out.close();
            } catch (IOException e) {
                logger.info("***关闭流"+fileBean.getFullname()+"出错!!!***");
                return false;
            }
        }
        return flag;
    }

    /**
     * 传送普通文件
     * @param inputStream
     * @param fileBean
     * @param sourceFile
     * @param sourceOperation
     * @return
     */
    public boolean processBytes(InputStream inputStream,FileBean fileBean,SourceFile sourceFile,SourceOperation sourceOperation ){
          boolean flag = false;
          int length = -1;
          long readLength = 0;
          byte[] bytes = new byte[sourceFile.getPacketsize()*MB];
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          try {
               int pointer = 0;
              while ((length = inputStream.read(bytes)) != -1){
                  if(readLength >= sourceFile.getPacketsize()*MB){
                      fileBean.setFilepostlocation(pointer);
                      fileBean.setSyncflag(FileContext.Str_SyncFileStart);
                      flag  =  sourceOperation.process(out.toByteArray(),fileBean);
                      if(flag == false) {
                          readLength = -1;
                          return false;
                      }
                      pointer ++;
                      readLength = 0;
                      out.reset();
                      out.write(bytes, 0, length);
                      readLength += length;
                  } else {
                      out.write(bytes, 0, length);
                      readLength += length;
                  }
              }
              if(readLength >= 0){
                  fileBean.setSyncflag(FileContext.Str_SyncFileEnd);
                  fileBean.setFilepostlocation(pointer);
                  flag = sourceOperation.process(out.toByteArray(),fileBean);
                  pointer ++;
                  out.reset();
                  readLength = 0;
              }
          } catch (IOException e) {
              logger.info("***目标端发送文件"+fileBean.getFullname()+"数据报错!!!***");
              return false;
          }finally {
              try {
                  inputStream.close();
                  out.close();
              } catch (IOException e) {
                  logger.info("***关闭流"+fileBean.getFullname()+"出错!!!***");
                  return false;
              }
          }
          return flag;
      }
    /**
     * 发送文件到目标端的方法
     * @param fileBean      源端文件对象
     * @param path            源端文件路径
     * @param sardine         源端操作对象
     * @param iTargetProcess   目标端操作对象
     * @param sourceFile         源端配置文件
     */
    public void processFile(FileBean fileBean,SourceOperation sourceOperation,Sardine sardine,SourceFile sourceFile){
        //转码后的文件全名
         String path = webDavUtils.getSourceHostAndName(sourceFile);
         if(fileBean.getFullname().startsWith("/")){
              path += webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(sourceFile.getDir()) + fileBean.getFullname()),sourceFile).replaceAll("\\+", "%20");
         }else {
              path += webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(sourceFile.getDir()) + "/" + fileBean.getFullname()),sourceFile).replaceAll("\\+", "%20");
         }
        InputStream inputStream = null;
        webDavUtils.createLinkFile(fileBean,sardine,sourceFile);//构建标识文件
        try {
            inputStream = sardine.get(path);
        } catch (IOException e) {
            logger.info("***获取"+fileBean.getFullname()+"文件流不成功,文件可能被移动!!!!***");
        }
        if(inputStream != null){
            boolean  flag = processBytes(inputStream,fileBean,sourceFile,sourceOperation);
            if(flag){
                try{
                    webDavUtils.deleteLinkFile(fileBean,sardine,sourceFile);//删除标识文件
                    if(sourceFile.isDeletefile()){
                        //如果源端删除，直接删除
                        sardine.delete(path);
                        //删除源端文件文件夹
                        deleteSourceDirectory(fileBean, sardine, sourceFile);
                    }
                }catch (IOException e){
                    logger.error(fileBean.getFullname()+"***改回原名不成功!!!文件可能被修改或移动或修改!!!***"+e.getMessage());
                    return;
                }
            }
        }else {
            logger.info("***获取文件流"+fileBean.getFullname()+"出错!!!可能文件已修改!!!***");
            return;
        }
    }

    /**
     * 上传有分批个数的文件
     * @param sourceFile     源配置文件
     * @param fileList         列表
     * @param sardine           源操作对象
     * @param iTargetProcess   目标端操作对象
     */
    public void processList(SourceFile sourceFile,FileList fileList,Sardine sardine,SourceOperation sourceOperation) {
        if(fileList.size() > 0 && fileList != null){
        //取出文件列表
        Iterator<FileBean> it = fileList.iterable();
        while (it.hasNext()){
            FileBean fileBean = it.next();
            //发送文件方法
             processFile(fileBean, sourceOperation, sardine, sourceFile);
        }
        fileList.clear();
        }
    }

    /**
     *  循环删除源端文件夹
     * @param sardine       源端操作对象
     * @param requestUrl        请求路径
     * @param sourceFile            源端配置文件
     */
    public void deleteSourceFolder(Sardine sardine,String requestUrl,SourceFile sourceFile){
        try {
            if(sardine.exists(requestUrl)){
                List<DavResource> davResourceList = sardine.list(requestUrl);
                Iterator<DavResource> iterator = davResourceList.iterator();
                iterator.next();
                if(!iterator.hasNext()) {
                    if(requestUrl.endsWith("/")){
                        sardine.delete(requestUrl);
                    }else {
                        requestUrl += "/";
                        sardine.delete(requestUrl);
                    }
                    requestUrl = requestUrl.substring(0,requestUrl.lastIndexOf("/"));
                    if(requestUrl.contains("/")){
                        requestUrl = requestUrl.substring(0,requestUrl.lastIndexOf("/"));
                        if(!requestUrl.equals(webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir()),sourceFile))){
                            deleteSourceFolder(sardine,requestUrl,sourceFile);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage()+"***删除源端文件夹"+webDavUtils.urlDecoder(requestUrl,sourceFile)+"不成功!!!***");
        }
    }

    /**
     * 删除源端文件夹
     * @param fileBean   文件对象
     * @param sardine         文件操作对象
     * @param sourceFile           源端配置文件
     */
    public void deleteSourceDirectory(FileBean fileBean,Sardine sardine,SourceFile sourceFile){
        //得到fileBean的路径，不包括文件名，主机名和端口
        String targetDir = webDavUtils.getFileDir(fileBean);
        //得到fileBean的路径，不包括文件名
        String requestUrl = webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+targetDir,sourceFile);
        //循环判断删除路径
        deleteSourceFolder(sardine,requestUrl,sourceFile);
    }

    /**
     * 遍历源端根目录
     * @param sourceOperation     目标端操作对象
     * @param sourceFile               源端配置文件
     * @param uploadCount               分批上传个数
     * @param pool                         线程池
     */
    public void ergodicSource(SourceOperation sourceOperation,SourceFile sourceFile,long uploadCount,ExecutorService pool){
        //保存源端同步过的所有文件
        FileList sendFileList = new FileList();
        //保存的目录列表
        List<DavResource> folder = new ArrayList<DavResource>();
        //初始请求路径
        String sourceRequestUrl = webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir()),sourceFile);
        //构建操作对象
        Sardine sardine = SardineFactory.begin(sourceFile.getUserName(), sourceFile.getPassword());
        //webDAV资源
        List<DavResource> sourceDavResource=null;
            try {
                if(sardine.exists(sourceRequestUrl)){
                    sourceDavResource = sardine.list(sourceRequestUrl);
                }
            }catch (IOException e){
                logger.info(e.getMessage()+"***遍历服务器目录出错，连接失败或服务器目录设置出错!!!***");
                return;
            }
        if(!sourceDavResource.isEmpty()){
            //遍历
            Iterator<DavResource> sourceIterator = sourceDavResource.iterator();
            //跳过第一个指向父目录的资源
            sourceIterator.next();
            //循环遍历根目录
            while (sourceIterator.hasNext()){
                DavResource resource = sourceIterator.next();
                if(resource.isDirectory()){
                    folder.add(resource);
                }else{
                    if(resource.getContentLength()>0){
                        String fileName = webDavUtils.getDavResourceName(resource,sourceFile); //得到资源名称
                        boolean flag = webDavFilterUtils.fileBeanFilter(fileName,sourceFile);
                        if(flag) {
                                FileBean fileBean =buildFileBean(resource,sourceFile);
                                if(fileBean!=null)
                                 sendFileList.addFileBean(fileBean);
                            if(sendFileList.size() == (uploadCount*(sourceFile.getThreads()))){
                                poolFileList(sendFileList,sourceFile,uploadCount,pool,sourceOperation);
                            }
                        }
                    }
                }
            }
        }
        if(sendFileList.size() > 0){
            poolFileList(sendFileList,sourceFile,uploadCount,pool,sourceOperation);
        }
        if(!folder.isEmpty()){
            ergodicSourceFolder(sourceFile,folder,sardine,uploadCount,sourceOperation,pool);      //遍历下层目录
        }
    }

    /**
     * 遍历下层目录方法
     * @param sourceFile  源端配置文件
     * @param sourceFolder    源端上层文件夹
     * @param sardine          源端操作对象
     * @param uploadCount     分批个数
     * @param iTargetProcess     目标端操作对象
     * @param pool                 源端线程池
     */
    public void ergodicSourceFolder(SourceFile sourceFile,List<DavResource> sourceFolder,Sardine sardine,long uploadCount,SourceOperation sourceOperation,ExecutorService pool){
        if(sourceFile.isIsincludesubdir()){
            FileList sendFileList=new FileList();
            //文件夹列表
            List<DavResource> folder = new ArrayList<DavResource>();
            //遍历传过来的文件夹集合
            if(!sourceFolder.isEmpty()){
                Iterator<DavResource> soIterator = sourceFolder.iterator();
                while (soIterator.hasNext()){
                    DavResource davResource = soIterator.next();
                    String requestUrl = null;
                    if(davResource.getHref().toString().contains("http://")) {
                        String decoder = webDavUtils.urlDecoder(davResource.getHref().toString(),sourceFile).replaceFirst(webDavUtils.getSourceHostAndName(sourceFile),"");
                        requestUrl =  webDavUtils.getSourceHostAndName(sourceFile)+
                                webDavUtils.urlEncoder(webDavUtils.urlDecoder(decoder,sourceFile),sourceFile).replaceAll("\\+", "%20");
                    }else {
                        requestUrl = webDavUtils.getSourceHostAndName(sourceFile) +
                                webDavUtils.urlEncoder(webDavUtils.urlDecoder(davResource.getHref().toString(),sourceFile),sourceFile).replaceAll("\\+", "%20");
                    }
                    //得到资源
                    List<DavResource> davResources = null;
                    try {
                        if(sardine.exists(requestUrl)){
                            davResources = sardine.list(requestUrl);
                        }
                    } catch (IOException e) {
                        logger.info(e.getMessage()+"***Error!!!文件夹可能移动或修改!!!***");
                        return;
                    }
                    if(davResources!=null){
                        if(davResources.size()>0){
                            Iterator<DavResource> iterator = davResources.iterator();
                            //跳过父目录
                            iterator.next();
                            while (iterator.hasNext()){
                                DavResource resource = iterator.next();
                                if(resource.isDirectory()){
                                    folder.add(resource); //下层文件夹加入些对象
                                }else{
                                    if(resource.getContentLength()>0){
                                    String fileName = webDavUtils.getDavResourceName(resource,sourceFile);   //得到资源名称
                                        boolean flag = webDavFilterUtils.fileBeanFilter(fileName,sourceFile);
                                        if(flag) {
                                             FileBean fileBean =buildFileBean(resource,sourceFile);
                                             if(fileBean!=null)
                                              sendFileList.addFileBean(fileBean);
                                            if(sendFileList.size() == (uploadCount*(sourceFile.getThreads()))){
                                                poolFileList(sendFileList,sourceFile,uploadCount,pool,sourceOperation);
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
            if(sendFileList.size() > 0){
                poolFileList(sendFileList,sourceFile,uploadCount,pool,sourceOperation);
            }
            //遍历下层目录
            if(folder.size() > 0 ){
                ergodicSourceFolder(sourceFile,folder,sardine,uploadCount,sourceOperation,pool); //下层遍历
            }
        }
    }

    /**
     * 分线程发送文件
     * @param poolList 需要发送的文件列表
     * @param sourceFile    源端配置文件
     * @param uploadCount        分批上传个数
     * @param pool                    线程池
     * @param sourceOperation          目标端操作对象
     */
    public void poolFileList(FileList poolList,SourceFile sourceFile,long  uploadCount,ExecutorService pool,SourceOperation sourceOperation){
        FileList sFileList = alterFileList(poolList) ;
        FileList sendFileList = sourceOperation.procesFileList(sFileList);
        if(sendFileList!=null){
            if(sendFileList.size()>0){
        //如果文件列表大小等于，线程数乘以分批大小
                if(sendFileList.size() == (sourceFile.getThreads()*uploadCount)){    //当列表个数等于线程数乘以分批个数时就开妈发送
                    Iterator<FileBean> its = sendFileList.iterable();
                    FileList poolFileList = new FileList();
                    while (its.hasNext()){
                        poolFileList.addFileBean(its.next());
                        if(poolFileList.size() == uploadCount){
                            poolList(sourceOperation,poolFileList,pool,sourceFile);       //发送文件列表任务
                        }
                    }
                    if(poolFileList.size() > 0&&poolFileList.size() < sourceFile.getThreads()){
                        poolFile(sourceOperation,poolFileList,pool,sourceFile);   //发送单个文件任务
                    }else {
                        poolList(sourceOperation,poolFileList,pool,sourceFile);        //发送文件列表任务
                    }
                    sendFileList.clear();
                    //如果文件列表大小，大于线程数但小于线程数乘以分批个数
                }else if((sourceFile.getThreads() < sendFileList.size())&&(sendFileList.size() < (sourceFile.getThreads()*uploadCount))){
                    long count = sendFileList.size()/sourceFile.getThreads()+1;
                    Iterator<FileBean> its = sendFileList.iterable();
                    FileList poolFileList = new FileList();
                    while (its.hasNext()){
                        poolFileList.addFileBean(its.next());
                        if(poolFileList.size() == count){
                            poolList(sourceOperation,poolFileList,pool,sourceFile);    //发送文件列表任务
                        }
                    }
                    if(poolFileList.size() > 0&&poolFileList.size() < sourceFile.getThreads()){    //发送文件列表任务
                        //文件数小于线程数时候的处理方式
                        poolFile(sourceOperation,poolFileList,pool,sourceFile);    //发送单个文件任务
                    }else {
                        poolList(sourceOperation,poolFileList,pool,sourceFile);                     //发送文件列表任务
                    }
                    sendFileList.clear();
                }else { //文件数小于线程数时候的处理方式
                        poolFile(sourceOperation,sendFileList,pool,sourceFile);   //发送单个文件任务
                        sendFileList.clear();
                }
            }
        }
    }

    /**
     * 发送文件列表到目标端
     * @param sourceOperation  目标端操作对象
     * @param poolFileList     需要发送的文件列表
     * @param pool               发送的线程池对象
     * @param sourceFile        源端配置文件
     */
    public void poolList(SourceOperation sourceOperation,FileList poolFileList,ExecutorService pool,SourceFile sourceFile){
        if(poolFileList.size() > 0){
            pool.execute(new WebDavSourceListTask(poolFileList,sourceFile,sourceOperation));
        }
    }

    /**
     *   单个发送文件对象
     * @param sourceOperation    目标端操作对象
     * @param poolFileList      需要发送的列表
     * @param pool                线程池
     * @param sourceFile        源端配置文件
     */
    public void poolFile(SourceOperation sourceOperation,FileList poolFileList,ExecutorService pool,SourceFile sourceFile){
        if(poolFileList.size() > 0){
            Iterator<FileBean> iterator = poolFileList.iterable();
            while (iterator.hasNext()){
                FileBean fileBean = iterator.next();
                pool.execute(new WebDavSourceFileTask(fileBean,sourceFile,sourceOperation));
            }
        }

    }

    /**
     * 转换要同步的文件列表
     * @param poolFileList 同步文件列表
     * @return  要上传伯文件列表
     */
    public FileList alterFileList(FileList poolFileList){
        FileList sendFileList = new FileList();
        if(poolFileList.size()>0&&poolFileList!=null){
            Iterator<FileBean> fileBeanIterator = poolFileList.iterable();
            while (fileBeanIterator.hasNext())
                sendFileList.addFileBean(fileBeanIterator.next());
        }
        poolFileList.clear();
        return sendFileList;
    }
}
