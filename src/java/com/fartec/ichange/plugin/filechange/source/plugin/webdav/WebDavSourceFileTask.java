package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-25
 * Time: 下午2:16
 * To change this template use File | Settings | File Templates.
 */
public class WebDavSourceFileTask implements Runnable{
    //要发送的文件对象
    private FileBean sendFileBean;
    //源端配置文件
    private SourceFile sourceFile;
    //发送到目标端的操作对象
    private SourceOperation sourceOperation;
    //打印日志对象
    private Logger logger = Logger.getLogger(WebDavSourceFileTask.class);
    //发送文件工具类
    private WebDavSourceSendUtils webDavSourceSendUtils = new WebDavSourceSendUtils();

    public WebDavSourceFileTask(FileBean sendFileBean, SourceFile sourceFile, SourceOperation sourceOperation){
        //源端配置 文件
        this.sourceFile = sourceFile;
        //传送到目标端操作对象
        this.sourceOperation = sourceOperation;
        //要发送的单个文件对象
        this.sendFileBean = sendFileBean;
    }

    @Override
    public void run() {
          //源端操作对象
          Sardine sardine= SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
          //发送文件
          webDavSourceSendUtils.processFile(sendFileBean,sourceOperation,sardine,sourceFile);
          //打印线程运行完成标识
          logger.info("**"+"线程名字**" + Thread.currentThread().getName() + "**结束!!!**");
    }
}
