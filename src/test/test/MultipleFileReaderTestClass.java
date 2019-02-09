package test;

import com.ben.mscit.HTTPRequest;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.TimestampedEntry;
import com.hazelcast.jet.pipeline.ContextFactory;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import static com.hazelcast.jet.pipeline.Sources.filesBuilder;
import com.hazelcast.jet.pipeline.StreamStage;
import static com.hazelcast.jet.pipeline.WindowDefinition.tumbling;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author benjamin
 */
public class MultipleFileReaderTestClass {

    //private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss"));
    private static final ThreadLocal<SimpleDateFormat> VALUE_MAP_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("\"HH:mm\""));

    public static void main(String[] args) {

        Path sourceFile = Paths.get("/home/Benjamin/Desktop/access-logs/").toAbsolutePath();

        //build pipeline
        Pipeline p = Pipeline.create();

        StreamStage<HTTPRequest> stage = p.drawFrom(filesBuilder(sourceFile.toString())
                .glob("access*")
                .build((file, line) -> line))
                .mapUsingContext(ContextFactory.withCreateFn((JetInstance jet) -> new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss")),
                        (format, line) -> {

                            String[] split = line.split(",");

                            HTTPRequest httpRequest = new HTTPRequest();
                            httpRequest.setSourceIP(split[0]);
                            httpRequest.setRequestDateTime(format.parse(split[1]).getTime());
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
                        })
                .addTimestamps(HTTPRequest::getRequestDateTime, SECONDS.toMillis(5));

        stage
                .filter((httpRequest) -> httpRequest.getModule().length() > 1 && httpRequest.getUpstreamServerIP().length() > 1)
                .window(tumbling(MINUTES.toMillis(5)))
                .groupingKey(HTTPRequest::getModule)
                .aggregate(counting())
                .mapUsingContext(ContextFactory.withCreateFn((JetInstance t) -> new SimpleDateFormat("\"HH:mm\"")), (SimpleDateFormat fmt, TimestampedEntry<String, Long> u) -> entry(u.getKey(), "{\"time\":" + fmt.format(new Date(u.getTimestamp())) + ",\"value\":" + u.getValue() + "}"))
                //.map((TimestampedEntry<String, Long> t) -> entry(t.getKey(), "{\"time\":" + VALUE_MAP_DATE_FORMAT.get().format(new Date(t.getTimestamp())) + ",\"value\":" + t.getValue() + "}"))
                //.drainTo(Sinks.logger());
                .drainTo(Sinks.files("/home/Benjamin/Desktop/"));

        JetInstance jet = Jet.newJetClient(createJetConfig());

        try {

            JobConfig jobConfig = new JobConfig();
            jobConfig.addClass(HTTPRequest.class);

            jet.newJob(p, jobConfig).join();

        } catch (Exception ex) {

            ex.printStackTrace(System.err);

        } finally {
            jet.shutdown();
        }
    }

    private static ClientConfig createJetConfig() {

        ClientConfig jetClientConfig = new ClientConfig();

        jetClientConfig.setGroupConfig(new GroupConfig("jet", "jet-pass"));

        ClientNetworkConfig networkConfig = jetClientConfig.getNetworkConfig();
        networkConfig.addAddress("localhost:6701");
        networkConfig.addAddress("localhost:6702");

        networkConfig.setSmartRouting(true);
        networkConfig.setConnectionTimeout(500);
        networkConfig.setConnectionAttemptPeriod(250);
        networkConfig.setConnectionAttemptLimit(1);

        jetClientConfig.setNetworkConfig(networkConfig);

        return jetClientConfig;
    }

}
