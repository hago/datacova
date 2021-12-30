<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <style type="text/css">
        .succeed { color: green; }
        .fail { color: red; }
        .emphasize { font-weight:bold; }
        .ident { color: green; }
        .field { color: gray; }
        .value { 
            color: red;
            font-style: italic;
        }
        .expect {
            color: orangered;
        } 
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
            <#if !result.isAllActionsPerformed()>
              <p>部分后序操作因为前序操作的设置没有被执行。已执行的操作有：
              <#list result.actionDetailMap as index, detail>
                <li>操作 ${index}: ${detail.getAction().getName()}</li>
              </#list>
              </p>
            </#if>
            <#include "/execution/complete/datamessage/zh_CN.ftl">
            <p>祝贺你!</p>
        <#else>
            <p>您的文件 ${execution.fileInfo.originalName} 已经处理完成, 耗时${calctime(duration)}, 处理过程中出现了下列的问题.</p>
            <#if result.dataLoadingError??>
                <div>
                    <span>数据文件加载时出现错误: </span>
                    <span class="fail">${result.dataLoadingError.message}</span>
                </div>
            </#if>
            <ul>
            <#list result.actionDetailMap as index, detail>
                <li>
                    <div>
                        任务"<span class="ident">${execution.task.name}</span>"的第 <span class="ident">${index?number+1}</span> 步操作
                        "<span class="ident">${detail.action.name}</span>"出现错误
                    </div>
                    <div class="fail">
                        <p>action type is ${detail.action.type}</p>
                        <p>错误信息：${((detail.error.detailMessage)!detail.error.message)!detail.error}</p>
                    </div>
                </li>
            </#list>
            </ul>
            <#include "/execution/complete/datamessage/zh_CN.ftl">
        </#if>
        <p></p>
        <p>祝一切顺利</p>
        <p>DataCoVa 团队</p>
    </body>
</html>