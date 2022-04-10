package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.utils.ConfigUtils;
import com.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntelliFL2 extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String[] bugArray = config.getBugIdArr();
        for (String bug : bugArray) {
            dealOneBug(runTime, bug);
        }
    }

    public void dealOneBug(Runtime runtime, String bug)throws Exception{
        String testDir = Utils.getSrcTestDir(runtime, config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) +
                File.separator + config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bug + File.separator);
        //1生成的文件
        File removeFilePath = new File(System.getProperty("user.home") + File.separator +
                "intelliFL" + File.separator + "methods" + File.separator +
                config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bug + File.separator +
                "removeMethods.txt");
        List<String> removeList = FileUtils.readLines(removeFilePath, "utf-8");
        for (String removeMethod : removeList) {
            Map<String, String> map = getPathByRemoveMethod(removeMethod);
            String testClassFilePath = testDir + File.separator + map.get("CLASS").replace(".", File.separator) + ".java";
            File testClassFile = new File(testClassFilePath);
            if(!testClassFile.exists()){
                throw new Exception("文件" + testClassFile.getAbsolutePath() + "不存在！");
            }
            CompilationUnit compilationUnit = StaticJavaParser.parse(testClassFile);
            System.out.println("[DEBUG] 开始移除类" + map.get("CLASS") + "中的测试用例");
            ModifierVisitor<String> visitor = new MethodRemoveVister();
            visitor.visit(compilationUnit, map.get("METHOD"));
            FileUtils.writeStringToFile(testClassFile, compilationUnit.toString(), "utf-8", false);
        }
    }

    private static class MethodRemoveVister extends ModifierVisitor<String> {
        @Override
        public Visitable visit(MethodDeclaration method, String removeMethodName) {
            System.out.println("[DEBUG] 移除测试用例：" + method.getNameAsString());
            if(method.getNameAsString().equals(removeMethodName)){
                method.remove();
            }
            return super.visit(method, removeMethodName);
        }
    }

    public Map<String, String> getPathByRemoveMethod(String removeMehtod){
        Map<String, String> map = new HashMap<>(2);
        String[] array = removeMehtod.replace(".txt", "").split("\\.");
        map.put("METHOD", array[array.length - 1]);
        StringBuilder className = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++) {
            className.append(array[i]).append(".");
        }
        if(className.toString().endsWith(".")){
            className = new StringBuilder(className.substring(0, className.length() - 1));
        }
        map.put("CLASS", className.toString());

        return map;
    }
}
