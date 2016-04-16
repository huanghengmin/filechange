package com.fartec.ichange.plugin.filechange.utils;

import com.inetec.common.exception.E;
import com.inetec.common.exception.Ex;
import com.inetec.common.i18n.Message;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Configuration {
    static Logger logger = Logger.getLogger(Configuration.class);

    private Document document;
    public String confPath;

    public Configuration(Document doc) {
        this.document = doc;
    }

    public Configuration(String path) throws Ex {
        this.confPath = path;
        SAXReader saxReader = new SAXReader();
        try {
            document = saxReader.read(path);
        } catch (DocumentException e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }

    public Configuration(InputStream is,String path) throws Ex {
        this.confPath = path;
        SAXReader saxReader = new SAXReader();
        try {
            document = saxReader.read(is);
        } catch (DocumentException e) {
            logger.info(e.getMessage());
        }
    }

    public void addFileList(List<FileBean> fileBeans, long time) {
        try{
            Element fileList = (Element) document.selectSingleNode("/root/filelists/filelist[@time=" + time + "]");
            if(fileList == null){
                Element filelists = (Element) document.selectSingleNode("/root/filelists");
                fileList = filelists.addElement("filelist");
                fileList.addAttribute("time", String.valueOf(time));
                fileList.addAttribute("flag","");
                fileList.addElement("files");
            }
            Element files = fileList.addElement("files");
            for(FileBean fileBean : fileBeans){
                Element child = files.addElement("file");
                child.addAttribute("name",fileBean.getFullname());
                child.addAttribute("time",String.valueOf(fileBean.getTime()));
            }
            save();
        } catch (Exception e){
            logger.info(e.getMessage());
        }
    }

    public void addFileList(List<FileBean> fileBeans, String flag) {
        try{
            Element fileList = (Element) document.selectSingleNode("/root/filelists/filelist[@flag=" + flag + "]");
            if(fileList == null){
                Element filelists = (Element) document.selectSingleNode("/root/filelists");
                fileList = filelists.addElement("filelist");
                fileList.addAttribute("flag",flag);
                fileList.addAttribute("time", "");
                fileList.addElement("files");
            }
            Element files = fileList.addElement("files");
            for(FileBean fileBean : fileBeans){
                Element child = files.addElement("file");
                child.addAttribute("name",fileBean.getFullname());
                child.addAttribute("time",String.valueOf(fileBean.getTime()));
            }
            save();
        } catch (Exception e){
            logger.info(e.getMessage());
        }
    }

    public boolean isExist(String name, long time) {
        List<Element> fileses = null;
        try{
            fileses = document.selectNodes("/root/filelists/filelist[@time=" + time + "]/files");
            if(fileses !=null){
                for ( Element files : fileses ){
                    List<Element> list = files.selectNodes("file");
                    if(list.size()>0){
                        for (Element e : list){
                            if(e.attributeValue("name").equals(name)){
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }catch (Exception e){
            logger.info("查找filelist_smb.xml中file " + name +"失败,下个周期继续!");
            return true;
        }
    }

    public void deleteFileList(long time) {
        Element fileList = (Element) document.selectSingleNode("/root/filelists/filelist[@time=" + time + "]");
//        Element root = document.getRootElement();
        if(fileList!=null){
            fileList.remove(fileList);
        }
    }

    public void save() throws Ex {

        String fileName = confPath.substring(confPath.lastIndexOf("/"), confPath.lastIndexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("'['yyyy-MM-dd_HH-mm-ss']'");
        String historyPath =System.getProperty("ichange.home") + "/temp/history";
        File historyF = new File(historyPath);
        File[] historyFs = historyF.listFiles();
        for (File f : historyFs){
            f.delete();
        }
        String historyFile = historyPath + fileName + sdf.format(new Date()) + ".xml";
        XMLWriter output = null;
        try {
            File file = new File(confPath);

            FileInputStream fin = new FileInputStream(file);
            byte[] bytes = new byte[fin.available()];
            while (fin.read(bytes) < 0) fin.close();
            File history = new File(historyFile);
            if (!history.getParentFile().exists())
                history.getParentFile().mkdir();
            FileOutputStream out = new FileOutputStream(history);
            out.write(bytes);
            out.close();
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            output = new XMLWriter(new FileOutputStream(file),format);
            if(document != null){
                output.write(document);
            }else{

            }
        } catch (FileNotFoundException e) {
            throw new Ex().set(E.E_FileNotFound, e, new Message("File {0} not found!", historyPath));
        } catch (IOException e) {
            throw new Ex().set(E.E_IOException, e, new Message("ccured exception when move Internal configuration To History"));
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                throw new Ex().set(E.E_IOException, e, new Message("Occured exception when close XMLWrite"));
            }
        }
    }
}