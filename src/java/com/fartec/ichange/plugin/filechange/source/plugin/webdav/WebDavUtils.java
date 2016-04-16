package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Int IDEA.
 * User: hhm
 * Date: 12-4-19
 * Time: 上午8:35
 * To change this template use File | Settings | File Templates.
 */
public class WebDavUtils {

    private static Logger logger=Logger.getLogger(WebDavUtils.class);

    /**
     * 得到link文件路径
     * @param fileBean  文件对象
     * @param sourceFile        源端配置文件
     * @return
     */
    public String getLinkFilePath(FileBean fileBean,SourceFile sourceFile){
        String path =  getSourceHostAndName(sourceFile)+judgeWorkDir(sourceFile.getDir()) + getFileDir(fileBean);
       if(path.endsWith("/")){
           try {
               path = path  + FileContext.Str_Sync+URLEncoder.encode(getFileBeanName(fileBean),sourceFile.getCharset())+"_"+fileBean.getTime()+FileContext.Str_Lnk;
           } catch (UnsupportedEncodingException e) {
              logger.info("服务器不支持url转码操作");
           }
       }else{
           try {
               path =   path  +"/"+ FileContext.Str_Sync+URLEncoder.encode(getFileBeanName(fileBean),sourceFile.getCharset())+"_"+fileBean.getTime()+FileContext.Str_Lnk;
           } catch (UnsupportedEncodingException e) {
               logger.info("服务器不支持url转码操作");
           }
       }
        return  path.replaceAll("\\+","%20");
    }

    /**
     * 构建link文件
     * @param fileBean 文件对象
     * @param sardine          源端操作对象
     * @param sourceFile                 源端配置文件
     */
    public void createLinkFile(FileBean fileBean,Sardine sardine,SourceFile sourceFile){
        String newPath = getLinkFilePath(fileBean,sourceFile);
        try {
            sardine.put(newPath,new String("").getBytes());
        } catch (IOException e) {
            logger.info("构建空标识文件出错!"+fileBean.getFullname());
        }
    }

    /**
     * 删除空标识文件
     * @param fileBean  文件对象
     * @param sardine    源端操作对象
     * @param sourceFile   源端配置文件
     */
    public void deleteLinkFile(FileBean fileBean,Sardine sardine,SourceFile sourceFile) {
        String newPath = getLinkFilePath(fileBean,sourceFile);
        try {
            sardine.delete(newPath);
        } catch (IOException e) {
            logger.info("删除空标识文件出错!"+fileBean.getFullname());
        }
    }

    /**
     * 得到syncFile中的时间
     * @param fileBean    文件对象
     * @return 返回文件最后被修改的时间
     */
    public long getSyncIngFileTime(String  fileName){
        if(fileName.startsWith(FileContext.Str_Sync)){
            if(fileName.endsWith(FileContext.Str_Lnk)){
                String path =  fileName.substring(0,fileName.lastIndexOf("."));
                String time =  path.substring(path.lastIndexOf("_")+1,path.length());
                return Long.parseLong(time);
            }
        }
        return -1;
    }

    /**
     * 得到fileBean的文件名
     * @param fileBean   文件对象
     * @return
     */
    public String getFileBeanName(FileBean fileBean){
        String name = fileBean.getFullname();
        if(name.contains("/")){
            name = name.substring(name.lastIndexOf("/")+1,name.length()) ;
        }
        return name;
    }

    /**
     * 得到对象路径
     * @param resource  服务器资源对象
     * @return    返回服务器对应路径
     */
    public String getCorrespondingLinkFilePath(DavResource resource,SourceFile sourceFile){
        String path = null;
        String href = null;      //中文的转码后的href，可能含有主机名和端口号,可能没有
        try {
            href = URLDecoder.decode(resource.getHref().toString().replaceAll("\\+","%2B"),sourceFile.getCharset());
        } catch (UnsupportedEncodingException e) {
            logger.info("******服务器不支持url转码操作*****");
        }
        if(!href.contains("http://"))  {
            path = getSourceHostAndName(sourceFile) + href;
        } else {
            path = href;
        }
        return path;
    }

    /**
     * 转码后的url
     * @param resource 源端资源文件
     * @param sourceFile       源端配置文件
     * @return         返回转码后的文件路径
     */
    public String encodeCorrespondingLinkFilePath(DavResource resource,SourceFile sourceFile){
        String path = getCorrespondingLinkFilePath(resource,sourceFile);
        return  getSourceHostAndName(sourceFile)+urlEncoder(path.replaceFirst(getSourceHostAndName(sourceFile),""),sourceFile) ;
    }

    /**
     * 得到对应文件路径
     * @param resource 资源名称
     * @param sourceFile     源端配置文件
     * @return
     */
    public String  getCorrespondingFilePath(DavResource resource,SourceFile sourceFile){
        String sourceFileName = getDavResourceName(resource,sourceFile) ;
        String path = getCorrespondingLinkFilePath(resource,sourceFile);
        if(path.contains("/")){
            path = path.substring(0,path.lastIndexOf("/"));
        }
        if(path.endsWith("/")){
            path =  path + sourceFileName;
        }  else {
            path = path+"/"+sourceFileName;
        }
        return path;
    }

    /**
     * 得到同步文件名
     * @param resource  资源文件
     * @param sourceFile      源端配置文件
     * @return
     */
    public String  getSyncIngFileName (DavResource resource,SourceFile sourceFile){
          String sourceFileName = getDavResourceName(resource,sourceFile)  ;
          sourceFileName = sourceFileName.substring(0,sourceFileName.lastIndexOf("_"));
          return  sourceFileName.replaceFirst(FileContext.Str_Sync,"");
    }

    /**
     * 构建文件到fileBean
     * @param resource
     * @param sourceFile
     * @return
     */
    public FileBean buildSyncIngFile(DavResource resource,SourceFile sourceFile,Sardine sardine){
        String name = getSyncIngFileName(resource,sourceFile);
        String path = getCorrespondingLinkFilePath(resource,sourceFile);
        if(path.contains("/")){
            path = path.substring(0,path.lastIndexOf("/"));
        }
        if(path.endsWith("/")){
            path =  path + name;
        }  else {
            path = path+"/"+name;
        }
        try {
            List<DavResource> resources =  sardine.list(path);
            if(resources.size()>0){
                DavResource resource1 = resources.iterator().next();
                FileBean fileBean =new FileBean();
                fileBean.setName(name);
                fileBean.setFullname(path.replace( getSourceHostAndName(sourceFile) + judgeWorkDir(sourceFile.getDir()),""));
                fileBean.setFilesize(resource1.getContentLength());
                fileBean.setTime(resource1.getModified().getTime());
                return fileBean;
            }
        } catch (IOException e) {
            logger.info("不存在此文件");
        }
        return  null;
    }

    
    public String getSyncName(String fileName,SourceFile sourceFile){
        String name =  fileName.substring(0,fileName.lastIndexOf("_"));
        return name.replaceFirst(FileContext.Str_Sync,"");
    }
    
    /**
     * 检查文件是否被 修改过，如果修改过直接删除标识文件
     * @param fileBean  标识文件对象
     * @param sourceFile  源端配置文件
     * @param sardine  源端操作对象
     * @param time   修改 时间
     * @return   返回文件是否被修改的标识
     */
     public boolean judeFileModify(DavResource resource,SourceFile sourceFile,Sardine sardine){
         boolean  flag =false;
         String path = getCorrespondingFilePath(resource,sourceFile);
         String resourceName = getDavResourceName(resource,sourceFile);
         long time = getSyncIngFileTime(resourceName);
         if(path.contains("/")){
             path = path.substring(0,path.lastIndexOf("/")+1);
         }
         path = path+getSyncName(resourceName,sourceFile);
         try {
             if(sardine.exists(path)){
                   List<DavResource> resources = sardine.list(path);
                  if(resources.size()>0){
                      DavResource resource1 = resources.iterator().next();
                      long  rTime =   resource1.getModified().getTime() ;
                      if(time ==rTime){
                          flag=true;
                      }
                 }
             }
         } catch (IOException e) {
             logger.info("源端不存在此文件");
             return false;
         }
         return flag;
     }

    /**
     * 替换最后一个separator
     * @param source    源字符串
     * @param separator    替换的字符串
     * @return
     */
     public String replaceEnd(String source,String separator) {
         StringBuilder sb=new StringBuilder("");
         int last = 0;
          if(source.contains(separator)){
               last = source.lastIndexOf(separator);
          }
         sb.append(source.substring(0,last)).append(source.substring(last+separator.length(),source.length()));
         return sb.toString();
     }

    /**
     * 得到服务器上资源文件名
     * @param resource   资源名称
     * @param sourceFile    源端配置文件
     */
    public String getDavResourceName(DavResource resource, SourceFile sourceFile){
         String href = urlDecoder(resource.getHref().toString(),sourceFile);
         String fileName=null;
         if(href.contains("/")){
             fileName = href.substring(href.lastIndexOf("/")+1,href.length());
         }  else {
             fileName = href;
         }
      return   fileName;
  }

    /**
     * 得到不包括文件类型的文件名称
     * @param fileName  文件全名
     * @return   返回文件名称不包括文件类型
     */
     public String getFileName(String fileName){
        String fName=null;
        if(fileName.contains(".")){
            fName=fileName.substring(0,fileName.lastIndexOf("."));
        }else{
            fName=fileName;
        }
        return fName;
     }

    /**
     * 解码URL
     * @param dir  需要解码的url路径
     * @param sourceFile      源端配置文件
     * @return         解码后的url
     */
     public String urlDecoder(String dir,SourceFile sourceFile){
        String url=null;
        try {
            url = URLDecoder.decode(dir,sourceFile.getCharset());
        } catch (UnsupportedEncodingException e) {
            logger.info(e.getMessage());
        }
        return url;
     }

    /**
     * 得到文件的目录
     * @param fileBean  文件对象
     * @return          文件目录
     */
     public String getFileDir(FileBean fileBean){
        String dir=null;
        if(fileBean.getFullname().contains("/")){
            dir=fileBean.getFullname().substring(0,fileBean.getFullname().lastIndexOf("/"));
        }else{
            dir="";
        }
        return dir;
     }
    
    /**
     * 得到不包括文件名文件类型
     * @param fileName  文件全名
     * @return          不包括文件类型的文件名
     */
     public String getFileType(String fileName){
        String fType=null;
        if(fileName.contains(".")){
            fType=fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
        }else{
            fType="";
        }
        return  fType;
    }

    /**
     * 对路径进行URL转码操作
     * @param dir   需要转码的文件路径
     * @return       进行URL转码后的文件路径
     */
    public String urlEncoder(String dir,SourceFile sourceFile){
        String encodeUrl="/";
        if(!dir.equals("")&&dir!=null){
        if(dir.contains("/")){
            String [] strings=dir.split("/");
            if(strings!=null){
                for (int i=0;i<strings.length;i++){
                    if(!strings[i].equals("")&&strings[i]!=null) {
                        if(i!=(strings.length-1)){
                            try {
                                encodeUrl+= URLEncoder.encode(strings[i],sourceFile.getCharset())+"/";
                            } catch (UnsupportedEncodingException e) {
                                logger.error(e.getMessage()+"url转码报错!!!");
                            }
                        }else{
                            if(dir!=null){
                                try {
                                    encodeUrl+=URLEncoder.encode(strings[i],sourceFile.getCharset());
                                }catch (UnsupportedEncodingException e) {
                                    logger.error(e.getMessage()+"url转码报错!!!");
                                }
                            }
                        }
                    }
                }
            }
        }else{
            try {
                encodeUrl=URLEncoder.encode(dir,sourceFile.getCharset());
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage()+"url转码报错!!!");
            }
          }
        }
        return  encodeUrl;
    }

    /**
     * 格式化工作空间
     * @param workDir  传入的工作空间
     * @return      返回格式化后的工作空间
     */
    public String judgeWorkDir(String workDir){
        if(workDir.startsWith("/")){
            if(workDir.endsWith("/")){
                return  workDir.substring(0,workDir.length()-1);
            }else{
                return  workDir;
            }
        }else {
            if(workDir.endsWith("/")){
                return  "/"+workDir.substring(0,workDir.length()-1);
            }else{
                return "/"+workDir;
            }
        }
    }

    /**
     * 得到源端的主机名和地址
     * @param sourceFile 源端配置文件
     * @return       返回源端请求根路径
     */
    public String getSourceHostAndName(SourceFile sourceFile){
        return  "http://"+sourceFile.getServerAddress()+":"+sourceFile.getPort();
    }
}
