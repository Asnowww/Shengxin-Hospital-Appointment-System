# 数据库设计说明

## 运行指南
- 先运行hospital.sql，再运行hospital_data.sql(已插入少量排班数据)
- 默认创建3个测试账户，可用于测试：
  - 患者账户(patient)
    - 用户名：p
    - 密码：1
  - 医生账户(doctor)
    - 用户名：d
    - 密码：1
  - 管理员账户(admin)
    - 用户名：a
    - 密码：1

## 用户与认证模块
### status用户状态
1. `user`表中：
    ```
    status 四种状态：unverified,pending,verified,rejected.
    ```
   - 用户注册后默认为 `unverified`（未认证，初始状态）
   - 上传校园卡或教工证后进入 `pending`（待审核）
   - 管理员审核通过后变为 `verified`（已认证）
   - 若审核拒绝则变为 `rejected`（审核未通过）
    ```状态流转逻辑
    unverified -→ pending -→ verified
    │
    └────────→ rejected --→ pending（重新提交）
    ```
2. `user_verifications`用户认证表中：
    ```
   status 当前申请状态：'pending','approved','rejected'.
   ```
   - 管理员在后台查看所有 `pending` 状态的认证申请
   - 审核通过 → 更新 `user_verifications.status='approved'`，并同步 `users.status='verified'`
   - 审核拒绝 → 更新 `user_verifications.status='rejected'`，并同步 `users.status='rejected'`
   - 系统自动记录审核人 (`reviewed_by`) 与审核时间 (`reviewed_at`)
   - 所有审核记录可追溯，满足医院系统的安全与合规要求。
    