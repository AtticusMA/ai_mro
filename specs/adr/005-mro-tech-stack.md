---
id: ADR-005
title: MRO 技术栈扩展 — 端边云协同架构
status: accepted
date: 2026-05-25
deciders: ['@arch']
---

# ADR-005: MRO 技术栈扩展 — 端边云协同架构

## Context

智慧机务 8 大业务模块引入了原有基础底座未覆盖的技术领域：IoT 设备接入、实时音视频协作、AI 大模型推理、3D 数字孪生渲染、RFID 硬件交互、VR/AR 沉浸式体验。需要在现有技术栈基础上进行扩展，采用"端—边—云"协同的四层架构。

## Decision

### 总体架构：四层体系

| 层级 | 职责 | 关键技术 |
|------|------|----------|
| 感知层 | 硬件设备数据采集 | AR 眼镜、智能工具柜、RFID 读写器、温湿度传感器、摄像头 |
| 网络层 | 泛在连接 | 5G 专网、WiFi 6、物联网网关、MQTT Broker |
| 平台层 | 数据融合与算法引擎 | 数据中台、AI 中台、物联网中台 |
| 应用层 | 业务模块 | 8 大 MRO 应用 + 基础管理底座 |

### 技术选型

| 技术领域 | 选型 | 备选 | 选择理由 |
|---------|------|------|----------|
| IoT 接入 | EMQX (MQTT Broker) | Mosquitto | 集群能力强，百万级连接 |
| 消息队列 | Kafka | RabbitMQ | 高吞吐事件流，IoT 场景适配 |
| 时序数据库 | InfluxDB 3.0 | TDengine | 生态成熟，InfluxQL 查询便捷 |
| AI 推理 | vLLM + RAG Pipeline | Ollama | 高性能批量推理，生产级部署 |
| 向量检索 | Elasticsearch dense_vector | Milvus | 复用现有 ES 集群，减少运维组件 |
| 3D 引擎(Web) | Three.js | Babylon.js | 社区大，与 Vue 3 集成案例多 |
| 3D 引擎(VR) | Unity | Unreal | 航空维修培训场景案例丰富 |
| AR SDK | 厂商 SDK (依 AR 眼镜品牌) | — | 需与硬件选型联动确定 |
| RFID 中间件 | 自研适配层 | 商用 RFID 中间件 | 需对接多品牌读写器，自研灵活性高 |
| 区块链 | Hyperledger Fabric (许可链) | 长安链 | 企业级许可链，工卡签署上链 |
| 边缘计算 | NVIDIA Jetson + K3s | 华为 Atlas | AI 推理边缘部署，机库端低延迟 |

### 通信协议

| 场景 | 协议 | 要求 |
|------|------|------|
| IoT 设备→平台 | MQTT 3.1.1 / 5.0 | QoS 1，TLS 加密 |
| RFID 读写器→中间件 | TCP + 自定义帧协议 / LLRP | 实时性 < 100ms |
| AR 视频协作 | WebRTC + SFU | 延迟 < 500ms |
| 浏览器→后端 | HTTPS REST + WebSocket | JWT 鉴权 |

## Consequences

### 正面
- 四层解耦，各层独立演进
- 边缘计算解决机库环境低延迟 AI 推理需求
- MQTT + Kafka 组合应对 IoT 高并发场景

### 负面
- 技术栈复杂度大幅增加，团队需补充 IoT/AI/3D 技能
- 多硬件品牌适配工作量大（AR 眼镜、RFID 读写器、智能工具柜）
- 边缘节点运维需额外投入

## Alternatives Considered

| 方案 | 拒绝理由 |
|------|----------|
| 纯云端架构 | 机库网络不稳定时 AI 推理中断，无法满足实时性要求 |
| 全私有化部署 | 初期投入过大，不利于快速迭代验证 |
| 采购商用 IoT 平台 | 定制化程度不足，与 RFID 工具柜等专用设备集成困难 |

## References

- 关联 Charter: `CHARTER.md` §4 技术栈
- 源文档: `智慧机务模块/11-技术架构与实施计划.md`
- 关联 ADR: ADR-004 (数据架构)