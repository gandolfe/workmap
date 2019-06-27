package com.nfsw.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	private static final String QueryDBName = "nfsw";
	private static final String CREATE_SQL1 = "create table if not exists `ab_signed` ( `strCheckPointNo`  varchar2,"
			+ " `strWorkerNo` 	varchar2, `strCheckPointPosition` varchar2, `strDevicePosition` varchar2,"
			+ " `fDistanceToCheckPoint` varchar2, `strDeviceSignDatetime` varchar2, `strToken` varchar2);";
	private static final String CREATE_SQL2 = "create table if not exists `ab_exception` ( `strWorkerNo`  varchar2,"
			+ " `strDeviceMAC` 	varchar2, `strDevicePosition` varchar2, `strExPosName` varchar2,"
			+ " `strDeviceDatetime` varchar2, `strExceptionPosition` varchar2, `TypeNo` varchar2,"
			+ "`Type` varchar2, `strExcepDesc` varchar2, `picName` varchar2,`picBytes` text, `voiceName` varchar2,"
			+ "`voiceBytes` text, `strToken` varchar2);";
	private static final String CREATE_SQL3 = "create table if not exists `ab_cmd` ( `strDeviceMAC` varchar2, "
			+ "`strStatusDesc` varchar2, `strDeviceDatetime` varchar2);";
	private static final String CREATE_SQL4 = "create table if not exists `ab_plandata` ( `checkpointno` varchar2, "
			+ "`checkpointname` varchar2, `checkpointlng` varchar2,`checkpointlat` varchar2,`checkflag` varchar2);";
	
	public DBHelper(Context context,String uerID) {
		super(context, QueryDBName+uerID+".db", null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SQL1);
		db.execSQL(CREATE_SQL2);
		db.execSQL(CREATE_SQL3);
		db.execSQL(CREATE_SQL4);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
