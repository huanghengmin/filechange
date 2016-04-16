package com.fartec.ichange.plugin.filechange.utils;

import com.fartec.ichange.plugin.filechange.exception.EFile;
import com.inetec.common.exception.Ex;
import com.inetec.common.i18n.Message;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-5
 * Time: 6:40:48
 * To change this template use File | Settings | File Templates.
 */
public class FileList {
    public static final String Str_XmlHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n <root>\r\n";
    public static final String Str_XmlEnd = " </root>";
    public static final String Str_FileList = "filelist";
    private Map filemap = new HashMap();
    private long time;
    private String syncFileListFlag = FileContext.Str_SyncFileListStart;
    private boolean syncFileListDD = false;
    private String syncFileListType = FileContext.Str_SyncFileListType_Normal;
    private long syncFileListTotal = 0;

    public long getSyncFileListTotal() {
        return syncFileListTotal;
    }

    public void setSyncFileListTotal(long syncFileListTotal) {
        this.syncFileListTotal = syncFileListTotal;
    }

    public String getSyncFileListFlag() {
        return syncFileListFlag;
    }

    public void setSyncFileListFlag(String syncFileListFlag) {
        this.syncFileListFlag = syncFileListFlag;
    }

    public String getSyncFileListType() {
        return syncFileListType;
    }

    public void setSyncFileListType(String syncFileListType) {
        this.syncFileListType = syncFileListType;
    }

    public boolean getSyncFileListDD() {
        return syncFileListDD;
    }

    public void setSyncFileListDD(boolean syncFileListDD) {
        this.syncFileListDD = syncFileListDD;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void addFileBean(FileBean file) {
        filemap.put(file, file);
    }

    public FileBean getFileBean(String name) {
        return (FileBean) filemap.get(new File(name));
    }

    public Iterator<FileBean> iterable() {
        return (Iterator<FileBean>) filemap.keySet().iterator();
    }

    public String toXml() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Str_XmlHead);
        buffer.append("<filelist time=\"");
        buffer.append(time);
        buffer.append("\" ");
        buffer.append(FileContext.Str_SyncFileListFlag);
        buffer.append("=\"");
        buffer.append(syncFileListFlag);
        buffer.append("\" ");
        buffer.append(FileContext.Str_SyncFileListType);
        buffer.append("=\"");
        buffer.append(syncFileListType);
        buffer.append("\" ");
        buffer.append(FileContext.Str_SyncFileListTotal);
        buffer.append("=\"");
        buffer.append(syncFileListTotal);
        buffer.append("\" ");
        buffer.append(FileContext.Str_SyncFileListDD);
        buffer.append("=\"");
        buffer.append(syncFileListDD);
        buffer.append("\">");
        Iterator it = filemap.values().iterator();
        while (it.hasNext()) {
            FileBean file = (FileBean) it.next();
            buffer.append(file.toXml());
        }
        buffer.append("</filelist>\r\n");
        buffer.append(Str_XmlEnd);
        return buffer.toString();
    }

    public static FileList xmlToBean(Element node) throws Ex {
        FileList result = new FileList();
        Element filelist = node.getChild("filelist");
        try {
            result.setTime(filelist.getAttribute("time").getLongValue());
            result.setSyncFileListFlag(filelist.getAttribute(FileContext.Str_SyncFileListFlag).getValue());
            result.setSyncFileListType(filelist.getAttribute(FileContext.Str_SyncFileListType).getValue());
            result.setSyncFileListTotal(filelist.getAttribute(FileContext.Str_SyncFileListTotal).getLongValue());
            result.setSyncFileListDD(filelist.getAttribute(FileContext.Str_SyncFileListDD).getBooleanValue());
            List fileBeanList = filelist.getChildren("file");
            Iterator filebeanIt = fileBeanList.iterator();
            while (filebeanIt.hasNext()) {
                Element filebean = (Element) filebeanIt.next();
                result.addFileBean(FileBean.xmlToBean(filebean));
            }
        } catch (DataConversionException e) {
            throw new Ex().set(EFile.E_DataFormatError, e);
        }
        return result;
    }

    public static FileList stringToFileList(String data) throws Ex {
        SAXBuilder builder = new SAXBuilder(false);
        data.trim();
        Document doc = null;
        try {
            doc = builder.build(new StringReader(data));
        } catch (JDOMException e) {
            throw new Ex().set(EFile.E_DataFormatError, e);
        } catch (IOException e) {
            throw new Ex().set(EFile.E_DataFormatError, e);
        }
        Element action = doc.getRootElement();
        return xmlToBean(action);
    }

    public int size() {
        return filemap.size();
    }

    public void clear() {
        filemap.clear();
    }
}
