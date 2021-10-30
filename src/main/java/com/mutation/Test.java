package com.mutation;

import java.util.Arrays;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.Utils;

public class Test extends FinalBean implements IFinalProcessModule{

	@Override
	public void process(Runtime runTime) throws Exception {
		String[] command = config.getConfig("COMMAND").split(",");
		String[] ret = Utils.executeCommandLine(runTime, command);
		System.out.println(Arrays.toString(ret));
		
	}

}
