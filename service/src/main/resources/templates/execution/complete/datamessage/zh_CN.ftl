<p>xyz</p>
<#if result.hasDataMessages()>
    <p>数据处理程序为您记录了下列消息，需要您关注：</p>
    <ul>
    <#list result.actionDetailMap as index, detail>
        <#if detail.hasDataMessage()>
        <li>
            <div>
                任务"<span class="ident">${execution.task.name}</span>"的第 <span class="ident">${index?number+1}</span> 步操作
                "<span class="ident">${detail.action.name}</span>"提示：
            </div>
            <div class="datamessage">
                <ul>
                <#list detail.datamessages as line, messages>
                    <#list messages as message>
                    <p>字段“${message.field}”的值是“${messgae.value}”，期望是：“${message.descriptionExpected}”</p>
                    </#list>
                </#list>
                </ul>
            </div>
        </li>
        </#if>
    </#list>
    </ul>
</#if>
