package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-25
 * Time: 下午2:16
 * To change this template use File | Settings | File Templates.
 */
public class WebDavTargetFileTask implements  Runnable{
    Logger logger=Logger.getLogger(WebDavTargetFileTask.class);
    private WebDavTargetSendUtils webDavTargetSendUtils= WebDavTargetSendUtils.getSingleWebDavTargetUtils();
    private FileBean sendFileBean;
    private TargetFile targetFile;
    private TargetOperation targetOperation;

    @Override
    public void run() {
        //每个线程拥有自己的操作对象
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //发送源端文件到目标端
        webDavTargetSendUtils.send_FileBean(sendFileBean,targetOperation,sardine,targetFile);
        //线程结束日志
        logger.info("*****"+"线程名字*****"+Thread.currentThread().getName()+"*****结束*****");
    }

    public WebDavTargetFileTask(FileBean sendFileBean, TargetFile targetFile, TargetOperation targetOperation){
        this.targetFile=targetFile;
        this.targetOperation=targetOperation;
        this.sendFileBean=sendFileBean;
    }
}
