package experiment.preprocess.ebt;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import experiment.preprocess.util.CleanUp;
import experiment.project.EBT;
import util.FileIOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProcessCometEBT {
    private static EBT ebt = new EBT();
    private static String reqPath = "dataset/ebt/originals/comet/req.txt";
    private static String tcPath = "dataset/ebt/originals/comet/testcase.txt";
    private static String codePath = "dataset/ebt/originals/comet/code.txt";
    private static String codeOrderPath = "dataset/ebt/originals/comet/codeOrder.txt";

    public static void main(String[] args) {
        initDirectory(ebt);
        readCometTxt(reqPath, ebt.getUnprocessedLevel1ArtifactPath(), ebt.getProcessedLevel1ArtifactPath());
        readCometTxt(tcPath, ebt.getUnprocessedLevel2ArtifactPath(), ebt.getProcessedLevel2ArtifactPath());
        readCometCodeTxt(codePath, codeOrderPath, ebt.getProcessedLevel3ArtifactPath());
    }

    private static void initDirectory(EBT ebt) {
        FileIOUtil.initDirectory(ebt.getProcessedLevel1ArtifactPath());
        FileIOUtil.initDirectory(ebt.getProcessedLevel2ArtifactPath());
        FileIOUtil.initDirectory(ebt.getProcessedLevel3ArtifactPath());
    }

    private static void readCometCodeTxt (String cometPath, String codeOrderPath,String outputDir) {
        List<String> cometList = FileIOUtil.readFileByLine(cometPath);
        List<String> codeOrderList = FileIOUtil.readFileByLine(codeOrderPath);

        for(int i =0;i< cometList.size();i++) {
            String id = codeOrderList.get(i).split("\\.")[0];
            String content = cometList.get(i).replaceAll("," ,"").replaceAll("'","").replaceAll("\\[","").replaceAll("]","");
            content = CleanUp.chararctorClean(content);
            FileIOUtil.writeFile(content, outputDir+File.separator + id +".txt");
        }
    }

    private static void readCometTxt (String cometPath, String unprocessedPath,String outputDir) {
        List<String> cometList = FileIOUtil.readFileByLine(cometPath);
        File file = new File(unprocessedPath);
        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);

        CsvContainer csv = null;
        try {
            csv = csvReader.read(file, StandardCharsets.UTF_8);
            for(int i =0;i< csv.getRows().size();i++) {
                CsvRow row = csv.getRow(i);
                String id = row.getField("id");
                String text = row.getField("text");
                String content = cometList.get(i).replaceAll("," ,"").replaceAll("'","").replaceAll("\\[","").replaceAll("]","");
                FileIOUtil.writeFile(content, outputDir+File.separator + id +".txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
