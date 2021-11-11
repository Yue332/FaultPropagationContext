package com.utils.cal;

import java.util.LinkedHashMap;
import java.util.Map;

import com.utils.ConfigUtils;
import com.utils.Configer;


public class TFuncRegister {
	public static LinkedHashMap<String, IAnalysisFunc> FUNC_MAP = null;

	public static LinkedHashMap<String, IAnalysisFunc> getRegistClass(Configer config) throws Exception{
		if(FUNC_MAP == null || FUNC_MAP.size() == 0) {
			String[] funcArr = config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
			FUNC_MAP = new LinkedHashMap<String, IAnalysisFunc>();
			for(String func : funcArr) {
				Class clz = Class.forName(func);
				Object o = clz.newInstance();
				if(!(o instanceof IAnalysisFunc)) {
					throw new Exception("注册方法类【"+func+"】需要实现" + IAnalysisFunc.class.getName());
				}
				FUNC_MAP.put(clz.getSimpleName(), (IAnalysisFunc) o);
			}
		}
		return FUNC_MAP;
	}

	public static LinkedHashMap<String, IAnalysisFunc> getRegistClass(Configer config, String configKey) throws Exception{
		String[] funcArr = config.getConfig(configKey).split(",");
		LinkedHashMap<String, IAnalysisFunc> funcMap = new LinkedHashMap<>(funcArr.length);
		for(String func : funcArr) {
			Class<?> clz = Class.forName(func);
			Object o = clz.newInstance();
			if(!(o instanceof IAnalysisFunc)) {
				throw new Exception("注册方法类【"+func+"】需要实现" + IAnalysisFunc.class.getName());
			}
			funcMap.put(clz.getSimpleName(), (IAnalysisFunc) o);
		}
		return funcMap;
	}
}
