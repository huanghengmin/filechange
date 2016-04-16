package com.fartec.ichange.plugin.filechange;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.inetec.common.io.IOUtils;
import com.inetec.ichange.api.*;
import com.inetec.ichange.main.ChangeControlRequest;
import com.inetec.common.exception.Ex;
import com.inetec.common.exception.E;
import com.inetec.common.config.nodes.*;
import com.inetec.common.logs.LogHelper;
import com.inetec.common.i18n.Message;

import java.io.*;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-3
 * Time: 3:56:55
 * To change this template use File | Settings | File Templates.
 */
public class FileChangeTarget implements ITargetPlugin {
    protected final static Logger m_logger = Logger.getLogger(FileChangeTarget.class);
    protected LogHelper m_log = null;
    public static String Str_FileChangeType = "IChange FileChange";

    private int[] m_cap = null;
    private boolean m_bConfigured = false;
    private ISourcePlugin m_source = null;
    private IChangeMain m_changeMain;
    private IChangeType m_changeType;
    private TargetFile m_config;
    private TargetOperation targetOperation;

    public DataAttributes process(String collectionType, DataAttributes dataProps, InputStream is) throws Ex {
        String appType = collectionType;
        if (appType == null) {
            throw new Ex().set(E.E_NullPointer, new Message("can not get the apptype value from DataAttributes object"));
        }
        if (!configred()) {
            throw new Ex().set(E.E_OperationError, new Message("Target not config."));
        }
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("file change start procee 1.");
        }
        DataAttributes dataa = new DataAttributes();

        try {
            /*  byte[] temp = IOUtils.readByteArrayNoAvailabel(is);
       if(m_logger.isDebugEnabled())
       m_logger.info("filechange recv data:" + new String(temp));*/
            dataa.load(is);

        } catch (IOException e) {
            m_logger.warn("Data Parser IOException .", e);
            throw new Ex().set(E.E_IOException, e);

        }

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("file change start procee 2.");
        }
        try {
            // byte[] temp = IOUtils.readByteArray(dataa.getResultData());
            //m_logger.info("FileChangeTarget process Data:" + new String(dataa.getContent()));
            //m_logger.info("FileChangeTarget process ResultData is null:" + dataa.getResultData() == null);
            //dataa.setResultData(temp);
            dataProps = targetOperation.process(dataa);
        } catch (IOException e) {
            m_logger.warn("Data Process IOException .", e);
            throw new Ex().set(E.E_IOException, e);
        }

        // targetOperation.
        return dataProps;
    }


    public DataAttributes process(String collectionType, DataAttributes dataProps, String filename) throws Ex {

        String appType = collectionType;
        if (appType == null) {
            throw new Ex().set(E.E_NullPointer, new Message("can not get the apptype value from DataAttributes object"));
        }
        if (!configred()) {
            throw new Ex().set(E.E_OperationError, new Message("Target not config."));
        }
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("Start to process " + filename);
        }
        try {
            dataProps = process(collectionType, dataProps, new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            m_logger.warn("data file not found.", e);
            throw new Ex().set(E.E_FileNotFound, e);
        }
        return dataProps;
    }


    /**
     * get the capabilities of the implementation in the decendent order (element[0] being preferred).
     *
     * @return the capabilities as defined by the constents at the top.
     */
    public int[] getCapabilities() {
        return new int[]{I_StreamCapability};

        //return m_cap;
    }

    public void init(IChangeMain iChangeMain, IChangeType iChangeType, ISourcePlugin iSourePlugin) throws Ex {
        //To change body of implemented methods use File | Settings | File Templates.
        m_changeMain = iChangeMain;
        m_changeType = iChangeType;
        m_source = iSourePlugin;
        m_cap = new int[]{I_FileCapability, I_StreamCapability};
        m_log = iChangeMain.createLogHelper();
        targetOperation = new TargetOperation(m_changeMain, m_changeType);


    }


    public DataAttributes control(String command, DataAttributes dataAttributes) throws Ex {
        DataAttributes dp = dataAttributes;
        if (!configred()) {
            throw new Ex().set(E.E_OperationError, new Message("Target not config."));
        }
        boolean isProcess = false;
        if (command == null) {
            command = "";
        }
        if (ChangeControlRequest.Str_Start.equalsIgnoreCase(command)) {
            boolean result = false;
            //targetpool.closePool();
            result = true;
            if (result) {
                dp.setStatus(Status.S_Success_TargetProcess);
            } else {
                dp.setStatus(Status.S_Faild_TargetProcess);
            }
        }
        if (ChangeControlRequest.Str_Stop.equalsIgnoreCase(command)) {
            boolean result = false;
            //targetpool.closePool();
            result = true;
            if (result) {
                dp.setStatus(Status.S_Success_TargetProcess);
            } else {
                dp.setStatus(Status.S_Faild_TargetProcess);
            }
        }
        if (!isProcess)

        {
            throw new Ex().set(E.E_OperationFailed, new Message("the command {0} is not recognized by the source of {1}.", command, m_changeType.getType()));
        }
        return dp;
    }

    public void config(IChange iChange) throws Ex {
        if (configred()) {
            m_changeMain.setStatus(m_changeType, EStatus.E_AlreadyConfigured, null, false);
            throw new Ex().set(E.E_Unknown, new Message("Already Configured. "));
        }
        String network = System.getProperty("privatenetwork");

        Type type = iChange.getType(m_changeType.getType());
        Str_FileChangeType = type.getAppType();
        Plugin plugin = type.getPlugin();
        if (plugin == null) {
            m_bConfigured = false;
            return;
        }
        m_config = plugin.getTargetFile();
        if (m_config != null) {
            targetOperation.config(m_config);
            m_bConfigured = true;

            m_changeMain.setStatus(m_changeType, EStatus.E_CONFIGOK, null, false);
            if (m_logger.isDebugEnabled()) {
                m_logger.debug("finish config target ftpchange");
            }
            m_log.setAppName(m_changeType.getType());
            m_log.setAppType(Str_FileChangeType);
            m_log.setSouORDes(LogHelper.Str_souORDes_Dest);
        }
    }


    public boolean configred() {
        return m_bConfigured;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
