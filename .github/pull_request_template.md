## 变更目的与范围

说明解决的问题、最终行为，以及关联需求。

## 验证

- [ ] H5 构建：`cd frontend && npm ci && npm run build:h5`
- [ ] 微信小程序构建：`cd frontend && npm run build:mp-weixin`
- [ ] 类型检查：`cd frontend && npm run type-check`
- [ ] 后端构建与测试：`cd backend && ./mvnw clean verify`

记录实际执行结果；未执行项说明原因。小程序真机验证应单独说明。

## 风险与未完成事项

说明配置变化、兼容性影响、待确认事项及必要回退方法。

## 检查

- [ ] 没有提交密钥、数据库、日志或构建产物
- [ ] 文档与最终实现一致
- [ ] 等待人工审核，不自动合并
