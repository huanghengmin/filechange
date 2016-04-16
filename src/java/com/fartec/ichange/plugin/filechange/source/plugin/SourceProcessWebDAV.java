package com.fartec.ichange.plugin.filechange.source.plugin;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.source.plugin.webdav.WebDavSourceSendUtils;
import com.fartec.ichange.plugin.filechange.source.plugin.webdav.WebDavUtils;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SourceProcessWebDAV implements ISourceProcess {
    private static  int i = 1;
    private SourceFile sourceFile;
    private boolean  isRun = false;
    private ExecutorService pool = null;
    private ITargetProcess iTargetProcess;
    private WebDavUtils webDavUtils=new WebDavUtils();
    private SourceOperation sourceOperation;
    Logger logger=Logger.getLogger(SourceProcessWebDAV.class);
    private WebDavSourceSendUtils webDavSendUtils = new WebDavSourceSendUtils();

    @Override
    public boolean process(InputStream in, FileBean fileBean) {
      return false;
    }

    @Override
    public boolean process(byte[] data, FileBean bean) {
        return false;
    }

    @Override
    public void init(SourceOperation source, SourceFile config) {
        this.sourceFile =config;
        this.sourceOperation=source;
    }

    @Override
    public void init(ITargetProcess target, SourceFile config) {
        this.sourceFile =config;
        this.iTargetProcess=target;
    }

    @Override
    public void stop() {
        if(this.isRun()==true){
            this.isRun=false;
        }
    }

    @Override
    public boolean isRun() {
        return  this.isRun;
    }

    @Override
    public FileList procesFileList(FileList fileList) {
        return null;
    }

    /**
     * 判断是否存在对应文件
     * @param resource   服务器资源对象
     * @return   返回服务器是否存在标识
     */
    public FileList existCorrespondingFile(List<DavResource> resourceList){
       FileList fileList = new FileList();
       if(resourceList.size()>0){
           Iterator<DavResource>  davResourceIterator = resourceList.iterator();
           while (davResourceIterator.hasNext()){
               DavResource resource = davResourceIterator.next();
               Sardine sardine = SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
               boolean  flag = webDavUtils.judeFileModify(resource,sourceFile,sardine);
               if(flag){
                   FileBean fileBean = webDavUtils.buildSyncIngFile(resource,sourceFile,sardine);
                   fileList.addFileBean(fileBean);
               }else {
                  String path = webDavUtils.encodeCorrespondingLinkFilePath(resource,sourceFile);
                   try {
                       if(sardine.exists(path)){
                           if(resource.getContentLength()==0){
                              sardine.delete(path);
                               logger.info("删除成功"+path);
                           }
                       }
                   } catch (IOException e) {
                       logger.info("不存在link文件"+ webDavUtils.urlDecoder(path,sourceFile));
                   }
               }
           }
       }
        return fileList;
    }

    @Override
    public void run() {
        this.isRun=true;
        while (this.isRun){
            logger.info("*********************************** Webdav 第"+i+"次同步开始***********************************");

            pool =Executors.newFixedThreadPool(sourceFile.getThreads());

            List<DavResource> syncIngFileList = webDavSendUtils.ergodicSourceSyncIng(sourceFile);

            FileList fileList = existCorrespondingFile(syncIngFileList);
            if(fileList.size()>0){
            fileList.setSyncFileListDD(true);
                FileList  sendFileList = sourceOperation.procesFileList(fileList);
                if(sendFileList.size()>0){
                webDavSendUtils.syncIngFileList(sendFileList,sourceFile,this.sourceOperation);
                }
            }
            webDavSendUtils.ergodicSource(this.sourceOperation,sourceFile,sourceFile.getFilelistsize(),pool);

            pool.shutdown();

            while (!pool.isTerminated()){
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                   logger.info(e.getMessage());
                }
            }
            try {
                Thread.sleep(sourceFile.getInterval());
            } catch (InterruptedException e) {
               logger.info("主线程sleep出错！！！");
            }
            logger.info("*********************************** Webdav 第"+(i++)+"次同步结束***********************************");
        }
    }
}
