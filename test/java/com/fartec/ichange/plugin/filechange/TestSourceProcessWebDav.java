package com.fartec.ichange.plugin.filechange;

import com.inetec.common.config.ConfigParser;
import com.inetec.common.config.nodes.IChange;
import com.inetec.ichange.api.DataAttributes;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 12-3-13
 * Time: ÏÂÎç6:04
 * To change this template use File | Settings | File Templates.
 */
public class TestSourceProcessWebDav extends TestCase {
    ChangeMainImp main = new ChangeMainImp();
    FileChangeSource source = new FileChangeSource();
    FileChangeTarget target = new FileChangeTarget();
    ChangeTypeImp type = new ChangeTypeImp("filewebdav");

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFileChangeWebDav() throws Exception {
        ConfigParser config = new ConfigParser("D:\\fartec\\ichange\\filechange\\test\\resource\\config_webdav.xml");
        IChange changeNode = config.getRoot();
        System.setProperty("privatenetwork", "false");
        main.setTargetPlugin(target)
        ;
        target.init(main, type, source);
        source.init(main, type, target);
        target.config(changeNode);
        source.config(changeNode);
        source.start(new DataAttributes());
        while (true){
            Thread.sleep(5*1000);
        }
    }
}
