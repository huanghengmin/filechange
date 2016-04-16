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
 * Time: ����11:12
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
        //ÿ���߳�ӵ���Լ��Ĳ�������
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //����Ŀ����ļ���Դ��
        webDavTargetSendUtils.send_BatchFileBeans(targetFile, fileList,sardine,targetOperation);
        //��ӡ�߳̽�����־
        logger.info("�߳����֣�����������������"+Thread.currentThread().getName());
    }

    public WebDavTargetListTask(FileList fileList, TargetFile targetFile1, TargetOperation targetOperation){
        this.targetFile=targetFile1;
        this.targetOperation=targetOperation;
        this.fileList=fileList;
    }
}
