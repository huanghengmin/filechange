package com.fartec.ichange.plugin.filechange.utils;

import com.fartec.ichange.plugin.filechange.source.plugin.ISourceProcess;
import com.fartec.ichange.plugin.filechange.source.plugin.SourceProcessFtp;
import com.fartec.ichange.plugin.filechange.source.plugin.SourceProcessSmb;
import com.fartec.ichange.plugin.filechange.source.plugin.SourceProcessWebDAV;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.target.plugin.TargetProcessFtp;
import com.fartec.ichange.plugin.filechange.target.plugin.TargetProcessSmb;
import com.fartec.ichange.plugin.filechange.target.plugin.TargetProcessWebDAV;
import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.exception.E;
import com.inetec.common.exception.Ex;
import com.inetec.common.i18n.Message;
import com.inetec.ichange.api.EChange;
import org.apache.log4j.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 11-11-28
 * Time: 上午11:14
 * To change this template use File | Settings | File Templates.
 */
public final class ProcessFactory {
    private static final Logger logger = Logger.getLogger(ProcessFactory.class);
    private static final String sourceProcess_Ftp = SourceProcessFtp.class.getName();
    private static final String sourceProcess_Smb = SourceProcessSmb.class.getName();
    private static final String sourceProcess_Ftps = SourceProcessFtp.class.getName();
    private static final String sourceProcess_WebDAV = SourceProcessWebDAV.class.getName();
    private static final String targetProcess_Ftp = TargetProcessFtp.class.getName();
    private static final String targetProcess_Smb = TargetProcessSmb.class.getName();
    private static final String targetProcess_Ftps = TargetProcessFtp.class.getName();
    private static final String targetProcess_WebDAV = TargetProcessWebDAV.class.getName();

    public static ISourceProcess getSourceProcess(String protocol) {
        if (protocol == null) {
            protocol = "";
        }
        try {
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_Ftp)) {
                return (ISourceProcess) newObjectByClass(sourceProcess_Ftp, ISourceProcess.class);
            }
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_Ftps)) {
                return (ISourceProcess) newObjectByClass(sourceProcess_Ftps, ISourceProcess.class);
            }
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_SMB)) {
                return (ISourceProcess) newObjectByClass(sourceProcess_Smb, ISourceProcess.class);
            }
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_WebDAV)) {
                return (ISourceProcess) newObjectByClass(sourceProcess_WebDAV, ISourceProcess.class);
            }
        } catch (Ex ex) {
            logger.warn("File change ISourceProcess create object by protocol is:" + protocol, ex);
        }
        return null;
    }

    public static ITargetProcess getTargetProcess(String protocol) {
        if (protocol == null) {
            protocol = "";
        }
        try {
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_Ftp)) {
                return (ITargetProcess) newObjectByClass(targetProcess_Ftp, ITargetProcess.class);
            }
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_SMB)) {
                return (ITargetProcess) newObjectByClass(targetProcess_Smb, ITargetProcess.class);
            }
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_Ftps)) {
                return (ITargetProcess) newObjectByClass(targetProcess_Ftps, ITargetProcess.class);
            }
            if (protocol.equalsIgnoreCase(SourceFile.Str_Protocol_WebDAV)) {
                return (ITargetProcess) newObjectByClass(targetProcess_WebDAV, ITargetProcess.class);
            }
        } catch (Ex ex) {
            logger.warn("File change ITargetProcess by protocol is:" + protocol, ex);
        }
        return null;
    }

    public static Object newObjectByClass(String classname, Class cls) throws Ex {
        // Make a class object with the plug-in name
        Class c = null;
        try {
            c = Class.forName(classname, true, ProcessFactory.class.getClassLoader());
        } catch (ClassNotFoundException Ex) {
            throw new Ex().set(E.E_Unknown, new Message("Class not found:{0} ", Ex.getMessage()));
        }

        // Make sure c implements the two required interfaces:

        if (!cls.isAssignableFrom(c)) {
            throw new Ex().set(EChange.E_CF_InterfaceNotImplemented, new Message("The  class  does not implement the required interface {0}", cls.getName()));
        }
        // Now, ready to create an instance.
        Object fr = null;
        try {
            fr = c.newInstance();
        } catch (InstantiationException Ex) {
            throw new Ex().set(EChange.E_UNKNOWN, new Message("Failed to instantiate class: ", Ex.getMessage()));
        } catch (IllegalAccessException Ex) {
            throw new Ex().set(EChange.E_UNKNOWN, new Message("Failed to instantiate class; access exception: ", Ex.getMessage()));
        }

        return fr;
    }
}
