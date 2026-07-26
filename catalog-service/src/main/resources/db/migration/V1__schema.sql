--
--    Copyright 2010-2026 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- V1__schema.sql
-- Catalog service schema: category, product, item, inventory, supplier

CREATE TABLE IF NOT EXISTS supplier (
    suppid     INT          NOT NULL,
    name       VARCHAR(80),
    status     VARCHAR(2)   NOT NULL,
    addr1      VARCHAR(80),
    addr2      VARCHAR(80),
    city       VARCHAR(80),
    state      VARCHAR(80),
    zip        VARCHAR(5),
    phone      VARCHAR(80),
    CONSTRAINT pk_supplier PRIMARY KEY (suppid)
);

CREATE TABLE IF NOT EXISTS category (
    catid  VARCHAR(10)  NOT NULL,
    name   VARCHAR(80),
    descn  VARCHAR(255),
    CONSTRAINT pk_category PRIMARY KEY (catid)
);

CREATE TABLE IF NOT EXISTS product (
    productid  VARCHAR(10)  NOT NULL,
    category   VARCHAR(10)  NOT NULL,
    name       VARCHAR(80),
    descn      VARCHAR(255),
    CONSTRAINT pk_product PRIMARY KEY (productid),
    CONSTRAINT fk_product_category FOREIGN KEY (category) REFERENCES category (catid)
);

CREATE INDEX IF NOT EXISTS idx_product_category ON product (category);
CREATE INDEX IF NOT EXISTS idx_product_name     ON product (name);

CREATE TABLE IF NOT EXISTS item (
    itemid     VARCHAR(10)   NOT NULL,
    productid  VARCHAR(10)   NOT NULL,
    listprice  DECIMAL(10,2),
    unitcost   DECIMAL(10,2),
    supplier   INT,
    status     VARCHAR(2),
    attr1      VARCHAR(80),
    attr2      VARCHAR(80),
    attr3      VARCHAR(80),
    attr4      VARCHAR(80),
    attr5      VARCHAR(80),
    CONSTRAINT pk_item PRIMARY KEY (itemid),
    CONSTRAINT fk_item_product  FOREIGN KEY (productid) REFERENCES product (productid),
    CONSTRAINT fk_item_supplier FOREIGN KEY (supplier)  REFERENCES supplier (suppid)
);

CREATE INDEX IF NOT EXISTS idx_item_product ON item (productid);

CREATE TABLE IF NOT EXISTS inventory (
    itemid  VARCHAR(10)  NOT NULL,
    qty     INT          NOT NULL,
    CONSTRAINT pk_inventory PRIMARY KEY (itemid)
);
