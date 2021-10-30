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
	private Map<SootMethod, List<Value>> map; // ��Ҫ�����ķ���-�����б�
	private List<Value> memberParamList;// ��Ա����
	
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
			// ��Ա����
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
			if(v instanceof ThisRef) {// ����ʵ��������
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
		if(!stmt.containsInvokeExpr()) {// �����÷���
			List<ValueBox> boxList = stmt.getUseAndDefBoxes();
			for(ValueBox box : boxList) {
				Value v = box.getValue();
				if(v instanceof JInstanceFieldRef) { // ��Ա����
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
//			System.out.println("��� ["+unit+"] ���ص�Ϊ������������");
			return;
		}

		Body b = this.sootMethod.retrieveActiveBody();
		List<Unit> defUnitList = new ArrayList<Unit>();
		for(Unit u : b.getUnits()) {
			List<ValueBox> defBoxList = u.getDefBoxes();
			for(ValueBox defBox : defBoxList) {
				Value value = defBox.getValue();
				if(value.equals(v)) {
//					System.out.println("����["+value.toString()+"]��["+u.toString()+"]�����塣Դ�����к�["+MyMain.getLineNumber(u)+"]");
					defUnitList.add(u);
				}
			}
			List<ValueBox> useBoxList = u.getUseBoxes();
			for(ValueBox useBox : useBoxList) {
				Value value = useBox.getValue();
				if(value.equals(v)) {
//					System.out.println("����["+value.toString()+"]��["+u.toString()+"]��ʹ�á�Դ�����к�["+MyMain.getLineNumber(u)+"]");
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
//						System.out.println("����["+j.toString()+"]Ϊ����ʵ�������󣬲����з���");
						continue;
					}
					this.addMap(this.sootMethod, value);
				}else if(value instanceof JInstanceFieldRef) {
					JInstanceFieldRef j = (JInstanceFieldRef) value;
//					System.out.println("����["+j.toString()+"]Ϊ��Ա���������з���");
					MyMain.addList(memberParamList, value);
				}else {
//					System.out.println("��������["+value.getClass().getName().toString()+"]δʵ�ִ����߼�");
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
		// ���÷���
		SootMethod callMethod = exp.getMethod();
		// ����ķ����Ŵ���
		if(!callMethod.getDeclaringClass().equals(this.sootClass)) {
//			System.out.println("���["+unit.toString()+"]�����˷Ǳ���ķ�����������");
			return;
		}
		if(callMethod.isAbstract()) {
//			System.out.println("����["+callMethod.toString()+"]Ϊ���󷽷���������");
			return;
		}
		List<Integer> argsIdxList = new ArrayList<Integer>();
		int idx = 0;
		for(Value v : argList) {
			if(v instanceof Constant) {
//				System.out.println("����["+callMethod.toString()+"]��������Ĳ���["+v.toString()+"]Ϊ������������");
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
//			System.out.println("[������ERROR������] ����["+callMethod.toString()+"]������ParamNamesTag��ǩ���޷�������");
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
//						System.out.println("����["+callMethod.toString()+"]�ĵ�["+(i + 1)+"]������Ϊ["+def.toString()+"]("+def.getType().toString()+")");
						this.addMap(callMethod, def);
					}
					i ++;
				}
			}
		}
	}
}
