CREATE TABLE "Bar"(
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Date" DATE,
    "Misc" VARCHAR(50)
);

CREATE TABLE "SortPage"(
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Number" INT
);

CREATE TABLE "ForOrderByTests" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Number" INT,
    "Date" DATE,
    "Str" VARCHAR(50),
    "Str2" VARCHAR(50)
);

CREATE TABLE "ForGroupByTests" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Number" INT,
    "Date" DATE,
    "Str" VARCHAR(50),
    "Str2" VARCHAR(50)
);

CREATE TABLE "ForNumericTests" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Number" INT
);

CREATE TABLE "Foo"(
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "FirstName" VARCHAR(50),
    "LastName" VARCHAR(50),
    "Bar_id" INT,
    "Address" TEXT,
    "Named_SortPage_id" INTEGER
);

CREATE TABLE "Baz"(
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Text" VARCHAR(255)
);

CREATE TABLE "join_Foo_Baz"(
    "Foo_id" BIGINT,
    "Baz_id" BIGINT
);

CREATE TABLE "Relatives_join_Bar_Baz"(
    "Bar_id" BIGINT,
    "Baz_id" BIGINT
);

CREATE TABLE "SelfJoins_join_Baz_Baz"(
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "Baz_src_id" BIGINT,
    "Baz_dest_id" BIGINT
);