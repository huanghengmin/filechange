package com.fartec.ichange.plugin.filechange.source.plugin.webdav;

import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hhm
 * Date: 12-4-7
 * Time: 下午8:41
 *过滤文件的类
 */

public class WebDavFilterUtils {

    private Logger logger = Logger.getLogger(WebDavFilterUtils.class);
    private WebDavUtils webDavUtils = new WebDavUtils();

    /**
     * 文件过滤方法
     * @param sourceFile    源端配置文件
     * @param resource      源端服务器资源
     * @return      通过滤的构建成的fileBean对象
     */
    public boolean fileBeanFilter(String fileName,SourceFile sourceFile){
        //定义标识位
        boolean flag=false;
        //接收能过滤的类型字符串
        String canFilterFileTypes=sourceFile.getFiltertypes();
        //接收不能过滤类型字符串
        String notCanFilterFileTypes=sourceFile.getNotfiltertypes();
        //能够过滤的类型
        String[] canFilterTypes=null;
        //不能够过滤的类型
        String[] notCanFilterTypes=null;
        //可过滤文件类型
        if(canFilterFileTypes!=null){
            canFilterTypes=canFilterFileTypes.split(",");
        }
        //不可过滤文件类型
        if(notCanFilterFileTypes!=null){
            notCanFilterTypes=notCanFilterFileTypes.split(",");
        }
        //如果过滤文件没有配置则跳出
        if(canFilterFileTypes.equals("")&&notCanFilterFileTypes.equals("")){
            logger.info("***没有配置过滤文件类型!请设置过滤类型后重试!***");
            return  false;
        } else if(canFilterFileTypes.equals("")){
                if(!notCanFilterFileTypes.equals("")) {
                    flag=true;
                    if(notCanFilterTypes!=null){
                        for (int j=0;j<notCanFilterTypes.length;j++){
                            if(!notCanFilterTypes[j].equals("")) {
                                if(webDavUtils.getFileName(notCanFilterTypes[j]).equals("*")){
                                    if(webDavUtils.getFileType(notCanFilterTypes[j]).equals("*")) {
                                        return false;

                                    }else {
                                        String  type= webDavUtils.getFileType(notCanFilterTypes[j]);
                                        String fileType= webDavUtils.getFileType(fileName);
                                        if(fileType.equals(type)) {
                                            return false;
                                        }
                                    }
                                }else{
                                    String  filename= webDavUtils.getFileName(notCanFilterTypes[j]);
                                    String  fileBeanName= webDavUtils.getFileName(fileName);
                                    if(filename.equals(fileBeanName)){
                                        if(webDavUtils.getFileType(notCanFilterTypes[j]).equals("*")){
                                            return false;
                                        }else{
                                            String  filenameType= webDavUtils.getFileType(notCanFilterTypes[j]);
                                            String  fileBeanNameType= webDavUtils.getFileType(fileName);
                                            if(filenameType.equals(fileBeanNameType)){
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
        }else {
           //表示切割后的字符数组
            if(canFilterTypes!=null){
                for (int i=0;i<canFilterTypes.length;i++){
                    if(!canFilterTypes[i].equals("")) {
                        if(webDavUtils.getFileName(canFilterTypes[i]).equals("*")){
                            if(webDavUtils.getFileType(canFilterTypes[i]).equals("*")) {
                                  flag=true;
                              }else{
                                String type= webDavUtils.getFileType(canFilterTypes[i]);
                                String fileType= webDavUtils.getFileType(fileName);
                                if(type.equals(fileType)){
                                    flag=true;
                                }
                            }
                        }else{
                            String  filename= webDavUtils.getFileName(canFilterTypes[i]) ;
                            String  fileBeanName= webDavUtils.getFileName(fileName);
                            if(filename.equals(fileBeanName)){
                                if(webDavUtils.getFileType(canFilterTypes[i]).equals("*")) {
                                       flag=true;
                                }else{
                                    String  filenameType= webDavUtils.getFileType(canFilterTypes[i]) ;
                                    String  fileBeanNameType= webDavUtils.getFileType(fileName);
                                    if(filenameType.equals(fileBeanNameType)){
                                        flag=true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(notCanFilterTypes!=null){
                for (int j=0;j<notCanFilterTypes.length;j++){
                    if(!notCanFilterTypes[j].equals("")) {
                        if(webDavUtils.getFileName(notCanFilterTypes[j]).equals("*")){
                            if(webDavUtils.getFileType(notCanFilterTypes[j]).equals("*")) {
                                return false;
                            }else {
                                String  type= webDavUtils.getFileType(notCanFilterTypes[j]);
                                String fileType= webDavUtils.getFileType(fileName);
                                if(fileType.equals(type)) {
                                    return false;
                                }
                            }
                        }else{
                            String  filename= webDavUtils.getFileName(notCanFilterTypes[j]);
                            String  fileBeanName= webDavUtils.getFileName(fileName);
                            if(filename.equals(fileBeanName)){
                                if(webDavUtils.getFileType(notCanFilterTypes[j]).equals("*")){
                                    return false;
                                }else{
                                    String  filenameType= webDavUtils.getFileType(notCanFilterTypes[j]);
                                    String  fileBeanNameType= webDavUtils.getFileType(fileName);
                                    if(filenameType.equals(fileBeanNameType)){
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return flag;
    }
}
