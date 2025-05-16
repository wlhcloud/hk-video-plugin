# 视频插件项目文档

## 项目简介

本项目实现了基于海康设备的视频监控插件，集成了设备预览、流媒体推送、WebSocket 实时视频帧分发等功能。  
通过策略模式管理设备操作命令，支持动态加载配置，并提供了高效的流处理和推送机制。

---

## 主要模块说明

### 1. VideoPluginMain

- 插件主入口，IoT 平台调用接口。
- 初始化设备视频配置和流媒体服务（SMS）及注册服务（CMS）。
- 根据操作类型选择对应策略执行设备操作。

### 2. VideoConfigManager

- 视频插件配置管理器。
- 支持通过 Map 加载动态配置，内部使用 Jackson 和 Spring BeanUtils 进行属性拷贝。

### 3. StrategyFactory

- 策略工厂类，维护操作类型与策略的映射。
- 支持扩展更多设备操作策略（如启动预览、停止预览、查询状态等）。

### 4. WebSocketServer

- 基于 `org.java_websocket` 的 WebSocket 服务实现。
- 支持多客户端连接，使用固定线程池异步发送视频帧数据。
- 新连接时自动推送 FLV 头和关键帧缓存，保证中途接入客户端能立即播放。
- 发送队列支持丢弃旧数据，避免阻塞。

### 5. HandleStreamV2

- 视频流处理核心类，使用 JavaCV（FFmpeg）抓取、编码视频流。
- 将原始流转换为 FLV 格式推送。
- 通过 `Consumer<byte[]>` 回调实现视频帧数据消费（例如推送到 WebSocket）。
- 支持异步流处理和推流状态回调。

### 6. StartPreviewOperation

- 设备预览启动策略实现。
- 根据设备 ID 获取设备信息，启动或获取对应端口的 WebSocket 服务。
- 调用 SMS SDK 启动实时预览，并异步等待启动结果。
- 成功后向客户端返回启动成功信息。

---

## 核心类及接口说明

### `VideoPluginMain`

| 方法          | 功能描述                      |
| ------------- | ----------------------------- |
| `init(params)`| 初始化插件及相关服务          |
| `execute(type, params)` | 根据操作类型执行设备操作策略 |

---

### `StrategyFactory`

| 方法          | 功能描述                      |
| ------------- | ----------------------------- |
| `getStrategy(type)` | 返回对应的设备操作策略实例   |

---

### `WebSocketServer`

| 事件回调      | 功能描述                      |
| ------------- | ----------------------------- |
| `onOpen`      | 新连接建立，发送 FLV 头与关键帧，开启异步发送线程 |
| `onClose`     | 连接关闭，清理资源             |
| `onMessage`   | 收到消息（可扩展指令处理）    |
| `sendToAll`   | 广播视频帧数据到所有连接客户端 |

---

### `HandleStreamV2`

| 方法          | 功能描述                      |
| ------------- | ----------------------------- |
| `processStream(data)` | 传入视频流数据进行处理       |
| `startProcessing()` | 启动视频流抓取及转码线程     |
| `stopProcessing()` | 停止视频处理线程             |

---

## 使用流程简述

1. IoT 平台启动时调用 `VideoPluginMain.init` 初始化插件及流媒体服务。
2. 通过 `execute` 方法传入操作类型（如 `"startPreview"`）及参数，执行对应策略。
3. `StartPreviewOperation` 调用 SMS SDK 启动设备预览，创建 WebSocket 服务转发视频流。
4. `HandleStreamV2` 实时处理并编码视频流，推送 FLV 数据至 WebSocket 客户端。
5. WebSocket 服务器维护连接队列，实现多客户端实时播放。

---

## 依赖说明

- JavaCV / FFmpeg（视频处理与转码）
- org.java_websocket（WebSocket 实现）
- Jackson（JSON 映射）
- Spring BeanUtils（对象属性拷贝）

---

## 注意事项

- WebSocket 发送队列最大容量为 1000，避免阻塞时丢弃旧帧。
- FLV 头和关键帧缓存机制保证中途接入客户端可快速开始播放。
- 各模块均设计为异步处理，避免阻塞主线程。
- 设备信息管理依赖于外部设备列表工具 `DeviceListUtil`，需保证设备注册完整。

---

如果需要，我可以帮你继续补充详细接口定义或示例调用代码说明。
