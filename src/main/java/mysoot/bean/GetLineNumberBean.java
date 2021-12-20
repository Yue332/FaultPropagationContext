package mysoot.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mysoot.MyMain;
import org.apache.commons.collections4.CollectionUtils;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JInstanceFieldRef;

public class GetLineNumberBean {
	private List<SootMethod> betweenClzMethodList;
	private SootClass sootClass;
	private Map<SootMethod, List<Value>> map;
	private List<Value> memberList;
	private List<String> lineNumberList = new ArrayList<>();

	public GetLineNumberBean(List<SootMethod> betweenClzMethodList){
		this.betweenClzMethodList = betweenClzMethodList;
	}

	public GetLineNumberBean(SootClass sootClass, Map<SootMethod, List<Value>> map, List<Value> memberList) {
		super();
		this.sootClass = sootClass;
		this.map = map;
		this.memberList = memberList;
	}

	public List<Map<String, Integer>> getBetweenClzAnalysis(){
		List<Map<String, Integer>> btnClzList = new ArrayList<>();
		if(CollectionUtils.isEmpty(lineNumberList)){
			return btnClzList;
		}

		a:for (SootMethod sootMethod : betweenClzMethodList) {
			SootClass sootClass = sootMethod.getDeclaringClass();
			for (SootMethod method : sootClass.getMethods()){
				if (sootMethod.equals(method)){
					for (Unit unit : method.retrieveActiveBody().getUnits()){
						int lineNumber = MyMain.getLineNumber(unit);
						if(lineNumber != -1){
							Map<String, Integer> map = new HashMap<>(1);
							map.put(sootClass.getName(), lineNumber - 1);
							btnClzList.add(map);
							break a;
						}
					}
				}
			}
		}
		return btnClzList;
	}
	
	public List<String> get() {
		for(Entry<SootMethod, List<Value>> entry : this.map.entrySet()) {
			SootMethod method = entry.getKey();
			if(method.isAbstract()) {
//				System.out.println("[INFO] method : ["+method.toString()+"]为抽象方法，无法分析！！！");
				continue;
			}
			List<Value> list = entry.getValue();
			Body b = method.retrieveActiveBody();
			for(Unit u : b.getUnits()) {
				List<ValueBox> valueBoxList = u.getUseAndDefBoxes();
				for(ValueBox valueBox : valueBoxList) {
					Value v = valueBox.getValue();
					if(list.contains(v)) {
						MyMain.addList(this.lineNumberList, MyMain.getLineNumber(u) + "");
					}
				}
			}
		}
		getFromList();
		return this.lineNumberList;
	}
	
	public void getFromList() {

		for(SootMethod method : this.sootClass.getMethods()) {
			if(method.isAbstract()) {
//				System.out.println("[INFO] method : ["+method.toString()+"]为抽象方法，无法分析！！！");
				continue;
			}
			Body b = method.retrieveActiveBody();
			for(Unit u : b.getUnits()) {
				List<ValueBox> valueBoxList = u.getUseAndDefBoxes();
				for(ValueBox valueBox : valueBoxList) {
					Value v = valueBox.getValue();
					if(v instanceof JInstanceFieldRef) {
						for(Value memV : this.memberList) {
							if(memV.toString().equals(v.toString())) {
								MyMain.addList(this.lineNumberList, MyMain.getLineNumber(u) + "");
							}
						}
					}
				}
			}
		}
	}
}
