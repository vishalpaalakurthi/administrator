package com.hazeladmin.onLoading;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ExecutorService;

@Component
public class InitializeHazel {

    @Autowired
    HazelcastInstance hazelcastInstance;

    String[] maps= new String[]{"UserMap","AdminMap","SubUsersMap"};
    String[] keys= new String[]{"key1","key2","key3"};
    String[] values= new String[]{"value1","value2","value3"};


    @Scheduled(fixedRate = 2000L)
    public void putDataIntoMap(){
        Random random= new Random();
        String key= keys[random.nextInt(keys.length)];
        String value= values[random.nextInt(values.length)];

        IMap map= hazelcastInstance.getMap(maps[random.nextInt(maps.length)]);
        map.put(key,value);
    }


    @Scheduled(fixedRate = 4000L)
    public void removeDataFromMap(){
        Random random= new Random();
        String key= keys[random.nextInt(keys.length)];

        IMap map= hazelcastInstance.getMap(maps[random.nextInt(maps.length)]);
        map.remove(key);
    }

    public String getRandomMapName(){
        Random random= new Random();
        String mapName= maps[random.nextInt(maps.length)];
        return mapName;
    }


    public void start(int i) {
        for (int j=0;j<i;j++){
            putDataIntoMap();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            removeDataFromMap();

        }
    }
}
