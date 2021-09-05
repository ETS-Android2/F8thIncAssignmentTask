package com.f8th_inc;

import java.util.List;

public class Utils {

    public static boolean isVolumeDownPressedContinously(List<Integer> list) {
        if (list.size() == 3) {
            for (int i : list) {
                if (i < 0)
                    return false;
            }
        } else if (list.size() == 6) {
            for (int i : list) {
                if (i > 0)
                    return false;
            }
        }
        return true;
    }
}
