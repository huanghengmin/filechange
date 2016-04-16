package com.fartec.ichange.plugin.filechange;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.inetec.common.config.nodes.IChange;
import com.inetec.common.config.nodes.Plugin;
import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.Type;
import com.inetec.common.exception.E;
import com.inetec.common.exception.Ex;
import com.inetec.common.i18n.Message;
import com.inetec.common.logs.LogHelper;
import com.inetec.ichange.api.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-3
 * Time: 3:56:38
 * To change this template use File | Settings | File Templates.
 */
public class FileChangeSource implements ISourcePlugin {
    public final static Logger m_logger = Logger.getLogger(FileChangeSource.class);
    public static String Str_AppType = "IChange FileChange";
    public LogHelper m_log = null;

    private boolean m_bConfigured = false;
    private IChangeType m_changeType = null;
    private IChangeMain m_dc = null;
    private ITargetPlugin m_target = null;
    private SourceOperation sourceOperation;
    private SourceFile socketChange = new SourceFile();

    public DataAttributes externalData(InputStream in, DataAttributes dp) throws Ex {
        DataAttributes dp1 = dp;

        dp1 = m_dc.dispose(m_changeType, in, dp);
        try {
            in.close();
        } catch (IOException e) {
            m_logger.error("close InputStream is Faild", e);
            m_log.error("close InputStream is Faild.", e);
        }

        return dp1;
    }


    /**
     * this method is used by the data ichange or another plugin to
     * send a control package to this source adapter.
     *
     * @param command   command: connect, disconnect, controlwrite, or getstatus; or
     *                  other customer control command
     * @param dataProps Nullable: no attributes when null. Attributes as parameters of the command.
     * @throws Ex
     */
    public DataAttributes control(String command, DataAttributes dataProps) throws Ex {
        DataAttributes dp = dataProps;
        dp.setStatus(Status.S_Success);
        return dp;
    }


    public void init(IChangeMain iChangeMain, IChangeType iChangeType, ITargetPlugin iTargetPlugin) throws Ex {
        m_dc = iChangeMain;
        m_changeType = iChangeType;
        m_target = iTargetPlugin;
        m_log = iChangeMain.createLogHelper();
        sourceOperation = new SourceOperation(m_dc, m_changeType);


    }

    public DataAttributes start(DataAttributes dataAttributes) throws Ex {
        if (!m_bConfigured) {
            m_dc.setStatus(m_changeType, EStatus.E_CF_NotConfigured, "", true);
            throw new Ex().set(E.E_OperationFailed, new Message("Not config."));
        }
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("Start file change source.");
        }
        if (!sourceOperation.isRun()) {
            new Thread(sourceOperation).start();
        }


        dataAttributes.setStatus(Status.S_Success);
        return dataAttributes;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataAttributes stop() throws Ex {
        DataAttributes dataAttributes = new DataAttributes();
        sourceOperation.stop();
        dataAttributes.setStatus(Status.S_Success);
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("stop file change source.");
        }

        return dataAttributes;
    }


    public void config(IChange iChange) throws Ex {

        if (m_bConfigured) {
            m_dc.setStatus(m_changeType, EStatus.E_AlreadyConfigured, "Already Configured.", true);
            throw new Ex().set(E.E_Unknown, new Message("Already Configured."));
        }

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("start config source ftpchange, the config path is " + iChange);
        }

        Type type = iChange.getType(m_changeType.getType());
        Plugin plugin = type.getPlugin();
        Str_AppType = type.getAppType();
        if (plugin == null) {
            m_dc.setStatus(m_changeType, EStatus.E_CF_Faild, "PlugIn is null.", true);
            throw new Ex().set(E.E_OperationFailed, new Message("PlugIn is null."));
        }

        socketChange = plugin.getSourceFile();
        if (socketChange != null) {
            sourceOperation.config(socketChange);
            m_bConfigured = true;
            m_dc.setStatus(m_changeType, EStatus.E_CONFIGOK, null, true);

            if (m_logger.isDebugEnabled()) {
                m_logger.debug("finish config source filechange.");
            }
            m_log.setAppName(m_changeType.getType());
            m_log.setAppType(Str_AppType);
            m_log.setSouORDes(LogHelper.Str_souORDes_Source);
        }

    }


    public boolean configred() {
        return m_bConfigured;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
