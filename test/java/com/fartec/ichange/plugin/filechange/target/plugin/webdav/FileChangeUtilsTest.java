package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.TargetFile;
import org.junit.Test;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-11
 * Time: ����7:58
 * To change this template use File | Settings | File Templates.
 */
public class FileChangeUtilsTest {

    @Test
    public void cycleSyncTest() {
        //Դ�������ļ�
         SourceFile sourceFile=new SourceFile();
        //Ŀ��������ļ�
         TargetFile targetFile=new TargetFile();
         //����Դ�˺�Ŀ��������ļ�
         FileChangeUtils.setConfig(sourceFile, targetFile, "/Դ��", "/webdav/1",
          "192.168.1.105", "192.168.1.252", "8888", "8888", "", "", "", "");
         //���ɵ������ļ���
         FileChangeUtils.createToLocal(500, "txt", "D:" + File.separator + "�����ļ�");
         //�ϴ���������
         FileChangeUtils.uploadToService(sourceFile, "D:" + File.separator + "�����ļ�");
         //��������ͬ��
         //FileChangeUtils.cycleSynchronous(targetFile, sourceFile);
    }
}
