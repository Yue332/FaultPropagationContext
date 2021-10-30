package com.finalmodule;

import java.io.File;

import com.finalmodule.base.IFinalProcessModule;

public class CalculateFuncPercentageOfTotalLine extends CalculatePercentageOfTotalLines implements IFinalProcessModule {
	
	@Override
    protected String getTopNCsvPath(String projectPath) {
    	return projectPath + File.separator + "TopN_SBFL" + File.separator;
    }
    
	@Override
    protected String getTopNCsvNameEnd(String func) {
    	return "-" + func + ".csv";
    }
    
	@Override
    protected String getOutputPath() {
    	return System.getProperty("user.home") + File.separator + "SBFL" + File.separator;
    }
    
	@Override
    protected String getOuputFileName(String func) {
    	return func + "_Percentage_of_Total_lines.csv";
    }
}
