package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-23
 * Time: ����2:26
 * To change this template use File | Settings | File Templates.
 */
public class CreateFolderTest {
    private static  final String rootPath="http://192.168.1.252:8888/webdav/";
    private static final Sardine sardine=SardineFactory.begin("","");

    @Test
    public void createSourceFolder(){
        try {
            sardine.createDirectory("http://192.168.1.252:8888/webdav/"+URLEncoder.encode("Դ��","GBK"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createTargetFolder(){
        try {
            sardine.createDirectory("http://192.168.1.252:8888/webdav/"+URLEncoder.encode("Ŀ���","GBK"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
