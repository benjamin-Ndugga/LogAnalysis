package test;

import com.ben.mscit.HTTPRequest;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import static com.hazelcast.jet.pipeline.Sources.filesBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

/**
 *
 * @author benjamin
 */
public class SingleFileReaderTestClass {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss"));

    public static void main(String[] args) {

        Path sourceFile = Paths.get("/home/Benjamin/Desktop/access-logs/file2.csv").toAbsolutePath();

        //build pipeline
        Pipeline p = Pipeline.create();

        //read from flat file
        BatchStage<HTTPRequest> stage = p.drawFrom(
                filesBuilder(sourceFile.getParent().toString())
                        .glob(sourceFile.getFileName().toString())
                        .build((file, line) -> {

                            String[] split = line.split(",");

                            HTTPRequest httpRequest = new HTTPRequest();

                            httpRequest.setSourceIP(split[0]);
                            httpRequest.setRequestDateTime(DATE_FORMAT.get().parse(split[1]).getTime());
                            httpRequest.setMethodType(split[2]);
                            httpRequest.setUrlCall(split[3]);
                            httpRequest.setHttpType(split[4]);
                            httpRequest.setHttpRespCode(split[5]);
                            httpRequest.setContentLength(split[6]);
                            httpRequest.setClientType(split[7]);
                            httpRequest.setUpstreamServerIP(split[8]);
                            httpRequest.setProcessingTime(split[9]);
                            httpRequest.setUpstreamConnectTime(split[10]);
                            httpRequest.setUpstreamResponseTime(split[11]);
                            httpRequest.setModule(split[12].trim());

                            return httpRequest;

                        }));

        stage.drainTo(Sinks.logger());

        JetInstance jet = Jet.newJetInstance();

        try {
            jet.newJob(p).join();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            jet.shutdown();
        }

    }

}
