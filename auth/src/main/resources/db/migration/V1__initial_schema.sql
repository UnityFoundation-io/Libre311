CREATE TABLE tenant
(
    id          bigint AUTO_INCREMENT PRIMARY KEY,
    name        varchar(255) NOT NULL,
    description text,
    status      varchar(255) NOT NULL,
    UNIQUE (name)
);

CREATE TABLE service
(
    id          bigint AUTO_INCREMENT PRIMARY KEY,
    name        varchar(255) NOT NULL,
    description text,
    status      varchar(255),
    UNIQUE (name)
);

CREATE TABLE tenant_service
(
    tenant_id  bigint NOT NULL,
    service_id bigint NOT NULL,
    status     varchar(255),
    PRIMARY KEY (tenant_id, service_id)
);
ALTER TABLE tenant_service
    ADD CONSTRAINT tenant_service_tenant_FK FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE tenant_service
    ADD CONSTRAINT tenant_service_service_FK FOREIGN KEY (service_id) REFERENCES service (id);

CREATE TABLE permission
(
    id          bigint AUTO_INCREMENT PRIMARY KEY,
    name        varchar(255) NOT NULL,
    description text,
    scope       varchar(255),
    UNIQUE (name)
);

CREATE TABLE role
(
    id          bigint AUTO_INCREMENT PRIMARY KEY,
    name        varchar(255) NOT NULL,
    description text,
    UNIQUE (name)
);

CREATE TABLE role_permission
(
    role_id       bigint NOT NULL,
    permission_id bigint NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);
ALTER TABLE role_permission
    ADD CONSTRAINT role_permission_permission_FK FOREIGN KEY (permission_id) REFERENCES permission (id);
ALTER TABLE role_permission
    ADD CONSTRAINT role_permission_role_FK FOREIGN KEY (role_id) REFERENCES role (id);


CREATE TABLE user
(
    id       bigint AUTO_INCREMENT PRIMARY KEY,
    email    varchar(255) NOT NULL,
    password varchar(255),
    status   varchar(255),
    UNIQUE KEY unique_email (email)
);

CREATE TABLE user_role
(
    tenant_id bigint NOT NULL,
    user_id   bigint NOT NULL,
    role_id   bigint NOT NULL,
    PRIMARY KEY (tenant_id, user_id, role_id)
);
ALTER TABLE user_role
    ADD CONSTRAINT user_role_tenant_FK FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE user_role
    ADD CONSTRAINT user_role_user_FK FOREIGN KEY (user_id) REFERENCES user (id);
ALTER TABLE user_role
    ADD CONSTRAINT user_role_role_FK FOREIGN KEY (role_id) REFERENCES role (id);
