package com.utils.cal.func;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.utils.cal.IAnalysisFunc;

public class GP03 implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();
         
        for (int i = 0; i < matrix.length; i++) {
        	double temp = Math.sqrt( Math.abs( Math.pow(matrix[i][1],2) - Math.sqrt(matrix[i][0]) ) );
            suspValue.add(i,new BigDecimal(temp));
        }
        return suspValue;
	}

}
