package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-28
 * Time: 上午9:33
 * To change this template use File | Settings | File Templates.
 */
public class WebDavFilterUtilsTest {
    private WebDavFilterUtils webDavFilterUtils=new WebDavFilterUtils();
    @Test
    public void testGetSingleWebDavFilterUtils() throws Exception {

    }

    @Test
    public void testNotFinishedFileBeanFilter() throws Exception {

    }

    @Test
    public void testFileBeanFilter() throws Exception {
        SourceFile sourceFile=new SourceFile();
        //能过滤的类型
        sourceFile.setFiltertypes("*.*");
        //不能过滤的类型
        sourceFile.setNotfiltertypes("*.doc,*.xml,main.txt,main");
        Sardine sardine= SardineFactory.begin("root", "root");
        try {
            List<DavResource> resourceList= sardine.list("http://192.168.1.252:8888/webdav") ;
            Iterator<DavResource> resourceIterator=resourceList.iterator();
            resourceIterator.next();
            while (resourceIterator.hasNext()){
                DavResource resource=resourceIterator.next();
                if(!resource.isDirectory()) {
                    FileBean fileBean= webDavFilterUtils.fileBeanFilter(resource.getName(),sourceFile,resource);
                    if(fileBean!=null){
                          System.out.println("fileBean的路径和文件名："+fileBean.getFullname());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
