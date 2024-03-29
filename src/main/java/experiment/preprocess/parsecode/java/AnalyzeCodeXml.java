package experiment.preprocess.parsecode.java;

import edu.stanford.nlp.util.CoreMap;
import experiment.preprocess.parsecode.java.entity.ClassEntity;
import experiment.preprocess.parsecode.java.entity.FieldEntity;
import experiment.preprocess.parsecode.java.entity.MethodEntity;
import experiment.preprocess.parsecode.java.util.ClassUtil;
import experiment.preprocess.parsecode.java.util.JavaToXmlUtil;
import experiment.preprocess.util.StanfordNlpUtil;
import experiment.preprocess.util.TextPreprocess;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import util.FileIOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AnalyzeCodeXml {
    private static String processPrefix;
    private static String unprocessPrefix;

    private static Set<String> rtmClassList;
    private static String projectPath;
    private static String rtmPath;
    private static String srcmlXmlDir;
    private static String commentDir;
    private static String commentSentenceDir;
    private static String commentProcessDir;
    private static String commentTpDir;

    private static String methodNameDir;
    private static String paramNameDir;
    private static String paramTypeDir;
    private static String invokeMethodDir;
    private static String classNameDir;
    private static String fieldNameDir;
    private static String fieldTypeDir;
    private static String astXmlDir;
    private static String classDir;


    private static Set<String> allMethodSet;
    private static Set<String> allClassSet = new HashSet<>();
    private static boolean handleTypeInfoFlag = true;

    public static StanfordNlpUtil stanfordNlpUtil = new StanfordNlpUtil();

    public AnalyzeCodeXml(String project) {
        projectPath = "dataset/" + project;
        rtmPath = projectPath + "/trace_matrices/issue-code.txt";

        unprocessPrefix = projectPath + "/unprocessed/code/";
        processPrefix = projectPath + "/processed/code/";
        srcmlXmlDir = unprocessPrefix + "code_xml";
        astXmlDir = unprocessPrefix + "ast_xml";
        classNameDir = "class_name";
        fieldNameDir = "field/field_name";
        fieldTypeDir = "field/field_type";
        commentDir = "comment";
        commentSentenceDir = "comment/sentence";
        commentProcessDir = "comment/processed";
        commentTpDir = "comment/term_pair";
        methodNameDir = "method_name";
        paramNameDir = "param/param_name";
        paramTypeDir = "param/param_type";
        invokeMethodDir = "invoke_method";
        classDir = "class";
    }

    public void run() {
        try {
            initDir();
            astXmlProcess();
            srcmlXmlProcess();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }


    private static void initDir() {
        FileIOUtil.initDirectory(unprocessPrefix + invokeMethodDir);
        FileIOUtil.initDirectory(unprocessPrefix + classNameDir);
        FileIOUtil.initDirectory(unprocessPrefix + methodNameDir);
        FileIOUtil.initDirectory(unprocessPrefix + paramNameDir);
        FileIOUtil.initDirectory(unprocessPrefix + paramTypeDir);
        FileIOUtil.initDirectory(unprocessPrefix + fieldTypeDir);
        FileIOUtil.initDirectory(unprocessPrefix + fieldNameDir);

        FileIOUtil.initDirectory(processPrefix + invokeMethodDir);
        FileIOUtil.initDirectory(processPrefix + classNameDir);

        FileIOUtil.initDirectory(processPrefix + fieldTypeDir);
        FileIOUtil.initDirectory(processPrefix + fieldNameDir);
        FileIOUtil.initDirectory(processPrefix + methodNameDir);
        FileIOUtil.initDirectory(processPrefix + paramNameDir);
        FileIOUtil.initDirectory(processPrefix + paramTypeDir);

        FileIOUtil.initDirectory(unprocessPrefix + commentDir);
        FileIOUtil.initDirectory(processPrefix + commentDir);
        FileIOUtil.initDirectory(processPrefix + commentSentenceDir);
        FileIOUtil.initDirectory(processPrefix + commentTpDir);
        FileIOUtil.initDirectory(processPrefix + commentProcessDir);

        FileIOUtil.initDirectory(processPrefix + classDir);
    }


    /**
     * extract class name, method name, and field
     */
    public static void astXmlProcess() {
        File directory = new File(astXmlDir);

        for (File classXmlFile : directory.listFiles()) {
            String classXml = ClassUtil.getXmlString(classXmlFile);
            ClassEntity classEntity = JavaToXmlUtil.xmlToBean(classXml, ClassEntity.class);
            String className = classXmlFile.getName().split("\\.")[0];
            StringBuilder methodSb = new StringBuilder();
            StringBuilder methodProcessedSb = new StringBuilder();

            StringBuilder paramNameSb = new StringBuilder();
            StringBuilder paramNameProcessedSb = new StringBuilder();

            StringBuilder paramTypeSb = new StringBuilder();
            StringBuilder paramTypeProcessedSb = new StringBuilder();

            StringBuilder fieldNameSb = new StringBuilder();
            StringBuilder fieldNameProcessedSb = new StringBuilder();

            StringBuilder fieldTypeSb = new StringBuilder();
            StringBuilder fieldTypeProcessedSb = new StringBuilder();

            List<MethodEntity> list = classEntity.getMethod();
            StringBuilder sb = new StringBuilder();
            for (MethodEntity methodEntity : list) {
                sb.append(methodEntity.getMethodName() + "\n");
            }
            allClassSet.add(className);
            String processedClsName = processClassName(className);
            List<FieldEntity> fieldList = classEntity.getField();

            for (FieldEntity field : fieldList) {
                String fieldType = field.getFieldType();
                String fieldName = field.getFieldName();
                processFieldName(fieldName, fieldNameSb, fieldNameProcessedSb);
                if (handleTypeInfoFlag)
                    processType(fieldType, fieldTypeSb, fieldTypeProcessedSb);
            }
            FileIOUtil.writeFile(fieldNameSb.toString(), unprocessPrefix + fieldNameDir + "/" + className + ".txt");
            FileIOUtil.writeFile(fieldNameProcessedSb.toString(), processPrefix + fieldNameDir + "/" + className + ".txt");

            FileIOUtil.writeFile(fieldTypeSb.toString(), unprocessPrefix + fieldTypeDir + "/" + className + ".txt");
            FileIOUtil.writeFile(fieldTypeProcessedSb.toString(), processPrefix + fieldTypeDir + "/" + className + ".txt");

            List<MethodEntity> methodList = classEntity.getMethod();
            for (MethodEntity methodEntity : methodList) {
                processMethodName(methodEntity.getMethodName(), methodSb, methodProcessedSb);
                if (methodEntity.getParametersList().size() != 0) {
                    for (String param : methodEntity.getParametersList()) {
                        if (param.equals(""))
                            continue;
                        String paramType = param.split(" ")[0];
                        if (handleTypeInfoFlag)
                            processType(paramType, paramTypeSb, paramTypeProcessedSb);
                        processParamName(param, paramNameSb, paramNameProcessedSb);
                    }
                }
            }
            FileIOUtil.writeFile(methodSb.toString(), unprocessPrefix + methodNameDir + "/" + className + ".txt");
            FileIOUtil.writeFile(methodProcessedSb.toString(), processPrefix + methodNameDir + "/" + className + ".txt");

            FileIOUtil.writeFile(paramNameSb.toString(), unprocessPrefix + paramNameDir + "/" + className + ".txt");
            FileIOUtil.writeFile(paramNameProcessedSb.toString(), processPrefix + paramNameDir + "/" + className + ".txt");

            FileIOUtil.writeFile(paramTypeSb.toString(), unprocessPrefix + paramTypeDir + "/" + className + ".txt");
            FileIOUtil.writeFile(paramTypeProcessedSb.toString(), processPrefix + paramTypeDir + "/" + className + ".txt");

            StringBuilder clsSb = new StringBuilder();
            clsSb.append(processedClsName + "\n" + fieldNameProcessedSb + "\n" + fieldTypeProcessedSb + "\n" + paramNameProcessedSb + "\n" + paramTypeProcessedSb + "\n" + methodProcessedSb + "\n");
            FileIOUtil.continueWriteFile(clsSb.toString(), processPrefix + classDir + "/" + className + ".txt");
        } // for
    }

    private static String processClassName(String className) {
        String processedClsName = "";
        FileIOUtil.writeFile(className + "\n", unprocessPrefix + classNameDir + "/" + className + ".txt");
        TextPreprocess preprocess = new TextPreprocess(className);
        processedClsName = preprocess.doJavaFileProcess();
        FileIOUtil.writeFile(processedClsName + "\n", processPrefix + classNameDir + "/" + className + ".txt");
        return processedClsName;
    }

    private static void processParamName(String param, StringBuilder paramSb, StringBuilder paramProcessedSb) {
        String paramName = param.split(" ")[1];
        paramSb.append(paramName + "\n");

        TextPreprocess preprocess = new TextPreprocess(paramName);
        paramProcessedSb.append(preprocess.doJavaFileProcess() + "\n");


    }

    private static void processMethodName(String methodName, StringBuilder methodSb, StringBuilder methodProcessedSb) {
        methodSb.append(methodName + "\n");
        TextPreprocess preprocess = new TextPreprocess(methodName);
        methodProcessedSb.append(preprocess.doJavaFileProcess() + "\n");
    }

    private static void processFieldName(String fieldName, StringBuilder fieldSb, StringBuilder fieldProcessedSb) {
        fieldSb.append(fieldName + "\n");
        TextPreprocess preprocess = new TextPreprocess(fieldName);
        fieldProcessedSb.append(preprocess.doJavaFileProcess() + "\n");
    }

    private static void processType(String typeName, StringBuilder typeSb, StringBuilder typeProcessedSb) {
        if (!allClassSet.contains(typeName))
            return;
        typeSb.append(typeName + "\n");
        TextPreprocess preprocess = new TextPreprocess(typeName);
        typeProcessedSb.append(preprocess.doJavaFileProcess() + "\n");
    }


    /**
     * extract comment, invoke method
     */
    private static void srcmlXmlProcess() throws IOException, JDOMException {
        allMethodSet = FileIOUtil.readEachFileLine(unprocessPrefix + "/" + methodNameDir);
        System.out.println("parse srcml file");

        File filePath = new File(srcmlXmlDir);
        if (filePath.isDirectory()) {
            for (File f : filePath.listFiles()) {
                String fileName = f.getName();
                if (!fileName.contains(".java"))
                    continue;
                fileName = fileName.substring(0, fileName.indexOf(".java"));
                SAXBuilder saxBuilder = new SAXBuilder();
                InputStream is = new FileInputStream(f);
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                StringBuilder commentSb = new StringBuilder();
                StringBuilder commentSentenceSb = new StringBuilder();
                StringBuilder commentProcessSb = new StringBuilder();
                StringBuilder commentBitermSb = new StringBuilder();

                StringBuilder invokeMethodSb = new StringBuilder();
                StringBuilder processedInvokeMethodSb = new StringBuilder();

                Queue<Element> children = new LinkedList<>();
                children.add(rootElement);
                while (!children.isEmpty()) {
                    Element child = children.poll();
                    if (child.getName().equals("comment")) {
                        String comment = child.getValue();
                        String parent = child.getParent().getParent().toString();
                        boolean isMethodFunction = parent.contains("class") || parent.contains("Document");

                        boolean isLegalLength = false;
                        if (comment.length() > 3 && comment.length() < 1000) {
                            isLegalLength = true;
                        }
                        // javadoc template
                        if (comment.contains("Licensed to the Apache Software Foundation (ASF) under one"))
                            continue;

                        if (isMethodFunction && isLegalLength && comment.substring(0, 3).equals("/**")) {
                            comment = comment
//                                    .replaceAll("=|&|\\{|}|\\[|\\]" ,"")
                                    .replaceAll("(<pre)([\\s\\S]*)(</pre>)", "")
                                    .replaceAll("(<([^>]*)>)", "")
                                    .replaceAll("(@author([^\n]*)\n)", "")
                                    .replaceAll("(@see([^\n]*)\n)", "")
                                    .replaceAll("(@since([^\n]*)\n)", "")
                                    .replaceAll("(@throws([^\n]*)\n)", "")
                                    .replaceAll("(@param([^\n]*)\n)", "")
                                    .replaceAll("(@return([^\n]*)\n)", "")
                                    .replaceAll("(@version([^\n]*)\n)", "");
                            processComment(comment, commentSb, commentSentenceSb, commentProcessSb, commentBitermSb);
                        }
                    }
                    if (child.getName().equals("call")) {
                        processInvokeMethod(child.getValue(), invokeMethodSb, processedInvokeMethodSb);
                    }
                    List<Element> children1 = child.getChildren();
                    if (children1.size() != 0) {
                        for (Element child1 : children1) {
                            children.offer(child1);
                        }
                    }
                }// while
                FileIOUtil.writeFile(commentSb.toString(), unprocessPrefix + commentDir + "/" + fileName + ".txt");
                FileIOUtil.writeFile(commentSentenceSb.toString(), processPrefix + commentSentenceDir + "/" + fileName + ".txt");
                FileIOUtil.writeFile(commentProcessSb.toString(), processPrefix + commentProcessDir + "/" + fileName + ".txt");
                FileIOUtil.writeFile(invokeMethodSb.toString(), unprocessPrefix + invokeMethodDir + "/" + fileName + ".txt");
                FileIOUtil.writeFile(processedInvokeMethodSb.toString(), processPrefix + invokeMethodDir + "/" + fileName + ".txt");
                FileIOUtil.writeFile(commentBitermSb.toString(), processPrefix + commentTpDir + "/" + fileName + ".txt");
                FileIOUtil.continueWriteFile(commentProcessSb.toString(), processPrefix + classDir + "/" + fileName + ".txt");
            } // for
        }
    }

    private static void processInvokeMethod(String invokeMethodName, StringBuilder invokeMethodSb, StringBuilder processedInvokeMethodSb) {
        if (invokeMethodName.contains(".") && (invokeMethodName.indexOf("(") > invokeMethodName.indexOf("."))) {
            invokeMethodName = invokeMethodName.substring(invokeMethodName.indexOf(".") + 1, invokeMethodName.indexOf("("));
        } else {
            invokeMethodName = invokeMethodName.substring(0, invokeMethodName.indexOf("("));
        }
        if (allMethodSet.contains(invokeMethodName)) {
            invokeMethodSb.append(invokeMethodName + "\n");
            TextPreprocess textPreprocess = new TextPreprocess(invokeMethodName);
            processedInvokeMethodSb.append(textPreprocess.doJavaFileProcess() + "\n");
        }
    }

    private static void processComment(String comment, StringBuilder commentSb, StringBuilder commentSentenceSb,
                                       StringBuilder commentProcessSb, StringBuilder commentBitermSb) {
        commentSb.append(comment + "\n\n");
        String[] ary = comment.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String str : ary) {
            str = str.replaceAll("\n", " ")
                    .replaceAll("/\\*", "")
                    .replaceAll("/\\*\\*", "")
                    .replaceAll("\\*", "")
                    .replaceAll("@[a-zA-z]*", "");
            sb.append(str);
        }
        List<CoreMap> sentenceList = stanfordNlpUtil.splitSentence(sb.toString());
        sentenceList.stream().forEach(s -> {
            commentSentenceSb.append(s + "\n");
            TextPreprocess preprocess = new TextPreprocess(s.toString());
            commentBitermSb.append(stanfordNlpUtil.getTermPair(s.toString()));
            commentProcessSb.append(preprocess.doJavaFileProcess() + "\n");
        });
    }
}
