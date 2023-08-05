package experiment.preprocess.libest;

import experiment.preprocess.util.StanfordNlpUtil;
import experiment.preprocess.util.TextPreprocess;
import experiment.project.LibEST;
import util.FileIOUtil;

import java.io.File;

public class LibestProcess {
    private static StanfordNlpUtil stanfordNlpUtil = new StanfordNlpUtil();
    private static LibEST libEST = new LibEST();

    public static void main(String[] args) {
        initDirectory();
        processReqText();
    }

    private static void initDirectory( ) {
        FileIOUtil.initDirectory(libEST.getProcessedLevel1ArtifactPath());
    }

    private static void processReqText( ) {
        File idDirectory = new File(libEST.getUnprocessedLevel1ArtifactPath());
        if (idDirectory.isDirectory()) {
            for (File f : idDirectory.listFiles()) {
                if (!f.getName().contains(".txt"))
                    continue;
                String text = FileIOUtil.readFile(f.getPath());
                TextPreprocess textPreprocess = new TextPreprocess(text);
                String processedText = textPreprocess.doNlpFileProcess();
                FileIOUtil.continueWriteFile(processedText, libEST.getProcessedLevel1ArtifactPath() + "/" + f.getName());
            }
        }
    }
}
