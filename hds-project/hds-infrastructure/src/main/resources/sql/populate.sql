/* Populating users table*/
insert into users values ('user1');
insert into users values ('user2');
insert into users values ('user3');
insert into users values ('user4');

/* Populating goods table */
insert into goods values ('good1', false);
insert into goods values ('good2', false);
insert into goods values ('good3', true);
insert into goods values ('good4', true);

/* Populating ownership table */
insert into ownership values ('good1', 'user1');
insert into ownership values ('good2', 'user2');
insert into ownership values ('good3', 'user3');
insert into ownership values ('good4', 'user4');
