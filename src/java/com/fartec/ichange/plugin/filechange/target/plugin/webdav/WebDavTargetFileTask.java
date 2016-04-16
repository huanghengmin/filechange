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
 * Time: ����2:16
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
        //ÿ���߳�ӵ���Լ��Ĳ�������
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //����Դ���ļ���Ŀ���
        webDavTargetSendUtils.send_FileBean(sendFileBean,targetOperation,sardine,targetFile);
        //�߳̽�����־
        logger.info("*****"+"�߳�����*****"+Thread.currentThread().getName()+"*****����*****");
    }

    public WebDavTargetFileTask(FileBean sendFileBean, TargetFile targetFile, TargetOperation targetOperation){
        this.targetFile=targetFile;
        this.targetOperation=targetOperation;
        this.sendFileBean=sendFileBean;
    }
}
