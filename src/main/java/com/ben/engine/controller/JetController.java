package com.ben.engine.controller;

import com.google.gson.Gson;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Benjamin
 */
@RestController
@RequestMapping("/")
public class JetController {

    private static final Logger LOGGER = Logger.getLogger("CLIENT");

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getWelcomeMsg() {

        LOGGER.log(Level.INFO, "APPLICATION-LOGS!");
        return "Application Log-Analysis!- Using Hazelcast-Jet";
    }

    @RequestMapping(value = "/module/{app}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTxnByApp(@PathVariable(value = "app") String module) {
        HazelcastInstance instance = null;
        try {

            instance = connectToIMDB();

            IMap<String, String> map = instance.getMap("mdstcnt");

            return map.get(module);

        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, null, e);

            return "{\"time\":\"00:00\",\"value\":0}";

        } finally {

            if (instance != null) {
                instance.shutdown();
            }
        }
    }

    @RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public String getTxnByApp() {
        HazelcastInstance instance = null;
        try {

            instance = connectToIMDB();

            IMap<String, String> imap = instance.getMap("mdstcnt");

            Gson gson = new Gson();

            return gson.toJson(imap);

        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, null, e);

            return null;

        } finally {

            if (instance != null) {
                instance.shutdown();
            }
        }
    }

    @RequestMapping(value = "/respcodes", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public String getRespCodeTxn() {

        HazelcastInstance instance = null;
        try {
            instance = connectToIMDB();

            IMap<String, String> imap = instance.getMap("httpcodecnt");
            Gson gson = new Gson();

            return gson.toJson(imap);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            return null;
        } finally {
            if (instance != null) {
                instance.shutdown();
            }
        }
    }

    @RequestMapping(value = "/clusters", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public String getClusterTxn() {

        HazelcastInstance instance = null;
        try {
            instance = connectToIMDB();

            IMap<String, String> imap = instance.getMap("clustercnt");
            Gson gson = new Gson();

            return gson.toJson(imap);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            return null;
        } finally {
            if (instance != null) {
                instance.shutdown();
            }
        }
    }

    @RequestMapping(value = "/proctimeavg", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public String getProcessingTimeAVG() {

        HazelcastInstance instance = null;
        try {
            instance = connectToIMDB();

            IMap<String, String> imap = instance.getMap("proctimeavg");
            Gson gson = new Gson();

            return gson.toJson(imap);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            return null;
        } finally {
            if (instance != null) {
                instance.shutdown();
            }
        }
    }

    private HazelcastInstance connectToIMDB() {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setProperty("hazelcast.logging.type", "none");

        clientConfig.setGroupConfig(new GroupConfig("dev", "dev-pass"));
        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();

        networkConfig.addAddress("localhost");
        networkConfig.setSmartRouting(true);
        networkConfig.setConnectionTimeout(500);
        networkConfig.setConnectionAttemptPeriod(250);
        networkConfig.setConnectionAttemptLimit(1);
        clientConfig.setNetworkConfig(networkConfig);

        return HazelcastClient.newHazelcastClient(clientConfig);

    }

}
