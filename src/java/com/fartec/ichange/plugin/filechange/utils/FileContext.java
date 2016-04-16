package com.fartec.ichange.plugin.filechange.utils;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 11-11-26
 * Time: 下午1:58
 * To change this template use File | Settings | File Templates.
 */
public final class FileContext {
    /**
     * 文件同步命令 （SyncFile,SyncFileList）
     */
    public static final String Str_SyncFileCommand = "SyncFileCommand";
    /**
     * 文件同步命令-文件同步
     */
    public static final String Str_SyncFile = "SyncFile";

    public static final String Str_Lnk = ".lnk";
    public static final String Str_Sync = "sync_";


    /**
     * 断点续传文件列表标记值为（true,false）
     */
    public static final String Str_SyncFileListDD="SyncFileListDD";

    /**
     * 文件同步命令-文件同步列表
     */
    public static final String Str_SyncFileList = "SyncFileList";
    /**
     * 文件列表总数
     */
    public static final String Str_SyncFileListTotal = "SyncFileListTotal";
    /**
     * 文件列表类型（SyncFilelistTypeDelete，SyncFilelistTypeNormal）
     */

    public static final String Str_SyncFileListType = "SyncFileListType";
    /**
     * 删除文件列表
     */
    public static final String Str_SyncFileListType_Delete = "SyncFilelistTypeDelete";
    /**
     * 正常文件列表
     */
    public static final String Str_SyncFileListType_Normal = "SyncFilelistTypeNormal";
    /**
     * 文件列表传输标记:(SyncFileListStart，SyncFileListIng,SyncFileListEnd)
     */
    public static final String Str_SyncFileListFlag = "SyncListFlag";
    /**
     * 文件列表开始传输
     */
    public static final String Str_SyncFileListStart = "SyncFileListStart";
    /**
     * 文件列表传输进行中
     */
    public static final String Str_SyncFileListIng = "SyncFileListIng";
    /**
     * 文件列表传输结束
     */
    public static final String Str_SyncFileListEnd = "SyncFileListEnd";
    /**
     * 文件同步传输标记:(SyncFileStart，SyncFileIng,SyncFileEnd)
     */
    public static final String Str_SyncFileFlag = "SyncFlag";
    /**
     * 开始文件同步
     */
    public static final String Str_SyncFileStart = "SyncFileStart";
    /**
     * 文件同步进行中
     */
    public static final String Str_SyncFileIng = "SyncFileIng";
    /**
     * 文件同步结束
     */
    public static final String Str_SyncFileEnd = "SyncFileEnd";

    /**
     * 文件名
     */
    public static final String Str_SyncFileName = "SyncFileName";
    /**
     * 文件大小
     */
    public static final String Str_SyncFileSize = "SyncFileSize";
    /**
     * 文件MD5值
     */
    public static final String Str_SyncFileMD5 = "SyncFileMD5";

    /*
    文件当前游标
     */
    public static final String Str_SyncFilePost = "SyncFilePost";
    /**
     * 文件数据包大小
     */
    public static final String Str_SyncFileDataSize = "SyncDataSize";
    /**
     * 文件全名
     */
    public static final String Str_SyncFileFullName = "SyncFileFullName";
    /**
     *
     */
    public static final String Str_SyncFileTime = "SyncFileTime";

    public static final String Str_SyncFile_Flag = "SyncFileFlag";

    public static final String Str_SyncFileSourceProcess_Flag = ".ispf";
    public static final String Str_SyncFileTargetProcess_Flag = ".itpf";
    public static final String Str_SyncFileSourceProcess_End_Flag = ".ispe";
    public static final String Str_SyncFileTargetProcess_End_Flag = ".itpe";

    /**
     * 源端文件/目标端文件
     */
    public static final String Str_SourceFile = "source";
    public static final String Str_TargetFile = "target";


}
