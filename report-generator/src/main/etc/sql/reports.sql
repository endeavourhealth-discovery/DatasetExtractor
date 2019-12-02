-- MySQL dump 10.13  Distrib 5.7.27, for Linux (x86_64)
--
-- Host: localhost    Database: data_extracts
-- ------------------------------------------------------
-- Server version	5.7.27-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
use data_extracts;
--
-- Table structure for table `CSVExport`
--

DROP TABLE IF EXISTS `CSVExport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CSVExport` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `db` varchar(255) DEFAULT NULL,
  `maxNumOfRowsInEachOutputFile` int(11) DEFAULT NULL,
  `outputDirectory` varchar(255) NOT NULL,
  `switchedOn` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CSVExport_Table`
--

DROP TABLE IF EXISTS `CSVExport_Table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CSVExport_Table` (
  `CSVExport_id` bigint(20) NOT NULL,
  `tables_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_l9bdt1lif1xvrjn5oynhhd8xq` (`tables_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CSVExport_db_table`
--

DROP TABLE IF EXISTS `CSVExport_db_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CSVExport_db_table` (
  `CSVExport_id` bigint(20) NOT NULL,
  `tables_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_cvalofmoujqg1qnigu108wlps` (`tables_id`),
  KEY `FKbppx3p6icv7vgdhyppkgcckv1` (`CSVExport_id`),
  CONSTRAINT `FKbppx3p6icv7vgdhyppkgcckv1` FOREIGN KEY (`CSVExport_id`) REFERENCES `CSVExport` (`id`),
  CONSTRAINT `FKhuolt5tum2xx8h96g3nuijxab` FOREIGN KEY (`tables_id`) REFERENCES `db_table` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Delta`
--

DROP TABLE IF EXISTS `Delta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Delta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `switchedOn` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeltaTable`
--

DROP TABLE IF EXISTS `DeltaTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeltaTable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `columnsToHash` varchar(255) NOT NULL,
  `deleteUniqueIdentifier` bit(1) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `uniqueIdentifier` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Delta_DeltaTable`
--

DROP TABLE IF EXISTS `Delta_DeltaTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Delta_DeltaTable` (
  `Delta_id` bigint(20) NOT NULL,
  `tables_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_kapf83i2ebssejifxb1mk6tqk` (`tables_id`),
  KEY `FK4u0wtwke71o21q1fgs47pnqma` (`Delta_id`),
  CONSTRAINT `FK36a8liptthovgc571nf26bbpd` FOREIGN KEY (`tables_id`) REFERENCES `DeltaTable` (`id`),
  CONSTRAINT `FK4u0wtwke71o21q1fgs47pnqma` FOREIGN KEY (`Delta_id`) REFERENCES `Delta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Extension`
--

DROP TABLE IF EXISTS `Extension`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Extension` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `switchedOn` bit(1) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `report_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmelwqsbqa36696chag78ft4e1` (`report_id`),
  CONSTRAINT `FKmelwqsbqa36696chag78ft4e1` FOREIGN KEY (`report_id`) REFERENCES `Report` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Report`
--

DROP TABLE IF EXISTS `Report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `endTime` datetime(6) DEFAULT NULL,
  `errorMessage` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `startTime` datetime(6) DEFAULT NULL,
  `csvExport_id` bigint(20) DEFAULT NULL,
  `delta_id` bigint(20) DEFAULT NULL,
  `schedule_id` bigint(20) DEFAULT NULL,
  `storedProcedureExecutor_id` bigint(20) DEFAULT NULL,
  `sftpUpload_id` bigint(20) DEFAULT NULL,
  `zipper_id` bigint(20) DEFAULT NULL,
  `dsmProjectId` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK99y4jwbn4nrju2gdwp3x0awi` (`delta_id`),
  KEY `FKbrpd196inl0nrqhmvr3akb704` (`schedule_id`),
  KEY `FKdgc9lb9jfcka66a4qunvumckf` (`storedProcedureExecutor_id`),
  KEY `FK3uup6b7ll03yjk15c31j2um6` (`csvExport_id`),
  KEY `FKpm6tqr35ty5ry38ky05opwsu8` (`sftpUpload_id`),
  KEY `FKpwuidw95f0o7af89a1pdbob9o` (`zipper_id`),
  CONSTRAINT `FK3uup6b7ll03yjk15c31j2um6` FOREIGN KEY (`csvExport_id`) REFERENCES `CSVExport` (`id`),
  CONSTRAINT `FK99y4jwbn4nrju2gdwp3x0awi` FOREIGN KEY (`delta_id`) REFERENCES `Delta` (`id`),
  CONSTRAINT `FKbrpd196inl0nrqhmvr3akb704` FOREIGN KEY (`schedule_id`) REFERENCES `Schedule` (`id`),
  CONSTRAINT `FKdgc9lb9jfcka66a4qunvumckf` FOREIGN KEY (`storedProcedureExecutor_id`) REFERENCES `StoredProcedureExecutor` (`id`),
  CONSTRAINT `FKpm6tqr35ty5ry38ky05opwsu8` FOREIGN KEY (`sftpUpload_id`) REFERENCES `SftpUpload` (`id`),
  CONSTRAINT `FKpwuidw95f0o7af89a1pdbob9o` FOREIGN KEY (`zipper_id`) REFERENCES `Zipper` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Schedule`
--

DROP TABLE IF EXISTS `Schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dayOfMonth` int(11) DEFAULT NULL,
  `dayOfWeek` int(11) DEFAULT NULL,
  `isDaily` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Schedule_skipDays`
--

DROP TABLE IF EXISTS `Schedule_skipDays`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Schedule_skipDays` (
  `Schedule_id` bigint(20) NOT NULL,
  `skipDays` int(11) DEFAULT NULL,
  KEY `FK3rpos1hboj7bqt8w4fpugwckh` (`Schedule_id`),
  CONSTRAINT `FK3rpos1hboj7bqt8w4fpugwckh` FOREIGN KEY (`Schedule_id`) REFERENCES `Schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SftpUpload`
--

DROP TABLE IF EXISTS `SftpUpload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SftpUpload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hostDirectory` varchar(255) NOT NULL,
  `hostfilename` varchar(255) DEFAULT NULL,
  `hostname` varchar(255) NOT NULL,
  `port` int(11) DEFAULT NULL,
  `privateKeyFile` varchar(255) NOT NULL,
  `switchedOn` bit(1) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `zipFilename` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `StoredProcedureExecutor`
--

DROP TABLE IF EXISTS `StoredProcedureExecutor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `StoredProcedureExecutor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `db` varchar(255) DEFAULT NULL,
  `switchedOn` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `StoredProcedureExecutor_postStoredProcedures`
--

DROP TABLE IF EXISTS `StoredProcedureExecutor_postStoredProcedures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `StoredProcedureExecutor_postStoredProcedures` (
  `StoredProcedureExecutor_id` bigint(20) NOT NULL,
  `postStoredProcedures` varchar(255) DEFAULT NULL,
  KEY `FKgxi6glvbdu7d4c0hyuo03k74l` (`StoredProcedureExecutor_id`),
  CONSTRAINT `FKgxi6glvbdu7d4c0hyuo03k74l` FOREIGN KEY (`StoredProcedureExecutor_id`) REFERENCES `StoredProcedureExecutor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `StoredProcedureExecutor_preStoredProcedures`
--

DROP TABLE IF EXISTS `StoredProcedureExecutor_preStoredProcedures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `StoredProcedureExecutor_preStoredProcedures` (
  `StoredProcedureExecutor_id` bigint(20) NOT NULL,
  `preStoredProcedures` varchar(255) DEFAULT NULL,
  KEY `FKxuqoe6uc5m1gh64eaqnoxedk` (`StoredProcedureExecutor_id`),
  CONSTRAINT `FKxuqoe6uc5m1gh64eaqnoxedk` FOREIGN KEY (`StoredProcedureExecutor_id`) REFERENCES `StoredProcedureExecutor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Zipper`
--

DROP TABLE IF EXISTS `Zipper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Zipper` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sourceDirectory` varchar(255) DEFAULT NULL,
  `splitFiles` bit(1) DEFAULT NULL,
  `encryptionMethod` varchar(20) DEFAULT NULL,
  `switchedOn` bit(1) DEFAULT NULL,
  `zipFilename` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-11-18  9:43:19
