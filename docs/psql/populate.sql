delete from ownership;
delete from goods;
delete from users;

/* Populating users table*/
insert into users values ('8001');
insert into users values ('8002');
insert into users values ('8003');
insert into users values ('8004');

/* Populating goods table */
insert into goods values ('good1', false);
insert into goods values ('good2', false);
insert into goods values ('good3', true);
insert into goods values ('good4', true);

/* Populating ownership table */
insert into ownership values ('good1', '8001');
insert into ownership values ('good2', '8002');
insert into ownership values ('good3', '8003');
insert into ownership values ('good4', '8004');
