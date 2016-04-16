package com.fartec.ichange.plugin.filechange.utils;

import com.inetec.common.config.nodes.SourceFile;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFilenameFilter;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Ç®ÏþÅÎ
 * Date: 12-5-9
 * Time: ÏÂÎç5:55
 * To change this template use File | Settings | File Templates.
 */
public class SambaFilterFile implements SmbFileFilter {
    private Logger logger = Logger.getLogger(SambaFilterFile.class);
    private SourceFile config;

    public boolean accept(SmbFile smbFile){
//        String filterEnds = smbFile.getName();
        boolean  isFilter = false;
        try {
            isFilter = smbFile.isDirectory();
        } catch (SmbException e) {

        }
        return isFilter;
    }

}
