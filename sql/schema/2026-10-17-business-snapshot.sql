-- Structure only, local MySQL 9.5.0, 2026-10-17. No data, no DROP, no database selection.
-- Snapshot, not an upgrade migration. Preserve current limitations; see sql/README.md.
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '楼宇名称(如: 南苑1号楼)',
  `code` varchar(20) DEFAULT NULL COMMENT '楼宇编码(如: N-01)',
  `gender_limit` tinyint NOT NULL DEFAULT '0' COMMENT '性别限制(0:不限, 1:男, 2:女)',
  `floor_count` int DEFAULT '0' COMMENT '总楼层数',
  `manager_id` bigint DEFAULT NULL COMMENT '负责宿管ID',
  `status` tinyint DEFAULT '1' COMMENT '状态(0:停用, 1:正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0:未删, 1:已删)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='楼宇信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '所属楼宇ID',
  `room_number` varchar(20) NOT NULL COMMENT '房间号(如: 301)',
  `floor_num` int DEFAULT NULL COMMENT '楼层',
  `capacity` int NOT NULL DEFAULT '4' COMMENT '床位总数',
  `occupied_count` int DEFAULT '0' COMMENT '已入住人数',
  `room_type` tinyint DEFAULT '1' COMMENT '房型(1:四人间, 2:六人间, 3:二人间)',
  `status` tinyint DEFAULT '1' COMMENT '状态(0:维修中, 1:正常)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_building_id` (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_no` varchar(30) NOT NULL COMMENT '学号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` tinyint NOT NULL COMMENT '性别(1:男, 2:女)',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `status` tinyint DEFAULT '1' COMMENT '状态(0:离校, 1:在校)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_accommodation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `bed_number` int DEFAULT NULL COMMENT '床号(1-6)',
  `check_in_date` date DEFAULT NULL COMMENT '入住日期',
  `check_out_date` date DEFAULT NULL COMMENT '退宿日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(0:已退宿, 1:在住)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住宿记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
