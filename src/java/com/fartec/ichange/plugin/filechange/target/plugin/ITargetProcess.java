package com.fartec.ichange.plugin.filechange.target.plugin;

import com.fartec.ichange.plugin.filechange.source.plugin.ISourceProcess;
import com.fartec.ichange.plugin.filechange.target.TargetOperation;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.inetec.common.config.nodes.TargetFile;


import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 11-11-26
 * Time: 下午3:21
 * To change this template use File | Settings | File Templates.
 */
public interface ITargetProcess extends Runnable {

    /**
     * 处理数据保存到标文件
     *
     * @param in   流对象
     * @param bean 文件对象
     * @return 处理成功返回true ,反之返回false;
     */
    public boolean process(InputStream in, FileBean bean);

    /**
     * 处理数据保存到标文件
     *
     * @param data 二进制对象
     * @param bean 文件对象
     * @return 处理成功返回true ,反之返回false;
     */

    public boolean process(byte[] data, FileBean bean);

    /**
     * 完成目标端处理插件的初始化
     *
     * @param target 目标端操作对象
     * @param config 目标端端配置信息
     */

    public void init(TargetOperation target, TargetFile config);


    /**
     * 停止方法
     */

    public void stop();

    /**
     * 判别该插件是否在运行
     *
     * @return 在运行返回true ,反之返回false;
     */

    public boolean isRun();

    /**
     * 处理源端文件列表。
     *
     * @param list 文件列表对象
     * @return 返回要同步的文件列表。
     */

    public FileList procesFileList(FileList list);


}
