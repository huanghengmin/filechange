package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;

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
 * User: Daly
 * Date: 12-4-19
 * Time: 下午3:27
 * To change this template use File | Settings | File Templates.
 */
public class WebDavTargetSendUtils {
    private Logger logger=Logger.getLogger(WebDavTargetSendUtils.class);
    private WebDavUtils webDavUtils= WebDavUtils.getSingleWebDavUtil();
    private JDomUtils jDomUtil= JDomUtils.getSingleJDomUtils();
    private static WebDavTargetSendUtils webDavSendUtils =null;
    private WebDavTargetSendUtils(){}
    public static WebDavTargetSendUtils getSingleWebDavTargetUtils(){
        if(webDavSendUtils ==null){
            webDavSendUtils =new WebDavTargetSendUtils();
        }
        return webDavSendUtils;
    }

    /**
     * 删除目标端文件
     * @param fileList  要删除的文件列表
     * @param targetFile  目标端配置文件
     */
    public void deleteFiles(FileList fileList,TargetFile targetFile){
        Iterator<FileBean> its=fileList.iterable();
        while (its.hasNext()){
            FileBean fileBean=its.next();
            //得到同步文件中是否存在此fileBean
            boolean bool = jDomUtil.getSyncXMLFileBean(fileBean);
            if(!bool){
                //目标端文件全名
                String targetFullName= webDavUtils.getTargetHostAndName(targetFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname(),targetFile);
                //目标端操作对象
                Sardine sardine=SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
               try {
                    if(sardine.exists(targetFullName)){
                        //删除文件
                        sardine.delete(targetFullName);       //删除文件
                        //删除目标端文件夹
                        deleteTargetDirectory(fileBean,sardine,targetFile);    //删除目标端文件夹
                        logger.info("删除目标端文件"+targetFullName+"成功");
                    }
                } catch (IOException e) {
                   logger.info("删除目标端文件"+targetFullName+"失败！可能文件已删除！！！");
                }
            }
        }
        fileList.clear();
    }


    /**
     * 目标端改名操作
     * @param path   目标端保存文件路径
     * @param sardine    操作对象
     * @param fileBean   文件对象
     */
    public void sendRename(String path,Sardine sardine,FileBean fileBean){
        try{
            if(sardine.exists(path)){
                sardine.move(path, path + FileContext.Str_SyncFileTargetProcess_Flag);
            }
        }catch (IOException e){
            logger.info("移动"+fileBean.getFullname()+"文件不成功");
        }
    }

    /**
     * 目标端改回原名操作
     * @param path       目标端保存文件路径
     * @param sardine    操作对象
     * @param fileBean   文件对象
     */
    public void sendRenameToTargetSourceName(String path,Sardine sardine,FileBean fileBean){
        try{
            if(!sardine.exists(path)&&sardine.exists(path + FileContext.Str_SyncFileTargetProcess_Flag)){
                sardine.move(path+ FileContext.Str_SyncFileTargetProcess_Flag,path);
            }
        }catch (IOException e){
            logger.info("文件"+fileBean.getFullname()+"改回原名不成功！！！！");
        }
    }

    /**
     *      得到目标端文件流
     * @param path      目标端文件路径
     * @param sardine   文件操作对象
     * @param fileBean  文件对象
     * @return           返回文件流
     */
    public InputStream getTargetFileBeanInputStream(String path,Sardine sardine,FileBean fileBean){
        InputStream inputStream=null;
        try{
            if(sardine.exists(path+ FileContext.Str_SyncFileTargetProcess_Flag)) {
                inputStream = sardine.get(path+ FileContext.Str_SyncFileTargetProcess_Flag);
            }
        }catch (IOException e) {
            logger.info("**********读文件"+fileBean.getFullname()+"流不成功**********");
        }
        return  inputStream;
    }

    /**
     * 发送文件到源端
     * @param inputStream      发送文件到源端服务器
     * @param fileBean          发送文件对象
     * @return                   返回源端是否保存成功标识
     */
    public boolean processFileBeanInputStreamToSource(TargetOperation targetOperation,InputStream inputStream,FileBean fileBean){
        int i=0;
        boolean flag = false;
        while (!flag){
            i++;
            flag = targetOperation.process(inputStream,fileBean);
            if(i==3)
                break;
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
     public void  send_FileBean(FileBean fileBean,TargetOperation targetOperation,Sardine sardine,TargetFile targetFile){
        //转码后的文件全名
         String path= webDavUtils.getTargetHostAndName(targetFile)+
                 webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname()).replace("+","%2B"),targetFile).replace("+","%20");
        //如果为未完成文件处理方式
         if(fileBean.getFullname().endsWith(FileContext.Str_SyncFileTargetProcess_Flag)){
             InputStream inputStream=null;
             try {
                //得到输入流
                inputStream= sardine.get(path);
                 boolean  flag=false;
                 if(inputStream!=null){
                 //发送文件到源端
                     flag = processFileBeanInputStreamToSource(targetOperation, inputStream, fileBean);      //发送文件到源端
                 } else {
                     logger.info("读取文件流"+fileBean.getFullname()+"出错！！！！！");
                 }
                if(flag){
                     //改回原名
                     sardine.move(path,path.replace(FileContext.Str_SyncFileTargetProcess_Flag,""));
                }
             } catch (IOException e) {
                  logger.info(e.getMessage()+"目标端改名出错！！可能文件正在操作！！！");
             }
         }else {
              //改名
              sendRename(path, sardine, fileBean);    //改名
              //读流
              InputStream inputStream= null;
                      if(inputStream!=null){

                          inputStream=getTargetFileBeanInputStream(path,sardine,fileBean);  //读流

                      }else {
                            logger.info("读取文件流出错，可能文件"+fileBean.getFullname()+"已移动！！！！ ");
                      }
              //发送文件到源端
              boolean  flag = processFileBeanInputStreamToSource(targetOperation, inputStream, fileBean);      //发送文件到源端
              //当返回结果为true就改回原名
              if(flag){
                  //改回原名
                   sendRenameToTargetSourceName(path, sardine, fileBean);     //改回原名
              }
         }
     }

    /**
     * 删除源端文件夹
     * @param fileBean 要删除文件夹的文件对象
     * @param sardine       目标端操作对象
     * @param targetFile    目标端配置文件
     */
     public synchronized void deleteTargetDirectory(FileBean fileBean,Sardine sardine,TargetFile targetFile){
        //得到目标端文件路径，不包括主机名，端口
        String targetDir= webDavUtils.getFileDir(fileBean);
        //得到目标端文件路径
        String requestUrl= webDavUtils.getTargetHostAndName(targetFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+targetDir,targetFile);
        //循环删除文件夹
        deleteTargetFolder(sardine,requestUrl,targetFile);
    }

    /**
     *  循环删除源端文件夹
     * @param sardine    目标端操作对象
     * @param requestUrl   判断删除文件的路径
     * @param targetFile   目标端配置文件
     */
     public synchronized void deleteTargetFolder(Sardine sardine,String requestUrl,TargetFile targetFile){
        try {
            if(sardine.exists(requestUrl)){
                List<DavResource> davResourceList= sardine.list(requestUrl);
                if(!davResourceList.isEmpty()){
                Iterator<DavResource> iterator=davResourceList.iterator();
                iterator.next();
                if(!iterator.hasNext()) {
                    if(requestUrl.endsWith("/"))  {
                       sardine.delete(requestUrl);
                    }
                    else{
                        requestUrl=requestUrl+"/";
                         sardine.delete(requestUrl);
                    }
                    requestUrl=requestUrl.substring(0,requestUrl.lastIndexOf("/"));
                    //判断上级文件夹
                    if(requestUrl.contains("/")){
                        requestUrl=requestUrl.substring(0,requestUrl.lastIndexOf("/"));
                        if(!requestUrl.equals(webDavUtils.getTargetHostAndName(targetFile)+
                                webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()),targetFile))){
                               //删除文件夹
                               deleteTargetFolder(sardine,requestUrl,targetFile);
                        }
                    }
                }
            }
            }
        } catch (IOException e) {
            logger.info("删除文件夹"+requestUrl+"出错！！目标可能已移动！！！");
        }
    }

    /**
     * 上传有分批后的文件列表
     * @param sourceFile     源配置文件
     * @param fileList         列表
     * @param sardine           源操作对象
     * @param iTargetProcess   目标端操作对象
     */
    public  void send_BatchFileBeans(TargetFile targetFile,FileList fileList,Sardine sardine,TargetOperation targetOperation) {
        //取出文件列表
        Iterator<FileBean> it=fileList.iterable();
        while (it.hasNext()){
            FileBean fileBean=it.next();
            //发送文件方法
            send_FileBean(fileBean, targetOperation, sardine, targetFile);
        }
        fileList.clear();
    }


    /**
     * 封装生成为fileBean对象
     * @param davResource 服务器资源
     * @param workDir      工作空间
     * @return            封装为fileBean对象
     */
     public FileBean createToFileBean(DavResource davResource,TargetFile targetFile){
         FileBean fileBean=new FileBean();
         fileBean.setFilesize(davResource.getContentLength());
         try{
             //中文的转码后的href，可能含有主机名和端口号,可能没有
             String href=URLDecoder.decode(davResource.getHref().toString(),targetFile.getCharset());
             if(!href.contains("http://"))  {
                 fileBean.setName(href.substring(href.lastIndexOf("/"),href.length()));
                 fileBean.setFullname(href.replace(webDavUtils.judgeWorkDir(targetFile.getDir()),""));
             } else {
                 fileBean.setName(href.substring(href.lastIndexOf("/"),href.length()));
                 fileBean.setFullname(href.replace(webDavUtils.getTargetHostAndName(targetFile)+webDavUtils.judgeWorkDir(targetFile.getDir()),""));
             }
         }catch (UnsupportedEncodingException e) {
             logger.info("不支持URL转码操作！！！");
         }
         /*fileBean.setTime(Long.parseLong(resource.getModified().toString()));
         try {
               fileBean.setMd5(FileMd5.getFileMD5String(resource.getPath()));
              } catch (IOException e) {
         }*/
         return  fileBean;
   }

    /**
     * 遍历下层目录方法
     * @param targetFile  源端配置文件
     * @param sourceFolder    源端上层文件夹
     * @param sardine          源端操作对象
     * @param uploadCount     分批个数
     * @param pool                 源端线程池
     */
     public void ergodicTargetFolder(TargetFile targetFile,List<DavResource> sourceFolder,Sardine sardine,long uploadCount,TargetOperation targetOperation,ExecutorService pool,boolean delete){
            FileList sendFileList=new FileList();
            //文件夹列表
            List<DavResource> folder=new ArrayList<DavResource>();
            //遍历传过来的文件夹集合
            if(!sourceFolder.isEmpty()){
                Iterator<DavResource> soIterator=sourceFolder.iterator();
                while (soIterator.hasNext()){
                    DavResource davResource=soIterator.next();
                    //改变请求路径
                    String requestUrl= null;
                    String href=webDavUtils.urlDecoder(davResource.getHref().toString(),targetFile);
                    if(href.contains("http://")) {
                        String decoder=href.replace(webDavUtils.getTargetHostAndName(targetFile),"");
                        requestUrl=  webDavUtils.getTargetHostAndName(targetFile)+webDavUtils.urlEncoder(decoder,targetFile);
                    }else {
                        requestUrl = webDavUtils.getTargetHostAndName(targetFile)+
                                webDavUtils.urlEncoder(href,targetFile);
                    }
                    //得到资源
                    List<DavResource> davResources=null;
                    try {
                        if(sardine.exists(requestUrl)){
                            davResources=sardine.list(requestUrl);
                        }
                    } catch (IOException e) {
                        logger.info(e.getMessage()+"error!!文件夹可能移动或修改！！");
                        return;
                    }
                    if(!davResources.isEmpty()){
                        Iterator<DavResource> iterator=davResources.iterator();
                        //跳过父目录
                        iterator.next();
                        while (iterator.hasNext()){
                            DavResource resource=iterator.next();
                            if(resource.isDirectory()){
                                //下层文件夹加入对象
                                folder.add(resource);
                            }else{
                                if(resource.getName().endsWith(FileContext.Str_SyncFileTargetProcess_End_Flag)){
                                    //不进行处理
                                }else {
                                    sendFileList.addFileBean(createToFileBean(resource,targetFile));
                                    if(sendFileList.size()==(uploadCount*(targetFile.getThreads()))){
                                        if(delete){
                                            deleteFiles(sendFileList,targetFile);//删除目标端文件
                                        }else {
                                           poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
                                        }
                                    }
                            }
                        }
                    }
                }
            }
            if(sendFileList.size()>0){
                if(delete){
                     deleteFiles(sendFileList,targetFile);//删除目标端文件
                }else {
                      poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
                }
            }
            //遍历下层目录
            if(folder.size()>0){
                ergodicTargetFolder(targetFile,folder,sardine,uploadCount,targetOperation,pool,delete); //下层遍历
            }
      }
   }

    /**
     * 遍历目标端文件 并发送
     * @param targetFile 目标端配置文件
     * @param targetOperation     源端操作对象
     * @param uploadCount         分批个数
     * @param pool                 线程池
     * @param delete               是否删除标识
     */
    public void ergodicTarget(TargetFile targetFile,TargetOperation targetOperation,long uploadCount,ExecutorService pool,boolean delete){
        FileList sendFileList=new FileList();
        //保存的目录列表
        List<DavResource> folder=new ArrayList<DavResource>();
        //初始请求路径
        String sourceRequestUrl= webDavUtils.getTargetHostAndName(targetFile)+
                webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()),targetFile);
        //构建操作对象
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //webDAV资源
        List<DavResource> sourceDavResource=null;
        boolean flag=true;
        while (flag){
        try {
            if(!sardine.exists(sourceRequestUrl)){
                logger.info("***服务器不存在此目录!!!***");
            } else {
                sourceDavResource=sardine.list(sourceRequestUrl);
                if(sourceDavResource!=null){
                    flag=false;
                }
            }
        }catch (IOException e){
            logger.info(e.getMessage()+"***遍历服务器目录出错，连接失败或服务器目录设置出错!!!***");
            return;
        }
        }
        if(!sourceDavResource.isEmpty()){
            Iterator<DavResource> sourceIterator=sourceDavResource.iterator();
            //跳过第一个指向父目录的资源
            sourceIterator.next();
            //循环遍历根目录
            while (sourceIterator.hasNext()){
                DavResource resource=sourceIterator.next();
                if(resource.isDirectory()){
                    folder.add(resource);
                }else{
                    if(resource.getName().endsWith(FileContext.Str_SyncFileTargetProcess_End_Flag)){
                        //如果为目标端同步过来的文件就不用处理

                    }else {
                            sendFileList.addFileBean(createToFileBean(resource,targetFile));
                                if(sendFileList.size()==(uploadCount*(targetFile.getThreads()))){
                                    if(delete){
                                             deleteFiles(sendFileList,targetFile);//删除目标端文件
                                    }else {
                                        poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
                                    }
                            }
                        }
                    }
                }
            }
        if(sendFileList.size()>0){
            if(delete){
                deleteFiles(sendFileList,targetFile);//删除目标端文件
            }else {
                   poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
            }
        }
        if(!folder.isEmpty()){
            ergodicTargetFolder(targetFile,folder,sardine,uploadCount,targetOperation,pool,delete);      //遍历下层目录
        }
    }

    /**
     *   分线程
     * @param sendFileList    发送文件列表
     * @param targetFile      目标端配置文件
     * @param uploadCount     分批上传个数
     * @param pool             线程池
     * @param targetOperation    源端操作对象
     */
    private void poolFileList(FileList sendFileList, TargetFile targetFile, long uploadCount, ExecutorService pool, TargetOperation targetOperation) {
        //如果文件列表大小等于，线程数乘以分批大小
        if(sendFileList.size()==(targetFile.getThreads()*uploadCount)){    //当列表个数等于线程数乘以分批个数时就开妈发送
            Iterator<FileBean> its=sendFileList.iterable();
            FileList poolFileList=new FileList();
            while (its.hasNext()){
                poolFileList.addFileBean(its.next());
                if(poolFileList.size()==uploadCount){
                    processList(targetOperation,poolFileList,pool,targetFile);       //发送文件列表任务
                }
            }
            if(poolFileList.size()>0&&poolFileList.size()<targetFile.getThreads()){
                processFile(targetOperation,poolFileList,pool,targetFile);   //发送单个文件任务
            }else {
                processList(targetOperation,poolFileList,pool,targetFile);        //发送文件列表任务
            }
            sendFileList.clear();
            //如果文件列表大小，大于线程数但小于线程数乘以分批个数
        }else if((targetFile.getThreads() < sendFileList.size())&&(sendFileList.size() <(targetFile.getThreads()*uploadCount))){
            long count=sendFileList.size()/targetFile.getThreads()+1;
            Iterator<FileBean> its=sendFileList.iterable();
            FileList poolFileList=new FileList();
            while (its.hasNext()){
                poolFileList.addFileBean(its.next());
                if(poolFileList.size()==count){
                    processList(targetOperation,poolFileList,pool,targetFile);    //发送文件列表任务
                }
            }
            if(poolFileList.size()>0&&poolFileList.size()<targetFile.getThreads()){    //发送文件列表任务
                //文件数小于线程数时候的处理方式
                processFile(targetOperation,poolFileList,pool,targetFile);    //发送单个文件任务
            }else {
                processList(targetOperation,poolFileList,pool,targetFile);                     //发送文件列表任务
            }
            sendFileList.clear();
        }else { //文件数小于线程数时候的处理方式
            processFile(targetOperation,sendFileList,pool,targetFile);   //发送单个文件任务
            sendFileList.clear();
        }

    }

    /**
     * 调用线程池发送文件列表方法
     * @param poolFileList    发送文件列表
     * @param pool              线程池
     * @param targetFile       目标端配置文件
     */
    private void processList(TargetOperation targetOperation, FileList poolFileList, ExecutorService pool, TargetFile targetFile) {
        FileList sFileList=targetOperation.procesFileList(alterFileList(poolFileList));
        if(sFileList.size()>0){
            pool.execute(new WebDavTargetListTask(sFileList,targetFile,targetOperation));
        }
    }

    /**
     *   单个发送文件对象
     * @param iTargetProcess    目标端操作对象
     * @param poolFileList      需要发送的列表
     * @param pool                线程池
     * @param sourceFile        源端配置文件
     */
    public void processFile(TargetOperation targetOperation,FileList poolFileList,ExecutorService pool,TargetFile targetFile){
        FileList fileList=targetOperation.procesFileList(alterFileList(poolFileList));
        Iterator<FileBean> iterator=fileList.iterable();
        while (iterator.hasNext()){
            FileBean fileBean=iterator.next();
            pool.execute(new WebDavTargetFileTask(fileBean,targetFile,targetOperation));    //发送单个文件任务
        }
    }

    /**
     * 转换要同步的文件列表
     * @param poolFileList 同步文件列表
     * @return       要上传伯文件列表
     */
    public FileList alterFileList(FileList poolFileList){
        Iterator<FileBean> its=poolFileList.iterable();
        FileList sendFileList=poolFileList;
        while (its.hasNext()){
            sendFileList.addFileBean(its.next());
        }

        poolFileList.clear();
        return sendFileList;
    }

}

