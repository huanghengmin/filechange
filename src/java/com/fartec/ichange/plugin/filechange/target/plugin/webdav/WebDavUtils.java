package com.fartec.ichange.plugin.filechange.target.plugin.webdav;

import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.inetec.common.config.nodes.TargetFile;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
     * 得到目标端未完成文件路径
     * @param fileBean        文件对象
     * @param targetFile              目标端配置文件
     * @return
     */
    public String getFilePath(FileBean fileBean,TargetFile targetFile){
        String fileName =  fileBean.getName().replaceFirst(FileContext.Str_Sync,"");
        fileName = fileName.substring(0,fileName.lastIndexOf("_"));
        return getTargetHostAndName(targetFile)+urlEncoder(judgeWorkDir(targetFile.getDir()),targetFile)+urlEncoder(getFileDir(fileBean),targetFile)+"/"+urlEncoder(fileName,targetFile);
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
     * 替换最后一个标识字符串为另外的字符串
     * @param source                源字符串
     * @param sourceSeparator            源字符串标记
     * @param targetSeparator                    目标字符串标记
     * @return
     */
    public String replaceOtherSeparator(String source,String sourceSeparator,String targetSeparator){
        StringBuilder sb=new StringBuilder("");
        int last = 0;
        if(source.contains(sourceSeparator)){
            last = source.lastIndexOf(sourceSeparator);
        }
        sb.append(source.substring(0,last)).append(targetSeparator).append(source.substring(last+sourceSeparator.length(),source.length()));
        return sb.toString();
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
     * 解码URL
     * @param dir   需要解码的路径
     * @param targetFile       目标端配置文件
     * @return
     */
    public String urlDecoder(String dir,TargetFile targetFile){
        String url=null;
        try {
            url = URLDecoder.decode(dir,targetFile.getCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 对路径进行URL转码操作
     * @param dir   需要转码的文件路径
     * @return       进行URL转码后的文件路径
     */
    public String urlEncoder(String dir,TargetFile targetFile){
        String encodeUrl="/";
        if(!dir.equals("")&&dir!=null){
            if(dir.contains("/")){
                String [] strings=dir.split("/");
                if(strings!=null){
                    for (int i=0;i<strings.length;i++){
                        if(!strings[i].equals("")&&strings[i]!=null) {
                            if(i!=(strings.length-1)){
                                try {
                                    encodeUrl+= URLEncoder.encode(strings[i],targetFile.getCharset())+"/";
                                } catch (UnsupportedEncodingException e) {
                                    logger.error(e.getMessage()+"url转码报错");
                                }
                            }else{
                                if(dir!=null){
                                    try {
                                        encodeUrl+=URLEncoder.encode(strings[i],targetFile.getCharset());
                                    }catch (UnsupportedEncodingException e) {
                                        logger.error(e.getMessage()+"url转码报错");
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                try {
                    encodeUrl=URLEncoder.encode(dir,targetFile.getCharset());
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage()+"url转码报错");
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
     * 得到目标端的主机名和地址
     * @param targetFile  目标端配置文件
     * @return        返回目标端请求根路径
     */
    public String getTargetHostAndName(TargetFile targetFile){
        return  "http://"+targetFile.getServerAddress()+":"+targetFile.getPort();
    }

}
