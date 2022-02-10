package com.istar.mediabroken.utils;


import java.util.HashSet;
import java.util.Set;

public class JaccardDistanceUtils {
    public static Set<Character> getCharSet(String str) {
        Set<Character> set = new HashSet<>(str.length());
        for (int i = 0; i < str.length(); i++) {
            set.add(str.charAt(i));
        }
        return set;
    }

    public static float computeJaccardDistance(Set<Character> set1, Set<Character> set2) {
        Set<Character> intersection = new HashSet<>();
        intersection.addAll(set1);
        intersection.retainAll(set2);

        Set<Character> union = new HashSet<>();
        union.addAll(set1);
        union.addAll(set2);
        return (float)(intersection.size()) / union.size();
    }

    public static float computeJaccardDistance(String str1, String str2) {
        return computeJaccardDistance(getCharSet(str1), getCharSet(str2));
    }

    public static int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }

    public static void main(String[] args) {
        Set<Character> set1 = getCharSet("一只猫坐在一张椅子上");
        Set<Character> set2 = getCharSet("一只猫坐在那张椅子上");
        Set<Character> set3 = getCharSet("一只狗趴在床上");
        System.out.println(computeJaccardDistance(set1, set2));
        System.out.println(computeJaccardDistance(set1, set3));
        System.out.println(computeJaccardDistance(set2, set3));

        long start = 0, end = 0;

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
//            compile("一只猫坐在一张椅子上", "一只猫坐在那张椅子上");
            computeJaccardDistance("一只猫坐在一张椅子上", "一只猫坐在那张椅子上");
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);





         start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            getCharSet("一只猫坐在一张椅子上");
        }
         end = System.currentTimeMillis();
        System.out.println(end - start);




        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
//            compile("一只猫坐在一张椅子上", "一只猫坐在那张椅子上");
            computeJaccardDistance(set1, set2);
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);


        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
//            compile("一只猫坐在一张椅子上", "一只猫坐在那张椅子上");
            computeJaccardDistance("一只猫坐在一张椅子上", "一只猫坐在那张椅子上");
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

}
