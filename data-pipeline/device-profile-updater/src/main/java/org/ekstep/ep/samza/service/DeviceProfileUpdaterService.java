package org.ekstep.ep.samza.service;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.gson.Gson;
<<<<<<< HEAD
import com.google.gson.reflect.TypeToken;
import org.apache.samza.config.Config;
import org.ekstep.ep.samza.core.Logger;
=======
import com.google.gson.JsonSyntaxException;
import org.apache.samza.config.Config;
import org.ekstep.ep.samza.core.Logger;
import org.ekstep.ep.samza.domain.DeviceProfile;
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
import org.ekstep.ep.samza.task.DeviceProfileUpdaterSink;
import org.ekstep.ep.samza.task.DeviceProfileUpdaterSource;
import org.ekstep.ep.samza.util.CassandraConnect;
import org.ekstep.ep.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
<<<<<<< HEAD

import java.lang.reflect.Type;
=======
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
import java.util.*;

public class DeviceProfileUpdaterService {

    private static Logger LOGGER = new Logger(DeviceProfileUpdaterService.class);
    private RedisConnect redisConnect;
    private Jedis deviceStoreConnection;
    private int deviceStoreDb;
    private CassandraConnect cassandraConnection;
    private String cassandra_db;
    private String cassandra_table;
    private Gson gson = new Gson();
<<<<<<< HEAD
    private List<String> contentModelListTypeFields;
    private Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();
=======
    private Type mapType = new TypeToken<Map<String, Object>>() { }.getType();
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6

    public DeviceProfileUpdaterService(Config config, RedisConnect redisConnect, CassandraConnect cassandraConnection) {
        this.redisConnect = redisConnect;
        this.cassandraConnection = cassandraConnection;
        this.deviceStoreDb = config.getInt("redis.database.deviceStore.id", 2);
        this.deviceStoreConnection = redisConnect.getConnection(deviceStoreDb);
        this.cassandra_db = config.get("cassandra.keyspace", "device_db");
        this.cassandra_table = config.get("cassandra.device_profile_table", "device_profile");

    }

    public void process(DeviceProfileUpdaterSource source, DeviceProfileUpdaterSink sink) {
<<<<<<< HEAD
            Map<String, Object> message = source.getMap();
            updateDeviceDetails(message, sink);
=======
        try {
            Map<String, Object> message = source.getMap();
            updateDeviceDetails(message, sink);
        } catch (JsonSyntaxException e) {
            LOGGER.error(null, "INVALID EVENT: " + source.getMessage());
            sink.toMalformedTopic(source.getMessage());
        }
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
    }

    private void updateDeviceDetails(Map<String, Object> message, DeviceProfileUpdaterSink sink) {
        Map<String, String> deviceData = new HashMap<String, String>();

        for (Map.Entry<String, Object> entry : message.entrySet()) {
            if (entry.getValue() instanceof String) {
                deviceData.put(entry.getKey(), entry.getValue().toString());
            }
        }
        if (deviceData.size() > 0) {
<<<<<<< HEAD
            deviceData.values().removeAll(Collections.singleton(null));
            deviceData.values().removeAll(Collections.singleton(""));
            addDeviceDataToCache(deviceData, deviceStoreConnection);
            sink.deviceCacheUpdateSuccess();
            addDeviceDataToDB(deviceData, cassandraConnection);
            sink.deviceDBUpdateSuccess();
            sink.success();
=======
            deviceData.values().removeAll(Collections.singleton("null"));
            deviceData.values().removeAll(Collections.singleton(""));
            deviceData.values().removeAll(Collections.singleton("{}"));
            DeviceProfile deviceProfile = new DeviceProfile().fromMap(deviceData);
            String deviceId = deviceData.get("device_id");
            if (null != deviceId && !deviceId.isEmpty()) {
                addDeviceDataToCache(deviceId, deviceProfile, deviceStoreConnection);
                sink.deviceCacheUpdateSuccess();
                addDeviceDataToDB(deviceData, cassandraConnection);
                sink.deviceDBUpdateSuccess();
                sink.success();
            }
            else { sink.failed(); }
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
        }

    }

<<<<<<< HEAD
    private void addDeviceDataToCache(Map<String, String> deviceData, Jedis redisConnection) {
        String deviceId = deviceData.get("device_id");
        try {
            if (null != deviceId && !deviceId.isEmpty() && null != deviceData && !deviceData.isEmpty()) {
                redisConnection.hmset(deviceId, deviceData);
            }
=======
    private void addDeviceDataToCache(String deviceId, DeviceProfile deviceProfile, Jedis redisConnection) {
        try {
            addToCache(deviceId, deviceProfile, redisConnection);
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
        } catch (JedisException ex) {
            redisConnect.resetConnection();
            try (Jedis redisConn = redisConnect.getConnection(deviceStoreDb)) {
                this.deviceStoreConnection = redisConn;
<<<<<<< HEAD
                if (null != deviceData)
                    addToCache(deviceId, gson.toJson(deviceData), deviceStoreConnection);
=======
                addToCache(deviceId, deviceProfile, deviceStoreConnection);
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
            }
        }
    }

    private void addDeviceDataToDB(Map<String, String> deviceData, CassandraConnect cassandraConnection) {
<<<<<<< HEAD
        String deviceId = deviceData.get("device_id");
        if (null != deviceId && !deviceId.isEmpty() && null != deviceData && !deviceData.isEmpty()) {
            Insert query = QueryBuilder.insertInto(cassandra_db, cassandra_table).values(new ArrayList<>(deviceData.keySet()), new ArrayList<>(deviceData.values()));
            cassandraConnection.upsert(query);
        }
    }

    private void addToCache(String key, String value, Jedis redisConnection) {
        if (null != key && !key.isEmpty() && null != value && !value.isEmpty()) {
            redisConnection.set(key, value);
            LOGGER.info(key,"Updated successfully");
        }
=======
        Map<String, String> parseduaspec = null != deviceData.get("uaspec") ? parseSpec(deviceData.get("uaspec")) : null;
        deviceData.values().removeAll(Collections.singleton(deviceData.get("uaspec")));
        Map<String, String> parsedevicespec = null != deviceData.get("device_spec") ? parseSpec(deviceData.get("device_spec")) : null;
        deviceData.values().removeAll(Collections.singleton(deviceData.get("device_spec")));

        Insert query = QueryBuilder.insertInto(cassandra_db, cassandra_table)
                .values(new ArrayList<>(deviceData.keySet()), new ArrayList<>(deviceData.values())).value("uaspec", parseduaspec).value("device_spec", parsedevicespec);
        cassandraConnection.upsert(query);
    }

    private Map<String, String> parseSpec(String spec) {
        Map<String, String> parseSpec = new HashMap<String, String>();
        parseSpec = gson.fromJson(spec, mapType);

        return parseSpec;
    }

    private void addToCache(String deviceId, DeviceProfile deviceProfile, Jedis redisConnection) {
            redisConnection.hmset(deviceId, deviceProfile.toMap());
            LOGGER.info(deviceId, "Updated successfully");
>>>>>>> 292e29c73c0c23b42b66a30a697870d7617207c6
    }
}
