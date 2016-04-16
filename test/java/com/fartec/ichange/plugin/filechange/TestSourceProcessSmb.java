package com.fartec.ichange.plugin.filechange;

import com.fartec.ichange.plugin.filechange.source.plugin.SourceProcessSmb;
import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.target.plugin.TargetProcessSmb;
import com.inetec.common.config.ConfigParser;
import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.TargetFile;
import com.inetec.common.config.nodes.Type;
import com.inetec.common.exception.Ex;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: 钱晓盼
 * Date: 12-3-14
 * Time: 下午7:10
 * To change this template use File | Settings | File Templates.
 */
public class TestSourceProcessSmb extends TestCase {

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
        TargetFile configTarget = getTartFile();
        SourceFile config = getSourceFile();
        ITargetProcess iSmb = new TargetProcessSmb();
        SourceProcessSmb smb = new SourceProcessSmb();
        TargetOperation targetOperation =null;// new TargetOperation();
        iSmb.init( targetOperation, configTarget);
        smb.init(iSmb, config);
        int index = 1;
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestSourceProcessSmb.class);
        logger.info("第"+ (index) +"个周期同步开始.");
        new Thread(smb).start();
        smb.run();
        while (smb.isRun()) {
            logger.info("第"+ (index++) +"个周期同步结束.等待..");
            smb.stop();
            try {
                Thread.sleep(config.getInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            smb.run();
        }
    }

    public static TargetFile getTartFile() {
        String path = "/test/resource/config_smb.xml";
        ConfigParser configParser = null;
        TargetFile config = null;
        try {
            configParser = new ConfigParser(path);
            Type type = configParser.getRoot().getType("filesmb");
            config = new TargetFile();
            type.getPlugin().getSourceFile();
            //config.setUserName(getUserName());
            config.setPassword("123456");
            config.setServerAddress("192.168.1.252");
            config.setPort("445");
            config.setDeletefile(String.valueOf(false));
            config.setOnlyadd(String.valueOf(false));
            config.setCharset("UTF-8");
            config.setDir("/Share");
            config.setIstwoway(String.valueOf(true));
            config.setThreads(String.valueOf(10));
        } catch (Ex ex) {
            ex.printStackTrace();
        }
        return config;
    }

    public static SourceFile getSourceFile() {
        SourceFile config = new SourceFile();
        try {
            config.setUserName("nobody");
            config.setPassword("123456");
            config.setServerAddress("192.168.1.121");
            config.setPort("445");
            config.setInterval(String.valueOf(1000 * 5));
            config.setFiltertypes("*.*");
//            config.setNotfiltertypes("*.bak");
            config.setDir("/Share");
            config.setProtocol("SMB");
            config.setCharset("UTF-8");
            config.setDeletefile(String.valueOf(false));
            config.setIsincludesubdir(String.valueOf(true));
            config.setThreads("10");
            config.setIstwoway(String.valueOf(true));
        } catch (Ex ex) {
            ex.printStackTrace();
        }
        return config;
    }
}
