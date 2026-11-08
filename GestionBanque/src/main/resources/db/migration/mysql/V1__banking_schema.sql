CREATE TABLE IF NOT EXISTS clients (
 id bigint auto_increment PRIMARY KEY, nom varchar(255) NOT NULL, prenom varchar(255) NOT NULL,
 email varchar(255) NOT NULL UNIQUE, password_hash varchar(255) NOT NULL, role varchar(255) NOT NULL
);
CREATE TABLE IF NOT EXISTS compte_bancaire (
 id bigint auto_increment PRIMARY KEY, date_creation date NOT NULL, solde decimal(19,2) NOT NULL,
 etat varchar(255) NOT NULL, client_id bigint NOT NULL, version bigint NOT NULL DEFAULT 0,
 CONSTRAINT fk_account_client FOREIGN KEY(client_id) REFERENCES clients(id)
);
CREATE TABLE IF NOT EXISTS compte_courant (id bigint PRIMARY KEY, CONSTRAINT fk_current_account FOREIGN KEY(id) REFERENCES compte_bancaire(id));
CREATE TABLE IF NOT EXISTS compte_epargne (id bigint PRIMARY KEY, CONSTRAINT fk_savings_account FOREIGN KEY(id) REFERENCES compte_bancaire(id));
CREATE TABLE IF NOT EXISTS operation (
 id bigint auto_increment PRIMARY KEY, compte_id bigint NOT NULL, date_operation datetime(6) NOT NULL,
 montant decimal(19,2) NOT NULL, type enum('CREDIT','DEBIT') NOT NULL, reference varchar(255) NOT NULL,
 CONSTRAINT fk_operation_account FOREIGN KEY(compte_id) REFERENCES compte_bancaire(id)
);
