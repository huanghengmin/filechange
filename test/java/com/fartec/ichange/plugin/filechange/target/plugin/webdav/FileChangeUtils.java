package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.source.plugin.webdav.WebDavUtils;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.TargetFile;
import com.inetec.common.exception.Ex;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daly
 * Date: 12-4-11
 * Time: ����7:50
 * To change this template use File | Settings | File Templates.
 */
public class FileChangeUtils {

    private static WebDavUtils webDavUtils= new WebDavUtils();

    /**
     * �����ļ�
     * @param sourceFile Դ������
     * @param targetFile Ŀ�������
     * @param sourceDir   Դ�˹����ռ�
     * @param targetDir   Ŀ��˹����ռ�
     * @param sourceIp    Դ�˵�ַ
     * @param targetIp    Ŀ���ַ
     * @param sourcePort   Դ�˶˿�
     * @param targetPort   Ŀ��˿�
     * @param sourceUser    Դ���û���
     * @param sourcePwd     Դ���û�������
     * @param targetUser    Ŀ����û���
     * @param targetPwd     Ŀ����û�������
     */
    public static  void  setConfig(SourceFile sourceFile,TargetFile targetFile,String sourceDir,String targetDir,
                                      String sourceIp,String targetIp, String sourcePort,String targetPort,
                                      String sourceUser,String sourcePwd,String targetUser,String targetPwd){
        //Դ������
        sourceFile.setPassword(sourcePwd);
        sourceFile.setUserName(sourceUser);
        sourceFile.setDir(sourceDir);
        try {
            sourceFile.setPort(sourcePort);
            sourceFile.setThreads("1");
        }catch (Ex ex) {
            System.out.println("Դ�����ö˿ںų���!");
            ex.printStackTrace();
        }
        sourceFile.setServerAddress(sourceIp);
        sourceFile.setCharset("utf-8");
        try {
            sourceFile.setDeletefile(String.valueOf(false));
        } catch (Ex ex) {
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            sourceFile.setInterval(String.valueOf(5*1000));
        } catch (Ex ex) {
            System.out.println("Դ��ת�����ʱ�����!");
            ex.printStackTrace();
        }
        sourceFile.setProtocol(SourceFile.Str_Protocol_WebDAV);
        try {
            sourceFile.setIsincludesubdir(String.valueOf(true));
        } catch (Ex ex) {
            System.out.println("Դ��ת���Ƿ������Ŀ¼����!");
            ex.printStackTrace();
        }
        try {
            sourceFile.setIstwoway(String.valueOf(true));
        } catch (Ex ex) {
            System.out.println("Դ��ת��ͬ���Ƿ�˫�����!");
            ex.printStackTrace();
        }
        //�ܹ����˵�����
        sourceFile.setFiltertypes("*.*");
        //���ܹ��˵�����
        sourceFile.setNotfiltertypes("");
        //Ŀ�������
        try {
            targetFile.setDeletefile(String.valueOf(false));
        } catch (Ex ex) {
            System.out.println("Ŀ���ת���Ƿ�ɾ���ļ�����!");
            ex.printStackTrace();
        }
        targetFile.setPassword(targetPwd);
        targetFile.setUserName(targetUser);
        targetFile.setServerAddress(targetIp);
        try {
            targetFile.setPort(targetPort);
        } catch (Ex ex) {
            System.out.println("Ŀ������ö˿ںų���!");
            ex.printStackTrace();
        }
        targetFile.setDir(targetDir);
        try {
            targetFile.setThreads(String .valueOf(10));
        } catch (Ex ex) {
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        targetFile.setCharset("GBK");
        try {
            targetFile.setOnlyadd(String.valueOf(false));
        } catch (Ex ex) {
            System.out.println("Ŀ��������Ƿ�ֻ�����ļ�����!");
            ex.printStackTrace();
        }
       
    }

    /**
     * �ϴ������ļ���������
     * @param sourceFile Դ�������ļ�
     * @param localPath  ����Ŀ¼
     */
    public static void  uploadToService(SourceFile sourceFile,String localPath){
        File file=new File(localPath);
        Sardine sardine= SardineFactory.begin(sourceFile.getUserName(),sourceFile.getPassword());
        if(file.isDirectory()) {
            File[] files=file.listFiles();
            int i=0;
            for (;i<files.length;i++){
                if( files[i].isFile()){
                    InputStream fis = null;
                    try {
                        fis = new FileInputStream(new File(files[i].getAbsolutePath()));
                        try {
                            String dir=files[i].getAbsolutePath().substring(files[i].getAbsolutePath().indexOf(":")+1,files[i].getAbsolutePath().length());
                            String uploadDir=dir.replaceAll("\\\\","/");
                            String requestUrl= webDavUtils.getSourceHostAndName(sourceFile)+
                                    webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+uploadDir,sourceFile);
                            if(!sardine.exists(requestUrl)) {
                                //Դ�ļ���ȥ�����Ͷ˿ڹ����ռ���·��
                                String targetDir=null;
                                if(uploadDir.contains("/")){
                                    //��ȥ���������˿ںţ������ռ���·���������ļ���
                                    targetDir=uploadDir.substring(0,uploadDir.lastIndexOf("/"));
                                }else {
                                    targetDir="";
                                }
                                //�ֱ𴴽���Ŀ¼
                                String dirMin=null;
                                if(!targetDir.equals("")&&targetDir!=null) {
                                    dirMin=targetDir;
                                }else{
                                    dirMin="";
                                }
                                String[] dir2=dirMin.split("/");
                                String requestDir= webDavUtils.getSourceHostAndName(sourceFile)+ webDavUtils.urlEncoder(
                                        webDavUtils.judgeWorkDir(sourceFile.getDir()),sourceFile)+"/";
                                for(String d:dir2){
                                    if(!d.equals("")&&d!=null){
                                        try {
                                            requestDir+= webDavUtils.urlEncoder(d,sourceFile)+"/";
                                            //���༶Ŀ¼�����Զ�����
                                            if(!sardine.exists(requestDir))
                                            {
                                                sardine.createDirectory(requestDir);
                                            }
                                        } catch (IOException e) {
                                            System.out.println("Ŀ��˴���Ŀ¼���ɹ���");
                                        }
                                    }
                                }
                            }
                            sardine.put(webDavUtils.getSourceHostAndName(sourceFile)+
                                    webDavUtils.urlEncoder(webDavUtils.judgeWorkDir(sourceFile.getDir())+"/"+
                                            uploadDir,sourceFile), fis);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    uploadToService(sourceFile,files[i].getAbsolutePath());
                }
            }
        }
    }

    /**
     * ��ɾ������Ŀ¼�������ļ�
     * @param file   �ļ�����
     */
    private static void deleteFile(File file){
        if(file.exists()){                    //�ж��ļ��Ƿ����
            if(file.isFile()){                    //�ж��Ƿ����ļ�
                file.delete();                       //delete()���� ��Ӧ��֪�� ��ɾ������˼;
            }else if(file.isDirectory()){              //�����������һ��Ŀ¼
                File files[] = file.listFiles();               //����Ŀ¼�����е��ļ� files[];
                for(int i=0;i<files.length;i++){            //����Ŀ¼�����е��ļ�
                    deleteFile(files[i]);             //��ÿ���ļ� ������������е���
                }
            }
            file.delete();
        }else{
        }
    }

    /**
     * �ϴ��ļ���Դ�˷�����
     * @param fileCount    �ļ�����
     * @param fileType     �ļ�����
     * @param localPath    ����Ŀ¼
     */
    public static void createToLocal(long  fileCount,String fileType,String localPath){
        File file=new File(localPath);
        if(file.exists()){
            if(file.isDirectory()){

                File files[] = file.listFiles();            //����Ŀ¼�����е��ļ� files[];
                for(int i=0;i<files.length;i++){            //����Ŀ¼�����е��ļ�
                deleteFile(files[i]);             //��ÿ���ļ� ������������е���
            }
        }
        FileOutputStream fileOutputStream=null;
        BufferedOutputStream bufferedOutputStream=null;
        for (int i=0;i<fileCount;i++){
            if(i%10==0){
                File file1=new File(localPath+File.separator+"�ƺ���"+i+i+i+i+i+i+i+i+File.separator+"�ƺ���"+i+i+File.separator+"�ƺ���"+i+i);
                if(!file1.exists()){
                file1.mkdirs();
                }
                try {
                    fileOutputStream=new FileOutputStream(new File(file1.getAbsolutePath()+File.separator+"11"+i+"."+fileType)) ;
                    bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
                    try {
                        bufferedOutputStream.write(new String("�ƺ���"+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i).getBytes());
                        bufferedOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else {
            try {
                fileOutputStream=new FileOutputStream(new File(localPath+File.separator+"�ƺ���"+i+"."+fileType)) ;
                bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
                try {
                    bufferedOutputStream.write(new String("�ƺ���"+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i+i).getBytes());
                    bufferedOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
              }
            }
          }
        }
    }


}
