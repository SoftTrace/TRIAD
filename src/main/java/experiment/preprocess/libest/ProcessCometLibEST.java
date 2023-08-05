package experiment.preprocess.libest;

import experiment.project.LibEST;
import util.FileIOUtil;

import java.io.File;
import java.util.List;

public class ProcessCometLibEST {
    private static LibEST libEST = new LibEST();
    private static String reqPath = "dataset/libEST/original/comet/req.txt";
    private static String tcPath = "dataset/libEST/original/comet/test.txt";
    private static String codePath = "dataset/libEST/original/comet/code.txt";

    public static void main(String[] args) {
        initDirectory(libEST);
        readCometTxt(reqPath, libEST.getProcessedLevel1ArtifactPath());
        readCometTxt(tcPath, libEST.getProcessedLevel2ArtifactPath());
        readCometTxt(codePath, libEST.getProcessedLevel3ArtifactPath());
    }

    private static void initDirectory(LibEST libEST) {
        FileIOUtil.initDirectory(libEST.getProcessedLevel1ArtifactPath());
        FileIOUtil.initDirectory(libEST.getProcessedLevel2ArtifactPath());
        FileIOUtil.initDirectory(libEST.getProcessedLevel3ArtifactPath());
    }

    private static void readCometTxt(String cometPath, String outputDir) {
        List<String> cometList = FileIOUtil.readFileByLine(cometPath);
        for (String comet : cometList) {
            String[] ary = comet.split(":");
            String artifactName = ary[0].replace(".txt", "");
            String content = ary[1].replaceAll(",", "").replaceAll("'", "").replaceAll("\\[", "").replaceAll("]", "");
            FileIOUtil.writeFile(content, outputDir + File.separator + artifactName + ".txt");
        }

    }
}
