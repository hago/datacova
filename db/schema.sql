/*

create user cova with nosuperuser noinherit encrypted password 'cova';

create database covadb owner cova encoding 'utf-8';

create database testimpdb owner cova encoding 'utf-8';

*/
create table if not exists workspace (
    id serial,
    name text not null,
    description text not null,
    ownerid bigint,
    addby bigint not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby bigint null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    wkstatus int not null default 0, /* 0 - normal 1 - deleted */
    unique(name, ownerid),
    primary key(id)
);

create table if not exists workspaceuser (
    id serial,
    wkid int not null references workspace(id),
    usergroup int not null,        /* 0 - admin  1 - maintainer  2 - loader */
    userid bigint not null,
    primary key(id),
    unique(wkid, userid, usergroup)
);

create table if not exists workspacelog (
    id serial,
    wkid int not null references workspace(id),
    userid bigint not null,
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
    usertype int not null default 0, /* 0 - local db 1 - ldap */
    primary key(id)
);

/* create test user with password 123456 */
insert into users (userid, name, pwdhash, addby, modifyby, modifytime)
values ('Admin', 'System Administrator', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', 0, 0, now()) returning id;
insert into users (userid, name, pwdhash, addby, modifyby, modifytime)
select
'test', 'CoVa Test', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', id, id, now()
from users where userid = 'Admin';

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
create table if not exists task (
    id serial,
    name text not null,
    description text not null default '',
    wkid int not null references workspace(id),
    actions json not null default '[]',
    extra json not null default '{}',
    addtime timestamp with time zone default CURRENT_TIMESTAMP not null,
    addby bigint not null,
    modifytime timestamp with time zone null,
    modifyby bigint null,
    unique(name, wkid),
    primary key(id)
);

create table if not exists taskexecution (
	id serial,
	fileinfo json not null,
	taskid int not null,
	task json not null,
	addby bigint,
	addtime timestamp with time zone default CURRENT_TIMESTAMP not null,
	starttime timestamp with time zone null,
	endtime timestamp with time zone null,
	detail json null,
	xstatus int not null default 0,		/* 0 - created 1 - executing 2 - success -1 - fail */
	primary key(id)
);

create table if not exists connection (
    id serial,
    name text not null,
    description text not null,
    configuration json not null default '{}',
    extra json not null default '{}',
    wkid int not null references workspace(id),
    addby bigint not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby bigint null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    primary key(id),
    unique(wkid, name)
);
