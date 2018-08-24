-- Sequence: seq_user_id

-- DROP SEQUENCE seq_user_id;

CREATE SEQUENCE JOB_CONTROL_SEQ_001
  INCREMENT BY 1
  MINVALUE    1000000001
  MAXVALUE    9999999999
  START WITH 1000000001
  CACHE 2;

  --ALTER TABLE seq_user_id OWNER TO oracle;


