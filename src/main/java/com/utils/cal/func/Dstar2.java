package com.utils.cal.func;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.utils.cal.IAnalysisFunc;


public class Dstar2 implements IAnalysisFunc {

	@Override
	public List<BigDecimal> onProcess(double[][] matrix) {
        List<BigDecimal> suspValue = new ArrayList<BigDecimal>();  
        
        for (int i = 0; i < matrix.length; i++) {
            if(0 == (matrix[i][0] + matrix[i][3])){
                suspValue.add(i, new BigDecimal("0"));
            }else{
            	
            	double temp = Math.pow(matrix[i][1],2) / (matrix[i][0] + matrix[i][3]);
            	               
                suspValue.add(i,new BigDecimal(temp));
            }
        }
        return suspValue;
	}

}
