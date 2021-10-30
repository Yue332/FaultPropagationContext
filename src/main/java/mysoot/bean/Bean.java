package mysoot.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mysoot.MyMain;
import soot.Body;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.ParamNamesTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class Bean {
	private SootClass sootClass;
	private SootMethod sootMethod;
	private Unit unit;
	private Map<SootMethod, List<Value>> map; // 需要分析的方法-参数列表
	private List<Value> memberParamList;// 成员变量
	
	public Bean(SootClass sootClass, SootMethod sootMethod, Unit unit) {
		super();
		this.sootClass = sootClass;
		this.sootMethod = sootMethod;
		this.unit = unit;
		this.map = new HashMap<SootMethod, List<Value>>();
		this.memberParamList = new ArrayList<Value>();
		
	}
	public SootClass getSootClass() {
		return sootClass;
	}
	public void setSootClass(SootClass sootClass) {
		this.sootClass = sootClass;
	}
	public SootMethod getSootMethod() {
		return sootMethod;
	}
	public void setSootMethod(SootMethod sootMethod) {
		this.sootMethod = sootMethod;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Map<SootMethod, List<Value>> getMap() {
		return map;
	}
	public void setMap(Map<SootMethod, List<Value>> map) {
		this.map = map;
	}
	
	public List<Value> getMemberParamList() {
		return memberParamList;
	}
	public void setMemberParamList(List<Value> memberParamList) {
		this.memberParamList = memberParamList;
	}
	public void addMap(SootMethod method, Value value) {
		List<Value> list;
		if(this.map.containsKey(method)) {
			list = this.map.get(method);
		}else {
			list = new ArrayList<Value>();
		}
		MyMain.addList(list, value);
		this.map.put(method, list);
	}
	
	public void analysis() {
		if(this.unit instanceof JAssignStmt) {
			analysisJAssignStmt();
		}else if(this.unit instanceof JReturnStmt) {
			analysisJReturnStmt();
		}else if(this.unit instanceof JIdentityStmt) {
			// 成员变量
			analysisJIdentityStmt();
		}else if(this.unit instanceof JInvokeStmt) {
			analysisJInvokeStmt();
		}
	}
	
	private void analysisJIdentityStmt() {
		JIdentityStmt stmt = (JIdentityStmt) unit;
		List<ValueBox> useBoxList = stmt.getUseBoxes();
		for(ValueBox useBox : useBoxList) {
			Value v = useBox.getValue();
			if(v instanceof ThisRef) {// 本类实例化对象
				continue;
			}
			
		}
	}
	
	private void analysisJInvokeStmt() {
		JInvokeStmt stmt = (JInvokeStmt) unit;
		InvokeExpr exp = stmt.getInvokeExpr();
		invokeExpAnalysis(exp);
	}
	
	private void analysisJAssignStmt() {
		JAssignStmt stmt = (JAssignStmt) unit;
		if(!stmt.containsInvokeExpr()) {// 不调用方法
			List<ValueBox> boxList = stmt.getUseAndDefBoxes();
			for(ValueBox box : boxList) {
				Value v = box.getValue();
				if(v instanceof JInstanceFieldRef) { // 成员变量
					MyMain.addList(this.memberParamList, v);
					continue;
				}
				if(v instanceof Constant
						|| v instanceof JNewExpr || v.getType().toString().equals(sootClass.toString())) {
					continue;
				}
				this.addMap(sootMethod, v);
			}
		}else {
			InvokeExpr exp = stmt.getInvokeExpr();
			invokeExpAnalysis(exp);
		}
	}
	
	private void analysisJReturnStmt() {
		JReturnStmt stmt = (JReturnStmt) unit;
		Value v = stmt.getOpBox().getValue();
		if(v instanceof Constant) {
//			System.out.println("语句 ["+unit+"] 返回的为常量，不分析");
			return;
		}

		Body b = this.sootMethod.retrieveActiveBody();
		List<Unit> defUnitList = new ArrayList<Unit>();
		for(Unit u : b.getUnits()) {
			List<ValueBox> defBoxList = u.getDefBoxes();
			for(ValueBox defBox : defBoxList) {
				Value value = defBox.getValue();
				if(value.equals(v)) {
//					System.out.println("变量["+value.toString()+"]在["+u.toString()+"]被定义。源代码行号["+MyMain.getLineNumber(u)+"]");
					defUnitList.add(u);
				}
			}
			List<ValueBox> useBoxList = u.getUseBoxes();
			for(ValueBox useBox : useBoxList) {
				Value value = useBox.getValue();
				if(value.equals(v)) {
//					System.out.println("变量["+value.toString()+"]在["+u.toString()+"]被使用。源代码行号["+MyMain.getLineNumber(u)+"]");
					this.addMap(this.sootMethod, value);
				}
			}
		}
		for(Unit u : defUnitList) {
			List<ValueBox> useBoxList = u.getUseBoxes();
			for(ValueBox useBox : useBoxList) {
				Value value = useBox.getValue();
				if(value instanceof Constant) {
					continue;
				}else if(value instanceof JimpleLocal) {
					JimpleLocal j = (JimpleLocal) value;
					if(j.getType().toString().equals(this.sootClass.getType().toString())) {
//						System.out.println("变量["+j.toString()+"]为该类实例化对象，不进行分析");
						continue;
					}
					this.addMap(this.sootMethod, value);
				}else if(value instanceof JInstanceFieldRef) {
					JInstanceFieldRef j = (JInstanceFieldRef) value;
//					System.out.println("变量["+j.toString()+"]为成员变量，另行分析");
					MyMain.addList(memberParamList, value);
				}else {
//					System.out.println("变量类型["+value.getClass().getName().toString()+"]未实现处理逻辑");
				}
			}
		}
	}
	
	private void invokeExpAnalysis(InvokeExpr exp) {
		List<ValueBox> defBoxList = this.unit.getDefBoxes();
		for(ValueBox defBox : defBoxList) {
			Value v = defBox.getValue();
			if(!(v instanceof Constant) && !(v instanceof ThisRef) && !(v instanceof JInstanceFieldRef)) {
				this.addMap(sootMethod, v);
			}
		}
		List<Value> argList = exp.getArgs();
		for(Value v : argList) {
			if(!(v instanceof Constant) && !(v instanceof ThisRef) && !(v instanceof JInstanceFieldRef)) {
				this.addMap(sootMethod, v);
			}
		}
		// 调用方法
		SootMethod callMethod = exp.getMethod();
		// 本类的方法才处理
		if(!callMethod.getDeclaringClass().equals(this.sootClass)) {
//			System.out.println("语句["+unit.toString()+"]调用了非本类的方法，不分析");
			return;
		}
		if(callMethod.isAbstract()) {
//			System.out.println("方法["+callMethod.toString()+"]为抽象方法，不分析");
			return;
		}
		List<Integer> argsIdxList = new ArrayList<Integer>();
		int idx = 0;
		for(Value v : argList) {
			if(v instanceof Constant) {
//				System.out.println("调用["+callMethod.toString()+"]方法传入的参数["+v.toString()+"]为常量，不分析");
				idx ++;
				continue;
			}
			argsIdxList.add(new Integer(idx));
			idx ++;
		}
		Body b = callMethod.retrieveActiveBody();
		List<Tag> tagList = callMethod.getTags();
		ParamNamesTag paramNamesTag = null;
		for(Tag tag : tagList) {
			if(tag instanceof ParamNamesTag) {
				paramNamesTag = (ParamNamesTag) tag;
				break;
			}
		}
		if(paramNamesTag == null) {
//			System.out.println("[！！！ERROR！！！] 方法["+callMethod.toString()+"]不包含ParamNamesTag标签，无法分析！");
			return;
		}
		List<String> paramNameList = paramNamesTag.getNames();
		int i = 0;
		for(Unit u : b.getUnits()) {
			List<ValueBox> useBoxList = u.getUseBoxes();
			for(ValueBox useBox : useBoxList) {
				Value v = useBox.getValue();
				if(v instanceof ParameterRef) {
					if(argsIdxList.contains(new Integer(i))) {
						Value def = u.getDefBoxes().get(0).getValue();
//						System.out.println("方法["+callMethod.toString()+"]的第["+(i + 1)+"]个参数为["+def.toString()+"]("+def.getType().toString()+")");
						this.addMap(callMethod, def);
					}
					i ++;
				}
			}
		}
	}
}
