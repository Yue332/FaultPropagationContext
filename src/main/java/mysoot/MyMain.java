package mysoot;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.utils.Utils;

import mysoot.bean.Bean;
import mysoot.bean.GetLineNumberBean;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.Constant;
import soot.jimple.internal.JNopStmt;
import soot.options.Options;
import soot.tagkit.InnerClassTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;


public class MyMain {
	public static void setSootEnv(String projectPath, String projectId, String bugId)throws Exception {
		Options.v().set_src_prec(Options.src_prec_java);
		Options.v().set_keep_line_number(true);
		Options.v().set_whole_program(true);
		
//		System.out.println("java.home = " + System.getProperty("java.home"));
//		System.out.println("user.home = " + System.getProperty("user.home"));
		String[] bootpaths = System.getProperty("sun.boot.class.path").split(File.pathSeparator);
//		System.out.println("java.boot.class.path = " + Arrays.toString(bootpaths));
		StringBuilder path = new StringBuilder();
		for(String bootPath: bootpaths) {
			if(bootPath.contains("rt.jar") || bootPath.contains("jce.jar")) {
				path.append(bootPath).append(File.pathSeparator);
			}
		}
//		System.out.println("java.class.path = " + Arrays.toString(System.getProperty("java.class.path").split(File.pathSeparator)));
		setJar2Path(path, projectId, bugId);
		// closure 需要取 lib下的jar包 和 build/lib下的rhino.jar
		if("Closure".equals(projectId)) {
			File dir = new File(projectPath + File.separator + "lib" + File.separator);
			File[] jarList = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			for(File jar : jarList) {
				path.append(jar.getAbsolutePath()).append(File.pathSeparator);
			}
			dir = new File(projectPath + File.separator + "build" + File.separator + "lib" + File.separator);
			if (dir.exists()) {
				jarList = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				});
				for(File jar : jarList) {
					path.append(jar.getAbsolutePath()).append(File.pathSeparator);
				}
			}
			
		}
		
//	    String javapath = System.getProperty("java.class.path");
//	    path.append(javapath).append(File.pathSeparator).append(projectPath);
		path.append(projectPath + File.separator + Utils.getCompilePathByProjectID(projectId, bugId));
	    System.out.println(path);
	    Scene.v().setSootClassPath(path.toString());
	    
	}
	
	public static void main(String[] args) throws Exception {
//		setSootEnv("C:\\Users\\44789\\Desktop\\Lang_23\\target\\classes\\");
//		doMyAnalysis("org.apache.commons.lang3.text.ExtendedMessageFormat", 151);
		
//		setSootEnv("/home/yy/Locate_buggylines/SBFL-Closure/Closure_1/", "Closure", "1");
		setSootEnv("C:\\Users\\86186\\Desktop\\Chart_2\\", "Chart", "2");
		doMyAnalysis("org.jfree.data.xy.XYIntervalSeriesCollection", 73);
	}
	
	public static String analysis(StringBuilder info, String clzName, int lineNum) {
		SootClass baseClz = Scene.v().loadClassAndSupport(clzName);//加载待分析的类
		Scene.v().loadNecessaryClasses();
		List<SootClass> clzList = new ArrayList<SootClass>();
		clzList.add(baseClz);
		List tagList = baseClz.getTags();
		for(Object o : tagList) {
			if(o instanceof InnerClassTag) {
				InnerClassTag tag = (InnerClassTag) o;
				String innerClz = tag.getInnerClass();
				innerClz = innerClz.replace("/", ".");
				SootClass innerClass = Scene.v().loadClassAndSupport(innerClz);
				clzList.add(innerClass);
			}
		}
		boolean canFindMethod = false;
		for(SootClass clz : clzList) {
			for(SootMethod method : clz.getMethods()) {
				if(method.isAbstract()) {
					continue;
				}
				Body body = method.retrieveActiveBody();
				for(Unit unit : body.getUnits()) {
					if(lineNum == getLineNumber(unit)) {
						canFindMethod = true;
//						System.out.println("[INFO] 类["+clzName+"]行号["+lineNum+"]对应方法为["+method.getName()+"]");
						info.append("[INFO] 类["+clz.getName()+"]行号["+lineNum+"]对应方法为["+method.getName()+"]").append("\r\n");
						return clz.getName() + "," + method.getName();
					}
				}
			}
		}
		if(!canFindMethod) {
//				System.out.println("[WARING] 类["+clzName+"]行号["+lineNum+"]未找到对应方法！！！");
			info.append("类["+clzName+"]行号["+lineNum+"]未找到对应方法！！！").append("\r\n");
		}
		return null;
	}

    public static void dataDepBetweenClasses(String clzName, int lineNum){
        SootClass sootClass = Scene.v().loadClassAndSupport(clzName);//加载待分析的类
        Scene.v().loadNecessaryClasses();
        List<SootMethod> methodList = sootClass.getMethods();
        methodList.removeIf(SootMethod::isAbstract);
        methodList.forEach(method -> {
            Body body = method.retrieveActiveBody();
            for(Unit unit : body.getUnits()){
                if(lineNum != getLineNumber(unit) || unit instanceof JNopStmt || method.getReturnType() instanceof VoidType && unit.toString().equals("return")){
                    continue;
                }

            }
        });
    }
	
	public static List<String> doMyAnalysis(String clzName, int lineNum) {
		Map<SootMethod, List<Value>> m = new HashMap<SootMethod, List<Value>>();
		List<Value> memberParamList = new ArrayList<Value>();
		SootClass sootClass = Scene.v().loadClassAndSupport(clzName);//加载待分析的类
		Scene.v().loadNecessaryClasses();
		for(SootMethod method : sootClass.getMethods()) {
			if(method.isAbstract()) {
//				System.out.println("[INFO] method : ["+method.toString()+"]为抽象方法，无法分析！！！");
				continue;
			}
			Body body = method.retrieveActiveBody();
			for(Unit unit : body.getUnits()) {
				if(lineNum == getLineNumber(unit)) {
					if(unit instanceof JNopStmt) {
//						System.out.println(String.format("unit [%s] 为JNopStmt，不做处理", unit.toString()));
						continue;
					}else if(method.getReturnType() instanceof VoidType && unit.toString().equals("return")) {
//						System.out.println("unit ["+unit.toString()+"] 为return，不做处理");
						continue;
					}
//					System.out.println("unit ["+unit.toString()+"]对应源代码行号["+lineNum+"]");
					
					Bean b = new Bean(sootClass, method, unit);
					b.analysis();
					Map<SootMethod, List<Value>> map = b.getMap();

					for(Entry<SootMethod, List<Value>> entry : map.entrySet()) {
						MyMain.addMap(m, entry.getKey(), entry.getValue());
					}
					
					List<Value> memberList = b.getMemberParamList();
					for(Value v : memberList) {
						MyMain.addList(memberParamList, v);
					}

					//类间分析
					betweenClzAnalysis(b.otherClzMethodList);
				}
			}
		}
//		System.out.println("------------------------");
//		for(Entry<SootMethod, List<Value>> entry : m.entrySet()) {
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue().toString());
//		}
//		System.out.println("-----------------------");
//		System.out.println(memberParamList.toString());
		GetLineNumberBean b = new GetLineNumberBean(sootClass, m, memberParamList);
//		System.out.println("行号：" + b.get().toString());
		
		return b.get();
	}

	public static void betweenClzAnalysis(List<SootMethod> methodList){
		if(CollectionUtils.isNotEmpty(methodList)){
			methodList.forEach(sootMethod -> {
				SootClass sootClass = sootMethod.getDeclaringClass();
				for (SootMethod method : sootClass.getMethods()){
					if (sootMethod.equals(method)){
						Unit unit = method.retrieveActiveBody().getThisUnit();
						System.out.println(getLineNumber(unit));
					}
				}
			});
		}
	}
	
	public static int getLineNumber(Unit u) {
		List<Tag> tagList = u.getTags();
		for(Tag t : tagList) {
			if(t instanceof LineNumberTag) {
				return ((LineNumberTag) t).getLineNumber();
			}
		}
		return -1;
	}
	
	public static Integer[] getNoConstantParamsIdx(List<Value> list) {
		List<Integer> l = new ArrayList<Integer>();
		int idx = 0;
		for(Value v : list) {
			if(v instanceof Constant) {
//				System.out.println("参数["+v.toString()+"]为常量，不进行分析");
			}else {
				l.add(idx);
			}
			idx ++;
		}
		return l.toArray(new Integer[0]);
	}
	
	public static void addList(List<String> list, String obj) {
		if(!list.contains(obj)) {
			list.add(obj);
		}
	}
	
	public static void addList(List<ValueBox> list, ValueBox obj) {
		if(!list.contains(obj)) {
			list.add(obj);
		}
	}
	
	public static void addList(List list, Object obj){
		if(!list.contains(obj)) {
			list.add(obj);
		}
	}
	
	public static void addMap(Map<SootMethod, List<Value>> m, SootMethod method, List<Value> oldList) {
		List<Value> newList;
		if(m.containsKey(method)) {
			newList = m.get(method);
			for(Value v : oldList) {
				MyMain.addList(newList, v);
			}
		}else {
			newList = oldList;
		}
		m.put(method, newList);
	}


	//需要引用额外jar包项目集合，如果还有再继续添加
	public static List<String> DEAL_LIST = Arrays.asList(new String[] {"Mockito","Math","Time","Gson"});
	
	public static void setJar2Path(StringBuilder path, String projectId, String bugId)throws Exception{
		if(!DEAL_LIST.contains(projectId)) {
			return;
		}
		File dir = new File(System.getProperty("user.home") + File.separator + "libs" + File.separator + projectId);
		if(!dir.exists()) {
			throw new Exception("[ERROR] 项目"+projectId+"需要额外jar包，请将jar包放至目录：" + dir.getAbsolutePath());
		}
		File[] jarList = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		for(File jarFile : jarList) {
			path.append(jarFile.getAbsolutePath()).append(File.pathSeparator);
		}
	}
}
