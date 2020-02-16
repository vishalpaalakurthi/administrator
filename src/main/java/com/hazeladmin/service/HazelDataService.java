package com.hazeladmin.service;

import com.hazelcast.internal.management.TimedMemberState;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.util.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HazelDataService {
    @Autowired
    Map<String, Map<Long, TimedMemberState>> timedMemberStateMap;

    long startTime = System.currentTimeMillis();

    public void getLocalMapStatsInTimeDuration(long timeduration, String mapName) {
        Map<String, LocalMapStats> localMapStatsMap = new HashMap<>();
        timedMemberStateMap.forEach((memberName, longTimedMemberStateMap) -> {
            localMapStatsMap.put(memberName, getLocalMapStats(memberName, mapName, timeduration));
        });
    }

    private LocalMapStats getLocalMapStats(String memberName, String mapName, long throughputInterval) {
        long intervalEnd = toStateTime(0L);
        long intervalBeg = this.startTime < intervalEnd - throughputInterval ? floorTime(intervalEnd - throughputInterval) : this.startTime;

        boolean findFirstRecord = false;
        Map<Long, TimedMemberState> memberStateMap = timedMemberStateMap.get(memberName);
        long putDiff = 0L;
        long getDiff = 0L;
        long removeDiff = 0L;
        long putLatencyDiff = 0L;
        long getLatencyDiff = 0L;
        long removeLatencyDiff = 0L;
        long lastPutCount = 0L;
        long lastGetCount = 0L;
        long lastRemoveCount = 0L;
        long lastPutLatency = 0L;
        long lastGetLatency = 0L;
        long lastRemoveLatency = 0L;
        double maxPutLatency = 0.0D;
        double maxGetLatency = 0.0D;
        double maxRemoveLatency = 0.0D;

        int listSize = 0;
        LocalMapStats endLocalMapStats = null;
        for (Long time = intervalBeg; time <= intervalEnd; time += 5000L) {
            TimedMemberState timedMemberState = memberStateMap.get(time);
            LocalMapStats localMapStats = null;
            if (timedMemberState != null) {
                localMapStats = timedMemberState.getMemberState().getLocalMapStats(mapName);
                if (localMapStats != null && !findFirstRecord) {
                    endLocalMapStats = localMapStats;
                    lastPutCount = localMapStats.getPutOperationCount();
                    lastGetCount = localMapStats.getGetOperationCount();
                    lastRemoveCount = localMapStats.getRemoveOperationCount();
                    lastPutLatency = localMapStats.getTotalPutLatency();
                    lastGetLatency = localMapStats.getTotalGetLatency();
                    lastRemoveLatency = localMapStats.getTotalRemoveLatency();
                    maxPutLatency = 0.0D;
                    maxGetLatency = 0.0D;
                    maxRemoveLatency = 0.0D;
                    findFirstRecord = true;
                } else if (localMapStats != null && findFirstRecord) {
                    endLocalMapStats = localMapStats;
                    putDiff += localMapStats.getPutOperationCount() - lastPutCount;
                    lastPutCount = localMapStats.getPutOperationCount();
                    putLatencyDiff += localMapStats.getTotalPutLatency() - lastPutLatency;
                    lastPutLatency = localMapStats.getTotalPutLatency();
                    getDiff += localMapStats.getGetOperationCount() - lastGetCount;
                    lastGetCount = localMapStats.getGetOperationCount();
                    getLatencyDiff += localMapStats.getTotalGetLatency() - lastGetLatency;
                    lastGetLatency = localMapStats.getTotalGetLatency();
                    removeDiff += localMapStats.getRemoveOperationCount() - lastRemoveCount;
                    lastRemoveCount = localMapStats.getRemoveOperationCount();
                    removeLatencyDiff += localMapStats.getTotalRemoveLatency() - lastRemoveLatency;
                    lastRemoveLatency = localMapStats.getTotalRemoveLatency();
                    if (putDiff < 0L) {
                        putDiff = 0L;
                        lastPutCount = 0L;
                    }

                    if (getDiff < 0L) {
                        getDiff = 0L;
                        lastGetCount = 0L;
                    }

                    if (removeDiff < 0L) {
                        removeDiff = 0L;
                        lastRemoveCount = 0L;
                    }

                    if (putLatencyDiff < 0L) {
                        putLatencyDiff = 0L;
                        lastPutLatency = 0L;
                    }

                    if (getLatencyDiff < 0L) {
                        getLatencyDiff = 0L;
                        lastGetLatency = 0L;
                    }

                    if (removeLatencyDiff < 0L) {
                        removeLatencyDiff = 0L;
                        lastRemoveLatency = 0L;
                    }

                    maxPutLatency = Math.max(maxPutLatency, putDiff == 0L ? 0.0D : (double) putLatencyDiff / (double) putDiff);
                    maxGetLatency = Math.max(maxGetLatency, getDiff == 0L ? 0.0D : (double) getLatencyDiff / (double) getDiff);
                }
                listSize += 1;
            }
        }
        double intervalInSeconds = (double) listSize * 5.0D;
        double putsPerSecond = (double) putDiff / intervalInSeconds;
        double getsPerSecond = (double) getDiff / intervalInSeconds;
        double removesPerSecond = (double) removeDiff / intervalInSeconds;
        String avgPutLatency = (double) putDiff == 0.0D ? "0" : format((double) putLatencyDiff / (double) putDiff);
        String avgGetLatency = (double) getDiff == 0.0D ? "0" : format((double) getLatencyDiff / (double) getDiff);
        String avgRemoveLatency = (double) removeDiff == 0.0D ? "0" : format((double) removeLatencyDiff / (double) removeDiff);
        String maxPutLatencyString = (double) putDiff == 0.0D ? "0" : format(maxPutLatency);
        String maxGetLatencyString = (double) getDiff == 0.0D ? "0" : format(maxGetLatency);
        String maxRemoveLatencyString = (double) removeDiff == 0.0D ? "0" : format(maxRemoveLatency);

        List<String> stats = buildOperationsTableStringList(memberName, format(putsPerSecond), format(getsPerSecond), format(removesPerSecond), avgPutLatency, avgGetLatency, avgRemoveLatency, maxPutLatencyString, maxGetLatencyString, maxRemoveLatencyString);
        System.out.println("LocalMapStats: " + stats);

        return null;
    }

    private long toStateTime(Long curtime) {
        return curtime > 0L ? floorTime(curtime) : floorTime(Clock.currentTimeMillis() - 5000L);
    }

    public long floorTime(long now) {
        return now - now % 5000L;
    }

    private static String format(Double number) {
        return number == 0.0D ? "0" : String.format("%.2f", number);
    }

    private List<String> buildOperationsTableStringList(String... args) {
        ArrayList<String> values = new ArrayList();
        Collections.addAll(values, args);
        return values;
    }
}
