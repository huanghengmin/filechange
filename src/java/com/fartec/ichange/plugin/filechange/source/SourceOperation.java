package com.fartec.ichange.plugin.filechange.source;

import com.fartec.ichange.plugin.filechange.FileChangeSource;
import com.fartec.ichange.plugin.filechange.exception.EFile;
import com.fartec.ichange.plugin.filechange.source.plugin.ISourceProcess;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.fartec.ichange.plugin.filechange.utils.ProcessFactory;
import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.exception.E;
import com.inetec.common.exception.Ex;
import com.inetec.common.i18n.Message;
import com.inetec.common.io.ByteArrayBufferedInputStream;
import com.inetec.common.io.IOUtils;
import com.inetec.common.logs.LogHelper;
import com.inetec.ichange.api.DataAttributes;
import com.inetec.ichange.api.EStatus;
import com.inetec.ichange.api.IChangeMain;
import com.inetec.ichange.api.IChangeType;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-3
 * Time: 5:37:37
 * To change this template use File | Settings | File Templates.
 */
public class SourceOperation implements Runnable {

    Logger auditLogger = Logger.getLogger("ichange.audit.file");
    public static Logger m_logger = Logger.getLogger(SourceOperation.class.getName());
    private boolean m_bTrusted = false;
    private String appType = null;
    public final static int Int_Thread_SleepTime = 10 * 1000;
    public final static int Int_ConnectionRetry_SleepTime = 5 * 60 * 1000;
    protected IChangeType m_changeType = null;
    protected IChangeMain m_changeMain;
    public LogHelper m_log = null;
    private boolean isRun = false;
    private boolean m_configed = false;
    private SourceFile m_config = null;
    private ISourceProcess sourceProcess = null;

    public boolean isRun() {
        return isRun;
    }

    public void stop() {
        isRun = false;
    }

    public void config(SourceFile file) {
        m_config = file;
        sourceProcess = ProcessFactory.getSourceProcess(file.getProtocol());
        if (sourceProcess != null) {
            m_configed = true;
            sourceProcess.init(this, m_config);
        }
    }


    public SourceOperation(IChangeMain dc, IChangeType type) {
        m_changeMain = dc;
        m_changeType = type;
        m_log = dc.createLogHelper();
        m_log.setAppType(FileChangeSource.Str_AppType);
        m_log.setAppName(type.getType());
        m_log.setSouORDes(LogHelper.Str_souORDes_Source);
        appType = m_changeType.getType();
        String trusted = System.getProperty("privatenetwork");
        if (trusted != null) {
            trusted.toLowerCase();
            if (trusted.equalsIgnoreCase("true"))
                m_bTrusted = true;

        }
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("privatenetwork = " + String.valueOf(m_bTrusted));
        }
    }

    public void run() {
        if (sourceProcess != null) {
            new Thread(sourceProcess).start();
            isRun = true;
        }
        while (isRun) {
            try {
                Thread.sleep(Int_Thread_SleepTime);
            } catch (InterruptedException e) {
                //okay
            }
        }
        isRun = false;
    }

    public boolean process(InputStream in, FileBean bean) {
        if (in == null) {
            m_logger.warn("Process InputStream is null.filebean filename:" + bean.getName() + " for Type name:" + m_changeType.getType());
            return false;
        }
        boolean result = false;
        DataAttributes da = new DataAttributes();
        da.setProperty(FileContext.Str_SyncFileCommand, FileContext.Str_SyncFile);
        da.setProperty(FileContext.Str_SyncFileFlag, bean.getSyncflag());
        da.setProperty(FileContext.Str_SyncFileName, bean.encode(bean.getName()));
        da.setProperty(FileContext.Str_SyncFilePost, "" + bean.getFilepostlocation());
        da.setProperty(FileContext.Str_SyncFileMD5, "" + bean.getMd5());
        da.setProperty(FileContext.Str_SyncFileSize, "" + bean.getFilesize());
        da.setProperty(FileContext.Str_SyncFile_Flag, bean.getFile_flag());
        da.setProperty(FileContext.Str_SyncFileFullName, bean.encode(bean.getFullname()));
        try {
            byte[] temp = IOUtils.readByteArrayNoAvailabel(in);
            da.setProperty(FileContext.Str_SyncFileDataSize, "" + temp.length);
            da.setResultData(temp);
            DataAttributes res = commandProcessIsReturn(da);
            result = res.getStatus().isSuccess();
            auditProcess(FileContext.Str_SyncFile, bean.getName(), temp.length, res.getStatus().isSuccess());
        } catch (Ex ex) {
            m_logger.warn("File change type:" + appType + " process file sync data error.", ex);
        } catch (IOException e) {
            m_logger.warn("File change type:" + appType + " process file sync data error.", e);
        }
        return result;
    }

    public boolean process(byte[] data, FileBean bean) {
        if (data == null && data.length == 0) {
            m_logger.warn("Process data is null or data length equel 0 .filebean filename:" + bean.getName() + " for Type name:" + m_changeType.getType());
            return false;
        }
        boolean result = false;
        DataAttributes da = new DataAttributes();
        da.setProperty(FileContext.Str_SyncFileCommand, FileContext.Str_SyncFile);
        da.setProperty(FileContext.Str_SyncFileFlag, bean.getSyncflag());
        da.setProperty(FileContext.Str_SyncFileName, bean.encode(bean.getName()));
        da.setProperty(FileContext.Str_SyncFilePost, "" + bean.getFilepostlocation());
        da.setProperty(FileContext.Str_SyncFileMD5, "" + bean.getMd5());
        da.setProperty(FileContext.Str_SyncFileSize, "" + bean.getFilesize());
        da.setProperty(FileContext.Str_SyncFileFullName, bean.encode(bean.getFullname()));
        da.setProperty(FileContext.Str_SyncFile_Flag, bean.getFile_flag());
        da.setProperty(FileContext.Str_SyncFileDataSize, "" + data.length);
        da.setResultData(data);
        try {
            DataAttributes res = commandProcessIsReturn(da);
            result = res.getStatus().isSuccess();
            auditProcess(FileContext.Str_SyncFile, bean.getName(), data.length, res.getStatus().isSuccess());
        } catch (Ex ex) {
            m_logger.warn("File change type:" + appType + " process file sync data error.", ex);
        }
        return result;
    }

    public FileList procesFileList(FileList list) {
        if (list == null && list.size() == 0) {
            m_logger.warn("Process Filelist is null or Filelist length equel 0 .Type name:" + m_changeType.getType());
            return list;
        }
        FileList result = null;

        DataAttributes da = new DataAttributes();

        da.setProperty(FileContext.Str_SyncFileCommand, FileContext.Str_SyncFileList);
        da.setProperty(FileContext.Str_SyncFileListTotal, "" + list.getSyncFileListTotal());
        da.setResultData(new FileBean().encode(list.toXml()).getBytes());
        try {
            DataAttributes res = commandProcessIsReturn(da);
            if (res.getStatus().isSuccess()) {
                result = FileList.stringToFileList(new String(IOUtils.toByteArray(res.getResultData())));
            }
            auditProcess(FileContext.Str_SyncFileList, "", list.size(), res.getStatus().isSuccess());
        } catch (Ex ex) {
            m_logger.warn("File change type:" + appType + " processFileList error.", ex);
        } catch (IOException e) {
            m_logger.warn("File change type:" + appType + " processFileList error.", e);
        }
        return result;
    }

    public DataAttributes commandProcessIsReturn(DataAttributes buff) throws Ex {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayBufferedInputStream in = new ByteArrayBufferedInputStream();
        try {
            buff.store(out, "");
            in.write(out.toByteArray());
            in.flush();
            buff = disposeData(in);

        } catch (IOException e) {
            throw new Ex().set(E.E_IOException, e);
        }
        return buff;
    }

    private DataAttributes disposeData(ByteArrayBufferedInputStream in) throws Ex {

        long length = 0;

        DataAttributes result;
        try {
            if (m_changeMain.isNetWorkOkay()) {
                DataAttributes props = new DataAttributes();
                props.putValue(DataAttributes.Str_FileSize, String.valueOf(in.getSize()));
                result = m_changeMain.dispose(m_changeType, in, props);

            } else
                throw new IOException();
        } catch (Ex Ex) {
            EStatus status = EStatus.E_NetWorkError;
            m_changeMain.setStatus(m_changeType, status, null, true);
            try {
                Thread.sleep(Int_Thread_SleepTime);
            } catch (InterruptedException ie) {
                //okay
            }
            throw Ex;
        } catch (IOException e) {
            throw new Ex().set(E.E_Unknown);
        }
        if (result.getStatus().isSuccess()) {
            if (m_logger.isDebugEnabled()) {
                m_logger.debug("process status Value:" + result.getStatus().isSuccess());
            }
        }

        if (!result.getStatus().isSuccess()) {
            if (m_logger.isDebugEnabled()) {
                m_logger.debug("process status Value:" + result.getStatus().isSuccess());
            }
            try {
                Thread.sleep(Int_Thread_SleepTime);
            } catch (InterruptedException ie) {
                //okay
            }
            Message Message = new Message("目标端处理出错");
            throw new Ex().set(EFile.E___TARGET_PROCESS_ERROR, Message);
        }
        return result;
//

    }


    private void auditProcess(String operator, String filename, long processedRows, boolean okay) throws Ex {
        StringBuffer buff = new StringBuffer();
        buff.append("文件交换审计信息 源端(交换应用):");
        buff.append(m_changeType.getType());
        buff.append(",");
        buff.append(operator);
        buff.append(":");
        if (operator.equalsIgnoreCase(FileContext.Str_SyncFileList)) {
            buff.append(processedRows + " file.");
        } else {
            buff.append(processedRows + " byte.");
        }

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("start auditProcess:" + m_changeType.getType());
        }

        auditLogger.info(buff.toString());

        m_changeMain.setStatus(m_changeType, EStatus.E_OK, "正常", true);
        m_log.setSource_ip(m_config.getServerAddress());
        m_log.setSource_port("" + m_config.getPort());
        m_log.setIp(m_config.getServerAddress());
        m_log.setUserName(m_config.getUserName());
        m_log.setFlux(processedRows + "");
        m_log.setFilename(filename);
        m_log.setOperate(operator);
        if (okay) {
            m_log.setStatusCode(EStatus.E_OK.getCode() + "");
        } else {
            m_log.setStatusCode(EStatus.E_DbChangeTargetProcessFaild.getCode() + "");
        }
        m_log.info(buff.toString());


        if (m_logger.isDebugEnabled()) {
            m_logger.debug("edit auditProcess:" + m_changeType.getType());
        }

    }

    private void auditProcess(String ip, String port, String url, String operator, long processedRows, boolean okay) throws Ex {
        StringBuffer buff = new StringBuffer();
        buff.append("文件交换审计信息 源端(交换应用):");
        buff.append(m_changeType.getType());
        buff.append(",");
        buff.append(operator);
        buff.append(":");
        buff.append(processedRows + " byte.");
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("start auditProcess:" + m_changeType.getType());
        }
        auditLogger.info(buff.toString());
        m_changeMain.setStatus(m_changeType, EStatus.E_OK, "正常", true);
        m_log.setDest_url(url);

        m_log.setDest_url(url);
        m_log.info(buff.toString());
        if (okay) {
            m_log.setStatusCode(EStatus.E_OK.getCode() + "");
        } else {
            m_log.setStatusCode(EStatus.E_DbChangeTargetProcessFaild.getCode() + "");
        }
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("edit auditProcess:" + m_changeType.getType());
        }

    }


}
