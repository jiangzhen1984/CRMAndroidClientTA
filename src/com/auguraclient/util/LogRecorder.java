package com.auguraclient.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogRecorder {
	
	public static final int TAG_INFO = 2;
	
	private static LogRecorder mRecorder;
	
	private LogRecorder() {
		
	}
	
	public static LogRecorder getInstance() {
		if(mRecorder == null) {
			mRecorder = new LogRecorder();
		}
		return mRecorder;
	}
	
	
	public void log(int tag, String log) {
		//FIXME should use buffer
		try {
			FileWriter fw =new FileWriter(new File(GlobalHolder.getInstance().getStoragePath()+Constants.CommonConfig.LOG_DIR+"aug.log"), true);
			fw.write(log);
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void i(String str) {
		LogRecorder.getInstance().log(LogRecorder.TAG_INFO, str);
	}

}
