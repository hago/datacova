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
        <p>Dear ${user.name}</p>
        <p></p>
        <p>You have registered at <a href="${homepage}">DataCoVa</a> successfully,
        please click <a href="${activateurl}">here</a> to activate your account, this link is valid before ${expire}.</p>
        <p></p>
        <p>Your sincerely</p>
        <p>DataCoVa team</p>
    </body>
</html>