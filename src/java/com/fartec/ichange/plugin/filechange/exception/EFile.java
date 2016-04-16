package com.fartec.ichange.plugin.filechange.exception;

import com.inetec.common.i18n.Key;
import com.inetec.common.exception.ErrorCode;

/**
 * Created by IntelliJ IDEA.
 * User: bluesky
 * Date: 2008-3-3
 * Time: 3:57:37
 * To change this template use File | Settings | File Templates.
 */
public class EFile extends ErrorCode {
     public static final int I_NOERROR = 0;
    public static final Key K_NOERROR = new Key("NO ERROR.");
    public static final EFile E_NOERROR = new EFile(I_NOERROR);

    public static final int I_DataFormatError = -10022;
    public static final Key K_DataFormatError = new Key("Data Format Error.");
    public static final EFile E_DataFormatError = new EFile(I_DataFormatError);

    public static final int I_UNKNOWN = -1;
    public static final Key K_UNKNOWN = new Key("UNKNOWN");
    public static final EFile E_UNKNOWN = new EFile(I_UNKNOWN);

    public static final int I_NOTIMPLEMENTED = -6;
    public static final Key K_NOTIMPLEMENTED = new Key("NOT IMPLEMENTED");
    public static final EFile E_NOTIMPLEMENTED = new EFile(I_NOTIMPLEMENTED);

    // General errors: 10010

    public static final int I_GE_NullString = 10011;
    public static final Key K_Ge_NullString = new Key("Get String is Null");
    public static final EFile E___GE___NULL_STRING = new EFile(I_GE_NullString);

    public static final int I_GE_IndexOutOfRange = 10012;
    public static final Key K_GE_IndexOutOfRange = new Key("Get String Inddex Out of Range.");
    public static final EFile E___GE___INDEX_OUT_OF_RANGE = new EFile(I_GE_IndexOutOfRange);

    // NodeInfo: 10020


    public static final int I_CF_AlreadyConfigured = 10035;
    public static final Key K_CF_AlreadyConfigured = new Key("Already Configured.");
    public static final EFile E___CF___ALREADY_CONFIGURED = new EFile(I_CF_AlreadyConfigured);

    public static final int I_CF_Failed = 10036;
    public static final Key K_CF_Failed = new Key("Config faild.");
    public static final EFile E___CF___FAILD = new EFile(I_CF_Failed);


    public static final int I_CF_VariableNotFound = 10037;
    public static final Key K_CF_VariableNotFound = new Key("Config Variable Not Found");
    public static final EFile E___CF___VARIABLE_NOT_FOUND = new EFile(I_CF_VariableNotFound);

    public static final int I_CF_NullConfigData = 10038;
    public static final Key K_CF_NullConfigData = new Key("Config data is null.");
    public static final EFile E___CF___NULL_CONFIG_DATA = new EFile(I_CF_NullConfigData);

    public static final int I_CF_InterfaceNotImplemented = 10039;
    public static final Key K_CF_InterfaceNotImplemented = new Key("Interfac not implemented.");
    public static final EFile E___CF___INTERFACE_NOT_IMPLEMENTED = new EFile(I_CF_InterfaceNotImplemented);

    public static final int I_CF_NotConfigured = 10040;
    public static final Key K_CF_NotConfigured = new Key("Not Configured.");
    public static final EFile E___CF___NOT_CONFIGURED = new EFile(I_CF_NotConfigured);


    public static final int I_NetWorkError = -10012;
    public static final Key K_NetWorkError = new Key("Network Error.");
    public static final EFile E___NET_WORK_ERROR = new EFile(I_NetWorkError);

    public static final int I_TargetProcessError = -10013;
    public static final Key K_TargetProcessError = new Key("Target process Error.");
    public static final EFile E___TARGET_PROCESS_ERROR = new EFile(I_TargetProcessError);
    public static final int I_DataIsNullError = -10014;
    public static final Key K_DataIsNullError = new Key("Data Is Null.");
    public static final EFile E___DATA_IS_NULL_ERRORR = new EFile(I_DataIsNullError);

    public static final int I_CancelledKeyException = -10016;
    public static final Key K_CancelledKeyException = new Key("CancelledKey Exception.");
    public static final EFile E___CANCELLED_KEY_EXCEPTION = new EFile(I_CancelledKeyException);

    private EFile(int i) {
        super(i);
    }
}
