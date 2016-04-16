package com.fartec.ichange.plugin.filechange;

import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.target.plugin.TargetProcessSmb;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.inetec.common.config.nodes.TargetFile;
import com.inetec.common.exception.Ex;
import com.inetec.ichange.api.IChangeMain;
import com.inetec.ichange.api.IChangeType;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Ç®ÏþÅÎ
 * Date: 12-3-16
 * Time: ÏÂÎç3:33
 * To change this template use File | Settings | File Templates.
 */
public class TestTargetProcessSmb extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDummy() {
        assertTrue(true);
    }

    public void testProcess(){
        TargetFile config = new TargetFile();
        TargetProcessSmb smb = new TargetProcessSmb();
        TargetOperation target =null; //new TargetOperation();

        FileList sourceFileList = new FileList();
        new Thread(smb).start();
        FileList compare = smb.procesFileList(sourceFileList);

    }

}
