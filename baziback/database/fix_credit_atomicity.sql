-- 积分系统修复：确保 tb_credit.balance 不出现负数、并发安全建议
USE `bazi`;

-- 1) 余额非负约束（MySQL 8.0.16+ 才真正生效；低版本会解析但不强制）
ALTER TABLE tb_credit
  ADD CONSTRAINT chk_credit_balance_non_negative CHECK (balance >= 0);

-- 2) 事务流水 amount 不为 0（可选）
ALTER TABLE tb_credit_transaction
  ADD CONSTRAINT chk_credit_tx_amount_non_zero CHECK (amount <> 0);

-- 3) 建议索引（按用户查流水/兑换记录）
CREATE INDEX idx_credit_tx_user_time ON tb_credit_transaction(user_id, create_time);
CREATE INDEX idx_exchange_record_user_time ON tb_exchange_record(user_id, create_time);
