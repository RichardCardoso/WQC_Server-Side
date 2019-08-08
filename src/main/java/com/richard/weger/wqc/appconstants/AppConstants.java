package com.richard.weger.wqc.appconstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AppConstants{
	
	protected AppConstants() {}
	
	protected int id=1;
	
    protected String FILE_EXTENSION = "json";
    protected String CONFIG_FILE_NAME = "configs";
    protected String CONTROLCARDREPORT_FILENAME = "kontrollkarte.xls";

    protected int CCRF_FIRSTLINE = 18;
    protected int CCRF_IO = 12;
    protected int CCRF_NIO = 13;
    protected int CCRF_NA = 14;
    protected int CCRF_COMMENTS = 15;
    @SuppressWarnings("serial")
	protected List<Integer> CCRF_JUMPONEMORE = new ArrayList<Integer>(){
        { add(36); }
        { add(40); }
    };
    protected int CCRF_CLIENTCOLUMN = 4;
    protected int CCRF_CLIENTROW = 5;
    protected int CCRF_COMMISSIONCOLUMN = 4;
    protected int CCRF_COMMISSIONROW = 7;
    protected int CCRF_DRAWINGCOLUMN = 19;
    protected int CCRF_DRAWINGROW = 5;
    protected int CCRF_PARTCOLUMN = 19;
    protected int CCRF_PARTROW = 7;
    protected int CCRF_SIGNATURECOL = 14;
    protected int CCRF_SIGNATURELINE = 48;
    protected int CCRF_REPORTCOMMENTSCOLUMN = 1;
    protected int CCRF_REPORTCOMMENTSROW = 43;
    protected int CCRF_REPORTRESPONSIBLECOLUMN = 14;
    protected int CCRF_REPORTRESPONSIBLEROW = 48;
    protected int CCRF_REPORTDATECOLUMN = 6;
    protected int CCRF_REPORTDATEROW = 48;

    protected String PICTURES_AUTHORITY = "com.richard.weger.wegerqualitycontrol.fileprovider";
    protected String CONSTRUCTION_PATH_KEY = "constructionPath";
    protected String TECHNICAL_PATH_KEY = "technicalPath";
    protected String COMMON_PATH_KEY = "commonPath";
    protected String PROJECT_NUMBER_KEY = "projectNumber";
    protected String DRAWING_NUMBER_KEY = "drawingNumber";
    protected String PART_NUMBER_KEY = "partNumber";
    protected String ITEM_LIST_KEY = "projectItemList";
    protected String ITEM_KEY = "projectItem";
    protected String ITEM_ID_KEY = "itemIdKey";
    protected String REPORT_KEY = "singleReport";
    protected String PROJECT_KEY = "singleProject";
    protected String CAMERA_PERMISSION = "android.permission.CAMERA";
    protected String EXTERNAL_DIR_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    protected String DATA_KEY = "data";
    protected String CLOSE_REASON = "closeReason";
    protected String DOCUMENT_TYPE_KEY = "documentType";
    protected String DOCUMENT_KEY = "documentKey";
    protected String FILE_PATH_KEY = "filePath";
    protected String DOCUMENT_HASH_POINTS_KEY = "documentHashPoints";
    protected String MAP_VALUES_KEY = "mapValues";

    protected int AUTOMATION_COMPONENTS_REPORT_ID = 0;
    protected int CONTROL_CARD_REPORT_ID = 1;
    protected int ELETRIC_REPORT_ID = 2;
    protected int FACTORY_TEST_REPORT_ID = 3;
    protected int REQUEST_IMAGE_CAPTURE_ACTION = 4;
    protected int PICTURE_VIEWER_SCREEN_ID = 5;
    protected int PROJECT_FINISH_SCREEN_ID = 6;
    protected int SOURCE_SELECTION_SCREEN_KEY = 7;
    protected int INTRINSIC_PERMISSIONS_CODE = 8;
    protected int CONTROL_CARD_REPORT_EDIT_SCREEN_KEY = 9;
    protected int CONTINUE_PROJECT_SCREEN_KEY = 10;
    protected int CONFIG_SCREEN_KEY = 11;
    protected int CLOSE_REASON_USER_FINISH = 12;
    protected int DOCUMENT_MARK_SCREEN = 13;

    protected String SOURCE_CODE_KEY = "sourceCodeKey";
    protected String SOURCE_CODE_QR = "sourceCodeQr";
    protected String SOURCE_CODE_CONTINUE = "sourceCodeContinue";
    protected String SOURCE_FROMSERVER = "sourceCodeFromServer";
    protected String CONTROL_CARD_REPORT_FILE_KEY = "controlCardReportFileKey";

    protected String QR_CODE_KEY = "qr_code_text";
    protected String CONTINUE_CODE_KEY = "continue_code_text";

    protected SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'T'");

    // 0 - not started
    // 1 - approved
    // 2 - reproved
    // 3 - not applicable
    protected int ITEM_NOT_CHECKED_KEY = 0;
    protected int ITEM_APROVED_KEY = 1;
    protected int ITEM_NOT_APROVED_KEY = 2;
    protected int ITEM_NOT_APLICABLE_KEY = 3;
    
	public int getId() {
		return id;
	}
	public String getFILE_EXTENSION() {
		return FILE_EXTENSION;
	}
	public String getCONFIG_FILE_NAME() {
		return CONFIG_FILE_NAME;
	}
	public String getCONTROLCARDREPORT_FILENAME() {
		return CONTROLCARDREPORT_FILENAME;
	}
	public int getCCRF_FIRSTLINE() {
		return CCRF_FIRSTLINE;
	}
	public int getCCRF_IO() {
		return CCRF_IO;
	}
	public int getCCRF_NIO() {
		return CCRF_NIO;
	}
	public int getCCRF_NA() {
		return CCRF_NA;
	}
	public int getCCRF_COMMENTS() {
		return CCRF_COMMENTS;
	}
	public List<Integer> getCCRF_JUMPONEMORE() {
		return CCRF_JUMPONEMORE;
	}
	public int getCCRF_CLIENTCOLUMN() {
		return CCRF_CLIENTCOLUMN;
	}
	public int getCCRF_CLIENTROW() {
		return CCRF_CLIENTROW;
	}
	public int getCCRF_COMMISSIONCOLUMN() {
		return CCRF_COMMISSIONCOLUMN;
	}
	public int getCCRF_COMMISSIONROW() {
		return CCRF_COMMISSIONROW;
	}
	public int getCCRF_DRAWINGCOLUMN() {
		return CCRF_DRAWINGCOLUMN;
	}
	public int getCCRF_DRAWINGROW() {
		return CCRF_DRAWINGROW;
	}
	public int getCCRF_PARTCOLUMN() {
		return CCRF_PARTCOLUMN;
	}
	public int getCCRF_PARTROW() {
		return CCRF_PARTROW;
	}
	public int getCCRF_SIGNATURECOL() {
		return CCRF_SIGNATURECOL;
	}
	public int getCCRF_SIGNATURELINE() {
		return CCRF_SIGNATURELINE;
	}
	public int getCCRF_REPORTCOMMENTSCOLUMN() {
		return CCRF_REPORTCOMMENTSCOLUMN;
	}
	public int getCCRF_REPORTCOMMENTSROW() {
		return CCRF_REPORTCOMMENTSROW;
	}
	public int getCCRF_REPORTRESPONSIBLECOLUMN() {
		return CCRF_REPORTRESPONSIBLECOLUMN;
	}
	public int getCCRF_REPORTRESPONSIBLEROW() {
		return CCRF_REPORTRESPONSIBLEROW;
	}
	public int getCCRF_REPORTDATECOLUMN() {
		return CCRF_REPORTDATECOLUMN;
	}
	public int getCCRF_REPORTDATEROW() {
		return CCRF_REPORTDATEROW;
	}
	public String getPICTURES_AUTHORITY() {
		return PICTURES_AUTHORITY;
	}
	public String getCONSTRUCTION_PATH_KEY() {
		return CONSTRUCTION_PATH_KEY;
	}
	public String getTECHNICAL_PATH_KEY() {
		return TECHNICAL_PATH_KEY;
	}
	public String getCOMMON_PATH_KEY() {
		return COMMON_PATH_KEY;
	}
	public String getPROJECT_NUMBER_KEY() {
		return PROJECT_NUMBER_KEY;
	}
	public String getDRAWING_NUMBER_KEY() {
		return DRAWING_NUMBER_KEY;
	}
	public String getPART_NUMBER_KEY() {
		return PART_NUMBER_KEY;
	}
	public String getITEM_LIST_KEY() {
		return ITEM_LIST_KEY;
	}
	public String getITEM_KEY() {
		return ITEM_KEY;
	}
	public String getITEM_ID_KEY() {
		return ITEM_ID_KEY;
	}
	public String getREPORT_KEY() {
		return REPORT_KEY;
	}
	public String getPROJECT_KEY() {
		return PROJECT_KEY;
	}
	public String getCAMERA_PERMISSION() {
		return CAMERA_PERMISSION;
	}
	public String getEXTERNAL_DIR_PERMISSION() {
		return EXTERNAL_DIR_PERMISSION;
	}
	public String getDATA_KEY() {
		return DATA_KEY;
	}
	public String getCLOSE_REASON() {
		return CLOSE_REASON;
	}
	public String getDOCUMENT_TYPE_KEY() {
		return DOCUMENT_TYPE_KEY;
	}
	public String getDOCUMENT_KEY() {
		return DOCUMENT_KEY;
	}
	public String getFILE_PATH_KEY() {
		return FILE_PATH_KEY;
	}
	public String getDOCUMENT_HASH_POINTS_KEY() {
		return DOCUMENT_HASH_POINTS_KEY;
	}
	public String getMAP_VALUES_KEY() {
		return MAP_VALUES_KEY;
	}
	public int getAUTOMATION_COMPONENTS_REPORT_ID() {
		return AUTOMATION_COMPONENTS_REPORT_ID;
	}
	public int getCONTROL_CARD_REPORT_ID() {
		return CONTROL_CARD_REPORT_ID;
	}
	public int getELETRIC_REPORT_ID() {
		return ELETRIC_REPORT_ID;
	}
	public int getFACTORY_TEST_REPORT_ID() {
		return FACTORY_TEST_REPORT_ID;
	}
	public int getREQUEST_IMAGE_CAPTURE_ACTION() {
		return REQUEST_IMAGE_CAPTURE_ACTION;
	}
	public int getPICTURE_VIEWER_SCREEN_ID() {
		return PICTURE_VIEWER_SCREEN_ID;
	}
	public int getPROJECT_FINISH_SCREEN_ID() {
		return PROJECT_FINISH_SCREEN_ID;
	}
	public int getSOURCE_SELECTION_SCREEN_KEY() {
		return SOURCE_SELECTION_SCREEN_KEY;
	}
	public int getINTRINSIC_PERMISSIONS_CODE() {
		return INTRINSIC_PERMISSIONS_CODE;
	}
	public int getCONTROL_CARD_REPORT_EDIT_SCREEN_KEY() {
		return CONTROL_CARD_REPORT_EDIT_SCREEN_KEY;
	}
	public int getCONTINUE_PROJECT_SCREEN_KEY() {
		return CONTINUE_PROJECT_SCREEN_KEY;
	}
	public int getCONFIG_SCREEN_KEY() {
		return CONFIG_SCREEN_KEY;
	}
	public int getCLOSE_REASON_USER_FINISH() {
		return CLOSE_REASON_USER_FINISH;
	}
	public int getDOCUMENT_MARK_SCREEN() {
		return DOCUMENT_MARK_SCREEN;
	}
	public String getSOURCE_CODE_KEY() {
		return SOURCE_CODE_KEY;
	}
	public String getSOURCE_CODE_QR() {
		return SOURCE_CODE_QR;
	}
	public String getSOURCE_CODE_CONTINUE() {
		return SOURCE_CODE_CONTINUE;
	}
	public String getSOURCE_FROMSERVER() {
		return SOURCE_FROMSERVER;
	}
	public String getCONTROL_CARD_REPORT_FILE_KEY() {
		return CONTROL_CARD_REPORT_FILE_KEY;
	}
	public String getQR_CODE_KEY() {
		return QR_CODE_KEY;
	}
	public String getCONTINUE_CODE_KEY() {
		return CONTINUE_CODE_KEY;
	}
	public SimpleDateFormat getSDF() {
		return SDF;
	}
	public int getITEM_NOT_CHECKED_KEY() {
		return ITEM_NOT_CHECKED_KEY;
	}
	public int getITEM_APROVED_KEY() {
		return ITEM_APROVED_KEY;
	}
	public int getITEM_NOT_APROVED_KEY() {
		return ITEM_NOT_APROVED_KEY;
	}
	public int getITEM_NOT_APLICABLE_KEY() {
		return ITEM_NOT_APLICABLE_KEY;
	}
}
