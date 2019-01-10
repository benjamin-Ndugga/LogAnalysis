# LogAnalysis

[Real-Time HTTP Traffic Analysis ](./src/com/ben/mscit) - A demostration on how streamed data continuously computes trends as it happens. Looking at events in last 5 Minutes and sliding by 10 secs,
computes the total number of requests per module, response code count, average response times , upstream server hit count. This then saves the results to an IMDG. There are lines of code that specify where the results can be wrtiten  with a terminal function `drainTo`. You can change this to see the results by tailing the output file.

Each module has been deployed on clusters 1 and 2, the application records the number of each cluster is taking in the given time window.

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
$ vim hazelcast.xml

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
$ vim start.sh

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
Edit the `HZJetIMDGConnector` by specifying the absolute path to the file `access.log-201812-sample.csv`. This contains nginx requests with **8,305,849** lines.


License
----
**Demostration Purposes of the USE of Hazelcast IMDG and Hazelcast-Jet**