package com.finalmodule.base;

import java.util.List;

import com.utils.Configer;

public interface IFinalProcessModule {
	public void process(Runtime runTime, StringBuilder processLog)throws Exception;
	
	public void setConfig(Configer conf);
	
	public void setFailBugId(List<String> failBugIdList);
	
	public void onPrepare();
}
