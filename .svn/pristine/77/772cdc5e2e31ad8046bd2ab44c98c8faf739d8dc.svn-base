package com.istar.mediabroken.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.wltea.expression.ExpressionEvaluator
import org.wltea.expression.datameta.Variable

/**
 * Created by luda on 2018/6/21.
 */
class ExprUtils {

    static Map isStrExprValidated(String sourceExpr) {
        Map returnResult = [isStrExprValidated: false, msg: ""]
        List keys = sourceExpr.replace(" and " as CharSequence, " " as CharSequence)
                .replace(" or " as CharSequence, " " as CharSequence)
                .replace("(" as CharSequence, " " as CharSequence)
                .replace(")" as CharSequence, " " as CharSequence)
                .split(" ")
        keys.removeAll("")
        //表达式是否符号逻辑
        List<Variable> variables = new ArrayList<Variable>();
        keys.each {
            if (StringUtils.isStartWithNumber(it)){
                sourceExpr = sourceExpr.replace(it as CharSequence, "replaceNum" as CharSequence)
                variables.add(Variable.createVariable(String.valueOf("replaceNum"), true));
            }else {
                variables.add(Variable.createVariable(String.valueOf(it), true));
            }
        }
        String reformatStr = sourceExpr.replace(" and " as CharSequence, " && " as CharSequence).replace(" or " as CharSequence, " || " as CharSequence)

        try {
            Object result = ExpressionEvaluator.evaluate(reformatStr,
                    variables);
        } catch (Exception e) {
            returnResult.msg = "表达式不符合逻辑"
            return returnResult
        }
        //是否有多层括号
        String target = reformatStr
        keys.each {
            target = target.replace(it as CharSequence, " " as CharSequence)
        }
        String allSeparatorStr = target.replace(" ", "")
        String parentheseStr = allSeparatorStr.replace("&&", "").replace("||", "")
        if (parentheseStr.indexOf("((") >= 0) {
            returnResult.msg = "不支持多层括号的逻辑"
            return returnResult
        }
        //括号内不能同时有and 和 or
        def allSeparatorArray = allSeparatorStr.toCharArray()
        int startIndex = 0
        int endIndex = 0
        List parentheseExprList = []
        for (int i = 0; i < allSeparatorArray.length; i++) {
            if ("(".equals(allSeparatorArray[i].toString())) {
                startIndex = i
            }
            if (")".equals(allSeparatorArray[i].toString())) {
                endIndex = i
                if (endIndex > startIndex) {
                    parentheseExprList.add(allSeparatorStr.substring(startIndex + 1, endIndex))
                }
            }
        }
        for (int i = 0; i < parentheseExprList.size(); i++) {
            if (parentheseExprList.get(i).contains("&&||") || parentheseExprList.get(i).contains("||&&")) {
                returnResult.msg = "括号内逻辑关系只能是支持一种"
                return returnResult
            }
        }
        returnResult.isStrExprValidated = true
        return returnResult
    }

    static Map strExpr2JsonExpr(String sourceStr) {
        Map returnResult = ["jsonExpr": "", "msg": ""]
        sourceStr = sourceStr.replace("（","(")
        sourceStr = sourceStr.replace("）",")")
        sourceStr = sourceStr.replace("\n"," ")
        Map isStrExprValidated = isStrExprValidated(sourceStr)
        if (!isStrExprValidated.isStrExprValidated) {
            returnResult.msg = isStrExprValidated.msg
            return returnResult
        }
        sourceStr = sourceStr.replace("(", " ( ")
        sourceStr = sourceStr.replace(")", " ) ")
        List keyList = sourceStr.split(" ")
        keyList.removeAll("")
        boolean containsAnd = keyList.contains("and")
        boolean containsOr = keyList.contains("or")
        boolean containsParenthese = keyList.contains("(")
        if(!containsParenthese){
            if((containsAnd && !containsOr) || (!containsAnd && containsOr)){
                keyList.add(0,"(")
                keyList.add(keyList.size(),")")
            }
        }
        //先找出（）内的内容
        JSONArray result = []
        for (int i = 0; i < keyList.size();) {
            if (keyList.get(i).equals("(")) {
                //去获取下一个结束符
                int start = i
                Map findResult = findParentheseStr(keyList, i)
                String op = findResult.op
                List keywords = findResult.keywords
                if (-1 == findResult.index) {
                    break
                    return
                } else {
                    i = findResult.index
                }
                result.add(["subOperator": op, keywords: keywords])
            } else {
                if (["and", "or"].contains(keyList.get(i))) {
                    result.add(["operator": keyList.get(i)])
                } else {
                    result.add(["subOperator": "and", keywords: [keyList.get(i)]])
                }
            }
            i++
        }
        returnResult.jsonExpr = (result as JSON).toString()
        return returnResult
    }

    static Map findParentheseStr(List sourceList, Integer index) {
        Map result = ["index": -1, "op": "and", "keywords": []]
        index++
        if (index >= sourceList.size()) {
            result - 1
        }
        for (; index < sourceList.size(); index++) {
            if (")".equals(sourceList.get(index))) {
                result.index = index
                return result
            } else if (["and", "or"].contains(sourceList.get(index))) {
                result.op = sourceList.get(index)
            } else {
                result.keywords.add(sourceList.get(index))
            }
        }
    }

    static Map jsonExpr2StrExpr(String jsonExpr) {
        Map returnResult = ["strExpr": "", "msg": ""]
        if (!jsonExpr) {
            return returnResult
        }
        JSONArray fromExpr = null
        String strExpr = ""
        try {
            fromExpr = JSONArray.parse(jsonExpr)
            fromExpr.each { JSONObject item ->
                if (item.keywords) {
                    def subOperator = item.subOperator
                    String currExp = ""
                    if (item.keywords.size() > 1) {
                        if(fromExpr.size() > 1){
                            currExp = "( " + item.keywords.join(" " + subOperator + " ") + " ) "
                        }else {
                            currExp = item.keywords.join(" " + subOperator + " ")
                        }

                    } else {
                        currExp = item.keywords.get(0)
                    }
                    strExpr += currExp
                } else {
                    strExpr += " " + item.operator + " "
                }
            }
        } catch (Exception e) {
            returnResult.msg = "JSON数据格式错误"
            return returnResult
        }
        returnResult.strExpr = strExpr.replace("  ", " ")
        return returnResult
    }

    public static void main(String[] args) {
        String sourceStr = "(a or d or c)";
        println(sourceStr)
        def jsonExpr = strExpr2JsonExpr(sourceStr)
        println(jsonExpr)
        jsonExpr.jsonExpr= "[{\"subOperator\":\"and\",\"keywords\":[\"共享单车\",\"绿色出行\",\"共享经济\"]},{\"operator\":\"and\"},{\"subOperator\":\"or\",\"keywords\":[\"北京\",\"上海\"]}]"
        Map strExpr = jsonExpr2StrExpr(jsonExpr.jsonExpr)
        println(strExpr.strExpr)
    }
}
