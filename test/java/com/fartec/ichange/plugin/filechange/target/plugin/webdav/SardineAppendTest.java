package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-24
 * Time: ÉÏÎç8:47
 * To change this template use File | Settings | File Templates.
 */
public class SardineAppendTest {

     private File file=new File("D:"+File.separator+"UltraEdit.rar");                                            //13M
    //private File file=new File("D:"+File.separator+"p4vinst.exe");                                              //50M
    //private File file=new File("D:" + File.separator + "jdk-7-fcs-bin-b147-windows-i586-27_jun_.exe");          //79M
    //private File file=new File("D:" + File.separator + "ideaIU-111.41.exe");                                    //160M
    //private File file=new File("D:"+File.separator+"200M_1.rar");                                               //200M
    //private File file=new File("D:"+File.separator+"500M_1.rar");                                               //500M
    //private File file=new File("D:" + File.separator + "myeclipse-9.0-offline-installer-windows.exe");//926M
    //private File file=new File("D:" + File.separator + "1.9G.rar");                                         //1.9G

    @Test
    public void testSardineAppend(){
        Sardine sardine= SardineFactory.begin("","");
        try {
            // byte [] bytes=new byte[2*1024*1024];
            FileInputStream fileInputStream=new FileInputStream(file);
            try {
                //sardine.enableCompression();
                //InputStreamEntity entity = new InputStreamEntity(fileInputStream, -1);
                sardine.put("http://192.168.1.252:8888/webdav/"+file.getName(),fileInputStream);
                    /* sardine.put("http://192.168.1.252:8888/webdav/myeclipse-9.0-offline-installer-windows.exe",
                            new ByteArrayInputStream(bytes,0,len),
                            DavResource.DEFAULT_CONTENT_TYPE,true);

                    sardine.put("http://192.168.1.252:8888/webdav/myeclipse-9.0-offline-installer-windows.exe",
                            IOUtils.copyArray(bytes,len),
                            DavResource.DEFAULT_CONTENT_TYPE);

                    sardine.put("http://192.168.1.252:8888/webdav/myeclipse-9.0-offline-installer-windows.exe",
                            new ByteArrayInputStream(bytes,0,len),
                            DavResource.DEFAULT_CONTENT_TYPE);*/
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
