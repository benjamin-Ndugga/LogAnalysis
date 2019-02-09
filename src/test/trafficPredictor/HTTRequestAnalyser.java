package trafficPredictor;

import com.ben.mscit.HTTPRequest;
import com.hazelcast.jet.IMapJet;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.linearTrend;
import com.hazelcast.jet.datamodel.TimestampedEntry;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import static com.hazelcast.jet.pipeline.Sources.filesBuilder;
import com.hazelcast.jet.pipeline.StreamStage;

import static com.hazelcast.jet.pipeline.WindowDefinition.sliding;
import static com.hazelcast.jet.pipeline.WindowDefinition.tumbling;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author Benjamin
 */
public class HTTRequestAnalyser {

    static {
        System.setProperty("hazelcast.multicast.group", "224.18.19.21");
        //System.setProperty("hazelcast.logging.type", "none");
    }

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss"));

    public static void main(String[] args) {

        Path sourceFile = Paths.get("/home/Benjamin/Desktop/access.log-20181203.gz.csv").toAbsolutePath();

        Pipeline p = Pipeline.create();

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

        StreamStage<TimestampedEntry<String, Long>> aggregate1 = stage
                .filter((httpRequest) -> httpRequest.getModule().length() > 1 && httpRequest.getModule().equals("MICROBUNDLE"))
                .addTimestamps(HTTPRequest::getRequestDateTime, SECONDS.toMillis(5))
                .window(tumbling(MINUTES.toMillis(1)))
                .groupingKey(HTTPRequest::getModule)
                .aggregate(counting());

        aggregate1
                //.window(tumbling(MINUTES.toMillis(5)))
                .window(sliding(MINUTES.toMillis(60), MINUTES.toMillis(5)))
                .groupingKey(TimestampedEntry::getKey)
                .aggregate(linearTrend(TimestampedEntry::getTimestamp, TimestampedEntry::getValue))
                .drainTo(Sinks.files("/home/Benjamin/Desktop/"));

//        stage.filter((httpRequest) -> true)
//                .groupingKey((request) -> {
//                    Calendar c = Calendar.getInstance();
//                    c.setTimeInMillis(request.getRequestDateTime());
//                    return c.get(Calendar.HOUR);//grouping key is HOUR
//                })
//                .aggregate(counting())
//                .drainTo(Sinks.files("/home/Benjamin/Desktop/"));
//
//        //module hits
//        stage.filter((httpRequest) -> httpRequest.getModule().length() > 1)
//                .groupingKey(HTTPRequest::getModule)
//                .aggregate(counting())
//                .drainTo(Sinks.map("count"));
//        
//        //response code hits
//        stage.groupingKey(HTTPRequest::getHttpRespCode)
//                .aggregate(counting())
//                .drainTo(Sinks.files("/home/Benjamin/Desktop/"));
        JetInstance jet = Jet.newJetInstance();

        try {

            // Perform the computation
            jet.newJob(p).join();

            IMapJet<Object, Object> map = jet.getMap("count");
            Set<Object> keySet = map.keySet();
            Iterator<Object> iterator = keySet.iterator();

            while (iterator.hasNext()) {
                Object next = iterator.next();

                System.out.println(next + " --> " + map.get(next));
            }

        } finally {
            Jet.shutdownAll();
        }
    }
}
