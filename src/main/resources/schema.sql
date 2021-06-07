CREATE TABLE listing
(
    id        SERIAL PRIMARY KEY,
    code      VARCHAR(255),
    dealer_id VARCHAR(255),
    make      VARCHAR(255),
    model     VARCHAR(255),
    power     INT,
    year      INT,
    color     VARCHAR(255),
    price     INT
);