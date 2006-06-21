connect 'sampleDB;create=true';

create table import1(  ProductID int,  Name varchar(40));

insert into import1 (ProductID, Name) values (1, 'toothpaste');
insert into import1 (ProductID, Name) values (2, 'milk');
insert into import1 (ProductID, Name) values (3, 'bread');
insert into import1 (ProductID, Name) values (4, 'cereal');
