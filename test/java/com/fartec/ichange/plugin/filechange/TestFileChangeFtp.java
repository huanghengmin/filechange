package com.fartec.ichange.plugin.filechange;

import com.inetec.common.config.ConfigParser;
import com.inetec.common.config.nodes.IChange;
import com.inetec.ichange.api.DataAttributes;
import junit.framework.TestCase;


/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2010-9-16
 * Time: 23:29:56
 * To change this template use File | Settings | File Templates.
 */
public class TestFileChangeFtp extends TestCase {
    ChangeMainImp main = new ChangeMainImp();
    FileChangeSource source = new FileChangeSource();
    FileChangeTarget target = new FileChangeTarget();
    ChangeTypeImp type = new ChangeTypeImp("fileftp");

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testFileChangeFtp() throws Exception {
        ConfigParser config = new ConfigParser("D:\\fartec\\ichange\\filechamge\\test\\resources\\config_ftp.xml");
        //ConfigParser config = new ConfigParser("/media/sda5/inetec/ichange/sipchange/utest/resources/config.xml");
        IChange changenode = config.getRoot();
        //\Plugin plugin = type.getPlugin();
        //System.setProperty("privatenetwork", "false");
        main.setTargetPlugin(target);
        //main.setTargetPlugin(target1);
        target.init(main, type, source);
        source.init(main, type, target);
        target.config(changenode);
        source.config(changenode);
        source.start(new DataAttributes());

    }


}
