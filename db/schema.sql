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
    userid varchar(100) not null,
    email varchar(100) not null,
    mobile varchar(100) not null,
    name varchar(200) not null,
    pwdhash text not null,
    description text not null default '',
    addby bigint not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby bigint null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    thumbnail varchar(255) null,
    eustatus int not null default 0, /* 0 - normal 1 - deleted 2 - password reset*/
    usertype int not null default 0, /* 0 - local db 1 - ldap */
    unique(usertype, email),
    unique(usertype, mobile),
    unique(usertype, userid),
    primary key(id)
);

/* create test user with password 123456 */
insert into users (userid, name, pwdhash, addby, modifyby, modifytime, email, mobile)
values ('Admin', 'System Administrator', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', 0, 0, now(), 'admin@datacova.com', '12345')
returning id;
insert into users (userid, name, pwdhash, addby, modifyby, modifytime, email, mobile)
select
'test', 'CoVa Test', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', id, id, now(), 'test@datacova.com', '12345'
from users where userid = 'Admin';
insert into users (userid, name, pwdhash, addby, modifyby, modifytime, email, mobile)
select
'uadmin', 'Role Admin Test', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', id, id, now(), 'uadmin@datacova.com', '12345'
from users where userid = 'Admin';
insert into users (userid, name, pwdhash, addby, modifyby, modifytime, email, mobile)
select
'umaintain', 'Role Maintainer Test', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', id, id, now(), 'umaintain@datacova.com', '12345'
from users where userid = 'Admin';
insert into users (userid, name, pwdhash, addby, modifyby, modifytime, email, mobile)
select
'uuser', 'Role User Test', '4e7dde2c5adfd4189fd0962ed9e7e821ef43b1d6', id, id, now(), 'uuser@datacova.com', '12345'
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
    wkid int not null references workspace(id),
    addby bigint not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby bigint null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    primary key(id),
    unique(wkid, name)
);

create table if not exists rules (
    id serial,
    name text not null,
    description text not null,
    ruleconfig json not null,
    wkid int not null references workspace(id),
    addby bigint not null,
    addtime timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    modifyby bigint null,
    modifytime TIMESTAMP WITH TIME ZONE null,
    primary key(id),
    unique (wkid, name)
);

create table if not exists settings (
    id serial,
    name varchar(100) not null,
    content json not null,
    unique(name),
    primary key(id)
);

create table if not exists permissions(
    id serial,
    name varchar(100) unique not null,
    description text not null default '',
    parentid int null references permissions(id),
    primary key(id)
);

create table if not exists userpermissions(
    id serial,
    userid bigint not null references users(id),
    permissionid bigint not null references permissions(id),
    unique(userid, permissionid),
    primary key(id)
);

create table if not exists roles(
    id serial,
    name varchar(500) unique not null,
    description text not null default '',
    primary key(id)
);

create table if not exists rolepermissions(
    id serial,
    roleid bigint not null references roles(id),
    permissionid bigint not null references permissions(id),
    primary key(id)
);

create table if not exists userroles(
    id serial,
    userid bigint not null references users(id),
    roleid bigint not null references roles(id),
    unique(userid, roleid),
    primary key(id)
);

insert into permissions(name, description) values ('adminroot', 'Permission to enter admin');
insert into userpermissions(userid, permissionid) 
select u.id, p.id from users as u full outer join permissions as p on 1=1
where u.userid = 'test' and p.name = 'adminroot';
