<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <style type="text/css">
        .succeed { color: green; }
        .fail { color: red; }
        .emphasize { font-weight:bold; }
        </style>
    </head>
    <body>
        <#function calctime d>
            <#local s = "">
            <#if (d.toHours() > 0)>
                <#local s += d.toHours()?c + "小时">
            </#if>
            <#if !s?has_content>
                <#if (d.toMinutesPart() > 0)>
                    <#local s += d.toMinutesPart()?c + "分钟">
                </#if>
            <#else>
                <#local s += d.toMinutesPart()?c + "分钟">
            </#if>
            <#if !s?has_content>
                <#if (d.toSecondsPart() > 0)>
                    <#local s += d.toSecondsPart()?c + "秒">
                </#if>
            <#else>
                <#local s += d.toSecondsPart()?c + "秒">
            </#if>
            <#if !s?has_content>
                <#if (d.toMillisPart() > 0)>
                    <#local s += d.toMillisPart()?c + "毫秒">
                </#if>
            <#else>
                <#local s += d.toMillisPart()?c + "毫秒">
            </#if>
            <#return s>
        </#function>
        <p>亲爱的用户 ${user.name}</p>
        <p></p>
        <#if result.isSucceeded()>
        <p>您的文件 ${execution.fileInfo.originalName} 已经成功处理完成, 耗时${calctime(duration)}</p>
        <p>祝贺你!</p>
        <#else>
        <p>您的文件 ${execution.fileInfo.originalName} 已经处理完成, 耗时${calctime(duration)}, 处理过程中出现了下列的问题.</p>
            <#if result.dataLoadingError??>
                <div>
                    <span>数据文件加载时出现错误: </span>
                    <span class="fail">${result.dataLoadingError.message}</span>
                </div>
            </#if>
            <#list execution.task.actions as action>
                <#switch action.type>
                    <#case 1>
                        <#include "/execution/action/ingest/index_zh_CN.ftl">
                        <#break>
                    <#case 2>
                        <#include "/execution/action/validation/index_zh_CN.ftl">
                        <#break>
                    <#case 3>
                        <#include "/execution/action/distribute/index_zh_CN.ftl">
                        <#break>
                    <#case 0>
                        <#include "/execution/action/idle/index_zh_CN.ftl">
                        <#break>
                    <#default>
                        <p>action type is ${action.type}</p>
                </#switch>
            </#list>
        </#if>
        <p></p>
        <p>祝一切顺利</p>
        <p>DataCoVa 团队</p>
    </body>
</html>