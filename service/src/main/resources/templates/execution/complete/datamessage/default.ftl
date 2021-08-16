<p>data message included</p>
<#if result.hasDataMessages()>
    <p>Following information needs your attention.</p>
    <ul>
    <#list result.actionDetailMap as index, detail>
        <#if detail.hasDataMessage()>
        <li>
            <div>
                In action <span class="ident">${index?number+1}</span> "<span class="ident">${detail.action.name}</span>"
                of task "<span class="ident">${execution.task.name}</span>":
            </div>
            <div class="datamessage">
                <ul>
                <#list detail.getDataMessages() as line, messages>
                    <#list messages as message>
                        <p>
                        line ${line} - Value of field "${message.field}" is 
                        ${(message.value)!"null"}, reason: ${message.descriptionExpected}
                        </p>
                    </#list>
                </#list>
                </ul>
            </div>
        </li>
        </#if>
    </#list>
    </ul>
</#if>
