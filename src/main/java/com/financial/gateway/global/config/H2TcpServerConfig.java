package com.financial.gateway.global.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.sql.SQLException;

@Configuration
@Profile("daemon")
public class H2TcpServerConfig {

    // 데몬 기동 시 9092 포트로 H2 TCP 리스너를 오픈하여 외부 JVM 프로세스의 메모리 접근 허용
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}