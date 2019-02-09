# Log Analysis

[Real-Time HTTP Traffic Analysis ](./src/main/java/com/ben/engine) - This is a Java Based application that demonstrates **Big Data Processing** to monitor important key performance indicators (KPI) in a business. In this example we read a big data file and make computations thereafter save the results.

The file is in csv format that contains HTTP traffic data. We use libraries to support this computation such as; **Hazelcast-Jet** for the computation and **Hazelcast-IMDG** as an in-memory data grid where the results are saved.

The computation engine looks at events in a `5 Minutes by 10 secs` Window and computes all events in that window. In other words for any event that occured in the last 5 minutes the computation happens and emits results every 10 secs.

Follow this [link](https://drive.google.com/open?id=1gRaHlsOfaSlrfslsIRtKMnhpvU6ImvBs) to download the sample file

This is the format shown here below.

SOURCE_IP|DATE|METHOD_TYPE|URL|HTTP_CLIENT_TYPE|RESPOSNE_CODE|CONTENT_LENGHT|CLIENT_TYPE|PROCESSING_NODE|REQUEST_TIME_TAKEN|UPSTREAM_CONN_TIME|UPSTREAM_RESP_TIME|MODULE|FILE_NAME
------|------|------|------|------|------|------|------|------|------|------|------|------|------
192.168.1.1|30/Nov/2018:03:35:54|GET|/client|HTTP/1.1|200|33|"Java/1.6.0_45"|192.168.2.5:28080|0.229|0|0.229|module4|access.log-20181201.gz
192.168.1.1|30/Nov/2018:03:35:55|GET|/client|HTTP/1.1|200|64|"Java/1.6.0_45"|192.168.2.4:28080|0.004|0|0.004|module25|access.log-20181201.gz
192.168.1.2|30/Nov/2018:03:35:56|GET|/client|HTTP/1.1|200|106|"Java/1.6.0_45"|192.168.2.5:28081|0.088|0|0.088|module19|access.log-20181201.gz
192.168.1.2|30/Nov/2018:03:35:56|GET|/client|HTTP/1.1|200|161|"Java/1.6.0_45"|192.168.2.3:28080|0.998|0.001|0.998|module13|access.log-20181201.gz
192.168.1.1|30/Nov/2018:03:35:56|GET|/client|HTTP/1.1|200|59|"Java/1.6.0_45"|192.168.2.4:28081|0.025|0|0.025|module5|access.log-20181201.gz
192.168.1.5|30/Nov/2018:03:35:56|POST|/client|HTTP/1.1|200|33|"-"|192.168.2.6:9060|0.002|0.001|0.002|module26|access.log-20181201.gz
192.168.1.1|30/Nov/2018:03:35:56|GET|/client|HTTP/1.1|200|161|"Java/1.6.0_45"|192.168.2.1:28080|0.583|0.001|0.583|module13|access.log-20181201.gz
192.168.1.2|30/Nov/2018:03:35:58|GET|/client|HTTP/1.1|200|160|"Java/1.6.0_45"|192.168.2.5:28080|0.994|0|0.994|module13|access.log-20181201.gz
192.168.1.1|30/Nov/2018:03:35:58|GET|/client|HTTP/1.1|200|84|"Java/1.6.0_45"|192.168.2.3:28080|1.096|0|1.096|module20|access.log-20181201.gz
192.168.1.1|30/Nov/2018:03:35:58|GET|/client|HTTP/1.1|200|161|"Java/1.6.0_45"|192.168.2.3:28080|0.532|0.001|0.532|module13|access.log-20181201.gz


These are some of the KPIs the computation engine makes:

- Number of requests per `module`
- Aggregated `response code` count
- Average response times
- Upstream server hit count 

## POM Declaration
```xml
<dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-client</artifactId>
    <type>jar</type>
</dependency>

<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
    <type>jar</type>
</dependency>

<dependency>
    <groupId>com.hazelcast.jet</groupId>
    <artifactId>hazelcast-jet</artifactId>
    <version>0.7</version>
    <type>jar</type>
</dependency>
```

### Dependencies 
This application depends on 3 jar files:
- Hazelcast IMDG [hazelcast-3.10.3.jar](https://hazelcast.org/download/)
- Hazelcast IMDG Client [hazelcast-client-3.10.3.jar](https://jet.hazelcast.org/download/)
- Hazelcast-Jet [hazelcast-jet-0.7.jar](https://jet.hazelcast.org/download/)

### Installation
Download the dependencies and unzip them

#### Configure Hazelcast IMDG
```sh
$ unzip hazelcast-3.10.3.zip
$ cd hazelcast-3.10.3/bin/
```
Configure the IMDG xml file. Disable multicast by setting the attribute value of the multicast element to *false* and enable the value of the attribute value tcp-ip element by setting it to *true*
```sh
vim hazelcast.xml

<multicast enabled="false">
    <multicast-group>224.2.2.3</multicast-group>
    <multicast-port>54327</multicast-port>
</multicast>

<tcp-ip enabled="true">
    <interface>127.0.0.1</interface>
    <member-list>
        <member>127.0.0.1</member>
    </member-list>
</tcp-ip>
```
Set a small Heap size of about 1GB by edting the start.sh
```sh
vim start.sh

MIN_HEAP_SIZE=1G
MAX_HEAP_SIZE=1G

```
Save the configurations and Start the IMDG.
```sh
$ ./start.sh
```
You should see something like this:
```sh
INFO: [127.0.0.1]:5701 [dev] [3.10.3] 

Members {size:1, ver:1} [
	Member [127.0.0.1]:5701 - 82b3ff9e-59c7-49c3-9a00-53708c8002d9 this
]
```

#### Configure Hazelcast-Jet

```sh
$ unzip hazelcast-jet-0.7.zip 
$ cd hazelcast-jet-0.7/config/
```
Configure the listening port of Hazelcast-Jet by settin the values of the port element to 6701

### Note: 
This port can be change if you modify the code in the `HZJetIMDGConnector.java`
```sh
<port auto-increment="true" port-count="100">6701</port>
```
save and quit and move to the bin directory
```sh
 cd ../bin/
./jet-start.sh
```

You should see something like this:
```sh
INFO: [localhost]:6701 [jet] [0.7] 

Members {size:1, ver:1} [
	Member [localhost]:6701 - 9009d80e-12f5-4dc5-acb0-f73928da80e0 this
]

Jan 10, 2019 8:53:32 PM com.hazelcast.core.LifecycleService
INFO: [localhost]:6701 [jet] [0.7] [localhost]:6701 is STARTED
```

## Putting it all Together
Edit the `HZJetIMDGConnector` by specifying the absolute path to the file `access.log-201812-sample.csv`. This contains **8,305,849** lines. You can then run it.

You can view the results by executing the `clientConsole.sh` file the comes shipped with HazelcastIMDG.

Change the directory to the demo folder and execute the `clientConsole.sh` file
```sh
$ cd /hazelcast-3.10.3/demo
$./clientConsole.sh
```
This will start a terminal that looks something like this.
```sh
INFO: hz.client_0 [dev] [3.10.3] 

Members [1] {
	Member [127.0.0.1]:5701 - 3199e28d-15bf-4095-83f9-99557ea60d2a
}

Jan 11, 2019 6:34:23 PM com.hazelcast.core.LifecycleService
INFO: hz.client_0 [dev] [3.10.3] HazelcastClient 3.10.3 (20180719 - fec4eef) is CLIENT_CONNECTED
Jan 11, 2019 6:34:23 PM com.hazelcast.internal.diagnostics.Diagnostics
INFO: hz.client_0 [dev] [3.10.3] Diagnostics disabled. To enable add -Dhazelcast.diagnostics.enabled=true to the JVM arguments.
hazelcast[default] > 
```

Our Appication saves the results in a named map in a format with a `key,value` pair. For example in the `HZJetIMDGConnector.java` Class on line 142 shows the computed `average response times per module` are saved in a map name `proctimeavg`. We can see these results by changing the prompt and sending a command as shown below with the results

```sh
hazelcast[default] > ns proctimeavg
namespace: proctimeavg
hazelcast[proctimeavg] > m.entries
module6: {"time":"07:10","value":0.02258823529411765}
client: {"time":"07:10","value":0.5729444444444444}
module20: {"time":"07:10","value":1.5407352941176464}
module13: {"time":"07:10","value":0.9694767955801097}
module22: {"time":"07:10","value":0.0022222222222222227}
module24: {"time":"07:10","value":0.503}
module19: {"time":"07:10","value":0.338747884072835}
module9: {"time":"07:05","value":0.0023333333333333335}
module5: {"time":"07:10","value":0.02036311159978035}
module23: {"time":"07:10","value":0.010943815331010439}
module2: {"time":"07:00","value":0.729}
module14: {"time":"07:10","value":0.023761682242990623}
module11: {"time":"07:10","value":0.42040000000000005}
module4: {"time":"07:10","value":0.3606}
module25: {"time":"07:10","value":0.3945730769230769}
module21: {"time":"07:10","value":0.5078409090909092}
module7: {"time":"07:10","value":0.5593333333333332}
module8: {"time":"07:05","value":0.283}
module16: {"time":"07:10","value":1.006}
module15: {"time":"06:55","value":0.005}
module1: {"time":"07:10","value":0.4699836467702371}
module12: {"time":"07:10","value":0.0022500000000000007}
module10: {"time":"07:10","value":0.9199082568807345}
module26: {"time":"07:10","value":0.002613333333333335}
Total 24
hazelcast[proctimeavg] > 
```

License
----
**Demostration Purposes of the USE of Hazelcast IMDG and Hazelcast-Jet**
