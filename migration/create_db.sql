
CREATE TABLE IF NOT EXISTS `players` (
  `uuid` CHAR(36) NOT NULL,
  `name` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `elements` (
  `player_uuid` CHAR(36) NOT NULL,
  `element` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`player_uuid`, `element`),
  CONSTRAINT `fk_element_player`
    FOREIGN KEY (`player_uuid`)
    REFERENCES `players` (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `affinities` (
  `player_uuid` CHAR(36) NOT NULL,
  `affinity` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`player_uuid`, `affinity`),
  CONSTRAINT `fk_affinity_player`
    FOREIGN KEY (`player_uuid`)
    REFERENCES `players` (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `credentials` (
  `player_uuid` CHAR(36) NOT NULL,
  `username` VARCHAR(64) NOT NULL,
  `token` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`player_uuid`),
  CONSTRAINT `fk_credentials_player`
    FOREIGN KEY (`player_uuid`)
    REFERENCES `players` (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `decks` (
  `uuid` CHAR(36) NOT NULL,
  `owner_uuid` CHAR(36) NOT NULL,
  `name` VARCHAR(64) NOT NULL DEFAULT 'default',
  `current` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 True\n0 False',
  PRIMARY KEY (`uuid`),
  UNIQUE INDEX `uq_deck_per_player` (`owner_uuid` ASC, `name` ASC),
  INDEX `ix_player_decks` (`owner_uuid` ASC),
  CONSTRAINT `fk_deck_owner`
    FOREIGN KEY (`owner_uuid`)
    REFERENCES `players` (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `deck_entries` (
  `deck_uuid` CHAR(36) NOT NULL,
  `slot` SMALLINT NOT NULL,
  `ability` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`deck_uuid`, `slot`),
  UNIQUE INDEX `uq_deck_slot` (`deck_uuid` ASC, `slot` ASC),
  CONSTRAINT `fk_entry_deck`
    FOREIGN KEY (`deck_uuid`)
    REFERENCES `decks` (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `abilities` (
  `player_uuid` CHAR(36) NOT NULL,
  `ability` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`player_uuid`, `ability`),
  CONSTRAINT `fk_ability_player`
    FOREIGN KEY (`player_uuid`)
    REFERENCES `players` (`uuid`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `perks` (
  `player_uuid` CHAR(36) NOT NULL,
  `perk` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`player_uuid`, `perk`),
  CONSTRAINT `fk_perk_player`
    FOREIGN KEY (`player_uuid`)
    REFERENCES `players` (`uuid`))
ENGINE = InnoDB;
