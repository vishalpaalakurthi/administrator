package com.hazeladmin.controller;

import com.google.gson.Gson;
import com.hazeladmin.service.HazelDataService;
import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.management.TimedMemberState;
import com.hazelcast.internal.management.dto.ClientBwListDTO;
import com.hazelcast.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class ManagementCenterController {
    Logger logger= Logger.getLogger(ManagementCenterController.class.getName());

    @Autowired
    Map<String, Map<Long, TimedMemberState>> timedMemberStateMap;

    @Autowired
    HazelDataService hazelDataService;

    @RequestMapping("/collector.do")
    public void collectTimedMemberState(HttpServletRequest request, HttpServletResponse response){
        try {
            JsonObject jsonObject = Json.parse(request.getReader()).asObject().get("timedMemberState").asObject();
            TimedMemberState timedMemberState = new TimedMemberState();
            timedMemberState.fromJson(jsonObject);

           /* System.out.println(jsonObject);
            System.out.println(new Gson().toJson(timedMemberState.getMemberState().getClients()));*/


            Map<Long, TimedMemberState> map = timedMemberStateMap.get(timedMemberState.getMemberState().getAddress());
            Long currentTime= hazelDataService.floorTime(System.currentTimeMillis());
            if (map==null) {
                Map<Long,TimedMemberState> memberStateMap = new HashMap<>();
                memberStateMap.put(currentTime, timedMemberState);
                timedMemberStateMap.put(timedMemberState.getMemberState().getAddress(), memberStateMap);
            } else {
                timedMemberStateMap.get(timedMemberState.getMemberState().getAddress()).put(currentTime, timedMemberState);
            }

            Sample sample = new Sample();
            ClientBwListDTO clientBwList= new ClientBwListDTO();
            clientBwList.mode= ClientBwListDTO.Mode.DISABLED;
            clientBwList.entries=new ArrayList<>();

            sample.setClientBwList(clientBwList);
            String configJson = new Gson().toJson(sample);
            String memberConfigETag = request.getHeader("If-None-Match");
            String configETag = MD5Util.toMD5String(configJson);

            response.setHeader("ETag", configETag);
            response.getWriter().write(configJson);
        }catch (Exception ex){
            logger.info("Exception at Collector.do "+ex.getMessage());
        }
    }
}

class Sample{
    ClientBwListDTO clientBwList;

    public ClientBwListDTO getClientBwList() {
        return clientBwList;
    }

    public void setClientBwList(ClientBwListDTO clientBwList) {
        this.clientBwList = clientBwList;
    }
}
