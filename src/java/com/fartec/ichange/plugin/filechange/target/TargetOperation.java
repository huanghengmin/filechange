package com.fartec.ichange.plugin.filechange.target;

import com.fartec.ichange.plugin.filechange.FileChangeTarget;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileContext;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.fartec.ichange.plugin.filechange.utils.ProcessFactory;
import com.inetec.common.config.nodes.TargetFile;
import com.inetec.common.exception.Ex;
import com.inetec.common.io.IOUtils;
import com.inetec.common.logs.LogHelper;
import com.inetec.ichange.api.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-4
 * Time: 6:13:24
 * To change this template use File | Settings | File Templates.
 */
public class TargetOperation implements Runnable {
    public final static Logger m_logger = Logger.getLogger(TargetOperation.class);
    Logger auditLogger = Logger.getLogger("ichange.audit.file");
    private int port = 4900;
    private int retrytime = 0;
    private long lastOperationTime = System.currentTimeMillis();
    private boolean processEnd = false;
    private DataAttributes data;
    private DataAttributes result;

    protected IChangeType m_changeType = null;
    protected IChangeMain m_changeMain;
    public LogHelper m_log = null;
    public static String Str_Format_Date = "yyyy-MM-dd HH:mm:ss";
    protected TargetFile m_config;
    protected ITargetProcess targetProcess;
    private boolean m_configed = false;

    public TargetOperation(IChangeMain dc, IChangeType type) {
        m_changeMain = dc;
        m_changeType = type;
        m_log = dc.createLogHelper();
        m_log.setAppName(type.getType());
        m_log.setAppType(FileChangeTarget.Str_FileChangeType);
        m_log.setSouORDes(LogHelper.Str_souORDes_Dest);
    }

    public void config(TargetFile targetFile) {
        m_config = targetFile;
        targetProcess = ProcessFactory.getTargetProcess(targetFile.getProtocol());
        if (targetProcess != null) {
            m_configed = true;
            targetProcess.init(this, m_config);
        }
    }

    /**
     * 处理数据保存到标文件
     *
     * @param in   流对象
     * @param bean 文件对象
     * @return 处理成功返回true ,反之返回false;
     */
    public boolean process(InputStream in, FileBean bean) {
        boolean result = false;
        if (targetProcess != null) {
            result = targetProcess.process(in, bean);
        }
        return result;
    }

    /**
     * 处理数据保存到标文件
     *
     * @param data 二进制对象
     * @param bean 文件对象
     * @return 处理成功返回true ,反之返回false;
     */

    public boolean process(byte[] data, FileBean bean) {
        boolean result = false;
        if (targetProcess != null) {
            result = targetProcess.process(data, bean);
        }
        return result;
    }

    public FileList procesFileList(FileList list) {
        FileList result = null;
        if (targetProcess != null) {
            result = targetProcess.procesFileList(list);
        }
        return result;
    }

    public DataAttributes process(DataAttributes data) throws Ex, IOException {
        DataAttributes result = new DataAttributes();
        String syncfilecommand = data.getValue(FileContext.Str_SyncFileCommand);
        if (m_logger.isDebugEnabled())
            m_logger.info("targetOperation process method recv command:" + syncfilecommand);
        //文件列表
        if (syncfilecommand.equalsIgnoreCase(FileContext.Str_SyncFileList)) {
            FileList newlist = new FileList();
            if (data.getResultData() != null) {
                if (m_logger.isDebugEnabled())
                    m_logger.info("targetOperation process filelist data is not null:");
                String temp = new String(IOUtils.readByteArray(data.getResultData()));
                if (m_logger.isDebugEnabled())
                    m_logger.info("targetOperation process filelist data:" + new FileBean().decode(temp));
                newlist = FileList.stringToFileList(new FileBean().decode(temp));
                FileList res = procesFileList(newlist);
                result.setResultData(res.toXml().getBytes());
                result.setStatus(Status.S_Success);
                auditProcess(syncfilecommand, "", newlist.getSyncFileListTotal(), true);
            } else {
                m_logger.warn("targetOperation process filelist data is null:");
                result.setStatus(Status.S_Faild_TargetProcess);
            }
        }
        if (syncfilecommand.equalsIgnoreCase(FileContext.Str_SyncFile)) {
            FileBean bean = getFileBean(data);
            if (data.getResultData() != null) {
                //byte[] temp = IOUtils.toByteArray(data.getResultData());
                //m_logger.info("recv data length:" + temp.length);
                boolean res = process(data.getResultData(), bean);
                if (res)
                    result.setStatus(Status.S_Success);
                else
                    result.setStatus(Status.S_Faild_TargetProcess);
                auditProcess(syncfilecommand, bean.getName(), Long.parseLong(data.getValue(FileContext.Str_SyncFileDataSize)), res);
            } else {
                m_logger.warn("targetOperation process syncfile data is null:");

                result.setStatus(Status.S_Faild_TargetProcess);
            }
        }


        return result;
    }


    public void run() {
        processEnd = false;
        String command = "";
        /* try {
            lastOperationTime = System.currentTimeMillis();
            command = data.getProperty(FtpCommand.Str_FtpCommand);
            String commandBody = data.getProperty(FtpCommand.Str_FTPCommandBody);
            if (commandBody != null) {
                data.setProperty(FtpCommand.Str_FTPCommandBody, decode(commandBody));
            }
            result = client.command(data);
            commandBody = data.getProperty(FtpCommand.Str_FTPCommandBody);
            if (commandBody != null) {
                data.setProperty(FtpCommand.Str_FTPCommandBody, encode(commandBody));
            }
            String transferState = result.getProperty(FtpCommand.Str_FTPTransferState);
            if (transferState == null || transferState.equalsIgnoreCase("") ||
                    transferState.equalsIgnoreCase(FtpCommand.Str_FTPTransferState_End))
            if(commandBody!=null)
                auditProcess(data.getProperty(FtpCommand.Str_FTPUser), command,encode(commandBody), result.getStatus().isSuccess());
            else
                auditProcess(data.getProperty(FtpCommand.Str_FTPUser), command, result.getStatus().isSuccess());
        } catch (Ex ex) {
            close();
            m_logger.warn("Ex: ftpclient exec command(" + command + ") message error(apptype=" + m_changeType.getType() + ").");
        } finally {
            processEnd = true;
            lastOperationTime = System.currentTimeMillis();
        }*/


    }

    public FileBean getFileBean(DataAttributes da) {
        FileBean result = new FileBean();
        result.setSyncflag(da.getValue(FileContext.Str_SyncFileFlag));
        result.setFullname(result.decode(da.getValue(FileContext.Str_SyncFileFullName)));
        result.setFilepostlocation(Long.parseLong(da.getValue(FileContext.Str_SyncFilePost)));
        result.setMd5(da.getValue(FileContext.Str_SyncFileMD5));
        result.setFilesize(Long.parseLong(da.getValue(FileContext.Str_SyncFileSize)));
        result.setSyncflag(da.getValue(FileContext.Str_SyncFileFlag));
        result.setFile_flag(da.getValue(FileContext.Str_SyncFile_Flag));

        return result;
    }

    public void close() {
        /* try {
        *//*if (client.isClosed())
                client.disconnect();*//*
        } catch (IOException e) {
            m_logger.warn("IOException: ftp client close error.(apptype=" + m_changeType.getType() + ").");
            //throw new Ex(E.E_IOException, e, new Message("IOException: socket client close error."));

        }*/
    }


    public boolean validation() {
        /* if (client.isClosed()) {
            return false;
        }*/
        boolean result = true;
        long temp = System.currentTimeMillis() - lastOperationTime;
        if (retrytime >= temp) {
            result = true;
        } else {
            result = false;
        }
        // result = !client.isClosed();
        return result;
    }

    public void auditProcess(String operator, String filename, long processedRows, boolean okay) throws Ex {
        StringBuffer buff = new StringBuffer();
        buff.append("文件交换审计信息 目标端(交换应用):");
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
        m_log.setDest_ip(m_config.getServerAddress());
        m_log.setDest_port("" + m_config.getPort());
        m_log.setIp(m_config.getServerAddress());
        m_log.setUserName(m_config.getUserName());
        m_log.setFlux(processedRows + "");
        m_log.setFilename(filename);
        m_log.setOperate(operator);

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

    public void auditProcess(String ip, String port, String url, String operator, long processedRows, int curconnect) throws Ex {
        StringBuffer buff = new StringBuffer();
        buff.append("文件交换审计信息 目标端(交换应用):");
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

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("edit auditProcess:" + m_changeType.getType());
        }

    }
}
