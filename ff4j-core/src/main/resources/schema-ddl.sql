-- Main Table to store Features
CREATE TABLE FF4J_FEATURES (
  FEATURE_NAME  		VARCHAR(50),
  FEATURE_STATUS		INTEGER NOT NULL,
  DESCRIPTION 			VARCHAR(255),
  STRATEGY				VARCHAR(255),
  EXPRESSION			VARCHAR(255),
  GROUPNAME				VARCHAR(255),
  REGION_IDENTIFIER   VARCHAR(255),
  PRIMARY KEY(FEATURE_NAME)
);

-- Roles to store ACL, FK to main table
CREATE TABLE FF4J_ROLES (
  FEATURE_NAME     VARCHAR(50) REFERENCES FF4J_FEATURES(FEATURE_NAME),
  ROLE_NAME    VARCHAR(50),
  GROUPNAME  VARCHAR(255),
  REGION_IDENTIFIER   VARCHAR(255),
  PRIMARY KEY(FEATURE_NAME, ROLE_NAME)
);
