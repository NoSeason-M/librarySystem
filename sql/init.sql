-- ============================================================================
-- 图书管理系统 - 数据库初始化脚本
-- 技术栈: MySQL 8.0 + InnoDB + utf8mb4
-- ============================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_system
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_system;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. 系统管理基础表
-- ============================================================================

-- 1.1 系统用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '用户ID',
    username        VARCHAR(50)     NOT NULL                    COMMENT '用户名',
    password        VARCHAR(255)    NOT NULL                    COMMENT '密码(BCrypt加密)',
    real_name       VARCHAR(50)     NOT NULL                    COMMENT '真实姓名',
    email           VARCHAR(100)    DEFAULT NULL                COMMENT '邮箱',
    phone           VARCHAR(20)     DEFAULT NULL                COMMENT '手机号',
    avatar          VARCHAR(255)    DEFAULT NULL                COMMENT '头像URL',
    gender          TINYINT         DEFAULT 0                   COMMENT '性别(0未知/1男/2女)',
    status          TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    failed_login    INT             DEFAULT 0                   COMMENT '连续登录失败次数',
    last_login_ip   VARCHAR(50)     DEFAULT NULL                COMMENT '最后登录IP',
    last_login_time DATETIME        DEFAULT NULL                COMMENT '最后登录时间',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0                   COMMENT '逻辑删除(0未删/1已删)',
    UNIQUE KEY uk_username (username),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 1.2 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '角色ID',
    name        VARCHAR(50)     NOT NULL                    COMMENT '角色名称',
    code        VARCHAR(50)     NOT NULL                    COMMENT '角色编码(ROLE_ADMIN/ROLE_LIBRARIAN/ROLE_CATALOGER/ROLE_READER)',
    description VARCHAR(255)    DEFAULT NULL                COMMENT '角色描述',
    sort        INT             DEFAULT 0                   COMMENT '排序号',
    status      TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT  AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT  NOT NULL COMMENT '用户ID',
    role_id BIGINT  NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.4 菜单权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '菜单ID',
    name        VARCHAR(100)    NOT NULL                    COMMENT '菜单名称',
    permission  VARCHAR(100)    DEFAULT NULL                COMMENT '权限标识(如 book:create)',
    path        VARCHAR(200)    DEFAULT NULL                COMMENT '前端路由路径(目录/菜单类型)',
    component   VARCHAR(255)    DEFAULT NULL                COMMENT '组件路径(菜单类型)',
    query       VARCHAR(255)    DEFAULT NULL                COMMENT '路由参数',
    parent_id   BIGINT          DEFAULT 0                   COMMENT '父菜单ID(0为顶级)',
    icon        VARCHAR(50)     DEFAULT NULL                COMMENT '图标',
    type        TINYINT         NOT NULL DEFAULT 1          COMMENT '类型(0目录/1菜单/2按钮)',
    sort        INT             DEFAULT 0                   COMMENT '排序号',
    visible     TINYINT         DEFAULT 1                   COMMENT '是否可见(0隐藏/1显示)',
    status      TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 1.5 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT  AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT  NOT NULL COMMENT '角色ID',
    menu_id BIGINT  NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 1.6 系统参数配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '参数ID',
    config_key   VARCHAR(100)    NOT NULL                    COMMENT '配置键',
    config_value VARCHAR(500)    DEFAULT NULL                COMMENT '配置值',
    config_type  TINYINT         DEFAULT 1                   COMMENT '配置类型(0系统内置/1自定义)',
    remark       VARCHAR(200)    DEFAULT NULL                COMMENT '备注',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数配置表';

-- 1.7 数据字典表
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '字典ID',
    dict_code   VARCHAR(50)     NOT NULL                    COMMENT '字典编码',
    dict_name   VARCHAR(100)    NOT NULL                    COMMENT '字典名称',
    description VARCHAR(200)    DEFAULT NULL                COMMENT '描述',
    status      TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 1.8 字典项表
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '字典项ID',
    dict_code    VARCHAR(50)     NOT NULL                    COMMENT '所属字典编码',
    item_label   VARCHAR(100)    NOT NULL                    COMMENT '字典项标签',
    item_value   VARCHAR(100)    NOT NULL                    COMMENT '字典项值',
    sort         INT             DEFAULT 0                   COMMENT '排序号',
    status       TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    default_flag TINYINT         DEFAULT 0                   COMMENT '是否默认(0否/1是)',
    remark       VARCHAR(200)    DEFAULT NULL                COMMENT '备注',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dict_code (dict_code),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- 1.9 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '日志ID',
    user_id         BIGINT          DEFAULT NULL                COMMENT '操作用户ID',
    username        VARCHAR(50)     DEFAULT NULL                COMMENT '操作用户名',
    operation       VARCHAR(200)    NOT NULL                    COMMENT '操作描述',
    module          VARCHAR(50)     NOT NULL                    COMMENT '所属模块(book/borrow/reader/system/auth)',
    request_ip      VARCHAR(50)     DEFAULT NULL                COMMENT '请求IP',
    request_method  VARCHAR(10)     DEFAULT NULL                COMMENT '请求方式(GET/POST/PUT/DELETE)',
    request_url     VARCHAR(255)    DEFAULT NULL                COMMENT '请求URL',
    request_params  TEXT            DEFAULT NULL                COMMENT '请求参数',
    result          VARCHAR(20)     DEFAULT NULL                COMMENT '操作结果(success/fail)',
    duration        INT             DEFAULT NULL                COMMENT '耗时(ms)',
    error_msg       TEXT            DEFAULT NULL                COMMENT '错误信息',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 1.10 数据备份记录表
DROP TABLE IF EXISTS sys_backup;
CREATE TABLE sys_backup (
    id            BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '备份ID',
    file_name     VARCHAR(200)    NOT NULL                    COMMENT '备份文件名',
    file_path     VARCHAR(500)    NOT NULL                    COMMENT '文件存储路径',
    file_size     BIGINT          DEFAULT 0                   COMMENT '文件大小(字节)',
    backup_type   VARCHAR(20)     DEFAULT 'manual'            COMMENT '备份类型(manual手动/auto自动)',
    backup_status TINYINT         DEFAULT 0                   COMMENT '状态(0进行中/1成功/2失败)',
    description   VARCHAR(200)    DEFAULT NULL                COMMENT '备份说明',
    create_by     BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_create_time (create_time),
    INDEX idx_backup_type (backup_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据备份记录表';

-- ============================================================================
-- 2. 读者管理表
-- ============================================================================

-- 2.1 读者类型配置表
DROP TABLE IF EXISTS reader_type;
CREATE TABLE reader_type (
    id                 BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '类型ID',
    name               VARCHAR(50)     NOT NULL                    COMMENT '类型名称(学生/教师/校外读者)',
    code               VARCHAR(50)     NOT NULL                    COMMENT '类型编码',
    max_borrow         INT             NOT NULL DEFAULT 5          COMMENT '最大借阅数量',
    borrow_days        INT             NOT NULL DEFAULT 30         COMMENT '借阅天数',
    renew_count        INT             NOT NULL DEFAULT 1          COMMENT '续借次数限制',
    renew_days         INT             NOT NULL DEFAULT 15         COMMENT '续借天数',
    overdue_fine_rate  DECIMAL(10,2)   NOT NULL DEFAULT 0.50       COMMENT '逾期费率(元/天)',
    reservation_keep_hours INT         NOT NULL DEFAULT 48         COMMENT '预约保留时长(小时)',
    can_borrow         TINYINT         DEFAULT 1                   COMMENT '是否可以借书',
    status             TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    sort               INT             DEFAULT 0                   COMMENT '排序号',
    create_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='读者类型配置表';

-- 2.2 读者信息表
DROP TABLE IF EXISTS reader;
CREATE TABLE reader (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '读者ID',
    user_id          BIGINT          NOT NULL                    COMMENT '关联用户ID',
    reader_no        VARCHAR(20)     NOT NULL                    COMMENT '读者证号',
    reader_type_id   BIGINT          NOT NULL                    COMMENT '读者类型ID',
    card_status      TINYINT         DEFAULT 1                   COMMENT '借阅证状态(0挂失/1正常/2冻结)',
    total_borrowed   INT             DEFAULT 0                   COMMENT '累计借阅次数',
    current_borrowed INT             DEFAULT 0                   COMMENT '当前在借数量',
    total_fines      DECIMAL(10,2)   DEFAULT 0.00               COMMENT '累计罚款金额',
    register_date    DATE            NOT NULL                    COMMENT '注册日期',
    expire_date      DATE            DEFAULT NULL                COMMENT '有效截止日期',
    remark           VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_reader_no (reader_no),
    INDEX idx_reader_type_id (reader_type_id),
    INDEX idx_card_status (card_status),
    INDEX idx_register_date (register_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='读者信息表';

-- ============================================================================
-- 3. 图书管理表
-- ============================================================================

-- 3.1 图书分类表
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '分类ID',
    name        VARCHAR(100)    NOT NULL                    COMMENT '分类名称',
    code        VARCHAR(50)     DEFAULT NULL                COMMENT '分类编码(中图法分类号)',
    parent_id   BIGINT          DEFAULT 0                   COMMENT '父分类ID(0为顶级)',
    level       TINYINT         DEFAULT 1                   COMMENT '层级(1/2/3)',
    sort        INT             DEFAULT 0                   COMMENT '排序号',
    status      TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 3.2 出版社表
DROP TABLE IF EXISTS publisher;
CREATE TABLE publisher (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '出版社ID',
    name        VARCHAR(200)    NOT NULL                    COMMENT '出版社名称',
    short_name  VARCHAR(50)     DEFAULT NULL                COMMENT '简称',
    isbn_prefix VARCHAR(10)     DEFAULT NULL                COMMENT 'ISBN前缀',
    phone       VARCHAR(20)     DEFAULT NULL                COMMENT '联系电话',
    address     VARCHAR(200)    DEFAULT NULL                COMMENT '地址',
    website     VARCHAR(200)    DEFAULT NULL                COMMENT '官网',
    sort        INT             DEFAULT 0                   COMMENT '排序号',
    status      TINYINT         DEFAULT 1                   COMMENT '状态(0禁用/1启用)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出版社表';

-- 3.3 馆藏地点表
DROP TABLE IF EXISTS location;
CREATE TABLE location (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '地点ID',
    name        VARCHAR(100)    NOT NULL                    COMMENT '馆藏地点名称',
    code        VARCHAR(50)     NOT NULL                    COMMENT '地点编码',
    description VARCHAR(200)    DEFAULT NULL                COMMENT '位置描述',
    floor       VARCHAR(20)     DEFAULT NULL                COMMENT '所在楼层',
    enabled     TINYINT         DEFAULT 1                   COMMENT '是否启用(0禁用/1启用)',
    sort        INT             DEFAULT 0                   COMMENT '排序号',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='馆藏地点表';

-- 3.4 图书信息表（图书元数据）
DROP TABLE IF EXISTS book_info;
CREATE TABLE book_info (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '图书ID',
    isbn             VARCHAR(20)     NOT NULL                    COMMENT 'ISBN(国际标准书号)',
    title            VARCHAR(200)    NOT NULL                    COMMENT '书名',
    sub_title        VARCHAR(200)    DEFAULT NULL                COMMENT '副标题',
    author           VARCHAR(200)    NOT NULL                    COMMENT '作者(多人用/分隔)',
    translator       VARCHAR(200)    DEFAULT NULL                COMMENT '译者',
    publisher_id     BIGINT          DEFAULT NULL                COMMENT '出版社ID',
    category_id      BIGINT          DEFAULT NULL                COMMENT '分类ID',
    publish_date     DATE            DEFAULT NULL                COMMENT '出版日期',
    pages            INT             DEFAULT NULL                COMMENT '页数',
    price            DECIMAL(10,2)   DEFAULT NULL                COMMENT '定价',
    binding          VARCHAR(20)     DEFAULT NULL                COMMENT '装帧(平装/精装/软精装)',
    language         VARCHAR(20)     DEFAULT 'Chinese'            COMMENT '语种',
    summary          TEXT            DEFAULT NULL                COMMENT '内容简介',
    cover_url        VARCHAR(255)    DEFAULT NULL                COMMENT '封面图片URL',
    table_of_contents TEXT           DEFAULT NULL                COMMENT '目录',
    total_copies     INT             DEFAULT 0                   COMMENT '总副本数',
    available_copies INT             DEFAULT 0                   COMMENT '可借数量',
    borrow_count     INT             DEFAULT 0                   COMMENT '累计借阅次数',
    rating           DECIMAL(2,1)    DEFAULT 0.0                COMMENT '评分(0.0~5.0)',
    rating_count     INT             DEFAULT 0                   COMMENT '评分人数',
    status           TINYINT         DEFAULT 1                   COMMENT '状态(0下架/1上架/2待审核)',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT         DEFAULT 0                   COMMENT '逻辑删除(0未删/1已删)',
    UNIQUE KEY uk_isbn (isbn),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_category_id (category_id),
    INDEX idx_publish_date (publish_date),
    INDEX idx_status (status),
    INDEX idx_borrow_count (borrow_count),
    INDEX idx_available_copies (available_copies),
    FULLTEXT INDEX ft_title_author (title, author)             COMMENT '全文检索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书信息表';

-- 3.5 图书副本表（每一本实体书）
DROP TABLE IF EXISTS book_copy;
CREATE TABLE book_copy (
    id            BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '副本ID',
    book_id       BIGINT          NOT NULL                    COMMENT '所属图书ID',
    barcode       VARCHAR(50)     NOT NULL                    COMMENT '条码号(每本实体书唯一)',
    location_id   BIGINT          DEFAULT NULL                COMMENT '当前馆藏地点ID',
    status        VARCHAR(20)     NOT NULL DEFAULT 'in'       COMMENT '状态(in在馆/borrowed已借出/reserved预约中/maintenance维修中/lost丢失/discarded注销)',
    price         DECIMAL(10,2)   DEFAULT NULL                COMMENT '购入价格',
    purchase_date DATE            DEFAULT NULL                COMMENT '购入日期',
    source        VARCHAR(50)     DEFAULT NULL                COMMENT '来源(采购/捐赠/调拨)',
    damage_level  TINYINT         DEFAULT 0                   COMMENT '损坏程度(0完好/1轻微/2严重)',
    remark        VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_barcode (barcode),
    INDEX idx_book_id (book_id),
    INDEX idx_location_id (location_id),
    INDEX idx_status (status),
    INDEX idx_purchase_date (purchase_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书副本表';

-- ============================================================================
-- 4. 流通管理表
-- ============================================================================

-- 4.1 借阅记录表
DROP TABLE IF EXISTS borrow_record;
CREATE TABLE borrow_record (
    id            BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '借阅记录ID',
    reader_id     BIGINT          NOT NULL                    COMMENT '读者ID',
    book_copy_id  BIGINT          NOT NULL                    COMMENT '图书副本ID',
    book_info_id  BIGINT          NOT NULL                    COMMENT '图书信息ID(冗余,便于查询)',
    borrow_date   DATETIME        NOT NULL                    COMMENT '借出日期',
    due_date      DATE            NOT NULL                    COMMENT '应还日期',
    return_date   DATETIME        DEFAULT NULL                COMMENT '实际归还日期',
    renew_count   INT             DEFAULT 0                   COMMENT '续借次数',
    status        VARCHAR(20)     NOT NULL DEFAULT 'borrowed' COMMENT '状态(borrowed已借出/returned已归还/overdue逾期归还)',
    operator_id   BIGINT          DEFAULT NULL                COMMENT '操作管理员ID',
    remark        VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_copy_id (book_copy_id),
    INDEX idx_book_info_id (book_info_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    INDEX idx_borrow_date (borrow_date),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 4.2 预约记录表
DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '预约ID',
    reader_id        BIGINT          NOT NULL                    COMMENT '读者ID',
    book_info_id     BIGINT          NOT NULL                    COMMENT '图书ID',
    book_copy_id     BIGINT          DEFAULT NULL                COMMENT '分配到的副本ID(有书可借时填充)',
    reserve_date     DATETIME        NOT NULL                    COMMENT '预约时间',
    expire_date      DATETIME        DEFAULT NULL                COMMENT '预约过期时间',
    status           VARCHAR(20)     NOT NULL DEFAULT 'waiting'  COMMENT '状态(waiting等待中/ready待取书/fulfilled已完成/cancelled已取消/expired已过期)',
    pick_location_id BIGINT          DEFAULT NULL                COMMENT '取书地点ID',
    operator_id      BIGINT          DEFAULT NULL                COMMENT '操作员ID(取书确认时记录)',
    remark           VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_info_id (book_info_id),
    INDEX idx_book_copy_id (book_copy_id),
    INDEX idx_status (status),
    INDEX idx_reserve_date (reserve_date),
    INDEX idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

-- 4.3 罚款记录表
DROP TABLE IF EXISTS fine_record;
CREATE TABLE fine_record (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '罚款ID',
    reader_id        BIGINT          NOT NULL                    COMMENT '读者ID',
    borrow_record_id BIGINT          DEFAULT NULL                COMMENT '关联借阅记录ID',
    fine_type        VARCHAR(20)     NOT NULL                    COMMENT '罚款类型(overdue逾期/damage损坏/lost丢失)',
    amount           DECIMAL(10,2)   NOT NULL                    COMMENT '罚款金额',
    paid             TINYINT         DEFAULT 0                   COMMENT '是否已缴(0未缴/1已缴)',
    paid_date        DATETIME        DEFAULT NULL                COMMENT '缴费日期',
    operator_id      BIGINT          DEFAULT NULL                COMMENT '收费管理员ID',
    waive            TINYINT         DEFAULT 0                   COMMENT '是否豁免(0否/1是)',
    waive_reason     VARCHAR(200)    DEFAULT NULL                COMMENT '豁免原因',
    remark           VARCHAR(500)    DEFAULT NULL                COMMENT '备注',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reader_id (reader_id),
    INDEX idx_borrow_record_id (borrow_record_id),
    INDEX idx_fine_type (fine_type),
    INDEX idx_paid (paid),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚款记录表';

-- ============================================================================
-- 5. 读者端功能表
-- ============================================================================

-- 5.1 收藏表
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
    id          BIGINT  AUTO_INCREMENT PRIMARY KEY,
    reader_id   BIGINT  NOT NULL COMMENT '读者ID',
    book_info_id BIGINT NOT NULL COMMENT '图书ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_reader_book (reader_id, book_info_id),
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_info_id (book_info_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 5.2 通知消息表
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '通知ID',
    reader_id    BIGINT          NOT NULL                    COMMENT '接收者读者ID',
    title        VARCHAR(200)    NOT NULL                    COMMENT '通知标题',
    content      TEXT            NOT NULL                    COMMENT '通知内容',
    type         VARCHAR(30)     NOT NULL                    COMMENT '类型(overdue_due逾期/即将到期/arrival到书/cancel超时取消/fine罚款/system系统公告)',
    related_type VARCHAR(30)     DEFAULT NULL                COMMENT '关联业务类型(borrow/reservation/fine)',
    related_id   BIGINT          DEFAULT NULL                COMMENT '关联业务ID',
    read_flag    TINYINT         DEFAULT 0                   COMMENT '是否已读(0未读/1已读)',
    read_time    DATETIME        DEFAULT NULL                COMMENT '阅读时间',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reader_id (reader_id),
    INDEX idx_type (type),
    INDEX idx_read_flag (read_flag),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

-- 5.3 公告表
DROP TABLE IF EXISTS announcement;
CREATE TABLE announcement (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '公告ID',
    title        VARCHAR(200)    NOT NULL                    COMMENT '公告标题',
    content      TEXT            NOT NULL                    COMMENT '公告内容(支持Markdown)',
    type         VARCHAR(20)     DEFAULT 'general'           COMMENT '公告类型(general一般/urgent紧急/notice通知)',
    target_roles VARCHAR(100)    DEFAULT 'all'               COMMENT '发布范围(all/reader/admin)',
    top_flag     TINYINT         DEFAULT 0                   COMMENT '是否置顶(0否/1是)',
    status       TINYINT         DEFAULT 0                   COMMENT '状态(0草稿/1已发布/2已下架)',
    publish_time DATETIME        DEFAULT NULL                COMMENT '发布时间',
    create_by    BIGINT          DEFAULT NULL                COMMENT '发布人ID',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_top_flag (top_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 5.4 推荐图书表
DROP TABLE IF EXISTS book_recommendation;
CREATE TABLE book_recommendation (
    id          BIGINT  AUTO_INCREMENT PRIMARY KEY  COMMENT '推荐ID',
    book_info_id BIGINT NOT NULL                    COMMENT '图书ID',
    sort        INT     DEFAULT 0                   COMMENT '排序号',
    remark      VARCHAR(200) DEFAULT NULL           COMMENT '推荐语',
    create_by   BIGINT  DEFAULT NULL                COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_book_info (book_info_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐图书表';

-- ============================================================================
-- 6. 外键约束
-- ============================================================================

-- 6.1 用户相关
ALTER TABLE sys_user_role ADD CONSTRAINT fk_sur_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_user_role ADD CONSTRAINT fk_sur_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE;

-- 6.2 角色菜单
ALTER TABLE sys_role_menu ADD CONSTRAINT fk_srm_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE;
ALTER TABLE sys_role_menu ADD CONSTRAINT fk_srm_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE;

-- 6.3 读者相关
ALTER TABLE reader ADD CONSTRAINT fk_reader_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE reader ADD CONSTRAINT fk_reader_type FOREIGN KEY (reader_type_id) REFERENCES reader_type(id) ON DELETE RESTRICT;

-- 6.4 图书相关
ALTER TABLE book_info ADD CONSTRAINT fk_book_publisher FOREIGN KEY (publisher_id) REFERENCES publisher(id) ON DELETE SET NULL;
ALTER TABLE book_info ADD CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL;
ALTER TABLE book_copy ADD CONSTRAINT fk_copy_book FOREIGN KEY (book_id) REFERENCES book_info(id) ON DELETE CASCADE;
ALTER TABLE book_copy ADD CONSTRAINT fk_copy_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE SET NULL;

-- 6.5 流通相关
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_reader FOREIGN KEY (reader_id) REFERENCES reader(id) ON DELETE RESTRICT;
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_copy FOREIGN KEY (book_copy_id) REFERENCES book_copy(id) ON DELETE RESTRICT;
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_book FOREIGN KEY (book_info_id) REFERENCES book_info(id) ON DELETE RESTRICT;
ALTER TABLE borrow_record ADD CONSTRAINT fk_borrow_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE SET NULL;

ALTER TABLE reservation ADD CONSTRAINT fk_reserve_reader FOREIGN KEY (reader_id) REFERENCES reader(id) ON DELETE RESTRICT;
ALTER TABLE reservation ADD CONSTRAINT fk_reserve_book FOREIGN KEY (book_info_id) REFERENCES book_info(id) ON DELETE RESTRICT;
ALTER TABLE reservation ADD CONSTRAINT fk_reserve_copy FOREIGN KEY (book_copy_id) REFERENCES book_copy(id) ON DELETE SET NULL;
ALTER TABLE reservation ADD CONSTRAINT fk_reserve_location FOREIGN KEY (pick_location_id) REFERENCES location(id) ON DELETE SET NULL;
ALTER TABLE reservation ADD CONSTRAINT fk_reserve_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE SET NULL;

ALTER TABLE fine_record ADD CONSTRAINT fk_fine_reader FOREIGN KEY (reader_id) REFERENCES reader(id) ON DELETE RESTRICT;
ALTER TABLE fine_record ADD CONSTRAINT fk_fine_borrow FOREIGN KEY (borrow_record_id) REFERENCES borrow_record(id) ON DELETE SET NULL;
ALTER TABLE fine_record ADD CONSTRAINT fk_fine_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 6.6 读者端功能
ALTER TABLE favorite ADD CONSTRAINT fk_fav_reader FOREIGN KEY (reader_id) REFERENCES reader(id) ON DELETE CASCADE;
ALTER TABLE favorite ADD CONSTRAINT fk_fav_book FOREIGN KEY (book_info_id) REFERENCES book_info(id) ON DELETE CASCADE;

ALTER TABLE notification ADD CONSTRAINT fk_notify_reader FOREIGN KEY (reader_id) REFERENCES reader(id) ON DELETE CASCADE;

ALTER TABLE announcement ADD CONSTRAINT fk_announce_create FOREIGN KEY (create_by) REFERENCES sys_user(id) ON DELETE SET NULL;

ALTER TABLE book_recommendation ADD CONSTRAINT fk_recommend_book FOREIGN KEY (book_info_id) REFERENCES book_info(id) ON DELETE CASCADE;

-- ============================================================================
-- 7. 种子数据
-- ============================================================================

-- 7.1 系统参数配置
INSERT INTO sys_config (config_key, config_value, config_type, remark) VALUES
    ('system.name',            '图书管理系统',           0, '系统名称'),
    ('system.logo',            '',                        0, '系统Logo URL'),
    ('system.copyright',       '© 2026 图书管理系统',     0, '版权信息'),
    ('system.icp',             '',                        0, 'ICP备案号'),
    ('system.contact_email',   'admin@library.com',       0, '联系邮箱'),
    ('borrow.max_books',       '5',                       0, '最大借阅数(默认值,可通过读者类型覆盖)'),
    ('borrow.days',            '30',                      0, '默认借阅天数'),
    ('borrow.renew_count',     '1',                       0, '默认续借次数'),
    ('borrow.renew_days',      '15',                      0, '默认续借天数'),
    ('fine.overdue_rate',      '0.50',                    0, '逾期费率(元/天)'),
    ('fine.damage_multiple',   '2.00',                    0, '损坏赔偿倍数(定价×倍数)'),
    ('fine.lost_multiple',     '3.00',                    0, '丢失赔偿倍数(定价×倍数)'),
    ('reservation.keep_hours', '48',                      0, '预约保留时长(小时)'),
    ('reader.initial_password','123456',                  0, '读者初始密码'),
    ('reader.card_prefix',     'RD',                      0, '读者证号前缀'),
    ('security.captcha',       '1',                       0, '启用验证码(0关闭/1开启)'),
    ('security.failed_limit',  '5',                       0, '登录失败锁定次数'),
    ('notify.overdue_advance_days', '3',                  0, '逾期提前提醒天数'),
    ('notify.daily_overdue_check', '1',                   0, '每日逾期检查(0关闭/1开启)');

-- 7.2 角色数据 (4个预设角色)
INSERT INTO sys_role (name, code, description, sort) VALUES
    ('系统管理员', 'ROLE_ADMIN',     '拥有系统全部权限',      1),
    ('图书管理员', 'ROLE_LIBRARIAN', '负责图书借还流通操作',  2),
    ('采编员',     'ROLE_CATALOGER', '负责图书编目采编工作',  3),
    ('读者',       'ROLE_READER',    '普通读者/学生用户',      4);

-- 7.3 管理端菜单/权限数据
-- 一级菜单 (目录)
INSERT INTO sys_menu (id, name, type, path, component, icon, sort, parent_id, visible) VALUES
    (1,   '工作台',       0, '/admin/dashboard',   '/admin/dashboard/index',   'Odometer',       1, 0, 1),
    (2,   '流通管理',     0, '/admin/borrow',      'Layout',                   'Reading',          2, 0, 1),
    (3,   '图书管理',     0, '/admin/books',       'Layout',                   'Notebook',         3, 0, 1),
    (4,   '读者管理',     0, '/admin/readers',     '/admin/reader/index',      'UserFilled',       4, 0, 1),
    (5,   '统计分析',     0, '/admin/statistics',  'Layout',                   'DataAnalysis',     5, 0, 1),
    (6,   '系统管理',     0, '/admin/system',      'Layout',                   'Setting',          6, 0, 1),
    (7,   '公告管理',     0, '/admin/announcement','/admin/announcement/index','Warning',          7, 0, 1);

-- 二级菜单 (菜单)
-- 流通管理子菜单
INSERT INTO sys_menu (id, name, type, path, component, parent_id, sort) VALUES
    (10,  '借书',          1, '/admin/borrow/borrow',    '/admin/borrow/Borrow',   2, 1),
    (11,  '还书',          1, '/admin/borrow/return',    '/admin/borrow/Return',   2, 2),
    (12,  '预约管理',      1, '/admin/borrow/reserve',  '/admin/borrow/Reservation', 2, 3),
    (13,  '罚款管理',      1, '/admin/borrow/fine',     '/admin/borrow/Fine',     2, 4);

-- 图书管理子菜单
INSERT INTO sys_menu (id, name, type, path, component, parent_id, sort) VALUES
    (20,  '图书列表',      1, '/admin/books/list',      '/admin/books/BookList',   3, 1),
    (21,  '新增图书',      1, '/admin/books/create',    '/admin/books/BookForm',   3, 2),
    (22,  '批量导入',      1, '/admin/books/import',    '/admin/books/BookImport', 3, 3),
    (23,  '分类管理',      1, '/admin/books/category',  '/admin/books/Category',   3, 4),
    (24,  '出版社管理',    1, '/admin/books/publisher', '/admin/books/Publisher',  3, 5),
    (25,  '馆藏地点',      1, '/admin/books/location',  '/admin/books/Location',   3, 6),
    (26,  '图书盘点',      1, '/admin/books/inventory', '/admin/books/Inventory',  3, 7);

-- 统计分析子菜单
INSERT INTO sys_menu (id, name, type, path, component, parent_id, sort) VALUES
    (30,  '借阅统计',      1, '/admin/statistics/borrow',   '/admin/statistics/BorrowStats',  5, 1),
    (31,  '热门排行',      1, '/admin/statistics/hot',      '/admin/statistics/HotBooks',     5, 2),
    (32,  '馆藏统计',      1, '/admin/statistics/collection','/admin/statistics/CollectionStats',5, 3);

-- 系统管理子菜单
INSERT INTO sys_menu (id, name, type, path, component, parent_id, sort) VALUES
    (40,  '用户管理',      1, '/admin/system/users',    '/admin/system/User',       6, 1),
    (41,  '角色管理',      1, '/admin/system/roles',    '/admin/system/Role',       6, 2),
    (42,  '菜单管理',      1, '/admin/system/menus',    '/admin/system/Menu',       6, 3),
    (43,  '系统参数',      1, '/admin/system/config',   '/admin/system/Config',     6, 4),
    (44,  '操作日志',      1, '/admin/system/logs',     '/admin/system/Log',        6, 5),
    (45,  '数据字典',      1, '/admin/system/dict',     '/admin/system/Dict',       6, 6),
    (46,  '备份恢复',      1, '/admin/system/backup',   '/admin/system/Backup',     6, 7);

-- 按钮权限 (三级)
INSERT INTO sys_menu (name, type, permission, parent_id, sort) VALUES
    -- 流通管理按钮
    ('执行借书',       2, 'borrow:create',          10, 1),
    ('执行还书',       2, 'borrow:return',          11, 1),
    ('查询预约',       2, 'reservation:list',       12, 1),
    ('取书确认',       2, 'reservation:pickup',     12, 2),
    ('取消预约',       2, 'reservation:cancel',     12, 3),
    ('查询罚款',       2, 'fine:list',              13, 1),
    ('缴纳罚款',       2, 'fine:pay',               13, 2),
    ('豁免罚款',       2, 'fine:waive',             13, 3),
    -- 图书管理按钮
    ('查询图书',       2, 'book:list',              20, 1),
    ('新增图书',       2, 'book:create',            21, 1),
    ('编辑图书',       2, 'book:edit',              20, 2),
    ('删除图书',       2, 'book:delete',            20, 3),
    ('上架下架',       2, 'book:status',            20, 4),
    ('导出图书',       2, 'book:export',            20, 5),
    ('导入图书',       2, 'book:import',            22, 1),
    ('查询分类',       2, 'category:list',          23, 1),
    ('新增分类',       2, 'category:create',        23, 2),
    ('编辑分类',       2, 'category:edit',          23, 3),
    ('删除分类',       2, 'category:delete',        23, 4),
    ('查询出版社',     2, 'publisher:list',         24, 1),
    ('新增出版社',     2, 'publisher:create',       24, 2),
    ('编辑出版社',     2, 'publisher:edit',         24, 3),
    ('删除出版社',     2, 'publisher:delete',       24, 4),
    ('查询地点',       2, 'location:list',          25, 1),
    ('新增地点',       2, 'location:create',        25, 2),
    ('编辑地点',       2, 'location:edit',          25, 3),
    ('删除地点',       2, 'location:delete',        25, 4),
    ('盘点导入',       2, 'inventory:import',       26, 1),
    ('盘点报告',       2, 'inventory:report',       26, 2),
    -- 读者管理按钮
    ('查询读者',       2, 'reader:list',            4, 1),
    ('新增读者',       2, 'reader:create',          4, 2),
    ('编辑读者',       2, 'reader:edit',            4, 3),
    ('挂失解挂',       2, 'reader:card',            4, 4),
    ('冻结解冻',       2, 'reader:freeze',          4, 5),
    ('重置密码',       2, 'reader:reset-pwd',       4, 6),
    -- 系统管理按钮
    ('查询用户',       2, 'system:user:list',        40, 1),
    ('新增用户',       2, 'system:user:create',      40, 2),
    ('编辑用户',       2, 'system:user:edit',        40, 3),
    ('删除用户',       2, 'system:user:delete',      40, 4),
    ('重置密码',       2, 'system:user:reset-pwd',   40, 5),
    ('查询角色',       2, 'system:role:list',        41, 1),
    ('新增角色',       2, 'system:role:create',      41, 2),
    ('编辑角色',       2, 'system:role:edit',        41, 3),
    ('删除角色',       2, 'system:role:delete',      41, 4),
    ('查询菜单',       2, 'system:menu:list',        42, 1),
    ('新增菜单',       2, 'system:menu:create',      42, 2),
    ('编辑菜单',       2, 'system:menu:edit',        42, 3),
    ('删除菜单',       2, 'system:menu:delete',      42, 4),
    ('查询参数',       2, 'system:config:list',      43, 1),
    ('编辑参数',       2, 'system:config:edit',      43, 2),
    ('查询日志',       2, 'system:log:list',         44, 1),
    ('查询字典',       2, 'system:dict:list',        45, 1),
    ('新增字典',       2, 'system:dict:create',      45, 2),
    ('编辑字典',       2, 'system:dict:edit',        45, 3),
    ('删除字典',       2, 'system:dict:delete',      45, 4),
    ('创建备份',       2, 'system:backup:create',    46, 1),
    ('下载备份',       2, 'system:backup:download',  46, 2),
    ('恢复备份',       2, 'system:backup:restore',   46, 3);

-- 7.4 为管理员角色分配所有菜单权限
-- (ROLE_ADMIN 的 ID 为 1)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 7.5 默认管理员用户 (密码: admin123, BCrypt 加密)
INSERT INTO sys_user (id, username, password, real_name, email, status) VALUES
    (1, 'admin', '$2a$10$v/cUl3YItml6dwF2dDF.keklXKvffZydWLPYYnYe2TT/h7UBYZx8G', '系统管理员', 'admin@library.com', 1),
    (2, 'librarian', '$2a$10$v/cUl3YItml6dwF2dDF.keklXKvffZydWLPYYnYe2TT/h7UBYZx8G', '张图书', 'lib@library.com', 1),
    (3, 'cataloger', '$2a$10$v/cUl3YItml6dwF2dDF.keklXKvffZydWLPYYnYe2TT/h7UBYZx8G', '李采编', 'cat@library.com', 1),
    (4, 'reader01', '$2a$10$v/cUl3YItml6dwF2dDF.keklXKvffZydWLPYYnYe2TT/h7UBYZx8G', '王同学', 'reader@library.com', 1);

-- 7.6 为用户分配角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
    (1, 1), -- admin → ROLE_ADMIN
    (2, 2), -- librarian → ROLE_LIBRARIAN
    (3, 3), -- cataloger → ROLE_CATALOGER
    (4, 4); -- reader01 → ROLE_READER

-- 7.7 读者类型配置
INSERT INTO reader_type (id, name, code, max_borrow, borrow_days, renew_count, renew_days, overdue_fine_rate, reservation_keep_hours) VALUES
    (1, '学生',     'STUDENT',  5,  30, 1, 15, 0.50,  48),
    (2, '教师',     'TEACHER',  10, 60, 2, 30, 0.00,  72),
    (3, '教职工',   'STAFF',    8,  45, 1, 20, 0.30,  48),
    (4, '校外读者', 'EXTERNAL', 3,  14, 0, 0,  1.00,  24);

-- 7.8 读者信息 (关联 sys_user 中的 reader01)
INSERT INTO reader (user_id, reader_no, reader_type_id, register_date) VALUES
    (4, 'RD20260001', 1, CURDATE());

-- 7.9 图书分类 (中图法一级分类)
INSERT INTO category (id, name, code, level, sort) VALUES
    (1,  '马克思主义、列宁主义、毛泽东思想、邓小平理论', 'A', 1, 1),
    (2,  '哲学、宗教',                                 'B', 1, 2),
    (3,  '社会科学总论',                               'C', 1, 3),
    (4,  '政治、法律',                                 'D', 1, 4),
    (5,  '军事',                                       'E', 1, 5),
    (6,  '经济',                                       'F', 1, 6),
    (7,  '文化、科学、教育、体育',                     'G', 1, 7),
    (8,  '语言、文字',                                 'H', 1, 8),
    (9,  '文学',                                       'I', 1, 9),
    (10, '艺术',                                       'J', 1, 10),
    (11, '历史、地理',                                 'K', 1, 11),
    (12, '自然科学总论',                               'N', 1, 12),
    (13, '数理科学和化学',                             'O', 1, 13),
    (14, '天文学、地球科学',                           'P', 1, 14),
    (15, '生物科学',                                   'Q', 1, 15),
    (16, '医药、卫生',                                 'R', 1, 16),
    (17, '农业科学',                                   'S', 1, 17),
    (18, '工业技术',                                   'T', 1, 18),
    (19, '交通运输',                                   'U', 1, 19),
    (20, '航空、航天',                                 'V', 1, 20),
    (21, '环境科学、安全科学',                         'X', 1, 21),
    (22, '综合性图书',                                 'Z', 1, 22);

-- 7.10 馆藏地点
INSERT INTO location (id, name, code, description, floor) VALUES
    (1, '一楼社科书库',   'FL01', '一楼东侧，主要存放A-G类图书',     '1F'),
    (2, '二楼文学书库',   'FL02', '二楼东侧，主要存放I-K类图书',     '2F'),
    (3, '三楼科技书库',   'FL03', '三楼全层，主要存放N-Z类图书',     '3F'),
    (4, '密集书库',       'FL04', '地下一层，存放低频借阅图书',      'B1'),
    (5, '报刊阅览室',     'FL05', '二楼西侧，报刊杂志阅览',          '2F'),
    (6, '电子阅览室',     'FL06', '三楼西侧，提供电子资源查阅',      '3F');

-- 7.11 出版社示例数据
INSERT INTO publisher (id, name, short_name) VALUES
    (1, '人民文学出版社',       '人民文学'),
    (2, '机械工业出版社',       '机工社'),
    (3, '清华大学出版社',       '清华大学出版社'),
    (4, '电子工业出版社',       '电子工业'),
    (5, '中信出版社',           '中信'),
    (6, '科学出版社',           '科学'),
    (7, '高等教育出版社',       '高教'),
    (8, '商务印书馆',           '商务'),
    (9, '人民邮电出版社',       '人邮'),
    (10,'北京大学出版社',       '北大出版社');

-- 7.12 数据字典
INSERT INTO sys_dict (dict_code, dict_name, description) VALUES
    ('book_binding',    '图书装帧',     '图书装帧类型'),
    ('book_language',   '图书语种',     '图书语言'),
    ('book_copy_source','副本来源',     '图书副本来源渠道'),
    ('fine_type',       '罚款类型',     '罚款分类'),
    ('notification_type','通知类型',    '消息通知分类'),
    ('book_status',     '图书状态',     '图书上架状态'),
    ('copy_status',     '副本状态',     '图书副本流通状态'),
    ('announcement_type','公告类型',    '公告分类'),
    ('gender',          '性别',         '用户性别');

INSERT INTO sys_dict_item (dict_code, item_label, item_value, sort, default_flag) VALUES
    ('book_binding', '平装', '平装', 1, 1),
    ('book_binding', '精装', '精装', 2, 0),
    ('book_binding', '软精装', '软精装', 3, 0),
    ('book_language', '中文', '中文', 1, 1),
    ('book_language', '英文', '英文', 2, 0),
    ('book_language', '中英双语', '中英双语', 3, 0),
    ('book_language', '日文', '日文', 4, 0),
    ('book_language', '其他', '其他', 5, 0),
    ('book_copy_source', '采购', '采购', 1, 1),
    ('book_copy_source', '捐赠', '捐赠', 2, 0),
    ('book_copy_source', '调拨', '调拨', 3, 0),
    ('fine_type', '逾期罚款', 'overdue', 1, 1),
    ('fine_type', '损坏赔偿', 'damage', 2, 0),
    ('fine_type', '丢失赔偿', 'lost', 3, 0),
    ('notification_type', '逾期提醒', 'overdue_due', 1, 0),
    ('notification_type', '即将到期', '即将到期', 2, 0),
    ('notification_type', '预约到书', 'arrival', 3, 0),
    ('notification_type', '预约取消', 'cancel', 4, 0),
    ('notification_type', '罚款通知', 'fine', 5, 0),
    ('notification_type', '系统公告', 'system', 6, 0),
    ('copy_status', '在馆', 'in', 1, 1),
    ('copy_status', '已借出', 'borrowed', 2, 0),
    ('copy_status', '预约中', 'reserved', 3, 0),
    ('copy_status', '维修中', 'maintenance', 4, 0),
    ('copy_status', '丢失', 'lost', 5, 0),
    ('copy_status', '注销', 'discarded', 6, 0),
    ('gender', '未知', '0', 1, 1),
    ('gender', '男', '1', 2, 0),
    ('gender', '女', '2', 3, 0);

-- ============================================================================
-- 8. 借阅流程种子数据
-- ============================================================================

-- 8.1 补充分类 (二级分类，便于测试)
INSERT INTO category (id, name, code, parent_id, level, sort) VALUES
    (23, '中国文学',   'I2', 9,  2, 1),
    (24, '外国文学',   'I3', 9,  2, 2),
    (25, '自动化技术、计算机技术', 'TP3', 18, 2, 1),
    (26, '中国历史',   'K2', 11, 2, 1);

-- 8.2 补充出版社
INSERT INTO publisher (id, name, short_name) VALUES
    (11, '重庆出版社',       '重庆出版社'),
    (12, '上海译文出版社',   '上海译文');

-- 8.3 图书信息 (6 本热门图书)
INSERT INTO book_info (id, isbn, title, author, translator, publisher_id, category_id, publish_date, pages, price, binding, language, summary, total_copies, available_copies, borrow_count, rating, rating_count, status) VALUES
    (1, '9787536692930', '三体',            '刘慈欣',        NULL,               11, 23, '2008-01-01', 302,  23.00, '平装', 'Chinese', '文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划"红岸工程"取得了突破性进展。', 3, 2, 128, 4.8, 1024, 1),
    (2, '9787544253994', '百年孤独',        '加西亚·马尔克斯', '范晔',          12, 24, '2011-06-01', 360,  39.50, '精装', 'Chinese', '魔幻现实主义文学代表作，描写了布恩迪亚家族七代人的传奇故事。', 2, 1, 85,  4.7, 856,  1),
    (3, '9787111641247', '深入理解Java虚拟机', '周志明',      NULL,               2,  25, '2019-12-01', 540, 129.00, '平装', 'Chinese', '从Java虚拟机的整体架构出发，深入剖析其工作原理和实现细节。', 3, 1, 210, 4.9, 1520, 1),
    (4, '9787111407010', '算法导论',        'Thomas H.Cormen', '殷建平等',       2,  25, '2012-12-01', 1312, 128.00, '精装', 'Chinese', '全面介绍了多种算法的设计和分析方法。', 2, 1, 156, 4.8, 2100, 1),
    (5, '9787508660752', '人类简史',        '尤瓦尔·赫拉利',  '林俊宏',          5,  26, '2014-11-01', 440,  68.00, '平装', 'Chinese', '从十万年前有生命迹象开始到21世纪资本、科技交织的人类发展史。', 2, 1, 92,  4.6, 680,  1),
    (6, '9787020024759', '围城',            '钱钟书',         NULL,               1,  23, '1991-02-01', 359,  29.80, '平装', 'Chinese', '一幅栩栩如生的市井百态图，人生的酸甜苦辣千般滋味均在其中。', 2, 2, 67,  4.5, 450,  1);

-- 8.4 图书副本 (每本书 2~3 本，含多种状态)
INSERT INTO book_copy (book_id, barcode, location_id, status, price, purchase_date, source) VALUES
    -- 三体 (3 本: 1 borrowed, 1 in, 1 in)
    (1, '9787536692930-001', 2, 'borrowed', 23.00, '2024-01-15', '采购'),
    (1, '9787536692930-002', 2, 'in',       23.00, '2024-01-15', '采购'),
    (1, '9787536692930-003', 2, 'in',       23.00, '2024-06-01', '捐赠'),
    -- 百年孤独 (2 本: 1 borrowed, 1 in)
    (2, '9787544253994-001', 2, 'borrowed', 39.50, '2024-03-01', '采购'),
    (2, '9787544253994-002', 2, 'in',       39.50, '2024-03-01', '采购'),
    -- 深入理解Java虚拟机 (3 本: 1 borrowed 逾期, 1 borrowed, 1 in)
    (3, '9787111641247-001', 3, 'borrowed', 129.00,'2024-02-01', '采购'),
    (3, '9787111641247-002', 3, 'borrowed', 129.00,'2024-02-01', '采购'),
    (3, '9787111641247-003', 3, 'in',       129.00,'2024-08-01', '采购'),
    -- 算法导论 (2 本: 1 borrowed, 1 in)
    (4, '9787111407010-001', 3, 'borrowed', 128.00,'2024-04-01', '采购'),
    (4, '9787111407010-002', 3, 'in',       128.00,'2024-04-01', '采购'),
    -- 人类简史 (2 本: 1 borrowed, 1 in)
    (5, '9787508660752-001', 1, 'borrowed', 68.00, '2024-05-01', '采购'),
    (5, '9787508660752-002', 1, 'in',       68.00, '2024-05-01', '采购'),
    -- 围城 (2 本: 1 已还, 1 in)
    (6, '9787020024759-001', 2, 'in',       29.80, '2024-06-01', '采购'),
    (6, '9787020024759-002', 2, 'in',       29.80, '2024-06-01', '采购');

-- 8.5 新增读者(reader02 李华，教师身份)
INSERT INTO sys_user (id, username, password, real_name, email, status) VALUES
    (5, 'reader02', '$2a$10$v/cUl3YItml6dwF2dDF.keklXKvffZydWLPYYnYe2TT/h7UBYZx8G', '李华', 'lihua@test.com', 1);
INSERT INTO sys_user_role (user_id, role_id) VALUES
    (5, 4); -- reader02 → ROLE_READER
INSERT INTO reader (id, user_id, reader_no, reader_type_id, register_date) VALUES
    (2, 5, 'RD20260002', 2, CURDATE());

-- 8.6 借阅记录 (borrow_record)
-- status: borrowed=当前借阅中, returned=已归还
-- 王同学(RD20260001, reader_id=1): 当前3本 + 1本已还
-- 李华(RD20260002, reader_id=2): 当前2本
INSERT INTO borrow_record (id, reader_id, book_copy_id, book_info_id, borrow_date, due_date, return_date, renew_count, status, operator_id) VALUES
    -- 王同学借三体 (副本001) — 逾期未还 (应还 2026-07-15)
    (1, 1, 1, 1, '2026-07-01 10:00:00', '2026-07-15', NULL, 0, 'borrowed', 2),
    -- 王同学借百年孤独 (副本004) — 当前正常 (应还 2026-07-31)
    (2, 1, 4, 2, '2026-07-01 10:30:00', '2026-07-31', NULL, 0, 'borrowed', 2),
    -- 王同学借人类简史 (副本011) — 当前正常 (应还 2026-08-05)
    (3, 1, 11, 5, '2026-07-06 14:00:00', '2026-08-05', NULL, 0, 'borrowed', 2),
    -- 王同学借围城 (副本013) — 已归还
    (4, 1, 13, 6, '2026-07-01 09:00:00', '2026-07-15', '2026-07-14 16:30:00', 0, 'returned', 2),
    -- 李华借深入理解JVM (副本006) — 逾期未还 (应还 2026-07-10)
    (5, 2, 6, 3, '2026-06-10 11:00:00', '2026-07-10', NULL, 0, 'borrowed', 2),
    -- 李华借深入理解JVM (副本007) — 当前正常 (应还 2026-08-10)
    (6, 2, 7, 3, '2026-07-11 15:00:00', '2026-08-10', NULL, 0, 'borrowed', 2),
    -- 李华借算法导论 (副本009) — 当前正常 (应还 2026-08-10)
    (7, 2, 9, 4, '2026-07-11 15:30:00', '2026-08-10', NULL, 0, 'borrowed', 2);

-- 8.7 更新读者当前借阅数量
UPDATE reader SET current_borrowed = 3, total_borrowed = 4 WHERE id = 1;  -- 王同学: 借3本 + 还1本
UPDATE reader SET current_borrowed = 3, total_borrowed = 3 WHERE id = 2;  -- 李华: 借3本

-- 8.8 更新图书副本数量 (book_info 的 total_copies / available_copies 已在 8.3 中设好)
-- 由于拷入数据库时 available_copies 是预设的静态值，此处无需再 UPDATE

-- 8.9 罚款记录 (逾期罚款)
-- 记录1: 王同学 三体 逾期 (rate=0.50/day, 9 days = 4.50)
INSERT INTO fine_record (reader_id, borrow_record_id, fine_type, amount, paid, waive, operator_id, remark, create_time) VALUES
    (1, 1, 'overdue', 4.50, 0, 0, 2, '三体逾期9天自动生成', DATE_SUB(NOW(), INTERVAL 2 DAY));
-- 记录2: 李华 深入理解JVM 副本006 逾期 (教师 rate=0.00, 但演示用仍生成一条)
INSERT INTO fine_record (reader_id, borrow_record_id, fine_type, amount, paid, waive, operator_id, remark, create_time) VALUES
    (2, 5, 'overdue', 7.00, 0, 0, 2, '深入理解JVM逾期14天自动生成', DATE_SUB(NOW(), INTERVAL 5 DAY));

SET FOREIGN_KEY_CHECKS = 1;
