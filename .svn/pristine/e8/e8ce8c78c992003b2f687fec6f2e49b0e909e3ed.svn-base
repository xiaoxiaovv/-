package com.istar.mediabroken.utils

import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 * @author zxj
 * @create 2018/6/21
 */
class NumUtils {
    static final DecimalFormat sdf = new DecimalFormat("######0.00");

    static def doubleForMat(double d) {
        return String.format("%.2f", d);
    }

    //1-100整数
    static boolean isIntegerRange(int mun) {
        if (mun > 100 || mun < 1) {
            return false
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(mun + "").matches();
    }
    //1-1000整数
    static boolean isIntegerRange1000(int mun) {
        if (mun > 1000 || mun < 1) {
            return false
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(mun + "").matches();
    }

    static double getRate(def strNum, def endNum) {
        if (strNum == 0) {
            return 0.00
        }
        return (endNum - strNum) / strNum
    }

    static String doublePoints(Double num) {
        return sdf.format(num)
    }
}
