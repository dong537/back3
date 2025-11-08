package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpInitializeRequest {
    private String jsonrpc = "2.0";
    private Integer id = 1;
    private String method = "initialize"; // 方法名：初始化
    // 关键：添加params字段，包含必要参数
    private Params params = new Params();

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    @Data
    public static class Params {
        // 1. 协议版本（必须，需与服务器兼容，如"2024-11-05"）
        private String protocolVersion = "2024-11-05";
        // 2. 客户端信息（可选，但建议添加）
        private ClientInfo clientInfo = new ClientInfo();

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public ClientInfo getClientInfo() {
            return clientInfo;
        }

        public void setClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
        }

        @Data
        public static class ClientInfo {
            private String name = "McpDemoClient";
            private String version = "1.0.0";

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }
        }
    }
}