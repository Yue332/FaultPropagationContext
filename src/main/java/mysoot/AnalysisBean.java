package mysoot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.ParamNamesTag;

public class AnalysisBean {
	/**
	 * ��ǰ����
	 */
	public static final int ANALYSIS_DIRECTION_UP = 1;
	/**
	 * ������
	 */
	public static final int ANALYSIS_DIRECTION_DOWN = 2;
	/**
	 * ��ǰ+������
	 */
	public static final int ANALYSIS_DIRECTION_ALL = 3;
	
	
	private SootClass sootClass;
	private SootMethod sootMethod;
	private Unit unit;
	private int analysisDirection;
	private List<String> lineNumberList;
	private List<ValueBox> analysisValueList;
	
	
	public AnalysisBean(SootClass sootClass, SootMethod sootMethod, Unit unit) {
		super();
		this.lineNumberList = new ArrayList<String>();
		this.analysisValueList = new ArrayList<ValueBox>();
		this.sootClass = sootClass;
		this.sootMethod = sootMethod;
		this.unit = unit;
		// unitΪreturnʱΪ��ǰ���ϣ�����  ������ʱ����ȫ������
		if(unit instanceof JReturnVoidStmt || unit instanceof JReturnStmt) {
			this.analysisDirection = ANALYSIS_DIRECTION_UP;
		}else {
			this.analysisDirection = ANALYSIS_DIRECTION_ALL;
		}
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
	public int getAnalysisDirection() {
		return analysisDirection;
	}
	public void setAnalysisDirection(int analysisDirection) {
		this.analysisDirection = analysisDirection;
	}
	
	public void up() {
		if(unit instanceof JReturnStmt) {
			JReturnStmt stmt = (JReturnStmt) unit;
			Value v = stmt.getOpBox().getValue();
			if(v instanceof Constant) {
				System.out.println("��� ["+unit+"] ���ص�Ϊ������������");
				return;
			}
			Body body = sootMethod.retrieveActiveBody();
			for(Unit unit : body.getUnits()) {
				List<ValueBox> defBoxList = unit.getDefBoxes();
				for(ValueBox box : defBoxList) {
					Value value = box.getValue();
					if(value.equals(v)) {
						int lineNum = MyMain.getLineNumber(unit);
						System.out.println("Jimple����["+v.toString()+"]��Դ����" + lineNum + "������");
						System.out.println(unit.toString() + "Դ�����к�["+lineNum+"]");
						lineNumberList.add(lineNum + "");
						break;
					}
				}
				List<ValueBox> useBoxList = unit.getUseBoxes();
				for(ValueBox box : useBoxList) {
					Value value = box.getValue();
					if(v.equals(value)) {
						System.out.println(unit.toString() + "Դ�����к�["+MyMain.getLineNumber(unit)+"]");
					}
				}
				if(unit.equals(this.unit)) {
					break;
				}
			}
		}
	}
	
	/**
	 * ����˼·��
	 * 1.ʶ������ı��� defbox usebox  �ж��Ƿ��г�Ա���� ���г�Ա��������Ҫ�ҵ�����������ʹ�ó�Ա���������  ������Ա������ֻ��Ҫ���������������
	 * 2.�ҵ��������Ƿ������෽�����ø�������ڵķ�����
	 */
	public void doAnalysis() {
		if(this.analysisDirection == ANALYSIS_DIRECTION_UP) {
			up();
		}else {
			getMethodsByUnit();
		}
	}
	
	/**
	 * ����unit��ȡ�����õ�sootMethod
	 * @return
	 */
	public List<SootMethod> getMethodsByUnit(){
		List<SootMethod> list = new ArrayList<SootMethod>();
		Map<SootMethod, Integer[]> map = new HashMap<SootMethod, Integer[]>();
		// ����ǵ�����ĳ������
		if(unit instanceof JInvokeStmt) {
			JInvokeStmt stmt = (JInvokeStmt) unit;
			if(!stmt.containsInvokeExpr()) {
				System.out.println("���["+unit.toString()+"]Ӧ���÷�������δ���ã�");
			}else {//TODO: ��һ�������ö�������Ĵ�����������������unit����Ͳ��ô���
				InvokeExpr exp = stmt.getInvokeExpr();
				SootMethod callMethod = exp.getMethod();// ���õķ���
				// ����ķ����Ŵ���
				if(callMethod.getDeclaringClass().equals(sootClass)) {
					List<Value> callArgs = exp.getArgs();
					System.out.println("���["+unit.toString()+"]�����˷���["+callMethod.toString()+"]������["+callArgs.toString()+"]");
					map.put(callMethod, MyMain.getNoConstantParamsIdx(callArgs));
				}else {
					System.out.println("���["+unit.toString()+"]�����˷Ǳ���ķ���["+callMethod.toString()+"]��������");
				}
			}
		}
		
		Set<Entry<SootMethod,Integer[]>> mapEntrySet = map.entrySet();
		for(Entry<SootMethod, Integer[]> entry : mapEntrySet) {
			SootMethod method = entry.getKey();
			if(this.sootMethod.equals(method)) {
				continue;
			}
			Integer[] paramIdx = entry.getValue();
			List tagList = method.getTags();
			ParamNamesTag paramTag = null;
			for(Object o : tagList) {
				if(o instanceof ParamNamesTag) {
					paramTag = (ParamNamesTag) o;
					break;
				}
			}
			if(paramTag == null) {
			}
			List<String> paramNames = paramTag.getNames();
			List<String> pAnalysisParamNames = new ArrayList<String>();
			for(int idx : paramIdx) {
				pAnalysisParamNames.add(paramNames.get(idx) + "@" + method.getParameterType(idx));
			}
			Body b = method.retrieveActiveBody();
			for(Unit u : b.getUnits()) {
				List<ValueBox> useBox = u.getUseBoxes();
				for(ValueBox box : useBox) {
					Value v = box.getValue();
					if(v instanceof JimpleLocal) {
						JimpleLocal j = (JimpleLocal) v;
						if(pAnalysisParamNames.contains(j.getName() + "@" + j.getType().toString())) {
							MyMain.addList(lineNumberList, MyMain.getLineNumber(unit) + "");
						}
					}else if(v instanceof JVirtualInvokeExpr) {
						JVirtualInvokeExpr j = (JVirtualInvokeExpr) v;
						List<Value> l = j.getArgs();
						for(Value v1 : l) {
							if(pAnalysisParamNames.contains(v1.toString() + "@" + v1.getType().toString())) {
								MyMain.addList(lineNumberList, MyMain.getLineNumber(unit) + "");
							}
						}
						j.toString();
					}else {
						System.out.println("value ["+v.toString()+"] instanceof ["+v.getClass().getName()+"] but not analysis");
					}
				}
			}
		}
		
		return list;
	}
	
	public void analysis1() {
		if(this.unit instanceof JAssignStmt) {
			JAssignStmt stmt = (JAssignStmt) unit;
			List<ValueBox> defBoxList = stmt.getDefBoxes();
			for(ValueBox valueBox : defBoxList) {
				Value v = valueBox.getValue();
				if(v instanceof Constant) {
					System.out.println("value ["+v.toString()+"]Ϊ������������");
					continue;
				}
				MyMain.addList(analysisValueList, valueBox);
			}
			List<ValueBox> useBoxList = stmt.getUseBoxes();
			for(ValueBox valueBox : useBoxList) {
				Value v = valueBox.getValue();
				if(v instanceof JVirtualInvokeExpr) {
					JVirtualInvokeExpr j = (JVirtualInvokeExpr) v;
					//����ķ���
					if(j.getMethodRef().declaringClass().equals(this.sootClass)) {
						Value callValue = j.getBaseBox().getValue();
						if(!(callValue instanceof Constant)) {
							MyMain.addList(analysisValueList, j.getBaseBox());
						}
						List<Value> argsBoxList = j.getArgs();
						for(Value argBox : argsBoxList) {
							if(argBox instanceof Constant) {
								continue;
							}
							MyMain.addList(analysisValueList, argBox);
						}
					}
				}else {
					MyMain.addList(analysisValueList, valueBox);
				}
			}
		}else if(this.unit instanceof JReturnStmt) {
			JReturnStmt stmt = (JReturnStmt) this.unit;
			System.out.println(stmt.toString());
		}
	}
	
	
}
