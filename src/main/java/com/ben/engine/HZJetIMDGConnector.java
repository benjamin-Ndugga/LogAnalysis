package com.ben.engine;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.TimestampedEntry;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;

import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.aggregate.AggregateOperations.averagingDouble;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.pipeline.Sources.filesBuilder;
import com.hazelcast.jet.pipeline.StageWithWindow;
import static com.hazelcast.jet.pipeline.WindowDefinition.tumbling;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author Benjamin
 */
@SpringBootApplication
public class HZJetIMDGConnector {

    private static final ThreadLocal<SimpleDateFormat> VALUE_MAP_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("\"HH:mm\""));

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss"));

    //change this line to point to the location where You file is
    private static final String ABSOLUTE_PATH_TO_FILE = "/home/Benjamin/Desktop/access-logs/";

    //private static final String ABSOLUTE_PATH_FOR_RESULTS = "/";

    public static void main(String[] args) {
        
        //start the spring-boot application
        SpringApplication.run(HZJetIMDGConnector.class, args);
        
        try {
            //build the pipeline
            Pipeline p = buildPipeline();

            //instantiate the jet configs
            JetInstance jet = Jet.newJetClient(createJetConfig());
            JobConfig jobConfig = new JobConfig();

            jobConfig.addClass(HZJetIMDGConnector.class);
            jobConfig.addClass(HTTPRequest.class);
            jobConfig.addClass(StreamedCountTxn.class);

            jobConfig.setName("http-log-stream-txns");


            //Perform the computation
            jet.newJob(p, jobConfig).join();

           
        } finally {
            //shut down when Job is done
            Jet.shutdownAll();
        }
    }

    public static Pipeline buildPipeline() {

        Path sourceFile = Paths.get(ABSOLUTE_PATH_TO_FILE).toAbsolutePath();

        //build pipeline
        Pipeline p = Pipeline.create();

        //read from flat file
        BatchStage<HTTPRequest> stage = p.drawFrom(
                filesBuilder(sourceFile.toString())
                        .glob("access.log-201812-sample.csv")
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

        /**
         * looking at events in last 5 Minutes and sliding by 10 secs. All the
         * events here are that occurred in the last 10seconds are emitted and
         * return an object <em> stageWithWindow </em> which is used as a
         * downstream to various sinks that are defined by a grouping key.
         */
        StageWithWindow<HTTPRequest> stageWithWindow = stage
                .filter((httpRequest) -> httpRequest.getModule().length() > 1 && httpRequest.getUpstreamServerIP().length() > 1)
                .addTimestamps(HTTPRequest::getRequestDateTime, SECONDS.toMillis(10))
                .window(tumbling(MINUTES.toMillis(5)));

        /**
         * Counting the number of Hits per Module using the Module Name as the
         * grouping key.
         */
        stageWithWindow
                .groupingKey(HTTPRequest::getModule)
                .aggregate(counting())
                .map((TimestampedEntry<String, Long> t) -> entry(t.getKey(), "{\"time\":" + VALUE_MAP_DATE_FORMAT.get().format(new Date(t.getTimestamp())) + ",\"value\":" + t.getValue() + "}"))
                .drainTo(Sinks.remoteMap("mdstcnt", createHzConfig()));
                //.drainTo(Sinks.files(ABSOLUTE_PATH_FOR_RESULTS));

        /**
         * Counting the number of response codes grouped by the response code.
         */
        stageWithWindow
                .groupingKey(HTTPRequest::getHttpRespCode)
                .aggregate(counting())
                .map((TimestampedEntry<String, Long> t) -> entry(t.getKey(), "{\"time\":" + VALUE_MAP_DATE_FORMAT.get().format(new Date(t.getTimestamp())) + ",\"value\":" + t.getValue() + "}"))
                .drainTo(Sinks.remoteMap("httpcodecnt", createHzConfig()));

        /**
         * Compute the average response times per module.
         */
        stageWithWindow
                .groupingKey(HTTPRequest::getModule)
                .aggregate(averagingDouble(HTTPRequest::getProcessingTime))
                .map((TimestampedEntry<String, Double> t) -> entry(t.getKey(), "{\"time\":" + VALUE_MAP_DATE_FORMAT.get().format(new Date(t.getTimestamp())) + ",\"value\":" + t.getValue() + "}"))
                .drainTo(Sinks.remoteMap("proctimeavg", createHzConfig()));

        /**
         * Count the number of hits per upstream server.
         */
        stageWithWindow
                .groupingKey(HTTPRequest::getUpstreamServerIP)
                .aggregate(counting())
                .map((TimestampedEntry<String, Long> t) -> entry(t.getKey().split("\\:"), "{\"time\":" + VALUE_MAP_DATE_FORMAT.get().format(new Date(t.getTimestamp())) + ",\"value\":" + t.getValue() + "}"))
                .drainTo(Sinks.remoteMap("upstreamcnt", createHzConfig()));

        /**
         * Count the number of hits per cluster.
         */
        stageWithWindow
                .groupingKey((HTTPRequest httpRequest) -> httpRequest.getUpstreamServerIP().split("\\:")[1])
                .aggregate(counting())
                .map((TimestampedEntry<String, Long> t) -> entry(getClusterName(t.getKey()), "{\"time\":" + VALUE_MAP_DATE_FORMAT.get().format(new Date(t.getTimestamp())) + ",\"value\":" + t.getValue() + "}"))
                .drainTo(Sinks.remoteMap("clustercnt", createHzConfig()));

        return p;

    }

    private static ClientConfig createJetConfig() {

        ClientConfig jetClientConfig = new ClientConfig();

        jetClientConfig.setGroupConfig(new GroupConfig("jet", "jet-pass"));
        
        jetClientConfig.setProperty("hazelcast.logging.type", "jdk");

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

    public static ClientConfig createHzConfig() {

        ClientConfig clientConfig = new ClientConfig();

        clientConfig.setGroupConfig(new GroupConfig("dev", "dev-pass"));
        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();

        networkConfig.addAddress("localhost");
        networkConfig.setSmartRouting(true);
        networkConfig.setConnectionTimeout(500);
        networkConfig.setConnectionAttemptPeriod(250);
        networkConfig.setConnectionAttemptLimit(1);
        clientConfig.setNetworkConfig(networkConfig);

        return clientConfig;

    }

    private static String getClusterName(String port) {

        switch (port) {
            case "28081":
                return "Cluster1";
            case "28080":
                return "Cluster2";
            default:
                return "Other";
        }

    }
}
