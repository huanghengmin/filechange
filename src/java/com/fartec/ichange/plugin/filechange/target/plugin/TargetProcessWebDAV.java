package com.fartec.ichange.plugin.filechange.target.plugin;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.target.plugin.webdav.WebDavUtils;
import com.fartec.ichange.plugin.filechange.target.plugin.webdav.WebdavTargetReceiveUtils;
import com.fartec.ichange.plugin.filechange.target.plugin.webdav.WebdavTargetTempFileUtils;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TargetProcessWebDAV implements ITargetProcess {
    private WebDavUtils webDavUtils = new WebDavUtils();
    private WebdavTargetReceiveUtils webdavTargetReceiveUtils=new WebdavTargetReceiveUtils();
    private WebdavTargetTempFileUtils webdavTargetTempFileUtils=new WebdavTargetTempFileUtils();
    private Logger logger=Logger.getLogger(TargetProcessWebDAV.class);
    private ExecutorService pool=null;
    private boolean  isRun=false;
    private TargetFile targetFile;
    private static  int i=1;
    private TargetOperation targetOperation;

    @Override
    public boolean process(byte[] data, FileBean bean) {
        return false;
    }

    @Override
    public void init(TargetOperation target, TargetFile config) {
        this.targetFile =config;
        this.targetOperation=target;
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
    public void run() {
        this.isRun=true;
        while (isRun){
        pool= Executors.newFixedThreadPool(targetFile.getThreads());

        logger.info("*****目标端第"+i+"次开始同步!!!*****");
        pool.shutdown();
        while (!pool.isTerminated()){
            try {
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                logger.info(e.getMessage());
            }
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            logger.info(e.getMessage());
        }
        logger.info("*****目标端第"+(i++)+"次同步结束!!!*****");
        }
    }

    @Override
    public boolean process(InputStream in, FileBean fileBean) {
        boolean  flag=false;
        if(fileBean.getFilesize() <= 2*1020*1024) {
            //构建目标端操作对象
            Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
            //目标端文件全名
            String path= webDavUtils.getTargetHostAndName(targetFile);
            if(fileBean.getFullname().startsWith("/")){
                path += webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()) + fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
            }else {
                path += webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()) + "/"+fileBean.getFullname(),targetFile).replaceAll("\\+","%20");
            }
                try{
                    webdavTargetReceiveUtils.createTargetDirectory(sardine,fileBean, targetFile);//分级创建目录
                    try {
                        sardine.put(path,in);
                    } catch (Exception e) {
                        logger.info("***获取本地临时文件流上传到服务器出错!!!!***");
                    }
                    //判断是否保存成功
                    if(webdavTargetReceiveUtils.existsTargetFile(targetFile,fileBean)){
                        logger.info(Thread.currentThread().getName()+"*****文件*****"+webDavUtils.getTargetHostAndName(targetFile) +webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname()+"::同步完成！");
                        flag = true;
                    }
                }catch (Exception e){
                    logger.info(e.getMessage()+"***目标端服务器可能存在连接错误!!!***");
                }
            } else {
              flag = webdavTargetReceiveUtils.putFile(fileBean,in,targetFile);
        }
        return  flag;
    }

    @Override
    public FileList procesFileList(FileList sourceFileList){
        FileList resultFileList=new FileList();
        Iterator<FileBean> sourceIterator=sourceFileList.iterable();
        if(sourceFileList.getSyncFileListDD()){
            while (sourceIterator.hasNext()){
                FileBean fileBean = sourceIterator.next();
                boolean  bool= webdavTargetTempFileUtils.existsSyncIngTempFile(fileBean);
                if(!bool){
                    fileBean.setFilepostlocation(-2);
                } else {
                long  contentLength= webdavTargetTempFileUtils.getTempFileContentLength(fileBean);
                    fileBean.setFilepostlocation(contentLength);
                }
                resultFileList.addFileBean(fileBean);
            }
        }else {
                while (sourceIterator.hasNext()){
                    FileBean fileBean = sourceIterator.next();
                    if(this.targetFile.isOnlyadd()){
                        webdavTargetReceiveUtils.onlyTargetAdd(fileBean,resultFileList,targetFile);  //调用目标端只增加的方法
                    }else {
                        boolean flag = webdavTargetReceiveUtils.existsTargetFile(targetFile,fileBean);
                        if(!flag){
                            resultFileList.addFileBean(fileBean);
                        }
                    }
                }

        }
        return resultFileList;
    }
}
