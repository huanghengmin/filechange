package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * 用jdom操作ＸＭＬ　工具类
 */
public class JDomUtils {
    private static Logger logger=Logger.getLogger(JDomUtils.class);
    private static final  String  SyncXmlFilePath=System.getProperty("ichange.home")+"/temp/webdav_Sync.xml";
    private static final  String fileList="fileList";
    private static final String fileBean="fileBean";
    private static final  String fileName="fileName";
    private static final String fileBeanFullName="fileBeanFullName";
    private static final String fileBeanSize="fileBeanSize";
    private static final String root="root";
    private static JDomUtils jDomUtils=null;

    private JDomUtils(){}

    public static JDomUtils getSingleJDomUtils(){
        if(jDomUtils==null){
            jDomUtils=new JDomUtils();
        }
        return jDomUtils;
    }

    /**
     * 得到xml中是否存在此文件
     * @param fileBean    文件对象
     * @return             是否存在此文件
     */
    public  boolean getSyncXMLFileBean(FileBean fileBean){
        boolean  flag=false;
        String path= JDomUtils.SyncXmlFilePath;

        Document document = buildFromFile(path);

        Element root=document.getRootElement();

        Element fileList= root.getChild(JDomUtils.fileList);
        List<Element> fileBeans=null;
        try{
            fileBeans = fileList.getChildren(JDomUtils.fileBean);
        }catch (Exception e){
            logger.info(e.getMessage());
        }


        if(!fileBeans.isEmpty()){
            Iterator<Element> its=fileBeans.iterator();
            while (its.hasNext()){
                Element file=its.next();
                if((file.getAttributeValue(JDomUtils.fileBeanFullName).equals(fileBean.getFullname()))&&
                        (Long.parseLong(file.getAttributeValue(JDomUtils.fileBeanSize))==(fileBean.getFilesize()))){
                    flag=true;
                }
            }

        }
        return flag;
    }

    /**
     * 删除原有的XML文件
     * @return  删除是否成功
     */
    public boolean removeSyncXMLFile(){
        File file=new File(JDomUtils.SyncXmlFilePath);
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
        String path= JDomUtils.SyncXmlFilePath;
        Document document = buildFromFile(path);
        if(document!=null){
            return true;
        }else{
            return false;
        }
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
     * 构建XML文件
     */
    public void createSyncXMLFile(){
        Element root=new Element(JDomUtils.root);
        Element element=new Element(JDomUtils.fileList);
        root.addContent(element);
        Document doc=new Document(root);
        outputToFile(doc,JDomUtils.SyncXmlFilePath);
    }

    /**
     * 将传过来的文件列表保存到同步的XML文件中
     * @param fileList     源端传过来的文件列表
     */
    public  void saveToSyncXMLFile(FileList fileList){
        String path= JDomUtils.SyncXmlFilePath;
        Document document = buildFromFile(path);
        if(document!=null){
            Element root=document.getRootElement();
            Element element=root.getChild(JDomUtils.fileList);
            Iterator<FileBean> its=fileList.iterable();
            while (its.hasNext()){
                FileBean fileBean=its.next();
                Element file=new Element(JDomUtils.fileBean);
                Attribute fileFullName=new Attribute(JDomUtils.fileBeanFullName,fileBean.getFullname());
                Attribute fileName=new Attribute(JDomUtils.fileName,fileBean.getName());
                Attribute fileSize=new Attribute(JDomUtils.fileBeanSize,String.valueOf(fileBean.getFilesize()));
                file.setAttribute(fileFullName);
                file.setAttribute(fileName);
                file.setAttribute(fileSize);
                element.addContent(file);
             }
             outputToFile(document,path);
        }else{
            Element root=new Element(JDomUtils.root);
            Element element=new Element(JDomUtils.fileList);
            Iterator<FileBean> its=fileList.iterable();
            while (its.hasNext()){
                FileBean fileBean=its.next();
                Element file=new Element(JDomUtils.fileBean);
                Attribute fileFullName=new Attribute(JDomUtils.fileBeanFullName,fileBean.getFullname());
                Attribute fileName=new Attribute(JDomUtils.fileName,fileBean.getName());
                Attribute fileSize=new Attribute(JDomUtils.fileBeanSize,String.valueOf(fileBean.getFilesize()));
                file.setAttribute(fileFullName);
                file.setAttribute(fileName);
                file.setAttribute(fileSize);
                element.addContent(file);
            }
            root.addContent(element);
            Document doc=new Document(root);
            createDir();
            outputToFile(doc,path);
        }
    }

	/**
	 * 根据指定路径的XML文件建立JDom对象
	 * @param filePath
	 *            XML文件的路径
	 * @return 返回建立的JDom对象，建立不成功返回null 。
	 */
	public  Document buildFromFile(String filePath) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document anotherDocument = builder.build(new File(filePath));
			return anotherDocument;
		} catch (JDOMException e) {
			logger.error(e.getMessage());
		} catch (NullPointerException e) {
            logger.error(e.getMessage());
		} catch (IOException e) {
            logger.error(e.getMessage());
		}
		return null;
	}

    /**
     * 通过流对象构建文档
     * @param inputStream   流对象
     * @return          ＸＭＬ文档对象
     */
	public  Document buildFromFile(InputStream inputStream) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document anotherDocument = builder.build(inputStream);
			return anotherDocument;
		} catch (JDOMException e) {
            logger.info(e.getMessage());
		} catch (NullPointerException e) {
            logger.info(e.getMessage());
		} catch (IOException e) {
            logger.info(e.getMessage());
		}
		return null;
	}

	/**
	 * 根据XML 字符串 建立JDom对象
	 * @param xmlString
	 *            XML格式的字符串
	 * @return 返回建立的JDom对象，建立不成功返回null 。
	 */
	public  Document buildFromXMLString(String xmlString) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document anotherDocument = builder
					.build(new StringReader(xmlString));
			return anotherDocument;
		} catch (JDOMException e) {
            logger.info(e.getMessage());
		} catch (NullPointerException e) {
            logger.info(e.getMessage());
		} catch (IOException e) {
            logger.info(e.getMessage());
		}
		return null;
	}

	/**
	 * 根据Dom对象建立JDom对象
	 * @param Dom
	 *            org.w3c.dom.Document对象
	 * @return 返回建立的JDom对象，建立不成功返回null 。
	 */
	public  Document buildFromDom(org.w3c.dom.Document Dom)throws JDOMException, IOException {
		DOMBuilder builder = new DOMBuilder();
		Document jdomDoc = builder.build(Dom);
 		return jdomDoc;
	}

    /**
	 * 这个方法使用XMLOutputer将一个JDom对象输出到标准输出设备，使用 GBK 编码 将要被输出的JDom对象
	 */
	public  void outputToStdout(Document myDocument) {
		outputToStdout(myDocument, "GBK");
	}

    /**
	 * 这个方法使用XMLOutputer将一个JDom对象输出到标准输出设备
	 * @param myDocument
	 *            将要被输出的JDom对象
	 * @param encoding
	 *            输出使用的编码
	 */
	public  void outputToStdout(Document myDocument, String encoding) {
		try {

			XMLOutputter outputter = new XMLOutputter();
			Format fm = Format.getRawFormat();
			fm.setEncoding(encoding);
			outputter.setFormat(fm);
			outputter.output(myDocument, System.out);
		} catch (IOException e) {
            logger.info(e.getMessage());
		}
	}

    /**
	 * 这个方法将JDom对象转换字符串.
	 * @param document
	 *            将要被转换的JDom对象
	 */
	public  String outputToString(Document document) {
		return outputToString(document, "GBK");
	}

	/**
	 * 这个方法将JDom对象转换字符串.
	 * @param document
	 *            将要被转换的JDom对象
	 * @param encoding
	 *            输出字符串使用的编码
	 */
	public  String outputToString(Document document, String encoding) {
		ByteArrayOutputStream byteRep = new ByteArrayOutputStream();
		XMLOutputter outputter = new XMLOutputter();
		Format fm = Format.getRawFormat();
		fm.setEncoding(encoding);
		outputter.setFormat(fm);
		try {
			outputter.output(document, byteRep);
		} catch (Exception e) {
            logger.info(e.getMessage());
		}
		return byteRep.toString();
	}

	/**
	 * 这个方法将List对象转换字符串.
	 * @param //document
	 *            将要被转换的JDom对象
	 */
	public  String outputToString(List<Object> list) {
		return outputToString(list, "GBK");
	}

	/**
	 * 这个方法将List对象转换字符串.
	 * @param //document
	 *            将要被转换的JDom对象
	 * @param encoding
	 *            输出字符串使用的编码
	 */
	public  String outputToString(List<Object> list, String encoding) {
		ByteArrayOutputStream byteRep = new ByteArrayOutputStream();
		XMLOutputter outputter = new XMLOutputter();
		Format fm = Format.getRawFormat();
		fm.setEncoding(encoding);
		outputter.setFormat(fm);
		try {
			outputter.output(list, byteRep);
		} catch (Exception e) {
            logger.info(e.getMessage());
		}
		return byteRep.toString();
	}

	/**
	 * 根据JDom对象建立Dom对象
	 * @param //Dom
	 *            org.w3c.dom.Document对象
	 * @return 返回建立的Dom对象，建立不成功返回null 。
	 */
	public  org.w3c.dom.Document outputToDom(Document jdomDoc)throws JDOMException {
		DOMOutputter outputter = new DOMOutputter();
		return outputter.output(jdomDoc);
	}

	/**
	 * 这个方法使用XMLOutputter将JDom对象输出到文件
	 * @param myDocument
	 *            将要输出的JDom对象
	 * @param filePath
	 *            将要输出到的磁盘路径
	 */
	public  void outputToFile(Document myDocument, String filePath) {
		outputToFile(myDocument, filePath, "GBK");
	}

	/**
	 * 这个方法使用XMLOutputter将JDom对象输出到文件
	 * @param myDocument
	 *            将要输出的JDom对象
	 * @param filePath
	 *            将要输出到的磁盘路径
	 * @param encoding
	 *            编码方式
	 */
	public  void outputToFile(Document myDocument, String filePath,String encoding) {
		try {
			XMLOutputter outputter = new XMLOutputter();
			Format fm = Format.getRawFormat();
            fm.setExpandEmptyElements(true);
			fm.setEncoding(encoding);
			outputter.setFormat(fm);
			FileWriter writer = new FileWriter(filePath);
			outputter.output(myDocument, writer);
			writer.close();

		} catch (IOException e) {
            logger.info(e.getMessage());
		}
	}

	/**
	 * 这个方法将JDom对象通过样式单转换.
	 * @param myDocument
	 *            将要被转换的JDom对象
	 * @param xslFilePath
	 *            XSL文件的磁盘路径
	 */
	public  void executeXSL(Document myDocument, String xslFilePath,StreamResult xmlResult) {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			DOMOutputter outputter = new DOMOutputter();
			org.w3c.dom.Document domDocument = outputter.output(myDocument);
			Source xmlSource = new javax.xml.transform.dom.DOMSource(
					domDocument);
			StreamSource xsltSource = new StreamSource(new FileInputStream(xslFilePath));
			Transformer transformer = tFactory.newTransformer(xsltSource);
			transformer.transform(xmlSource, xmlResult);
		} catch (FileNotFoundException e) {
            logger.info(e.getMessage());
		} catch (TransformerConfigurationException e) {
            logger.info(e.getMessage());
		} catch (TransformerException e) {
            logger.info(e.getMessage());
		} catch (JDOMException e) {
            logger.info(e.getMessage());
		}
	}

}