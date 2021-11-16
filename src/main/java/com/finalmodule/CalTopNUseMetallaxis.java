package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.cal.topn.TopNCalculator;

import java.io.File;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/10
 * @description:
 **/
public class CalTopNUseMetallaxis extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        TopNCalculator calculator = new TopNCalculator(config);
        String suspValueFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                "@PROJECT@" + File.separator + "@PROJECT@" + "-" + "@BUG@" + "-MetallaxisSuspValue.csv";;
        calculator.calculate(suspValueFilePath, processLog);
    }
}
