package experiment.preprocess.parsecode.java;

public class RunCodeProcess {
    public static void main(String[] args) {
        String project = "dronology";
        // 1. parse ASR
//        JavaParse.parseSourceCode(project);

        // 2. parse ast xml、srcml xml
        AnalyzeCodeXml analyzeCodeXml = new AnalyzeCodeXml(project);
        analyzeCodeXml.run();
    }
}
