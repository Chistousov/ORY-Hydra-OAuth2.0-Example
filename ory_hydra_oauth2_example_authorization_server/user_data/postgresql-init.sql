DO
'
declare
begin

	-- SCHEMA: horns_and_hooves
	CREATE SCHEMA IF NOT EXISTS horns_and_hooves;	

	-- Table Users
	CREATE TABLE IF NOT EXISTS horns_and_hooves.user
	(
		id bigserial NOT NULL,
		login text NOT NULL,
		password text NOT NULL,
		org_name text NOT NULL,
		CONSTRAINT "PK_user" PRIMARY KEY (id),
		CONSTRAINT "UNIQUE_login" UNIQUE (login),
		CONSTRAINT "UNIQUE_org_name" UNIQUE (org_name)
	);

	COMMENT ON TABLE horns_and_hooves.user IS ''Table with users'';

	-- Procedures

CREATE OR REPLACE PROCEDURE horns_and_hooves.add_user(
	IN login horns_and_hooves.user.login%type,
	IN password horns_and_hooves.user.password%type,
	IN org_name horns_and_hooves.user.org_name%type,
	OUT user_id horns_and_hooves.user.id%type
)
AS 
$BODY$
BEGIN
INSERT INTO horns_and_hooves.user (login, password, org_name) VALUES (login, password, org_name) RETURNING id INTO user_id;
END
$BODY$ LANGUAGE plpgsql;

COMMENT ON PROCEDURE horns_and_hooves.add_user(
		login horns_and_hooves.user.login%type,
		password horns_and_hooves.user.password%type,
		org_name horns_and_hooves.user.org_name%type,
		OUT user_id horns_and_hooves.user.id%type
	)
    IS ''Add new user
Params: in  login - new user login
		in  password - new user password
		in  org_name - new user organization
		out user_id - new user ID
Output: none'';



CREATE OR REPLACE PROCEDURE horns_and_hooves.get_login(
	IN login horns_and_hooves.user.login%type,
	INOUT ref_cursor refcursor
)
AS 
$BODY$
BEGIN

	OPEN ref_cursor FOR 
		SELECT u.login, u.password, u.org_name
		FROM horns_and_hooves.user u
		WHERE u.login = login;

END
$BODY$ LANGUAGE plpgsql;


COMMENT ON PROCEDURE horns_and_hooves.get_login(
		IN login horns_and_hooves.user.login%type,
		INOUT ref_cursor refcursor
	)
    IS ''Get login record
Params: in  login - user login
		out ref_cursor - login record
Output: none'';


end;
'  LANGUAGE PLPGSQL;