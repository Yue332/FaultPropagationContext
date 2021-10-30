package com.utils.cal.func;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.utils.cal.IAnalysisFunc;

public class GP19 implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
         
        for (int i = 0; i < matrix.length; i++) {
            //化简后公式
        	double temp = matrix[i][1]* Math.sqrt(Math.abs(matrix[i][3] - matrix[i][2]));
            suspValue.add(i,new BigDecimal(temp));
        }
        return suspValue;
	}

}
