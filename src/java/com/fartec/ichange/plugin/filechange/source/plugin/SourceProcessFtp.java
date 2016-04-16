package com.fartec.ichange.plugin.filechange.source.plugin;


import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.utils.*;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;


/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 11-11-26
 * Time: 下午3:19
 * To change this template use File | Settings | File Templates.
 */
public class SourceProcessFtp implements ISourceProcess {
    private static final Logger logger=Logger.getLogger(SourceProcessFtp.class);
    private  ITargetProcess iTargetProcess;
    private  SourceFile sourceFile;
    private SourceOperation sourceOperation;
    private int size=0;
    private FTPClient ftpClient;
    private ExecutorService pool = null;
    private int filelistsize=0;
    private int MB= (1024*1024);
    private boolean isRun=false;
    int i=1;
    public  SourceProcessFtp(SourceFile sourceFile){
        this.sourceFile=sourceFile;
    }

    public SourceProcessFtp(){

    }

    /*
     *查找源端列表    实现分批发送
     * @param ftpClient
     * @param dir        源端路径
     * @param fileListAll      把找到所有文件放入这个filelist   用于目标端是否删除 源端是否删除都为true时使用
     * @return
     * @throws Ex
 * @throws IOException
     */

    public FileList addSourceFile(String dir,FileList fileListAll)  {
        FTPClient ftpClient1 = new FTPClient();
        FileList fileList=new FileList();    //文件夹列表
        FileList sourcefilelist=new FileList();          // 源端文件列表
        FileList fileListFirst=new FileList();            //  先执行中断文件列表
        FTPFile[] files= new FTPFile[0];         //获取路径下所有内容（文件，文件夹）
        if(connectServer()){
            try {
                connectServer(ftpClient1);
                files = ftpClient.listFiles(dir,new FileFilterFtp(sourceFile.getFiltertypes(),sourceFile.getNotfiltertypes(),sourceFile,dir));
                ftpClient1.logout();
            } catch (IOException e) {
                logger.error("[FTP]源端文件夹没有找到"+dir,e);
                closeConnect();
                return null;
            }
        }
        int length=files.length;
        logger.info("[FTP]Files.length:=:" + length);
        for (FTPFile file: files){       //判断有没有中断文件  有加入filelistfrist
            if(file.isFile()){
                if(!FileFilter.existSubString(file.getName(),FileContext.Str_SyncFileTargetProcess_End_Flag)){          //文件类型不是。itpe的执行以下内容
                    FileBean fileBean =FtpFileCompare._FTPFileToBean(file,dir,sourceFile);   //组建filebean
                    if(fileBean!=null){
                        fileListAll.addFileBean(fileBean);
                    }
                    FileBean f=  InterceptedFileName(fileBean,dir,ftpClient,sourceFile);
                    if(f!=null){                                                 //判断是否存在中断文件
                        f.setFullname(f.getFullname()+FileContext.Str_SyncFileSourceProcess_Flag);
                        fileListFirst.addFileBean(f); //添加中断文件
                    }

//                    if(FileFilter.existSubString(file.getName(),FileContext.Str_SyncFileSourceProcess_Flag)){       //判断是否存在中断文件
//                        if(fileBean!=null){
//                            fileListFirst.addFileBean(fileBean); //添加中断文件
//                        }
//                    }
                }
            }
        }
        if(fileListFirst!=null&&fileListFirst.size()!=0){       //fileListFirst   存在文件
            logger.info("[FTP]进入中断文件处理");
            executeListInterrupt(fileListFirst, sourceOperation);
        }
        for (FTPFile file: files){
            if(file.isFile()){
                if(!FileFilter.existSubString(file.getName(),FileContext.Str_SyncFileTargetProcess_End_Flag)){          //文件类型不是。itpe的执行以下内容
                    FileBean fileBean =FtpFileCompare._FTPFileToBean(file,dir,sourceFile);   //组建filebean
                    String strings= FileContext.Str_Sync+fileBean.getFullname()+"_"+fileBean.getTime()+FileContext.Str_Lnk;
                    if(file.getName().equals(strings)){             //判断是否存在中断文件
                        continue;
                    }else {
                        if(fileBean!=null&&fileBean.getFilesize()!=0){
                            sourcefilelist.addFileBean(fileBean);
                            if(sourcefilelist.size()==filelistsize){
                                logger.info("[FTP]文件处理");
                                FileList list=   sourceOperation. procesFileList(sourcefilelist);      //判断目标端是否存在 如果不存在加入到上传列表 list
                                if(list!=null&&list.size()!=0){
                                    logger.info("[FTP]进入文件处理");
                                    executeList(list,sourceOperation);
                                }
                                sourcefilelist.clear();
                            }
                        }
                    }
                }
            } else {         //是文件夹的
                if(file.getName().equals(".")||file.getName().equals("..")||file.getName()==null){
                    continue;
                } else {
                    FileBean fileBean =FtpFileCompare._FTPFileToBean(file,dir,sourceFile);   //组建filebean
                    fileBean.setFile(false);
                    fileList.addFileBean(fileBean);
                }
            }
        }
//        if(fileList.size()!=0&&fileList!=null){
//            sourceOperation.procesFileList(fileList);
//        }

        //分批剩余的部分
        FileList list =null;
        if(sourcefilelist.size()!=0&&sourcefilelist!=null){
            list = sourceOperation.procesFileList(sourcefilelist) ;
        }
        if(list!=null&&list.size()!=0){
            logger.info("[FTP]进入分批剩余的部分文件处理");
            executeList(list,sourceOperation);
        }
        //完成当前目录进入下一个目录
        for (Iterator i=fileList.iterable();i.hasNext();){
            FileBean file= (FileBean) i.next();
            if(file!=null){
                if(sourceFile.isIsincludesubdir()){     //是否进入下级目录
                    String strdir=null;
                    if(dir.endsWith("/")){
                        strdir=(dir+file.getName()).trim();
                    } else {
                        strdir=(dir+"/"+file.getName()).trim();
                    }
                    addSourceFile( strdir,fileListAll);
                }
            }
        }
        return  fileListAll;
    }
    //截取文件名
    public FileBean InterceptedFileName(FileBean fileBean,String dir,FTPClient ftpClient,SourceFile sourceFile){
        String s=FileFilter.getFirstNewSubString(fileBean.getName(),FileContext.Str_Lnk);
        if(s==""||s==null){
        }else {
//            SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd HH:mm:ss:SSS");//
//            String time = sdf.format(fileBean.getTime());
            String ss=FileFilter.getSubString(s,FileContext.Str_Sync);
            if("".equals(ss)||ss==null){
                return null;
            }
            String string2= FileFilter.getFirstNewSubString(ss,"_");
            String s2=FileFilter.getEndSubString(ss,"_");
            long  l= Long.parseLong(s2);

            String url=null;
            try {
                if(dir.endsWith("/")){
                    url= dir+string2;
                } else {
                    url= dir+"/"+string2;
                }
                if(url!=null){
                    FTPFile[] ftpFile= ftpClient.listFiles(url);
                    if(ftpFile.length!=0){
                        if(ftpFile[0].getTimestamp().getTimeInMillis()!=l&&ftpFile[0].getSize()==0) {
                            boolean  bool= false; //删除文件
                            try {
                                bool = ftpClient.deleteFile(dir+fileBean.getFullname());
                            } catch (IOException e) {
                                logger.error(e);
                            }
                            logger.info("[FTP]删除文件"+dir+fileBean.getFullname()+"  是否成功   "+bool);
                            return null;
                        }
                        FileBean fileBeans =FtpFileCompare._FTPFileToBean(ftpFile[0],dir,sourceFile);   //组建filebean
                        return fileBeans;
                    }
                }
            } catch (IOException e) {
                logger.error("[FTP]源端文件夹没有找到"+string2,e);
                closeConnect();

            }
        }
        return null;
    }


    public  boolean  ss(String dir) {
        String dirs=FileFilter.getEnd_NewSubString(dir,FileContext.Str_SyncFileSourceProcess_Flag) ;            //ex:xxx.txt
        FTPFile ftpFile= null;
        FTPFile ftpFiles=null;
        try {
            ftpFile = ftpClient.mlistFile(dir);
            ftpFiles= ftpClient.mlistFile(dirs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(ftpFiles!=null&&ftpFile!=null){    //ex: xxx.txt  or xxx.txt.ispf 都存在
            return false;
        }else {
            return true;
        }
    }
    public void run() {
        isRun=true;
        while (isRun){
            logger.info("\n\n\n");
            logger.info("===============================同步第"+ i +"次=====================================");
            if(connectServerRun()){         //连接源端服务器
                String dir = sourceFile.getDir();                               //源端路径
                FileList fileListAll=new FileList();//源端所有文件列表
                logger.info("[FTP]获取源端文件列表");
                addSourceFile(dir,fileListAll);      //获取源端文件列表
                if (ftpClient!=null){
                    if(ftpClient.isConnected()){
                        closeConnect();
                        logger.info("[FTP]关闭run");
                    }
                }
            }
            try {
                logger.info("[FTP]同步第"+ i++ +"次");
                logger.info("[FTP]休息：=："+sourceFile.getInterval());
                Thread.sleep(sourceFile.getInterval());
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    public boolean process(InputStream in, FileBean bean) {
        return false;
    }

    public boolean process(byte[] data, FileBean bean) {
        return true;
    }

    public void init(SourceOperation source,SourceFile config) {
        this.sourceOperation=source;
        this.sourceFile=config;
        this.size=config.getThreads();
        this.filelistsize=config.getFilelistsize();
    }


    public void init(ITargetProcess target, SourceFile config) {
        this.iTargetProcess=target;
        this.sourceFile=config;
        this.size=config.getThreads();
        this.filelistsize=config.getFilelistsize();
    }

    public void stop()  {
        isRun=false;
    }
    /**
     *  删除源端空文件夹
     * @param dir
     * @return
     * @throws IOException
     */
    public boolean deleteSourceDir(String dir) {
        if(connectServer()){
            try {
                FTPFile[] files  = ftpClient.listFiles(dir);
                for (FTPFile file: files){
                    if(file.getName()==null||".".equals(file.getName())||"..".equals(file.getName())){
                    }else {
                        if (file.isDirectory()) {
                            deleteSourceDir(dir + "/" + file.getName());
                        }
                    }
                }
                ftpClient.removeDirectory(dir);
            } catch (IOException e) {
                logger.error(e);
            }
            return true;
        }else {
            return false;
        }
    }


    public boolean isRun() {
        return isRun;
    }

    @Override
    public FileList procesFileList(FileList list) {
        return null;
    }

    /*
     * 数据操作连接
     * @return
     */
    public  boolean connectServer() {
        boolean flag = true;
        if (ftpClient == null) {
            int reply;
            try {
                ftpClient = new FTPClient();
                ftpClient.setControlEncoding(sourceFile.getCharset());
                ftpClient.setDefaultPort(sourceFile.getPort());
                ftpClient.setDataTimeout(60000);       //设置传输超时时间为60秒
                ftpClient.setConnectTimeout(60000);       //连接超时为60秒
                try{
                    ftpClient.connect(sourceFile.getServerAddress());
                }catch (IOException e){
                    flag=false;
                    logger.info("源端连接异常", e);
                }
                int i=0;
                while (!ftpClient.isConnected()&&i<5){
                    i++;
                    try {
                        Thread.sleep(1000*60);
                    } catch (InterruptedException ee) {
                        logger.error(ee);
                    }
                    ftpClient=null;
                    connectServer();

                }
                ftpClient.setKeepAlive(true);
                ftpClient.setRemoteVerificationEnabled(true);
                ftpClient.login(sourceFile.getUserName(), sourceFile.getPassword());
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            if(sourceFile.getDir()!=null){
//                try {
//                    ftpClient.changeWorkingDirectory(sourceFile.getDir());
//                } catch (IOException e) {
//                    logger.error(e);
//                }
//            }else {
//                logger.warn("源端不存在根目录");
//                return false;
//            }
//                设置被动模式
                ftpClient.enterLocalPassiveMode();
                reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    logger.error("[FTP]连接源端FTP服务器" + sourceFile.getServerAddress() + "拒绝.");
                    flag = false;
                }


            } catch (SocketException e) {
                flag = false;
                logger.error("[FTP]登录源端ftp服务器"+sourceFile.getServerAddress()+"失败,连接超时！",e);
            } catch (IOException e) {
                flag = false;
                logger.error("[FTP]登录源端ftp服务器"+sourceFile.getServerAddress()+"失败，FTP服务器无法打开！",e);
            }
        }
        return flag;
    }
    public  boolean connectServer(FTPClient ftpClient) {
        boolean flag = true;

        int reply;
        try {
            ftpClient.setControlEncoding(sourceFile.getCharset());
            ftpClient.setDefaultPort(sourceFile.getPort());
            ftpClient.setDataTimeout(60000);       //设置传输超时时间为60秒
            ftpClient.setConnectTimeout(60000);       //连接超时为60秒
            try{
                ftpClient.connect(sourceFile.getServerAddress());
            }catch (IOException e){
                flag=false;
                logger.info("源端连接异常", e);
            }
            int i=0;
            while (!ftpClient.isConnected()&&i<5){
                i++;
                try {
                    Thread.sleep(1000*60);
                } catch (InterruptedException ee) {
                    logger.error(ee);
                }
                ftpClient=null;
                connectServer(ftpClient);

            }
            ftpClient.setKeepAlive(true);
            ftpClient.setRemoteVerificationEnabled(true);
            ftpClient.login(sourceFile.getUserName(), sourceFile.getPassword());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            if(sourceFile.getDir()!=null){
//                try {
//                    ftpClient.changeWorkingDirectory(sourceFile.getDir());
//                } catch (IOException e) {
//                    logger.error(e);
//                }
//            }else {
//                logger.warn("源端不存在根目录");
//                return false;
//            }
//                设置被动模式
            ftpClient.enterLocalPassiveMode();
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.error("[FTP]连接源端FTP服务器" + sourceFile.getServerAddress() + "拒绝.");
                flag = false;
            }


        } catch (SocketException e) {
            flag = false;
            logger.error("[FTP]登录源端ftp服务器"+sourceFile.getServerAddress()+"失败,连接超时！",e);
        } catch (IOException e) {
            flag = false;
            logger.error("[FTP]登录源端ftp服务器"+sourceFile.getServerAddress()+"失败，FTP服务器无法打开！",e);
        }
        return flag;
    }


    /**
     * 登陆连接
     * @return
     */
    public  boolean connectServerRun() {
        boolean flag = true;
        if (ftpClient == null) {
            int reply;
            try {
                ftpClient = new FTPClient();
                ftpClient.setControlEncoding(sourceFile.getCharset());
                ftpClient.setDefaultPort(sourceFile.getPort());
                ftpClient.setDataTimeout(60000);       //设置传输超时时间为60秒
                ftpClient.setConnectTimeout(60000);       //连接超时为60秒
                try{
                    ftpClient.connect(sourceFile.getServerAddress());
                }catch (SocketException e){
                    flag=false;
                    logger.info("[FTP]源端连接异常",e);
                }
                ftpClient.setKeepAlive(true);
                ftpClient.setRemoteVerificationEnabled(true);
                ftpClient.login(sourceFile.getUserName(), sourceFile.getPassword());
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                if(sourceFile.getDir()!=null){
                    try {
                        ftpClient.changeWorkingDirectory(sourceFile.getDir());
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }else {
                    logger.warn("[FTP]源端不存在根目录");
                    return false;
                }
//                设置被动模式
                ftpClient.enterLocalPassiveMode();
                reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    logger.error("[FTP]连接源端FTP服务器" + sourceFile.getServerAddress() + "拒绝.");
                    flag = false;
                }
            } catch (SocketException e) {
                flag = false;
                logger.error("[FTP]登录源端ftp服务器"+sourceFile.getServerAddress()+"失败,连接超时！",e);
            } catch (IOException e) {
                flag = false;
                logger.error("[FTP]登录源端ftp服务器"+sourceFile.getServerAddress()+"失败，FTP服务器无法打开！",e);
            }
        }
        return flag;
    }
    /*
    关闭连接
     */
    public  void closeConnect() {
        try {
            if (ftpClient != null) {
                ftpClient.logout();
//                logger.info("关闭ftp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftpClient != null) {
                if(ftpClient.isConnected()){
                    try {
                        ftpClient.disconnect();
//                logger.info("断开ftp连接");
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
                ftpClient=null;
            }
        }
    }
    /*
     *  发送文件
     *  1.迭代发送文件列表
     *  2.先处理后缀名是FileContext.Str_SyncFileSourceProcess_Flag   （没有话直接执行下一步）
     *  3.加锁（就是改名把文件名+后缀）
     *  4.读写流进行上传
     *  5.完成后把后缀名去掉改回原来名称
     *  6.如果源端配置了删除执行删除
     * @param list 发送文件列表
     * @return      boolean
     */

    public boolean executeList(FileList list,SourceOperation sourceOperation)  {
        logger.info("[FTP]进入executeList()方法");
        for (Iterator i=list.iterable();i.hasNext();){
            if(!connectServer()){
                return false;
            }else {
                String str="";
                if(sourceFile.getDir().equals("/")){
                    str="";
                }else if(sourceFile.getDir().endsWith("/")){
                    str=sourceFile.getDir();
                }else {
                    str=sourceFile.getDir()+"/";
                }
                FileBean f= (FileBean) i.next();
                if (!f.isFile()) {  //is dir 跳过
                    continue;
                }
                if(f.getFilesize()==0){      //文件大小为0 bit  跳过
                    continue;
                }
                String stringss=FileContext.Str_Sync+f.getName()+"_"+f.getTime()+FileContext.Str_Lnk;
                logger.info("[FTP]同步文件"+str+f.getFullname()+"开始");
                String s=FileFilter.getFirstNewSubString(str+f.getFullname(),"/");
//                logger.info(s+"/"+stringss);
                String strings=s+"/"+stringss;
                InputStream ftpIns= null;
                try {
                    CreateFileLnk(strings);
                    ftpClient.getReply();
                    ftpIns = ftpClient.retrieveFileStream(f.getFullname());
                    logger.info("[FTP]ftpIn:=:"+ftpIns+"         sourflag.size="+f.getFilesize());

                    sprocess(ftpIns, f,sourceOperation);
                    ftpClient.getReply();
                    ftpClient.noop();
                    boolean  bools=ftpClient.deleteFile(strings); //删除文件
                    logger.info("[FTP]删除临时文件"+strings+"  是否成功  "+bools);
                    if(sourceFile.isDeletefile()){
                        boolean  bool=ftpClient.deleteFile(str+f.getFullname()); //删除文件
                        logger.info("[FTP]删除文件"+str+f.getFullname()+"  是否成功   "+bool);
                    }
                    logger.info("[FTP]同步文件"+str+f.getFullname()+"结束");

                }catch (IOException e) {
                    logger.error(e);
                    closeConnect();
                    return false;
                }
            }
        }
        return true;
    }

    //创建lnk文件
    public  void CreateFileLnk(String strings) throws IOException {
        OutputStream outputStream= null;
        outputStream = ftpClient.storeFileStream(strings);
        outputStream.close();
    }

    //中断文件方法
    public boolean executeListInterrupt(FileList list,SourceOperation sourceOperation)  {
        logger.info("[FTP]executeListInterrupt()方法");
        try{
            for (Iterator i=list.iterable();i.hasNext();){
                if(!connectServer()){
                    return false;
                }else {
                    String str="";
                    if(sourceFile.getDir().equals("/")){
                        str="";
                    }else if(sourceFile.getDir().endsWith("/")){
                        str=sourceFile.getDir();
                    }else {
                        str=sourceFile.getDir()+"/";
                    }
                    FileBean f= (FileBean) i.next();
                    if (!f.isFile()) {  //is dir 跳过
                        continue;
                    }
                    FileList lists=new FileList();
                    lists.addFileBean(f);
                    FileList list1=sourceOperation.procesFileList(lists);
                    String url= FileFilter.getFirstNewSubString(f.getFullname(),FileContext.Str_SyncFileSourceProcess_Flag) ;
                    f.setFullname(url);
                    String stringss=FileContext.Str_Sync+f.getName()+"_"+f.getTime()+FileContext.Str_Lnk;
//                    logger.info("[FTP]同步文件"+str+f.getFullname()+"开始");
                    String s=FileFilter.getFirstNewSubString(str+f.getFullname(),"/");
//                    logger.info(s+"/"+stringss);
                    String strings=s+"/"+stringss;
//                    String strings= FileContext.Str_Sync+url+"_"+f.getTime()+FileContext.Str_Lnk;
                    if(list1==null||list1.size()==0){   //
//                        boolean  bools=ftpClient.rename(str+f.getFullname(),str+f.getFullname().split(FileContext.Str_SyncFileSourceProcess_Flag)[0]);
//                        if(bools==false){
//                            logger.info("源端完成上传后改名失败");
//                        }
                        boolean  bools=ftpClient.deleteFile(strings); //删除文件
                        logger.info("[FTP]删除文件"+str+url+"    "+bools);
                        if(sourceFile.isDeletefile()){
                            boolean  bool=ftpClient.deleteFile(str+url); //删除文件
                            logger.info("[FTP]删除文件"+str+f.getFullname()+"    "+bool);
                        }
                    }else {
                        for (Iterator j=list1.iterable();j.hasNext();){
                            FileBean fb= (FileBean) j.next();
                            if(fb.getFilesize()==f.getFilesize()){
                                InputStream ftpIn = ftpClient.retrieveFileStream(str+url);
                                logger.info("[FTP]同步文件"+str+url+"开始");
                                logger.info("[FTP]ftpIn:=:"+ftpIn+"         sourflag.size="+f.getFilesize());
                                sprocess(ftpIn,f,sourceOperation);
                                ftpClient.getReply();
                                boolean  bools=ftpClient.deleteFile(strings); //删除文件
                                logger.info("[FTP]删除文件"+str+url+"    "+bools);
                                if(sourceFile.isDeletefile()){
                                    boolean  bool=ftpClient.deleteFile(str+url); //删除文件
                                    logger.info("[FTP]删除文件"+str+url+"    "+bool);
                                }
                                logger.info("[FTP]同步文件"+str+f.getFullname()+"结束");
                            }
                            if(fb.getFilesize()<f.getFilesize()){
                                ftpClient.setRestartOffset(fb.getFilesize());
                                InputStream ftpIn = ftpClient.retrieveFileStream(str+url);
                                logger.info("[FTP]同步文件"+str+url+"开始");
                                logger.info("[FTP]ftpIn:=:"+ftpIn+"         sourflag.size="+f.getFilesize());
                                sprocess(ftpIn,f,sourceOperation);
                                ftpClient.getReply();
                                boolean  bools=ftpClient.deleteFile(strings); //删除文件
                                logger.info("[FTP]删除文件"+str+url+"    "+bools);
                                if(sourceFile.isDeletefile()){
                                    boolean  bool=ftpClient.deleteFile(str+url); //删除文件
                                    logger.info("[FTP]删除文件"+str+url+"    "+bool);
                                }
                                logger.info("[FTP]同步文件"+str+f.getFullname()+"结束");
                            }
                        }
                    }
                }
            }
            return  true;
        }catch (IOException e) {
            logger.error(e);
            closeConnect();
        }
        return false;
    }

    /**
     * 分包发送
     * @param in
     * @param fileBean
     * @return
     */
    public boolean  sprocess(InputStream in,FileBean fileBean,SourceOperation sourceOperation){
        logger.debug("[FTP]sourceOperation  == "+sourceOperation);
        if(in==null){
            return false;
        }
        byte [] bytes=new byte[sourceFile.getPacketsize()*MB];
        boolean flag=false;
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        int post=0;
        long fileSize = 0;
        int len=-1;
        fileBean.setFile_flag(FileContext.Str_SourceFile);
        if(fileBean.getName().endsWith(".rtf") || fileBean.getName().endsWith(".txt")){
            try {
                while ((len=in.read(bytes))!=-1){
                    if(post>=sourceFile.getPacketsize()*MB){
                        fileBean.setSyncflag(FileContext.Str_SyncFileStart);
                        fileSize+=post;
                        byte[] data = null ;
                        try {
                            data = KeywordsFilterFactory.getKeywordsFilterUtil(fileBean.getName()).filter(out.toByteArray(),Basic.getKeywords());
                        } catch (Exception e) {
                            logger.error("文件字符集编码不匹配",e); //To change body of catch statement use File | Settings | File Templates.
                            return flag=false;
                        }
                        flag =  sourceOperation.process(data,fileBean);
                        if (flag==false){
                            post=-1;
                            break;
                        }else {
                            post=0;
                            out.reset();
                            out.write(bytes,0,len);
                            post+=len;
                        }
                    } else {
                        out.write(bytes, 0, len);
                        post+=len;
                    }
                }
                if(post!=-1){
                    fileBean.setSyncflag(FileContext.Str_SyncFileEnd);
                    logger.debug(" [FTP] out.toByteArray().length ==   "+out.toByteArray().length);
                    byte[] data = null ;
                    try {
                        data = KeywordsFilterFactory.getKeywordsFilterUtil(fileBean.getName()).filter(out.toByteArray(),Basic.getKeywords());
                    } catch (Exception e) {
                        logger.error("文件字符集编码不匹配",e);  //To change body of catch statement use File | Settings | File Templates.
                        return flag=false;
                    }
                    flag=sourceOperation.process(data,fileBean);
                }
                in.close();
            } catch (IOException e) {
                logger.error(e);
                return flag=false;
            }
        }
        else {
            try {
                while ((len=in.read(bytes))!=-1){
                    if(post>=sourceFile.getPacketsize()*MB){
                        fileBean.setSyncflag(FileContext.Str_SyncFileStart);
                        fileSize+=post;
                        flag =  sourceOperation.process(out.toByteArray(),fileBean);
                        if (flag==false){
                            post=-1;
                            break;
                        }else {
                            post=0;
                            out.reset();
                            out.write(bytes,0,len);
                            post+=len;
                        }
                    } else {
                        out.write(bytes, 0, len);
                        post+=len;
                    }
                }
                if(post!=-1){
                    fileBean.setSyncflag(FileContext.Str_SyncFileEnd);
                    logger.debug(" [FTP] out.toByteArray().length ==   "+out.toByteArray().length);
                    flag=sourceOperation.process(out.toByteArray(),fileBean);
                }
                in.close();
            } catch (IOException e) {
                logger.error(e);
                return flag=false;
            }
        }
        return flag;
    }
    /*  public static  void main(String[] arg){
 FTPClient ftpClient=null;
 try {
     ftpClient = new FTPClient();
     ftpClient.setControlEncoding("gbk");
     ftpClient.setDefaultPort(21);
     ftpClient.setDataTimeout(60000);       //设置传输超时时间为60秒
     ftpClient.setConnectTimeout(60000);       //连接超时为60秒
     try{
         ftpClient.connect("127.0.0.1");
     }catch (IOException e){
         logger.info("源端连接异常", e);
     }
     int i=0;
    *//* while (!ftpClient.isConnected()&&i<5){
                 i++;
                 try {
                     Thread.sleep(1000*60);
                 } catch (InterruptedException ee) {
                     logger.error(ee);
                 }
                 ftpClient=null;
                 connectServer();

             }*//*
             ftpClient.setKeepAlive(true);
             ftpClient.setRemoteVerificationEnabled(true);
             ftpClient.login("root", "root");
             ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

//                设置被动模式
             ftpClient.enterLocalPassiveMode();



         } catch (SocketException e) {
             logger.error("登录源端ftp服务器127.0.0.1失败,连接超时！",e);
         } catch (IOException e) {
             logger.error("登录源端ftp服务器127.0.0.1失败，FTP服务器无法打开！",e);
         }
         try {
             InputStream inputStream=  ftpClient.retrieveFileStream("Ubuntu");
            String[] ftpClients=  ftpClient.listNames();
             FTPFile[] ftpFiles=ftpClient.listFiles();
             System.out.println("inputStream  ==  "+ inputStream);
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }*/

}

