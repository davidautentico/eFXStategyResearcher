package com.drosa.efx.domain.model.experimental.basicStrategies.base;

import java.util.ArrayList;

public class TestPriceBuffer {

    public static int getMinMaxBuff(ArrayList<Integer> maxMins, int begin, int end,
                                    int thr) {

        int index = -1;

        if (begin <= 0) begin = 0;
        if (end >= maxMins.size() - 1) end = maxMins.size() - 1;

        for (int i = end; i >= begin; i--) {
            int maxMin = maxMins.get(i);

            if (maxMin >= thr) return i;
            if (maxMin <= -thr) return i;
        }

        return index;
    }


}
