package com.utils.cal;

import java.math.BigDecimal;
import java.util.List;

public interface IAnalysisFunc {
	
	public List<BigDecimal> onProcess(double[][] matrix);
	
	public int scale = 19;
	
	public int roundingMode = BigDecimal.ROUND_HALF_UP;
	
}
