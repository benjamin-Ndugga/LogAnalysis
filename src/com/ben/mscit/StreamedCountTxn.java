package com.ben.mscit;

import static com.hazelcast.jet.impl.util.Util.toLocalDateTime;
import java.io.Serializable;

/**
 *
 * @author Benjamin
 */
public class StreamedCountTxn implements Serializable {

    private String module;
    private long time;

    public StreamedCountTxn(String module, long time) {
        this.module = module;
        this.time = time;

    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "StreamCntTxn { module=" + module + ", time=" + toLocalDateTime(time) + "}";
    }

}
