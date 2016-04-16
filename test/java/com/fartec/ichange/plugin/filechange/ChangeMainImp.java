package com.fartec.ichange.plugin.filechange;

import com.inetec.common.db.datasource.DatabaseSource;
import com.inetec.common.exception.Ex;
import com.inetec.common.logs.LogHelper;
import com.inetec.common.logs.util.LogCachce;
import com.inetec.ichange.api.DataAttributes;
import com.inetec.ichange.api.EStatus;
import com.inetec.ichange.api.IChangeType;
import com.inetec.ichange.api.ITargetPlugin;
import com.inetec.ichange.main.utils.TypeStatusSet;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2007-11-11
 * Time: 19:22:09
 * To change this template use File | Settings | File Templates.
 */
public class ChangeMainImp implements com.inetec.ichange.api.IChangeMain {
    private static final Logger m_logg = Logger.getLogger(ChangeMainImp.class);
    private boolean networkisOkay = true;
    private LogCachce logCachce = new LogCachce();
    private ITargetPlugin target = null;

    public void setTargetPlugin(ITargetPlugin target) {
        this.target = target;
    }

    public DataAttributes control(IChangeType iChangeType, String s, DataAttributes dataAttributes) throws Ex {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DataAttributes dispose(IChangeType iChangeType, InputStream inputStream, DataAttributes dataAttributes) throws Ex {
        return target.process(iChangeType.getType(), dataAttributes, inputStream);
    }

    public DataAttributes dispose(IChangeType iChangeType, byte[] bytes, DataAttributes dataAttributes) throws Ex {
        dataAttributes=target.process(iChangeType.getType(), dataAttributes, new ByteArrayInputStream(bytes));
        return dataAttributes;
    }

    public void setStatus(IChangeType iChangeType, EStatus eStatus, String s, boolean b) throws Ex {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public TypeStatusSet getStatus() throws Ex {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public LogHelper createLogHelper() {
        LogHelper logger = new LogHelper();
        logger.setLogCachce(logCachce);
        return logger;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DatabaseSource findDataSource(String s) throws Ex {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNetWorkOkay() {
        return networkisOkay;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
