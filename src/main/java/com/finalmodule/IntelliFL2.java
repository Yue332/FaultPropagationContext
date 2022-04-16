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
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntelliFL2 extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String[] bugArray = config.getBugIdArr();
        for (String bug : bugArray) {
            System.out.println("[INFO] 开始处理bug " + bug);
            dealOneBug(runTime, bug);
            System.out.println("[INFO] bug " + bug + "处理完成");
        }
    }

    public void dealOneBug(Runtime runtime, String bug)throws Exception{
        String projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String projectPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) +
                File.separator + projectId + "_" + bug + File.separator;
        String testDir = Utils.getSrcTestDir(runtime, projectPath);
        //1生成的文件
        File removeFilePath = new File(System.getProperty("user.home") + File.separator +
                "intelliFL" + File.separator + "methods" + File.separator +
                config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bug + File.separator +
                "removeMethods.txt");
        List<String> removeList = FileUtils.readLines(removeFilePath, "utf-8");
        for (String removeMethod : removeList) {
            Map<String, String> map = getPathByRemoveMethod(removeMethod);
            System.out.println("[DEBUG] bug + "+bug+" 开始移除类" + map.get("CLASS") + "中的测试用例");
            File testClassFile = getTestClassFile(projectId, projectPath, testDir, map);
            CompilationUnit compilationUnit = StaticJavaParser.parse(testClassFile);
            ModifierVisitor<String> visitor = new MethodRemoveVister();
            visitor.visit(compilationUnit, map.get("METHOD"));
            FileUtils.writeStringToFile(testClassFile, compilationUnit.toString(), "utf-8", false);
        }
    }

    private File getTestClassFile(String projectId, String projectPath, String testDir, Map<String, String> map)throws Exception{
        String testClassFilePath = testDir + File.separator + map.get("CLASS").replace(".", File.separator);
        if(testClassFilePath.contains("$")){
            System.out.println("[DEBUG] " + testClassFilePath + "包含内部类");
            int idx = testClassFilePath.indexOf("$");
            testClassFilePath = testClassFilePath.substring(0, idx);
            System.out.println("[DEBUG] 处理为：" + testClassFilePath);
        }
        testClassFilePath = testClassFilePath + ".java";
        File testClassFile = new File(testClassFilePath);
        if(!testClassFile.exists()){
            String[] arr = map.get("CLASS").split("\\.");
            String fileName = arr[arr.length - 1] + ".java";
            testClassFile = getFileFromPath(projectPath, fileName);
            if(ObjectUtils.isEmpty(testClassFile) || !testClassFile.exists()){
                throw new Exception("项目目录 " + projectPath + " 下未找到文件 " + fileName);
            }
        }
        return testClassFile;
    }

    private File getFileFromPath(String path, String fileName){
        File dir = new File(path);
        File[] files = dir.listFiles();
        if(ObjectUtils.isNotEmpty(files)){
            for (File file : files) {
                String name = file.getName();
                if(file.isDirectory()){
                    File retFile = getFileFromPath(file.getAbsolutePath(), fileName);
                    if(ObjectUtils.isEmpty(retFile)){
                        continue;
                    }
                    return retFile;
                }else if(name.endsWith(".java") && name.equals(fileName)){
                    return file;
                }
            }
        }
        return null;
    }

    private static class MethodRemoveVister extends ModifierVisitor<String> {
        @Override
        public Visitable visit(MethodDeclaration method, String removeMethodName) {
//            System.out.println("[DEBUG] 移除测试用例：" + method.getNameAsString());
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
