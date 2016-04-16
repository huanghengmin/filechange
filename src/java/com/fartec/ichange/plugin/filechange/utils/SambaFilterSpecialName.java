package com.fartec.ichange.plugin.filechange.utils;

import com.inetec.common.config.nodes.SourceFile;
import com.inetec.common.config.nodes.TargetFile;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Ç®ÏþÅÎ
 * Date: 12-5-10
 * Time: ÉÏÎç9:14
 * To change this template use File | Settings | File Templates.
 */
public class SambaFilterSpecialName implements SmbFilenameFilter{
    private Logger logger = Logger.getLogger(SambaFilterSpecialName.class);
    private SourceFile sourceFile;
    private TargetFile targetFile;
    public SambaFilterSpecialName(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public SambaFilterSpecialName(TargetFile targetFile) {
        this.targetFile = targetFile;
    }

    public boolean accept(SmbFile smbFile, String fileName) throws SmbException {
        if(sourceFile != null && targetFile == null){
            if(fileName.startsWith(FileContext.Str_Sync)&&fileName.endsWith(FileContext.Str_Lnk)){
                fileName = fileName.substring(5,fileName.lastIndexOf("_"));
                return fixFilter(fileName);
            }
        }else if(sourceFile == null && targetFile != null){
            if(fileName.endsWith(FileContext.Str_SyncFileTargetProcess_Flag)){
                return true;
            }
        }
        return false;
    }

    private boolean fixFilter(String fileName) {
        String filterType = null;
        boolean isFilterTypes = false;
        boolean isFilterTypesAll = false;
        if(sourceFile.getFiltertypes()!=null&&(sourceFile.getNotfiltertypes()==null||sourceFile.getNotfiltertypes().equals(""))){
            filterType = sourceFile.getFiltertypes();
            isFilterTypesAll = filterType.equals("*.*");
            if(isFilterTypesAll){
                return true;
            } else {
                String[] filterTypes = filterType.split(",");
                if(filterTypes.length>1){
                    for(int i = 0 ; i < filterTypes.length;i++){
                        isFilterTypes = fileName.endsWith(filterTypes[i].substring(filterTypes[i].lastIndexOf(".")));
                        if(isFilterTypes){
                            return true;
                        }
                    }
                }else if(filterTypes.length == 1){
                    isFilterTypes = fileName.endsWith(filterType.substring(filterType.lastIndexOf(".")));
                    if(isFilterTypes){
                        return true;
                    }
                }
            }
        }
        if(sourceFile.getNotfiltertypes()!=null&& (sourceFile.getFiltertypes()==null||sourceFile.getFiltertypes().equals(""))){
            filterType = sourceFile.getNotfiltertypes();
            isFilterTypesAll = filterType.equals("*.*");
            if(isFilterTypesAll){
                return false;
            } else {
                String[] filterTypes = filterType.split(",");
                if(filterTypes.length>1){
                    int flag = 0;
                    for(int i = 0 ; i < filterTypes.length;i++){
                        isFilterTypes = fileName.endsWith(filterTypes[i].substring(filterTypes[i].lastIndexOf(".")));
                        if(isFilterTypes){
                            flag ++;
                        }
                    }
                    if (flag == 0){
                        return true;
                    }
                }else if(filterTypes.length == 1){
                    isFilterTypes = fileName.endsWith(filterType.substring(filterType.lastIndexOf(".")));
                    if(!isFilterTypes){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
