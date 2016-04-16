package com.fartec.ichange.plugin.filechange.utils;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-3
 * Time: 5:45:03
 * To change this template use File | Settings | File Templates.
 */
public class FileFilter implements java.io.FileFilter {
    private Map filetypeset = new HashMap();
    private long synctime = System.currentTimeMillis();

    public void config(Map filetypeset, long synctime) {
        this.filetypeset = filetypeset;
        this.synctime = synctime;
    }

    public boolean accept(File dir) {
        boolean result = false;
        if (dir.isFile()) {
            if (dir.lastModified() > synctime) {
                result = true;
            }
            if (existSubString(dir.getAbsolutePath(), ".")) {
                String type = getEndSubString(dir.getAbsolutePath(), ".");
                String type1 = (String) filetypeset.get(type);
                if (type1 != null) {
                    result = result && true;
                }
            }
        }
        return result;
    }

    public static boolean existSubString(String str, String separator) {
        int i = str.indexOf(separator);
        if (i != -1)
            return true;
        else
            return false;
    }

    public static String getSubString(String str, String separator) {
        int i = str.lastIndexOf(separator);
        if (i != -1)
            return str.substring(i + separator.length(), str.length());
        else
            return "";
    }

    public static String getFirstSubString(String str, String separator) {
        int i = str.lastIndexOf(separator);
        if (i != -1)
            return str.substring(0, i + 1);
        else
            return "";
    }
    public static String getFirstNewSubString(String str, String separator) {
        int i = str.lastIndexOf(separator);
        if (i != -1)
            return str.substring(0, i );
        else
            return "";
    }

    public static String getEndSubString(String str, String separator) {
        int i = str.lastIndexOf(separator);
        if (i != -1) {
            return str.substring(i + 1);
        } else {
            return "";
        }

    }
    public static String getEnd_NewSubString(String str, String separator) {
        int i = str.lastIndexOf(separator);
        if (i != -1) {
            return str.substring(i);
        } else {
            return "";
        }

    }
}
