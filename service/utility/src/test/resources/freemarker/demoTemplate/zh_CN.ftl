这是一个FreeMarker模板管理器的文本模板例子.

今天是 ${date}.
一周里面每天的名字是
<#list weekDays as day>
  ${day},
</#list>