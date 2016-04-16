package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import org.junit.Test;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-5-5
 * Time: 下午6:38
 * To change this template use File | Settings | File Templates.
 */
public class test {

    @Test
    public void test2(){
        Sardine sardine = SardineFactory.begin("admin","hhm");
        String path= null;
        try {
            path = "http://192.168.1.160:80/uploads/"+ URLEncoder.encode("源端","GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
                sardine.createDirectory("http://192.168.1.160:80/uploads/a/");
              //  sardine.disableCompression();
                if(sardine.exists("http://192.168.1.160:80/uploads/a"))  {
                    System.out.print("存在");
                }
      /*        List<DavResource> resources = sardine.list(path);
                for (DavResource davResource:resources){
                    System.out.println(davResource.getName());
                    String rpath = path+"/"+davResource.getName()     ;
                    System.out.println(rpath);
                    if(davResource.getName().equals("源端")){
                        continue;
                    }else {
                        System.out.println(sardine.get(rpath));
                        sardine.move(rpath,rpath+".fdjfkdjslfjdlsjflsdjflksd");
                     }
                }
          //  }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        
        }*/
        }catch (Exception e){
        e.printStackTrace();
        }
    }
    
    @Test
    public void fileTest(){
        System.out.println(File.separator);
        String path="E:/temp/webdav/hh.txt";
        File file=new File(path);
        file.delete();
        //deleteTempFileFolder(file.getAbsolutePath());
        //System.out.print(file.listFiles().length);
       // file.delete();
    }
    
    @Test
    public void test(){
        Sardine sardine= SardineFactory.begin("","");
        try {
            InputStream inputStream=  sardine.get("http://192.168.1.105:8888/"+ URLEncoder.encode("源端","utf-8")+"/webdavclient4j-bin-0.92/webdavclient4j-bin-0.92/LICENSE.ispf") ;
            System.out.println(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUrl(){
        try {
            System.out.println(URLEncoder.encode(" ","UTF-8"));
            System.out.println(URLDecoder.decode(" ","UTF-8"));
            System.out.print("||");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getIn(){
        try {
            String path="http://192.168.1.105:8888/"+URLEncoder.encode("UltraEdit.rar.ispe","UTF-8");
            Sardine sardine=SardineFactory.begin("","");
            try {
              InputStream inputStream =  sardine.get(path);
                System.out.print(inputStream );
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void put252(){
        String path="http://192.168.1.252:8888/webdav/1.txt";
        Sardine sardine=SardineFactory.begin("","");
        try {
            FileInputStream fileInputStream=new FileInputStream(new File("E:\\Setup.exe"));
            try {
                sardine.put(path,fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void decoder(){
        try {
            System.out.println("||");
            System.out.println(URLDecoder.decode("%20","GBK"));
            System.out.println(URLDecoder.decode("%20","UTF-8"));
            System.out.println("||");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void createDr(){
        String path= null;
        //try {
            path = "http://192.168.1.105:8888/a/";
            Sardine sardine=SardineFactory.begin("","");
            try {
                sardine.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
       // } catch (UnsupportedEncodingException e) {
          //  e.printStackTrace();
        //}
    }
}
