package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-26
 * Time: 下午4:54
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jUtils {
    private static Logger logger=Logger.getLogger(Dom4jUtils.class);
    private static final  String  SyncXmlFilePath=System.getProperty("ichange.home")+"/temp/webdav_Sync.xml";
    private static final  String fileList="fileList";
    private static final String fileBean="fileBean";
    private static final  String fileName="fileName";
    private static final String fileBeanFullName="fileBeanFullName";
    private static final String fileBeanSize="fileBeanSize";
    private static final String root="root";
    private static Dom4jUtils dom4jUtils=null;

    private Dom4jUtils(){}

    public static Dom4jUtils getSingleDom4jUtils(){
        if(dom4jUtils==null){
            dom4jUtils=new Dom4jUtils();
        }
        return dom4jUtils;
    }

    /**
     * 构建文件路径
     */
    public void createDir(){
        String path=null;
        if(SyncXmlFilePath.contains("/")){
            path=SyncXmlFilePath.substring(0,SyncXmlFilePath.lastIndexOf("/"));
        }
        File file=new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    /**
     * 得到xml中是否存在此文件
     * @param fileBean    文件对象
     * @return             是否存在此文件
     */
    public  boolean getSyncXMLFileBean(FileBean fileBean){
        boolean  flag=false;
        SAXReader saxReader = new SAXReader();
        Document document=null;
        try {
          document =saxReader.read(new File(Dom4jUtils.SyncXmlFilePath));
        } catch (DocumentException e) {
            logger.error(e.getMessage());
        }
     /* String xpath ="/"+Dom4jUtils.root+"/"+Dom4jUtils.fileList+"/"+Dom4jUtils.fileBean+"[@"+Dom4jUtils.fileName+"='"+fileBean.getName()+"'"+","
        +Dom4jUtils.fileBeanFullName+"='"+fileBean.getFullname()+"'"+","+Dom4jUtils.fileBeanSize+"='"+fileBean.getFilesize()+"'"+"]";
        List<Element> have = document.selectNodes(xpath);*/

        Element root = document.getRootElement();
        Element fileList =  root.element(Dom4jUtils.fileList);
        List<Element> fileBeans = fileList.elements(Dom4jUtils.fileBean);
        Iterator<Element> its=fileBeans.iterator();
        while (its.hasNext()){
             Element element=its.next();
                 String fullName=element.attributeValue(Dom4jUtils.fileBeanFullName);
                 String name=element.attributeValue(Dom4jUtils.fileName);
                 String size=element.attributeValue(Dom4jUtils.fileBeanSize);
             if(fileBean.getFullname().equals(fullName)&&name.equals(fileBean.getName())&&(Long.parseLong(size)==fileBean.getFilesize())){
                 flag=true;
             }
        }

        return flag;
        }

    /**
     * 删除原有的XML文件
     * @return  删除是否成功
     */
    public   boolean removeSyncXMLFile(){
        File file=new File(Dom4jUtils.SyncXmlFilePath);
        if(file.exists()){
            return file.delete();
        }
        return false;
    }

    /**
     * 判断XMl文件是否存在
     * @return
     */
    public  boolean existsSyncXMLFile(){
        boolean flag=false;
        SAXReader saxReader = new SAXReader();
        Document document=null;
        try {
            document =saxReader.read(new File(Dom4jUtils.SyncXmlFilePath));
        } catch (DocumentException e) {
            logger.error(e.getMessage());
            return false;
        }
        if(document==null){
            flag=false;
        }else {
            flag=true;
        }
       return flag;
    }

    /**
     * 保存到文件
     */
    public  void saveToSyncXMLFile(FileList fileList){
        String path=Dom4jUtils.SyncXmlFilePath;
        SAXReader saxReader = new SAXReader();
        Document document=null;
         try {
            document =saxReader.read(new File(Dom4jUtils.SyncXmlFilePath));
         } catch (DocumentException e) {
            logger.error(e.getMessage());
         }
        if(document!=null){
            Element root=document.getRootElement();
            Element fileBeanList=root.element(Dom4jUtils.fileList);
            Iterator<FileBean> its=fileList.iterable();
            while (its.hasNext()){
                FileBean fileBean=its.next();
                Element file = fileBeanList.addElement(Dom4jUtils.fileBean);
                file.addAttribute(Dom4jUtils.fileName,fileBean.getName());
                file.addAttribute(Dom4jUtils.fileBeanFullName,fileBean.getFullname());
                file.addAttribute(Dom4jUtils.fileBeanSize,String.valueOf(fileBean.getFilesize()));
            }
              OutputFormat outputFormat=new OutputFormat("",true);
            try {
                XMLWriter xmlWriter=new XMLWriter(new FileOutputStream(new File(path)),outputFormat);
                try {
                    xmlWriter.write(document);
                } catch (IOException e) {
                    logger.info(e.getMessage());
                }
            } catch (UnsupportedEncodingException e) {
                logger.info(e.getMessage());
            } catch (FileNotFoundException e) {
                logger.info(e.getMessage());
            }

        }else{
            Document document1=DocumentHelper.createDocument();
            Element root=document1.addElement(Dom4jUtils.root);
            Element fileBeanList=root.addElement(Dom4jUtils.fileList);
            Iterator<FileBean> its=fileList.iterable();
            while (its.hasNext()){
                FileBean fileBean=its.next();
                Element file=fileBeanList.addElement(Dom4jUtils.fileBean);
                file.addAttribute(Dom4jUtils.fileName,fileBean.getName());
                file.addAttribute(Dom4jUtils.fileBeanFullName,fileBean.getFullname());
                file.addAttribute(Dom4jUtils.fileBeanSize,String.valueOf(fileBean.getFilesize()));
            }
            OutputFormat outputFormat=new OutputFormat("",true);
            try {
                createDir();  //构建文件路径
                XMLWriter xmlWriter=new XMLWriter(new FileOutputStream(new File(path)),outputFormat);
                try {
                    xmlWriter.write(document1);
                } catch (IOException e) {
                    logger.info(e.getMessage());
                }
            } catch (UnsupportedEncodingException e) {
                logger.info(e.getMessage());
            } catch (FileNotFoundException e) {
                logger.info(e.getMessage());
            }
        }
    }


}
