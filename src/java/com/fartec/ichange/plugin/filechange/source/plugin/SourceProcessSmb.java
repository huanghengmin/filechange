package com.fartec.ichange.plugin.filechange.source.plugin;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.inetec.common.config.nodes.SourceFile;
import org.apache.log4j.Logger;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: qxp
 * Date: 2012-04-27
 * Time: pm3:00
 *  实现文件共享的同步,
 */
public class SourceProcessSmb implements ISourceProcess {
    private Logger logger = Logger.getLogger(SourceProcessSmb.class);

    @Override
    public boolean process(InputStream in, FileBean bean) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean process(byte[] data, FileBean bean) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(SourceOperation source, SourceFile config) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(ITargetProcess target, SourceFile config) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRun() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FileList procesFileList(FileList list) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
