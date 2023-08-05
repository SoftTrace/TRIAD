package experiment.preprocess.ebt;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import experiment.preprocess.util.TextPreprocess;
import experiment.project.EBT;
import util.FileIOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class EbtProcess {

    public static void main(String[] args) {
        EBT ebt = new EBT();
        filterType();
        initDirectory(ebt);
        processRequirement(ebt.getUnprocessedLevel1ArtifactPath(), ebt.getProcessedLevel1ArtifactPath());
        processTestCase(ebt.getUnprocessedLevel2ArtifactPath(), ebt.getProcessedLevel2ArtifactPath());
        processCode(ebt, ebt.getProcessedLevel3ArtifactPath());
    }


    private static void initDirectory(EBT ebt) {
        FileIOUtil.initDirectory(ebt.getProcessedLevel1ArtifactPath());
        FileIOUtil.initDirectory(ebt.getProcessedLevel2ArtifactPath());
        FileIOUtil.initDirectory(ebt.getProcessedLevel3ArtifactPath());
    }

    private static void processCode(EBT ebt, String outputPath) {
        File clsDir = new File(ebt.getUnprocessedClsNamePath());
        for (File f : clsDir.listFiles()) {
            StringBuilder sb = new StringBuilder();

            processCodeElement(f.getName(), ebt.getUnprocessedClsNamePath(), ebt.getClsNamePath(), sb);
            processCodeElement(f.getName(), ebt.getUnprocessedMethodNamePath(), ebt.getMethodNamePath(), sb);
            processCodeElement(f.getName(), ebt.getUnprocessedInvokeMethodNamePath(), ebt.getInvokeMethodNamePath(), sb);

            processCodeElement(f.getName(), ebt.getUnprocessedFieldNamePath(), ebt.getFieldNamePath(), sb);
            processCodeElement(f.getName(), ebt.getUnprocessedFieldTypePath(), ebt.getFieldTypePath(), sb);
            processCodeElement(f.getName(), ebt.getUnprocessedParamNamePath(), ebt.getParamNamePath(), sb);
            processCodeElement(f.getName(), ebt.getUnprocessedParamTypePath(), ebt.getParamTypePath(), sb);
            processComment(f.getName(), ebt.getUnprocessedCodeCommentPath(), ebt.getCodeCommentPath(), sb);

            FileIOUtil.writeFile(sb.toString(), outputPath + File.separator + f.getName());
        }
    }

    private static void processTestCase(String tcCsvPath, String outputPath) {
        try {
            File file = new File(tcCsvPath);
            CsvReader csvReader = new CsvReader();
            csvReader.setContainsHeader(true);

            CsvContainer csv = csvReader.read(file, StandardCharsets.UTF_8);
            for (CsvRow row : csv.getRows()) {
                String id = row.getField("id");
                String text = row.getField("text").replaceAll("Test case:", "");
                if (text.contains("Preconditions:"))
                    text = text.replaceAll("Preconditions:", ".");
                if (text.contains("Preconditions"))
                    text = text.replaceAll("Preconditions", ".");
                if (text.contains("Postconditions:"))
                    text = text.replaceAll("Postconditions:", ".");
                if (text.contains("Postconditions"))
                    text = text.replaceAll("Postconditions", ".");
                TextPreprocess textPreprocess = new TextPreprocess(text);
                String processedText = textPreprocess.doNlpFileProcess();
                FileIOUtil.writeFile(processedText, outputPath + File.separator + id +".txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processRequirement(String reqCsvPath, String outputPath) {
        try {
            File file = new File(reqCsvPath);
            CsvReader csvReader = new CsvReader();
            csvReader.setContainsHeader(true);
            CsvContainer csv = csvReader.read(file, StandardCharsets.UTF_8);
            for (CsvRow row : csv.getRows()) {
                String id = row.getField("id");
                String text = row.getField("text");
                TextPreprocess textPreprocess = new TextPreprocess(text);
                String processedText = textPreprocess.doNlpFileProcess();
                FileIOUtil.writeFile(processedText, outputPath + File.separator + id +".txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processCodeElement(String fileName, String codePartDir, String outputDir, StringBuilder codeSb) {
        File f = new File(codePartDir + File.separator + fileName);
        StringBuilder sb = new StringBuilder();
        FileIOUtil.readFile2List(f).stream().forEach(s -> {
            TextPreprocess preprocess = new TextPreprocess(s);
            sb.append(preprocess.doJavaFileProcess() + "\n");
        });
        FileIOUtil.writeFile(sb.toString(), outputDir + File.separator + fileName);
        codeSb.append(sb.toString() + " ");
    }

    private static void processComment(String fileName, String codePartDir, String outputDir, StringBuilder codeSb) {
        File f = new File(codePartDir + File.separator + fileName);
        StringBuilder sb = new StringBuilder();
        FileIOUtil.readFile2List(f).stream().forEach(s -> {
            TextPreprocess preprocess = new TextPreprocess(s);
            sb.append(preprocess.doNlpFileProcess() + "\n");
        });
        FileIOUtil.writeFile(sb.toString(), outputDir + File.separator + fileName);
        codeSb.append(sb.toString() + " ");

    }

    private static void filterType() {
        String fieldTypeDir = "dataset/EBT/unprocessed/origin/code/field/field_type";
        String paramTypeDir = "dataset/EBT/unprocessed/origin/code/param/param_type";
        String filterFieldTypeDir = "dataset/EBT/unprocessed/code/field/field_type";
        String filterParamTypeDir = "dataset/EBT/unprocessed/code/param/param_type";
        String clsDir = "dataset/EBT/unprocessed/origin/code/class_name";

        FileIOUtil.initDirectory(filterFieldTypeDir);
        FileIOUtil.initDirectory(filterParamTypeDir);

        Set<String> clsSet = new HashSet<>();
        File clsNameDir = new File(clsDir);
        for (File f : clsNameDir.listFiles()) {
            FileIOUtil.readFile2List(f).stream().forEach(s -> {
                System.out.println(s);
                clsSet.add(s);
            });
        }

        File fieldDir = new File(fieldTypeDir);
        for (File f : fieldDir.listFiles()) {
            StringBuilder sb = new StringBuilder();
            FileIOUtil.readFile2List(f).stream().forEach(s -> {
                if (clsSet.contains(s))
                    sb.append(s + "\n");
            });
            FileIOUtil.writeFile(sb.toString(), filterFieldTypeDir + File.separator + f.getName());
        }

        File paramDir = new File(paramTypeDir);
        for (File f : paramDir.listFiles()) {
            StringBuilder sb = new StringBuilder();
            FileIOUtil.readFile2List(f).stream().forEach(s -> {
                if (clsSet.contains(s))
                    sb.append(s + "\n");
            });
            FileIOUtil.writeFile(sb.toString(), filterParamTypeDir + File.separator + f.getName());
        }
    }
}
