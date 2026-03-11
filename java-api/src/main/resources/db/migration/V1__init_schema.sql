-- ============================================================
-- Engie Bericht API — Database schema
-- Flyway migratie V1
-- ============================================================

-- ── Verzenders ───────────────────────────────────────────────
CREATE TABLE verzenders (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    ean_code      VARCHAR(18)     NOT NULL,
    naam          VARCHAR(255),
    aangemaakt_op TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_verzenders  PRIMARY KEY (id),
    CONSTRAINT uq_verzender_ean UNIQUE (ean_code)
);

-- ── Ontvangers ────────────────────────────────────────────────
CREATE TABLE ontvangers (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    ean_code      VARCHAR(18)     NOT NULL,
    naam          VARCHAR(255),
    aangemaakt_op TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ontvangers   PRIMARY KEY (id),
    CONSTRAINT uq_ontvanger_ean UNIQUE (ean_code)
);

-- ── Berichten ─────────────────────────────────────────────────
CREATE TABLE berichten (
    id                   VARCHAR(36)   NOT NULL,           -- UUID
    message_id           VARCHAR(255),                     -- mRID uit XML (uniek)
    correlation_id       VARCHAR(255),
    sender_ean           VARCHAR(18),
    receiver_ean         VARCHAR(18),
    content_type         VARCHAR(100),
    xml_payload          LONGTEXT      NOT NULL,
    status               VARCHAR(20)   NOT NULL DEFAULT 'ONTVANGEN',
    ontvangst_tijd       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aangemaakt_op        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_berichten        PRIMARY KEY (id),
    CONSTRAINT uq_bericht_msg_id   UNIQUE (message_id),
    CONSTRAINT fk_bericht_verzender FOREIGN KEY (sender_ean)
        REFERENCES verzenders (ean_code)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_bericht_ontvanger FOREIGN KEY (receiver_ean)
        REFERENCES ontvangers (ean_code)
        ON UPDATE CASCADE ON DELETE SET NULL
);

-- ── Validatiefouten ───────────────────────────────────────────
CREATE TABLE validatie_fouten (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    bericht_id  VARCHAR(36)   NOT NULL,
    fout_code   VARCHAR(20)   NOT NULL,
    omschrijving TEXT,
    fout_type   VARCHAR(20)   NOT NULL,                    -- TECHNISCH | SEMANTISCH
    tijdstip    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_validatie_fouten  PRIMARY KEY (id),
    CONSTRAINT fk_vf_bericht        FOREIGN KEY (bericht_id)
        REFERENCES berichten (id)
        ON DELETE CASCADE
);

-- ── Bericht logs ──────────────────────────────────────────────
CREATE TABLE bericht_logs (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    bericht_id  VARCHAR(36),                               -- nullable: log vóór opslaan
    actie       VARCHAR(50)   NOT NULL,
    omschrijving TEXT,
    processor   VARCHAR(255),
    tijdstip    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bericht_logs  PRIMARY KEY (id),
    CONSTRAINT fk_log_bericht   FOREIGN KEY (bericht_id)
        REFERENCES berichten (id)
        ON DELETE SET NULL
);

-- ── Indexen voor snelle queries ───────────────────────────────
CREATE INDEX idx_berichten_status        ON berichten (status);
CREATE INDEX idx_berichten_ontvangst     ON berichten (ontvangst_tijd);
CREATE INDEX idx_berichten_sender        ON berichten (sender_ean);
CREATE INDEX idx_vf_bericht_id           ON validatie_fouten (bericht_id);
CREATE INDEX idx_vf_fout_code            ON validatie_fouten (fout_code);
CREATE INDEX idx_logs_bericht_id         ON bericht_logs (bericht_id);
CREATE INDEX idx_logs_tijdstip           ON bericht_logs (tijdstip);

-- ── Seed-data: bekende EAN-partijen ──────────────────────────
INSERT INTO verzenders  (ean_code, naam) VALUES ('871686700000900000', 'Engie Sender Test');
INSERT INTO ontvangers  (ean_code, naam) VALUES ('871686700001600000', 'Engie Receiver Test');
