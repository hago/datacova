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
        <p>亲爱的用户 ${user.name}</p>
        <p></p>
        <p>你已经成功注册了<a href="${homepage}">DataCoVa</a>的账号,
        请点击 <a href="${activateurl}">这里</a> 完成邮箱验证激活账号. 此链接在${expire}之前有效。</p>
        <p></p>
        <p>祝 安好顺利</p>
        <p>DataCoVa 团队</p>
    </body>
</html>