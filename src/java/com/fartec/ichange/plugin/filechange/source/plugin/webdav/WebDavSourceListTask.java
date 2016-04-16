package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-4-17
 * Time: 上午11:12
 * To change this template use File | Settings | File Templates.
 */
public class WebDavSourceListTask implements  Runnable{
    //需要发送的列表对象
    private FileList sendFileList;
    //源端配置文件
    private SourceFile sourceFile;
    //传送文件到目标端的操作对象
    private SourceOperation sourceOperation;
    //打印日志对象
    private Logger logger=Logger.getLogger(WebDavSourceListTask.class);
    //发送文件工具类
    private WebDavSourceSendUtils webDavSourceSendUtils=new WebDavSourceSendUtils();

    public WebDavSourceListTask(FileList sendFileList, SourceFile sourceFile, SourceOperation sourceOperation){
        //源端配置文件
        this.sourceFile=sourceFile;
        //传送到目标端的操作对象
        this.sourceOperation=sourceOperation;
        //发送的文件列表对象
        this.sendFileList=sendFileList;
    }

    @Override
    public void run() {
            //源端操作对象
            Sardine sardine= SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
            //发送文件
            webDavSourceSendUtils.processList(sourceFile, sendFileList,sardine,sourceOperation);
            //打印线程结束标识
            logger.info("*****线程名字*****"+Thread.currentThread().getName()+"*****结束*****");
    }
}
