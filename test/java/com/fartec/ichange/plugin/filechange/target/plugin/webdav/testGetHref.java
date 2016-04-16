package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-5-6
 * Time: 下午12:56
 * To change this template use File | Settings | File Templates.
 */
public class testGetHref {
    @Test
    public void  test192_168_1_252(){
        String http252= null;
        try {
            http252= "http://192.168.1.252:8888/webdav/"+ URLEncoder.encode( "黄恒民122.txt","GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }             String h105=null;
        try {
            h105="http://192.168.1.105:8888/"+URLEncoder.encode( "源端/黄恒民198.txt","UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Sardine sardine= SardineFactory.begin("","");
        List<DavResource> davResources=null;
        try {
            davResources=sardine.list(http252);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            System.out.println(URLDecoder.decode(davResources.iterator().next().getHref().toString(),"gbk"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        List<DavResource> davResources2=null;
        try {
            davResources2=sardine.list(h105);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            System.out.println(URLDecoder.decode(davResources2.iterator().next().getHref().toString(),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
