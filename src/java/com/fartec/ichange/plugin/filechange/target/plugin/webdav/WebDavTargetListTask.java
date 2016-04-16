package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-4-17
 * Time: 上午11:12
 * To change this template use File | Settings | File Templates.
 */
public class WebDavTargetListTask implements  Runnable{
    Logger logger=Logger.getLogger(WebDavTargetListTask.class);
    private WebDavTargetSendUtils webDavTargetSendUtils= WebDavTargetSendUtils.getSingleWebDavTargetUtils();
    private FileList fileList;
    private TargetFile targetFile;
    private TargetOperation targetOperation;

    @Override
    public void run() {
        //每个线程拥有自己的操作对象
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //发送目标端文件到源端
        webDavTargetSendUtils.send_BatchFileBeans(targetFile, fileList,sardine,targetOperation);
        //打印线程结束日志
        logger.info("线程名字：：：：：：：：："+Thread.currentThread().getName());
    }

    public WebDavTargetListTask(FileList fileList, TargetFile targetFile1, TargetOperation targetOperation){
        this.targetFile=targetFile1;
        this.targetOperation=targetOperation;
        this.fileList=fileList;
    }
}
