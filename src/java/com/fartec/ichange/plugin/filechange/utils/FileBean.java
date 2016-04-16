package com.fartec.ichange.plugin.filechange.utils;

import com.inetec.common.exception.Ex;
import com.fartec.ichange.plugin.filechange.exception.EFile;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.jdom.Element;
import org.jdom.DataConversionException;
import org.apache.commons.codec.binary.Base64;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-5
 * Time: 6:37:07
 * To change this template use File | Settings | File Templates.
 */
public class FileBean {
    public static final String Str_Time = "time";
    public static final String Str_FileSize = "size";
    public static final String Str_FileMd5 = "md5";
    public static final String Str_FileFlag = "file_flag";
    public static final String Str_FilePostLocation = "filepostlocation";
    public static final String Str_FileFullName = "filefullname";
    public static final String Str_IsFile = "isfile";
    public static final String Str_FileName = "name";
    public static final String Str_SourceFile = "source";
    public static final String Str_TargetFile = "target";
    private String fullname;
    private long filesize;
    private String md5;
    private boolean isFile = true;
    /**
     * 同步标记：值为:
     */
    private String syncflag; //todo：add syncFlag;

    /**
     * 文件游标位置
     */
    public long filepostlocation;
    private String name;

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    private long time;
    /**
     * 文件标记，取值为(source :源端文件,target：目标文件)
     */
    private String file_flag;

    public String getSyncflag() {
        return syncflag;
    }

    public void setSyncflag(String syncflag) {
        this.syncflag = syncflag;
    }

    public long getFilepostlocation() {
        return filepostlocation;
    }

    public void setFilepostlocation(long filepostlocation) {
        this.filepostlocation = filepostlocation;
    }

    public String getFile_flag() {
        return file_flag;
    }

    public void setFile_flag(String file_flag) {
        this.file_flag = file_flag;
    }


    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String toXml() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<file " + Str_FileName + "=\"");
        buffer.append(encode(name));
        buffer.append("\" " + Str_Time + "=\"");
        buffer.append(time);
        buffer.append("\" " + Str_FileSize + "=\"");
        buffer.append(filesize);
        buffer.append("\" " + Str_FileFlag + "=\"");
        buffer.append(file_flag);
        //todo: add syncfile flag;
        buffer.append("\" " + FileContext.Str_SyncFileFlag + "=\"");
        buffer.append(syncflag);
        buffer.append("\" " + Str_FilePostLocation + "=\"");
        buffer.append(filepostlocation);
        buffer.append("\" " + Str_FileMd5 + "=\"");
        buffer.append(md5);
        buffer.append("\">");
        buffer.append(encode(fullname));
        buffer.append("</file>\r\n");
        return buffer.toString();
    }

    public static FileBean xmlToBean(Element node) throws Ex {
        FileBean result = new FileBean();
        result.setName(result.decode(node.getAttribute(Str_FileName).getValue()));
        try {
            result.setTime(node.getAttribute(Str_Time).getLongValue());
            result.setFilesize(node.getAttribute(Str_FileSize).getLongValue());
            result.setMd5(node.getAttribute(Str_FileMd5).getValue());
            result.setFile_flag(node.getAttribute(Str_FileFlag).getValue());
            result.setSyncflag(node.getAttribute(FileContext.Str_SyncFileFlag).getValue());
            result.setFilepostlocation(node.getAttribute(Str_FilePostLocation).getLongValue());
            result.setFullname(result.decode(node.getValue()));
        } catch (DataConversionException e) {
            throw new Ex().set(EFile.E___DATA_IS_NULL_ERRORR, e);
        }
        // result.setFullname(result.decode(node.getValue()));
        return result;
    }

    public static FileBean fileToBean(File file, String workdir) {
        FileBean result = new FileBean();
        result.setFullname(FileFilter.getEndSubString(file.getAbsolutePath(), workdir));
        String filename = file.getAbsolutePath();
        filename.replace('\\', '/');
        result.setName(FileFilter.getEndSubString(filename, "/"));
        result.setTime(file.lastModified());
        result.setFilesize(file.getUsableSpace());
        return result;
    }

    public String encode(String value) {
        return new String(new Hex().encode(value.getBytes()));
    }

    public String decode(String value) {
        try {
            return new String(new Hex().decode(value.getBytes()));
        } catch (DecoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
