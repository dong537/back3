package com.example.demo.dto.response;


import lombok.Data;

@Data
public class McpInitializeResponse {
    private String jsonrpc = "2.0"; // JSON-RPC协议版本
    private Long id;             // 与请求ID一致，用于关联
    private Result result;          // 初始化结果

    @Data
    public static class Result {
        private String protocolVersion; // 服务端支持的协议版本
        private Capabilities capabilities; // 服务端能力集
        private ServerInfo serverInfo;     // 服务端信息
        private String instructions;       // 初始化说明（可选）

        @Data
        public static class Capabilities {
            private Resources resources;
            private Tools tools;

            @Data
            public static class Resources {
                private boolean subscribe; // 资源订阅能力
            }

            @Data
            public static class Tools {
                private boolean listChanged; // 工具列表变更能力
            }
        }

        @Data
        public static class ServerInfo {
            private String name;    // 服务端名称
            private String version; // 服务端版本
        }
    }
}