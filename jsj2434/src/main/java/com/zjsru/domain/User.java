package com.zjsru.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 表单用户对象；Lombok 生成访问器、无参构造和包含全部字段的构造方法。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // 与表单输入框的 name 同名，用于绑定请求参数。
    private String username;
    // 仅用于演示账号校验，当前示例没有数据库存储。
    private String password;
}