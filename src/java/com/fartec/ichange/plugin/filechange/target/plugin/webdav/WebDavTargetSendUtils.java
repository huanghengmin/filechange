package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-19
 * Time: ����3:27
 * To change this template use File | Settings | File Templates.
 */
public class WebDavTargetSendUtils {
    private Logger logger=Logger.getLogger(WebDavTargetSendUtils.class);
    private WebDavUtils webDavUtils= WebDavUtils.getSingleWebDavUtil();
    private JDomUtils jDomUtil= JDomUtils.getSingleJDomUtils();
    private static WebDavTargetSendUtils webDavSendUtils =null;
    private WebDavTargetSendUtils(){}
    public static WebDavTargetSendUtils getSingleWebDavTargetUtils(){
        if(webDavSendUtils ==null){
            webDavSendUtils =new WebDavTargetSendUtils();
        }
        return webDavSendUtils;
    }

    /**
     * ɾ��Ŀ����ļ�
     * @param fileList  Ҫɾ�����ļ��б�
     * @param targetFile  Ŀ��������ļ�
     */
    public void deleteFiles(FileList fileList,TargetFile targetFile){
        Iterator<FileBean> its=fileList.iterable();
        while (its.hasNext()){
            FileBean fileBean=its.next();
            //�õ�ͬ���ļ����Ƿ���ڴ�fileBean
            boolean bool = jDomUtil.getSyncXMLFileBean(fileBean);
            if(!bool){
                //Ŀ����ļ�ȫ��
                String targetFullName= webDavUtils.getTargetHostAndName(targetFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname(),targetFile);
                //Ŀ��˲�������
                Sardine sardine=SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
               try {
                    if(sardine.exists(targetFullName)){
                        //ɾ���ļ�
                        sardine.delete(targetFullName);       //ɾ���ļ�
                        //ɾ��Ŀ����ļ���
                        deleteTargetDirectory(fileBean,sardine,targetFile);    //ɾ��Ŀ����ļ���
                        logger.info("ɾ��Ŀ����ļ�"+targetFullName+"�ɹ�");
                    }
                } catch (IOException e) {
                   logger.info("ɾ��Ŀ����ļ�"+targetFullName+"ʧ�ܣ������ļ���ɾ��������");
                }
            }
        }
        fileList.clear();
    }


    /**
     * Ŀ��˸�������
     * @param path   Ŀ��˱����ļ�·��
     * @param sardine    ��������
     * @param fileBean   �ļ�����
     */
    public void sendRename(String path,Sardine sardine,FileBean fileBean){
        try{
            if(sardine.exists(path)){
                sardine.move(path, path + FileContext.Str_SyncFileTargetProcess_Flag);
            }
        }catch (IOException e){
            logger.info("�ƶ�"+fileBean.getFullname()+"�ļ����ɹ�");
        }
    }

    /**
     * Ŀ��˸Ļ�ԭ������
     * @param path       Ŀ��˱����ļ�·��
     * @param sardine    ��������
     * @param fileBean   �ļ�����
     */
    public void sendRenameToTargetSourceName(String path,Sardine sardine,FileBean fileBean){
        try{
            if(!sardine.exists(path)&&sardine.exists(path + FileContext.Str_SyncFileTargetProcess_Flag)){
                sardine.move(path+ FileContext.Str_SyncFileTargetProcess_Flag,path);
            }
        }catch (IOException e){
            logger.info("�ļ�"+fileBean.getFullname()+"�Ļ�ԭ�����ɹ���������");
        }
    }

    /**
     *      �õ�Ŀ����ļ���
     * @param path      Ŀ����ļ�·��
     * @param sardine   �ļ���������
     * @param fileBean  �ļ�����
     * @return           �����ļ���
     */
    public InputStream getTargetFileBeanInputStream(String path,Sardine sardine,FileBean fileBean){
        InputStream inputStream=null;
        try{
            if(sardine.exists(path+ FileContext.Str_SyncFileTargetProcess_Flag)) {
                inputStream = sardine.get(path+ FileContext.Str_SyncFileTargetProcess_Flag);
            }
        }catch (IOException e) {
            logger.info("**********���ļ�"+fileBean.getFullname()+"�����ɹ�**********");
        }
        return  inputStream;
    }

    /**
     * �����ļ���Դ��
     * @param inputStream      �����ļ���Դ�˷�����
     * @param fileBean          �����ļ�����
     * @return                   ����Դ���Ƿ񱣴�ɹ���ʶ
     */
    public boolean processFileBeanInputStreamToSource(TargetOperation targetOperation,InputStream inputStream,FileBean fileBean){
        int i=0;
        boolean flag = false;
        while (!flag){
            i++;
            flag = targetOperation.process(inputStream,fileBean);
            if(i==3)
                break;
        }
        return flag;
    }

    /**
     * �����ļ���Ŀ��˵ķ���
     * @param fileBean      Դ���ļ�����
     * @param path            Դ���ļ�·��
     * @param sardine         Դ�˲�������
     * @param iTargetProcess   Ŀ��˲�������
     * @param sourceFile         Դ�������ļ�
     */
     public void  send_FileBean(FileBean fileBean,TargetOperation targetOperation,Sardine sardine,TargetFile targetFile){
        //ת�����ļ�ȫ��
         String path= webDavUtils.getTargetHostAndName(targetFile)+
                 webDavUtils.urlEncoder((webDavUtils.judgeWorkDir(targetFile.getDir())+fileBean.getFullname()).replace("+","%2B"),targetFile).replace("+","%20");
        //���Ϊδ����ļ�����ʽ
         if(fileBean.getFullname().endsWith(FileContext.Str_SyncFileTargetProcess_Flag)){
             InputStream inputStream=null;
             try {
                //�õ�������
                inputStream= sardine.get(path);
                 boolean  flag=false;
                 if(inputStream!=null){
                 //�����ļ���Դ��
                     flag = processFileBeanInputStreamToSource(targetOperation, inputStream, fileBean);      //�����ļ���Դ��
                 } else {
                     logger.info("��ȡ�ļ���"+fileBean.getFullname()+"������������");
                 }
                if(flag){
                     //�Ļ�ԭ��
                     sardine.move(path,path.replace(FileContext.Str_SyncFileTargetProcess_Flag,""));
                }
             } catch (IOException e) {
                  logger.info(e.getMessage()+"Ŀ��˸��������������ļ����ڲ���������");
             }
         }else {
              //����
              sendRename(path, sardine, fileBean);    //����
              //����
              InputStream inputStream= null;
                      if(inputStream!=null){

                          inputStream=getTargetFileBeanInputStream(path,sardine,fileBean);  //����

                      }else {
                            logger.info("��ȡ�ļ������������ļ�"+fileBean.getFullname()+"���ƶ��������� ");
                      }
              //�����ļ���Դ��
              boolean  flag = processFileBeanInputStreamToSource(targetOperation, inputStream, fileBean);      //�����ļ���Դ��
              //�����ؽ��Ϊtrue�͸Ļ�ԭ��
              if(flag){
                  //�Ļ�ԭ��
                   sendRenameToTargetSourceName(path, sardine, fileBean);     //�Ļ�ԭ��
              }
         }
     }

    /**
     * ɾ��Դ���ļ���
     * @param fileBean Ҫɾ���ļ��е��ļ�����
     * @param sardine       Ŀ��˲�������
     * @param targetFile    Ŀ��������ļ�
     */
     public synchronized void deleteTargetDirectory(FileBean fileBean,Sardine sardine,TargetFile targetFile){
        //�õ�Ŀ����ļ�·�������������������˿�
        String targetDir= webDavUtils.getFileDir(fileBean);
        //�õ�Ŀ����ļ�·��
        String requestUrl= webDavUtils.getTargetHostAndName(targetFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir())+targetDir,targetFile);
        //ѭ��ɾ���ļ���
        deleteTargetFolder(sardine,requestUrl,targetFile);
    }

    /**
     *  ѭ��ɾ��Դ���ļ���
     * @param sardine    Ŀ��˲�������
     * @param requestUrl   �ж�ɾ���ļ���·��
     * @param targetFile   Ŀ��������ļ�
     */
     public synchronized void deleteTargetFolder(Sardine sardine,String requestUrl,TargetFile targetFile){
        try {
            if(sardine.exists(requestUrl)){
                List<DavResource> davResourceList= sardine.list(requestUrl);
                if(!davResourceList.isEmpty()){
                Iterator<DavResource> iterator=davResourceList.iterator();
                iterator.next();
                if(!iterator.hasNext()) {
                    if(requestUrl.endsWith("/"))  {
                       sardine.delete(requestUrl);
                    }
                    else{
                        requestUrl=requestUrl+"/";
                         sardine.delete(requestUrl);
                    }
                    requestUrl=requestUrl.substring(0,requestUrl.lastIndexOf("/"));
                    //�ж��ϼ��ļ���
                    if(requestUrl.contains("/")){
                        requestUrl=requestUrl.substring(0,requestUrl.lastIndexOf("/"));
                        if(!requestUrl.equals(webDavUtils.getTargetHostAndName(targetFile)+
                                webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()),targetFile))){
                               //ɾ���ļ���
                               deleteTargetFolder(sardine,requestUrl,targetFile);
                        }
                    }
                }
            }
            }
        } catch (IOException e) {
            logger.info("ɾ���ļ���"+requestUrl+"������Ŀ��������ƶ�������");
        }
    }

    /**
     * �ϴ��з�������ļ��б�
     * @param sourceFile     Դ�����ļ�
     * @param fileList         �б�
     * @param sardine           Դ��������
     * @param iTargetProcess   Ŀ��˲�������
     */
    public  void send_BatchFileBeans(TargetFile targetFile,FileList fileList,Sardine sardine,TargetOperation targetOperation) {
        //ȡ���ļ��б�
        Iterator<FileBean> it=fileList.iterable();
        while (it.hasNext()){
            FileBean fileBean=it.next();
            //�����ļ�����
            send_FileBean(fileBean, targetOperation, sardine, targetFile);
        }
        fileList.clear();
    }


    /**
     * ��װ����ΪfileBean����
     * @param davResource ��������Դ
     * @param workDir      �����ռ�
     * @return            ��װΪfileBean����
     */
     public FileBean createToFileBean(DavResource davResource,TargetFile targetFile){
         FileBean fileBean=new FileBean();
         fileBean.setFilesize(davResource.getContentLength());
         try{
             //���ĵ�ת����href�����ܺ����������Ͷ˿ں�,����û��
             String href=URLDecoder.decode(davResource.getHref().toString(),targetFile.getCharset());
             if(!href.contains("http://"))  {
                 fileBean.setName(href.substring(href.lastIndexOf("/"),href.length()));
                 fileBean.setFullname(href.replace(webDavUtils.judgeWorkDir(targetFile.getDir()),""));
             } else {
                 fileBean.setName(href.substring(href.lastIndexOf("/"),href.length()));
                 fileBean.setFullname(href.replace(webDavUtils.getTargetHostAndName(targetFile)+webDavUtils.judgeWorkDir(targetFile.getDir()),""));
             }
         }catch (UnsupportedEncodingException e) {
             logger.info("��֧��URLת�����������");
         }
         /*fileBean.setTime(Long.parseLong(resource.getModified().toString()));
         try {
               fileBean.setMd5(FileMd5.getFileMD5String(resource.getPath()));
              } catch (IOException e) {
         }*/
         return  fileBean;
   }

    /**
     * �����²�Ŀ¼����
     * @param targetFile  Դ�������ļ�
     * @param sourceFolder    Դ���ϲ��ļ���
     * @param sardine          Դ�˲�������
     * @param uploadCount     ��������
     * @param pool                 Դ���̳߳�
     */
     public void ergodicTargetFolder(TargetFile targetFile,List<DavResource> sourceFolder,Sardine sardine,long uploadCount,TargetOperation targetOperation,ExecutorService pool,boolean delete){
            FileList sendFileList=new FileList();
            //�ļ����б�
            List<DavResource> folder=new ArrayList<DavResource>();
            //�������������ļ��м���
            if(!sourceFolder.isEmpty()){
                Iterator<DavResource> soIterator=sourceFolder.iterator();
                while (soIterator.hasNext()){
                    DavResource davResource=soIterator.next();
                    //�ı�����·��
                    String requestUrl= null;
                    String href=webDavUtils.urlDecoder(davResource.getHref().toString(),targetFile);
                    if(href.contains("http://")) {
                        String decoder=href.replace(webDavUtils.getTargetHostAndName(targetFile),"");
                        requestUrl=  webDavUtils.getTargetHostAndName(targetFile)+webDavUtils.urlEncoder(decoder,targetFile);
                    }else {
                        requestUrl = webDavUtils.getTargetHostAndName(targetFile)+
                                webDavUtils.urlEncoder(href,targetFile);
                    }
                    //�õ���Դ
                    List<DavResource> davResources=null;
                    try {
                        if(sardine.exists(requestUrl)){
                            davResources=sardine.list(requestUrl);
                        }
                    } catch (IOException e) {
                        logger.info(e.getMessage()+"error!!�ļ��п����ƶ����޸ģ���");
                        return;
                    }
                    if(!davResources.isEmpty()){
                        Iterator<DavResource> iterator=davResources.iterator();
                        //������Ŀ¼
                        iterator.next();
                        while (iterator.hasNext()){
                            DavResource resource=iterator.next();
                            if(resource.isDirectory()){
                                //�²��ļ��м������
                                folder.add(resource);
                            }else{
                                if(resource.getName().endsWith(FileContext.Str_SyncFileTargetProcess_End_Flag)){
                                    //�����д���
                                }else {
                                    sendFileList.addFileBean(createToFileBean(resource,targetFile));
                                    if(sendFileList.size()==(uploadCount*(targetFile.getThreads()))){
                                        if(delete){
                                            deleteFiles(sendFileList,targetFile);//ɾ��Ŀ����ļ�
                                        }else {
                                           poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
                                        }
                                    }
                            }
                        }
                    }
                }
            }
            if(sendFileList.size()>0){
                if(delete){
                     deleteFiles(sendFileList,targetFile);//ɾ��Ŀ����ļ�
                }else {
                      poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
                }
            }
            //�����²�Ŀ¼
            if(folder.size()>0){
                ergodicTargetFolder(targetFile,folder,sardine,uploadCount,targetOperation,pool,delete); //�²����
            }
      }
   }

    /**
     * ����Ŀ����ļ� ������
     * @param targetFile Ŀ��������ļ�
     * @param targetOperation     Դ�˲�������
     * @param uploadCount         ��������
     * @param pool                 �̳߳�
     * @param delete               �Ƿ�ɾ����ʶ
     */
    public void ergodicTarget(TargetFile targetFile,TargetOperation targetOperation,long uploadCount,ExecutorService pool,boolean delete){
        FileList sendFileList=new FileList();
        //�����Ŀ¼�б�
        List<DavResource> folder=new ArrayList<DavResource>();
        //��ʼ����·��
        String sourceRequestUrl= webDavUtils.getTargetHostAndName(targetFile)+
                webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(targetFile.getDir()),targetFile);
        //������������
        Sardine sardine= SardineFactory.begin(targetFile.getUserName(),targetFile.getPassword());
        //webDAV��Դ
        List<DavResource> sourceDavResource=null;
        boolean flag=true;
        while (flag){
        try {
            if(!sardine.exists(sourceRequestUrl)){
                logger.info("***�����������ڴ�Ŀ¼!!!***");
            } else {
                sourceDavResource=sardine.list(sourceRequestUrl);
                if(sourceDavResource!=null){
                    flag=false;
                }
            }
        }catch (IOException e){
            logger.info(e.getMessage()+"***����������Ŀ¼��������ʧ�ܻ������Ŀ¼���ó���!!!***");
            return;
        }
        }
        if(!sourceDavResource.isEmpty()){
            Iterator<DavResource> sourceIterator=sourceDavResource.iterator();
            //������һ��ָ��Ŀ¼����Դ
            sourceIterator.next();
            //ѭ��������Ŀ¼
            while (sourceIterator.hasNext()){
                DavResource resource=sourceIterator.next();
                if(resource.isDirectory()){
                    folder.add(resource);
                }else{
                    if(resource.getName().endsWith(FileContext.Str_SyncFileTargetProcess_End_Flag)){
                        //���ΪĿ���ͬ���������ļ��Ͳ��ô���

                    }else {
                            sendFileList.addFileBean(createToFileBean(resource,targetFile));
                                if(sendFileList.size()==(uploadCount*(targetFile.getThreads()))){
                                    if(delete){
                                             deleteFiles(sendFileList,targetFile);//ɾ��Ŀ����ļ�
                                    }else {
                                        poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
                                    }
                            }
                        }
                    }
                }
            }
        if(sendFileList.size()>0){
            if(delete){
                deleteFiles(sendFileList,targetFile);//ɾ��Ŀ����ļ�
            }else {
                   poolFileList(sendFileList,targetFile,uploadCount,pool,targetOperation);
            }
        }
        if(!folder.isEmpty()){
            ergodicTargetFolder(targetFile,folder,sardine,uploadCount,targetOperation,pool,delete);      //�����²�Ŀ¼
        }
    }

    /**
     *   ���߳�
     * @param sendFileList    �����ļ��б�
     * @param targetFile      Ŀ��������ļ�
     * @param uploadCount     �����ϴ�����
     * @param pool             �̳߳�
     * @param targetOperation    Դ�˲�������
     */
    private void poolFileList(FileList sendFileList, TargetFile targetFile, long uploadCount, ExecutorService pool, TargetOperation targetOperation) {
        //����ļ��б��С���ڣ��߳������Է�����С
        if(sendFileList.size()==(targetFile.getThreads()*uploadCount)){    //���б���������߳������Է�������ʱ�Ϳ��跢��
            Iterator<FileBean> its=sendFileList.iterable();
            FileList poolFileList=new FileList();
            while (its.hasNext()){
                poolFileList.addFileBean(its.next());
                if(poolFileList.size()==uploadCount){
                    processList(targetOperation,poolFileList,pool,targetFile);       //�����ļ��б�����
                }
            }
            if(poolFileList.size()>0&&poolFileList.size()<targetFile.getThreads()){
                processFile(targetOperation,poolFileList,pool,targetFile);   //���͵����ļ�����
            }else {
                processList(targetOperation,poolFileList,pool,targetFile);        //�����ļ��б�����
            }
            sendFileList.clear();
            //����ļ��б��С�������߳�����С���߳������Է�������
        }else if((targetFile.getThreads() < sendFileList.size())&&(sendFileList.size() <(targetFile.getThreads()*uploadCount))){
            long count=sendFileList.size()/targetFile.getThreads()+1;
            Iterator<FileBean> its=sendFileList.iterable();
            FileList poolFileList=new FileList();
            while (its.hasNext()){
                poolFileList.addFileBean(its.next());
                if(poolFileList.size()==count){
                    processList(targetOperation,poolFileList,pool,targetFile);    //�����ļ��б�����
                }
            }
            if(poolFileList.size()>0&&poolFileList.size()<targetFile.getThreads()){    //�����ļ��б�����
                //�ļ���С���߳���ʱ��Ĵ���ʽ
                processFile(targetOperation,poolFileList,pool,targetFile);    //���͵����ļ�����
            }else {
                processList(targetOperation,poolFileList,pool,targetFile);                     //�����ļ��б�����
            }
            sendFileList.clear();
        }else { //�ļ���С���߳���ʱ��Ĵ���ʽ
            processFile(targetOperation,sendFileList,pool,targetFile);   //���͵����ļ�����
            sendFileList.clear();
        }

    }

    /**
     * �����̳߳ط����ļ��б���
     * @param poolFileList    �����ļ��б�
     * @param pool              �̳߳�
     * @param targetFile       Ŀ��������ļ�
     */
    private void processList(TargetOperation targetOperation, FileList poolFileList, ExecutorService pool, TargetFile targetFile) {
        FileList sFileList=targetOperation.procesFileList(alterFileList(poolFileList));
        if(sFileList.size()>0){
            pool.execute(new WebDavTargetListTask(sFileList,targetFile,targetOperation));
        }
    }

    /**
     *   ���������ļ�����
     * @param iTargetProcess    Ŀ��˲�������
     * @param poolFileList      ��Ҫ���͵��б�
     * @param pool                �̳߳�
     * @param sourceFile        Դ�������ļ�
     */
    public void processFile(TargetOperation targetOperation,FileList poolFileList,ExecutorService pool,TargetFile targetFile){
        FileList fileList=targetOperation.procesFileList(alterFileList(poolFileList));
        Iterator<FileBean> iterator=fileList.iterable();
        while (iterator.hasNext()){
            FileBean fileBean=iterator.next();
            pool.execute(new WebDavTargetFileTask(fileBean,targetFile,targetOperation));    //���͵����ļ�����
        }
    }

    /**
     * ת��Ҫͬ�����ļ��б�
     * @param poolFileList ͬ���ļ��б�
     * @return       Ҫ�ϴ����ļ��б�
     */
    public FileList alterFileList(FileList poolFileList){
        Iterator<FileBean> its=poolFileList.iterable();
        FileList sendFileList=poolFileList;
        while (its.hasNext()){
            sendFileList.addFileBean(its.next());
        }

        poolFileList.clear();
        return sendFileList;
    }

}

