package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.TargetFile;
import org.junit.Test;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-11
 * Time: 下午7:58
 * To change this template use File | Settings | File Templates.
 */
public class FileChangeUtilsTest {

    @Test
    public void cycleSyncTest() {
        //源端配置文件
         SourceFile sourceFile=new SourceFile();
        //目标端配置文件
         TargetFile targetFile=new TargetFile();
         //配置源端和目标端配置文件
         FileChangeUtils.setConfig(sourceFile, targetFile, "/源端", "/webdav/1",
          "192.168.1.105", "192.168.1.252", "8888", "8888", "", "", "", "");
         //生成到本地文件夹
         FileChangeUtils.createToLocal(500, "txt", "D:" + File.separator + "生成文件");
         //上传到服务器
         FileChangeUtils.uploadToService(sourceFile, "D:" + File.separator + "生成文件");
         //测试周期同步
         //FileChangeUtils.cycleSynchronous(targetFile, sourceFile);
    }
}
