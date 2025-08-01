package org.example.studylog.entity.user;

import java.util.LinkedHashMap;
import java.util.Map;

public class LevelThresholds {
    private static final Map<Integer, Integer> thresholds =
            new LinkedHashMap<>() {{
                put(1, 10);
                put(2, 30);
                put(3, 60);
                put(4, 100);
                put(5, 150);
                put(6, 210);
                put(7, 280);
                put(8, 360);
                put(9, 450);
                put(10, 550);
            }};


    public static int getLevelForRecordCount(long recordCount){
        int level = 0;

        for (Map.Entry<Integer, Integer> entry : thresholds.entrySet()) {
            if(recordCount >= entry.getValue()){
                level = entry.getKey();
            } else{
                break;
            }
        }

        return level;
    }
}
