package com.example.demo.mapper;

import com.example.demo.entity.TokenUsage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TokenUsageMapper {

    @Insert("INSERT INTO tb_token_usage (agent_id, application_id, user_id, tokens_used, " +
            "input_tokens, output_tokens, started_at, ended_at, model_name, request_id, metadata) " +
            "VALUES (#{agentId}, #{applicationId}, #{userId}, #{tokensUsed}, " +
            "#{inputTokens}, #{outputTokens}, #{startedAt}, #{endedAt}, #{modelName}, #{requestId}, #{metadata})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TokenUsage tokenUsage);

    @Select("SELECT * FROM tb_token_usage WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<TokenUsage> findByUserId(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT * FROM tb_token_usage WHERE agent_id = #{agentId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<TokenUsage> findByAgentId(@Param("agentId") String agentId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COALESCE(SUM(tokens_used), 0) FROM tb_token_usage WHERE user_id = #{userId}")
    Long sumTokensByUserId(Long userId);

    @Select("SELECT COALESCE(SUM(tokens_used), 0) FROM tb_token_usage WHERE agent_id = #{agentId}")
    Long sumTokensByAgentId(String agentId);

    @Select("SELECT COUNT(*) FROM tb_token_usage WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
