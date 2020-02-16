package com.hazeladmin.controller;


import com.hazeladmin.onLoading.InitializeHazel;
import com.hazeladmin.service.HazelDataService;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.management.TimedMemberState;
import com.hazelcast.monitor.LocalMemoryStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HazelDataController {

    @Autowired
    InitializeHazel initializeHazel;

    @Autowired
    HazelDataService hazelDataService;

    @GetMapping(value = "/getalltimedmemberstates/{timeduration}")
    public void getAllTimedMemberStates(@PathVariable long timeduration){
        hazelDataService.getLocalMapStatsInTimeDuration(timeduration,initializeHazel.getRandomMapName());
    }

    @GetMapping("/start")
    public void start(){
        initializeHazel.start(10);
    }
}
