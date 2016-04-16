package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-19
 * Time: ����3:25
 * To change this template use File | Settings | File Templates.
 */
public class WebDavSourceReceiveUtils {
    private Logger logger = Logger.getLogger(WebDavSourceReceiveUtils.class);
    private static WebDavSourceReceiveUtils webdavSourceReceiveUtils =null;
    private WebDavUtils webDavUtils = WebDavUtils.getSingleWebDavUtil();
    private WebdavSourceTempFileUtils webdavSourceTempFileUtils = WebdavSourceTempFileUtils.getSingleWebdavSourceTempFile();
    private WebDavSourceReceiveUtils(){}
    public static WebDavSourceReceiveUtils getSingleWebDavSourceUtil(){
        if(webdavSourceReceiveUtils == null){
            webdavSourceReceiveUtils = new WebDavSourceReceiveUtils();
        }
            return webdavSourceReceiveUtils;
    }

    /**
     * ��ѯԴ�˵��ļ��Ƿ����
     * @param targetFile      Ŀ��������ļ�
     * @param fileBean         Դ��fileBean����
     * @return         �ж�Դ���ǲ����ڴ��ļ�
     */
    public boolean existsSourceFile(SourceFile sourceFile,FileBean fileBean){
        boolean  flag = false;
        //ת�����ļ�ȫ��
        String targetFileName = webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname(),sourceFile);
        //��������
        Sardine sardine= SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        try {
            if(sardine.exists(targetFileName)){
                List<DavResource> davResources = sardine.list(targetFileName);
                Iterator<DavResource> iterator = davResources.iterator();
                DavResource resource = iterator.next();
                if((resource.getContentLength() == fileBean.getFilesize())&&(webDavUtils.urlDecoder(resource.getPath().toString(),sourceFile).replace(webDavUtils.judgeWorkDir(sourceFile.getDir()),"").equals(fileBean.getFullname()))){
                    flag = true;
                }
            }
        }catch (IOException e) {
            return flag;
        }
        return flag;
    }


    /**
     *����δ��ɵ��ļ�
     * @param fileBean  �ļ�����
     * @param targetFile    Ŀ�������ļ�
     * @param sourceFile     Դ�������ļ�
     */
    public boolean processNotFinishedFileBean(InputStream inputStream,FileBean fileBean,SourceFile sourceFile,WebDavSourceReceiveUtils webDavReceiveUtils){
        boolean flag = false;
        //Ŀ����ļ�ȫ��
        String path= webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+
                fileBean.getFullname().replace(FileContext.Str_SyncFileSourceProcess_Flag,FileContext.Str_SyncFileSourceProcess_End_Flag),sourceFile);

        Sardine sardine = SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        try {
            if(sardine.exists(path))  {
                sardine.delete(path);
            }
        } catch (IOException e) {
            logger.info("ɾ���ļ�"+path+"��������");
        }
        //Ŀ���δ����ļ�ȫ��
        File file = null;
        try{
            boolean bool = false;
            if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileStart)){
                webdavSourceTempFileUtils.createToTempFile(fileBean,inputStream) ;
                inputStream.close();
            }else {
                if(fileBean.getSyncflag().equals(FileContext.Str_SyncFileEnd))
                    webdavSourceTempFileUtils.createToTempFile(fileBean,inputStream);
                inputStream.close();
                bool = webdavSourceTempFileUtils.existsTempFile(fileBean);
                if(bool){
                    logger.info(fileBean+"��ʱ�ļ�����ɹ�");
                }
            }
            if(bool){
                file = webdavSourceTempFileUtils.getFile(fileBean);
                 putStream(new FileInputStream(file) ,fileBean,sardine,path,sourceFile);//���浽Ŀ���
                //����Ŀ��˵�����
                 putRename(fileBean,sardine,path,sourceFile);
            }
        }catch (Exception e){
            logger.info("�����ļ�������"+fileBean.getFullname());
        }  finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.info("�ر��ļ�����������");
            }
        }
        //�ж��Ƿ񱣴�ɹ�
        if(webDavReceiveUtils.JudgeNotFinishedFileBeanSaveSuccess(fileBean,sourceFile)){
            flag = true;
            file.delete();
            logger.info(Thread.currentThread().getName()+"*****�ļ�*****"+webDavUtils.getSourceHostAndName(sourceFile) +webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"::ͬ����ɣ�");
        }
        return flag;
    }

    /**
     *  �ϴ���Դ��
     * @param inputStream ����������
     * @param fileBean     �ļ�����
     * @param sardine       Դ�˲�������
     * @param targetFullName  �ϴ���ȫ��
     */
    public void putStream(InputStream inputStream,FileBean fileBean,Sardine sardine,String targetFullName,SourceFile sourceFile){
        try {
            if(!sardine.exists(targetFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag)&&inputStream != null){
                //�ϴ���Դ�˷�����
                sardine.put(targetFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag,inputStream);
            }
        }catch (IOException e) {
            logger.info("Ŀ��˴����ļ�"+ webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"ʧ��");
        }
    }

    /**
     *Դ�˸Ļ�ԭ������
     * @param fileBean   �ļ�����
     * @param sardine          Դ�˲�������
     * @param targetFullName      �ļ�ȫ��
     */
    public void putRename(FileBean fileBean,Sardine sardine,String targetFileBeanFullName,SourceFile sourceFile){
        try {
            if(sardine.exists(targetFileBeanFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag)){
                //��������
                sardine.move(targetFileBeanFullName+ FileContext.Str_SyncFileTargetProcess_End_Flag,targetFileBeanFullName);
            }
        } catch (IOException e) {
            logger.info("******Ŀ����ļ�"+ webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"�������ɹ�*****");
        }
    }

    /**
     * ת����ȥĿ��˲���
     * @param fileBean    �ļ�����
     * @return     ת����ȥԴ�˲����ļ��Ƿ���ڣ��Ƿ񱣴�ɹ�
     */
    public boolean  JudgeNotFinishedFileBeanSaveSuccess(FileBean fileBean,SourceFile sourceFile){
        fileBean.setFullname(fileBean.getFullname().replace(FileContext.Str_SyncFileTargetProcess_Flag,""));
        return  existsSourceFile(sourceFile, fileBean);
    }

    /**
     * Դ�˹����ļ���
     * @param sardine      Դ�˲�������
     * @param fileBean          �ļ�����
     * @param sourceFile           Դ�������ļ�
     */
    public synchronized void createSourceDirectory(Sardine sardine,FileBean fileBean, SourceFile sourceFile){
        //Դ�ļ���ȥ�����Ͷ˿ڹ����ռ���·��
        String targetDir = webDavUtils.getFileDir(fileBean);
        //�ֱ𴴽���Ŀ¼
        String dirMin = null;
        if(!targetDir.equals("")&&targetDir!=null) {
            dirMin = sourceFile.getDir()+targetDir;
        }else{
            dirMin = sourceFile.getDir()+"";
        }
        String[] dir = dirMin.split("/");
        String requestDir = webDavUtils.getSourceHostAndName(sourceFile)+"/";
        for(String d:dir){
            if(!d.equals("")&&d != null){
                try {
                    requestDir += webDavUtils.urlEncoder(d,sourceFile)+"/";
                    //���༶Ŀ¼�����Զ�����
                    if(!sardine.exists(requestDir)) {
                        sardine.createDirectory(requestDir);
                    }
                } catch (IOException e) {
                    logger.error("*****Դ��"+webDavUtils.getSourceHostAndName(sourceFile)+webDavUtils.judgeWorkDir(sourceFile.getDir())+fileBean.getFullname()+"����Ŀ¼���ɹ�!***");
                }
            }
        }
    }


}
