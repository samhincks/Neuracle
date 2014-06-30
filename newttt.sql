/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50155
Source Host           : localhost:3306
Source Database       : newttt

Target Server Type    : MYSQL
Target Server Version : 50155
File Encoding         : 65001

Date: 2014-03-09 12:48:19
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for datalayer
-- ----------------------------
DROP TABLE IF EXISTS `datalayer`;
CREATE TABLE `datalayer` (
  `id` varchar(255) COLLATE latin1_german1_ci NOT NULL,
  `user_id` varchar(255) COLLATE latin1_german1_ci DEFAULT NULL,
  `parent_id` varchar(255) COLLATE latin1_german1_ci DEFAULT NULL,
  `data` longtext COLLATE latin1_german1_ci,
  `file_name` varchar(255) COLLATE latin1_german1_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_german1_ci;

-- ----------------------------
-- Records of datalayer
-- ----------------------------

-- ----------------------------
-- Table structure for label
-- ----------------------------
DROP TABLE IF EXISTS `label`;
CREATE TABLE `label` (
  `id` varchar(255) COLLATE latin1_german1_ci NOT NULL,
  `datalayer_id` varchar(255) COLLATE latin1_german1_ci DEFAULT NULL,
  `labelName` varchar(255) COLLATE latin1_german1_ci DEFAULT NULL,
  `channelLabels` longtext COLLATE latin1_german1_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_german1_ci;

-- ----------------------------
-- Records of label
-- ----------------------------

-- ----------------------------
-- Table structure for userinfo
-- ----------------------------
DROP TABLE IF EXISTS `userinfo`;
CREATE TABLE `userinfo` (
  `UserId` int(11) NOT NULL AUTO_INCREMENT,
  `UserName` varchar(20) NOT NULL,
  `UserPwd` varchar(20) NOT NULL,
  `Email` varchar(50) NOT NULL,
  PRIMARY KEY (`UserId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

CREATE TABLE `realtime1` (
  `IndexID` INT NOT NULL,
  `Chanel1` VARCHAR(45) NULL,
  `Chanel2` VARCHAR(45) NULL
);


-- ----------------------------
-- Records of userinfo
-- ----------------------------
INSERT INTO `userinfo` VALUES ('1', '11', '22', '33');
INSERT INTO `userinfo` VALUES ('2', '11111', '22222', '22@qq.com');
INSERT INTO `userinfo` VALUES ('4', '111111', '22222', '22@qq.com');
INSERT INTO `userinfo` VALUES ('5', 'jsjsjsjjs', '33333', 'smith@qq.com');
INSERT INTO `userinfo` VALUES ('6', 'username', 'password', '232@qq.com');
INSERT INTO `userinfo` VALUES ('7', 'erwerwerewre', 'erwregegfdgfsdf', 'fsdfsdfsdfsfsd@qq.com');
