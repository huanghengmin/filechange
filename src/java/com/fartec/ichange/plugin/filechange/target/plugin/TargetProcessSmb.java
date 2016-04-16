package com.fartec.ichange.plugin.filechange.target.plugin;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.inetec.common.config.nodes.TargetFile;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 11-11-27
 * Time: pm 6:32
 * To change this template use File | Settings | File Templates.
 */
public class TargetProcessSmb implements ITargetProcess {
    @Override
    public boolean process(InputStream in, FileBean bean) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean process(byte[] data, FileBean bean) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(TargetOperation target, TargetFile config) {
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
