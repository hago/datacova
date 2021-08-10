<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <style type="text/css">
        .succeed { color: green; }
        .fail { color: red; }
        .emphasize { font-weight:bold; }
        .ident { color: green; }
        </style>
    </head>
    <body>
        <#function calctime d>
            <#local s = "">
            <#if (d.toHours() > 0)>
                <#local s += d.toHours()?c + "hour">
                <#if (d.toHours() > 1)>
                    <#local s += "s">
                </#if>
                <#local s += " ">
            </#if>
            <#if !s?has_content>
                <#if (d.toMinutesPart() > 0)>
                    <#local s += d.toMinutesPart()?c + "minute">
                    <#if (d.toMinutesPart() > 1)>
                        <#local s += "s">
                    </#if>
                    <#local s += " ">
                </#if>
            <#else>
                <#local s += d.toMinutesPart()?c + "minute">
                <#if (d.toMinutesPart() > 1)>
                    <#local s += "s">
                </#if>
                <#local s += " ">
            </#if>
            <#if !s?has_content>
                <#if (d.toSecondsPart() > 0)>
                    <#local s += d.toSecondsPart()?c + "second">
                    <#if (d.toMinutesPart() > 1)>
                        <#local s += "s">
                    </#if>
                    <#local s += " ">
                </#if>
            <#else>
                <#local s += d.toSecondsPart()?c + "second">
                <#if (d.toMinutesPart() > 1)>
                    <#local s += "s">
                </#if>
                <#local s += " ">
            </#if>
            <#if !s?has_content>
                <#if (d.toMillisPart() > 0)>
                    <#local s += d.toMillisPart()?c + "millisecond">
                    <#if (d.toMinutesPart() > 1)>
                        <#local s += "s">
                    </#if>
                    <#local s += " ">
                </#if>
            <#else>
                <#local s += d.toMillisPart()?c + "millisecond">
                <#if (d.toMinutesPart() > 1)>
                    <#local s += "s">
                </#if>
                <#local s += " ">
           </#if>
            <#return s>
        </#function>
        <p>Dear ${user.name}</p>
        <p></p>
        <#if result.isSucceeded()>
        <p>Processing of your file ${execution.fileInfo.originalName} is complete successfully in ${calctime(duration)}.</p>
        <p>Congratulations!</p>
        <#else>
        <p>Processing of your file ${execution.fileInfo.originalName} is complete in ${calctime(duration)}, but some issues happened. Please see details below.</p>
            <#if result.dataLoadingError??>
                <div>
                    <span>Error occurs while data loading: </span>
                    <span class="fail">${result.dataLoadingError.message}</span>
                </div>
            </#if>
            <#list execution.task.actions as index, action>
                <li>
                    <div>
                        Error occurs while executing Action <span class="ident">${index?number+1}</span> - 
                        "<span class="ident">${detail.action.name}</span>" of
                        the task "<span class="ident">${execution.task.name}</span>"
                    </div>
                    <div class="fail">
                        <p>action type is ${detail.action.type}</p>
                        <p>Error message：${((detail.error.detailMessage)!detail.error.message)!detail.error}</p>
                    </div>
                </li>
            </#list>
        </#if>
        <p></p>
        <p>Your sincerely</p>
        <p>DataCoVa team</p>
    </body>
</html>