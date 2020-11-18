/*

create user cova with nosuperuser noinherit encrypted password 'cova';

create database covadb owner cova encoding 'utf-8';

create database testimpdb owner cova encoding 'utf-8';

*/
create table if not exists workspaces (
    id serial,
    name text not null,
    description text not null,
    ownerid varchar(100) not null,
    addby varchar(100) not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby varchar(100) null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    wkstatus int not null default 0, /* 0 - normal 1 - deleted */
    unique(name, ownerid),
    primary key(id)
);

create table if not exists workspaceusers (
    id serial,
    wkid int not null references workspaces(id),
    usergroup int not null,        /* 0 - admin  1 - maintainer  2 - loader */
    userid varchar(100) not null,
    primary key(id),
    unique(wkid, userid, usergroup)
);

create table if not exists workspacelog (
    id serial,
    wkid int not null references workspaces(id),
    userid varchar(100) not null,
    useraction json default '{}',    /* {"action":..., "data": {...} */
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    primary key(id)
);

create table if not exists users (
    id serial,
    userid varchar(100) not null unique,
    name varchar(200) not null,
    pwdhash text not null,
    description text not null default '',
    addby varchar(100) not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby varchar(100) null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    thumbnail bytea null,
    eustatus int not null default 0, /* 0 - normal 1 - deleted 2 - password reset*/
    primary key(id)
);

/* create test user with password 123456 */
insert into users (userid, name, pwdhash, addby, modifyby, modifytime)
values ('cova_test@outlook.com', 'CoVa Test', 'c524b8ab6854ae4097662d3687a2c265b0248712', 'AutoCreated', 'AutoCreated', now());

/*
actions: array of 
* import action 
    {
        "connectionid":...,
        "extra": {
            "createTable":true,
            "clearData":false,
            "addBatchNumber":false
        }
    }
*/
create table if not exists tasks (
    id serial,
    name text not null,
    description text not null default '',
    wkid int not null references workspaces(id),
    actions json not null default '[]',
    extra json not null default '{}',
    addtime timestamp with time zone default CURRENT_TIMESTAMP not null,
    addby varchar(100) not null,
    modifytime timestamp with time zone null,
    modifyby varchar(100) null,
    unique(name, wkid),
    primary key(id)
);

create table if not exists taskexecution (
	id serial,
	fileinfo json not null,
	taskid int not null,
	task json not null,
	addby varchar(100),
	addtime timestamp with time zone default CURRENT_TIMESTAMP not null,
	starttime timestamp with time zone null,
	endtime timestamp with time zone null,
	detail json null,
	xstatus int not null default 0,		/* 0 - created 1 - executing 2 - success -1 - fail */
	primary key(id)
);

create table if not exists connections (
    id serial,
    name text not null,
    description text not null,
    configuration json not null default '{}',
    extra json not null default '{}',
    wkid int not null references workspaces(id),
    addby varchar(100) not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby varchar(100) null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    primary key(id),
    unique(wkid, name)
);
