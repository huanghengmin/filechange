package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-26
 * Time: ÏÂÎç5:57
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jUtilsTest {
    Logger logger=Logger.getLogger(Dom4jUtilsTest.class);

    @Test
    public void testGetSyncXMLFileBean() throws Exception{
        FileBean fileBean2=new FileBean();
        fileBean2.setFullname("bbb");
        fileBean2.setName("b");
        fileBean2.setFilesize(24);
       // logger.info(Dom4jUtils.getSyncXMLFileBean(fileBean2));
    }

    @Test
    public void testRemoveSyncXMLFile() throws Exception {
           // logger.info(Dom4jUtils.removeSyncXMLFile());
    }

    @Test
    public void testExistsSyncXMLFile() throws Exception {
      //  logger.info(Dom4jUtils.existsSyncXMLFile());
    }

    @Test
    public void testSaveToSyncXMLFile() throws Exception {
        FileList fileList=new FileList();
        FileBean fileBean=new FileBean();
        fileBean.setFullname("aaa");
        fileBean.setName("a");
        fileBean.setFilesize(23);
        FileBean fileBean2=new FileBean();
        fileBean2.setFullname("bbb");
        fileBean2.setName("b");
        fileBean2.setFilesize(24);
        FileBean fileBean3=new FileBean();
        fileBean3.setFullname("ccc");
        fileBean3.setName("c");
        fileBean3.setFilesize(25);
        FileBean fileBean4=new FileBean();
        fileBean4.setFullname("ddd");
        fileBean4.setName("d");
        fileBean4.setFilesize(26);
        FileBean fileBean5=new FileBean();
        fileBean5.setFullname("eee");
        fileBean5.setName("ee");
        fileBean5.setFilesize(27);
        fileList.addFileBean(fileBean);
        fileList.addFileBean(fileBean2);
        fileList.addFileBean(fileBean3);
        fileList.addFileBean(fileBean4);
        fileList.addFileBean(fileBean5);
        //Dom4jUtils.saveToSyncXMLFile(fileList);
    }
}
