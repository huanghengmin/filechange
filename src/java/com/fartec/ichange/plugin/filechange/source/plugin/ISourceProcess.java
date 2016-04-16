package com.fartec.ichange.plugin.filechange.source.plugin;

import com.fartec.ichange.plugin.filechange.source.SourceOperation;
import com.fartec.ichange.plugin.filechange.target.plugin.ITargetProcess;
import com.fartec.ichange.plugin.filechange.utils.FileBean;
import com.fartec.ichange.plugin.filechange.utils.FileList;
import com.inetec.common.config.nodes.SourceFile;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 11-11-26
 * Time: 下午3:21
 * To change this template use File | Settings | File Templates.
 */
public interface ISourceProcess extends Runnable {
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
     * 完成源端处理插件的初始化
     *
     * @param source 原端操作对象
     * @param config 源端配置信息
     */

    public void init(SourceOperation source, SourceFile config);
    /**
     * 完成源端处理插件的初始化
     *
     * @param target 目标端操作对象
     * @param config 源端配置信息
     */

    public void init(ITargetProcess target, SourceFile config);

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
     * 处理目标端文件列表。
     *
     * @param list 文件列表对象
     * @return 返回要同步的文件列表。
     */

    public FileList procesFileList(FileList list);

}
