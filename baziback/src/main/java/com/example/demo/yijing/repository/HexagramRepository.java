package com.example.demo.yijing.repository;

import com.example.demo.entity.TbHexagram;
import com.example.demo.entity.TbHexagramYao;
import com.example.demo.mapper.HexagramMapper;
import com.example.demo.yijing.model.Hexagram;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HexagramRepository {

    private final HexagramMapper hexagramMapper;
    
    // 单卦到二进制的映射
    private static final Map<String, String> TRIGRAM_BINARY_MAP = Map.of(
            "乾", "111",
            "坤", "000",
            "震", "001",
            "巽", "110",
            "坎", "010",
            "离", "101",
            "艮", "100",
            "兑", "011"
    );

    public List<Hexagram> findAll() {
        List<TbHexagram> tbHexagrams = hexagramMapper.findAll();
        return tbHexagrams.stream()
                .map(this::convertToHexagram)
                .collect(Collectors.toList());
    }

    public Optional<Hexagram> findById(Integer id) {
        TbHexagram tbHexagram = hexagramMapper.findById(id);
        if (tbHexagram == null) {
            return Optional.empty();
        }
        return Optional.of(convertToHexagram(tbHexagram));
    }

    public Optional<Hexagram> findByBinary(String binary) {
        // 从二进制字符串解析上下卦
        // 注意：二进制格式为 下卦(1-3爻) + 上卦(4-6爻)，即前3位是下卦，后3位是上卦
        if (binary == null || binary.length() != 6) {
            log.warn("二进制字符串无效: {}", binary);
            return Optional.empty();
        }
        
        // 前3位是下卦（初爻到三爻），后3位是上卦（四爻到六爻）
        String lowerBinary = binary.substring(0, 3);
        String upperBinary = binary.substring(3, 6);
        
        String lowerGua = getTrigramFromBinary(lowerBinary);
        String upperGua = getTrigramFromBinary(upperBinary);
        
        if (upperGua == null || lowerGua == null) {
            log.warn("无法从二进制解析单卦: 上卦二进制={}, 下卦二进制={}", upperBinary, lowerBinary);
            return Optional.empty();
        }
        
        TbHexagram tbHexagram = hexagramMapper.findByUpperAndLower(upperGua, lowerGua);
        if (tbHexagram == null) {
            log.warn("数据库中未找到卦象: 上卦={}, 下卦={}, 二进制={}", upperGua, lowerGua, binary);
            return Optional.empty();
        }
        
        log.debug("成功找到卦象: ID={}, 上卦={}, 下卦={}, 二进制={}", 
                tbHexagram.getId(), upperGua, lowerGua, binary);
        return Optional.of(convertToHexagram(tbHexagram));
    }

    public Optional<Hexagram> findByName(String name) {
        TbHexagram tbHexagram = hexagramMapper.findByName(name);
        if (tbHexagram == null) {
            return Optional.empty();
        }
        return Optional.of(convertToHexagram(tbHexagram));
    }
    
    /**
     * 根据上下卦名称查找卦象
     */
    public Optional<Hexagram> findByUpperAndLower(String upperGua, String lowerGua) {
        TbHexagram tbHexagram = hexagramMapper.findByUpperAndLower(upperGua, lowerGua);
        if (tbHexagram == null) {
            return Optional.empty();
        }
        return Optional.of(convertToHexagram(tbHexagram));
    }

    /**
     * 将数据库实体转换为Hexagram模型
     */
    private Hexagram convertToHexagram(TbHexagram tbHexagram) {
        // 获取该卦的所有爻
        List<TbHexagramYao> yaos = hexagramMapper.findYaosByHexagramId(tbHexagram.getId());
        
        // 生成二进制字符串
        String binary = generateBinary(yaos);
        
        // 从数据库查询卦辞和象辞（按SQL：简体枚举）
        Map<String, Object> judgmentRow = hexagramMapper.findTextDetailByHexagramIdAndType(tbHexagram.getId(), "卦辞");
        Map<String, Object> imageRow = hexagramMapper.findTextDetailByHexagramIdAndType(tbHexagram.getId(), "大象");

        String judgment = judgmentRow == null ? null : (String) judgmentRow.get("content");
        String judgmentExplanation = judgmentRow == null ? null : (String) judgmentRow.get("explanation");
        String image = imageRow == null ? null : (String) imageRow.get("content");
        String imageExplanation = imageRow == null ? null : (String) imageRow.get("explanation");
        
        // 转换为Hexagram模型
        return Hexagram.builder()
                .id(tbHexagram.getId())
                .name(tbHexagram.getNameShort() != null ? tbHexagram.getNameShort() : "")
                .chinese(tbHexagram.getNameShort() != null ? tbHexagram.getNameShort() : "")
                .pinyin("")
                .binary(binary)
                .upper(tbHexagram.getUpperGua())
                .lower(tbHexagram.getLowerGua())
                .symbol(generateSymbol(tbHexagram.getUpperGua(), tbHexagram.getLowerGua()))
                .judgment(judgment != null ? judgment : "")
                .judgmentExplanation(judgmentExplanation != null ? judgmentExplanation : "")
                .image(image != null ? image : "")
                .imageExplanation(imageExplanation != null ? imageExplanation : "")
                .meaning(tbHexagram.getDescription() != null ? tbHexagram.getDescription() : "")
                .keywords(Collections.emptyList())
                .element(tbHexagram.getPalaceNature())
                .season("")
                .direction("")
                .applications(Collections.emptyMap())
                .lines(convertYaosToLines(yaos))
                .build();
    }

    /**
     * 根据爻列表生成二进制字符串（从下往上，1-6爻）
     */
    private String generateBinary(List<TbHexagramYao> yaos) {
        if (yaos == null || yaos.isEmpty()) {
            return "000000";
        }
        
        StringBuilder binary = new StringBuilder();
        // 按爻位排序（1-6）
        yaos.sort(Comparator.comparing(TbHexagramYao::getYaoPosition));
        
        for (TbHexagramYao yao : yaos) {
            // 阳爻为1，阴爻为0
            binary.append("阳".equals(yao.getYaoType()) ? "1" : "0");
        }
        
        return binary.toString();
    }

    /**
     * 生成卦象符号
     */
    private String generateSymbol(String upperGua, String lowerGua) {
        Map<String, String> symbolMap = Map.of(
                "乾", "☰", "坤", "☷", "震", "☳", "巽", "☴",
                "坎", "☵", "离", "☲", "艮", "☶", "兑", "☱"
        );
        return symbolMap.getOrDefault(upperGua, "☰") + symbolMap.getOrDefault(lowerGua, "☰");
    }

    /**
     * 将数据库爻转换为Hexagram.Line
     */
    private List<Hexagram.Line> convertYaosToLines(List<TbHexagramYao> yaos) {
        if (yaos == null || yaos.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取第一个爻的卦ID（所有爻都属于同一个卦）
        Integer hexagramId = yaos.get(0).getHexagramId();
        
        return yaos.stream()
                .sorted(Comparator.comparing(TbHexagramYao::getYaoPosition))
                .map((TbHexagramYao yao) -> {
                    // 查询爻辞（按SQL：简体枚举）
                    Map<String, Object> yaoTextRow = hexagramMapper.findTextDetailByHexagramIdAndYaoPosition(
                            hexagramId, "爻辞", yao.getYaoPosition());
                    String yaoText = yaoTextRow == null ? null : (String) yaoTextRow.get("content");
                    String yaoTextExplanation = yaoTextRow == null ? null : (String) yaoTextRow.get("explanation");
                    
                    return Hexagram.Line.builder()
                            .position(yao.getYaoPosition())
                            .type("阳".equals(yao.getYaoType()) ? "yang" : "yin")
                            .text(yaoText != null ? yaoText : "")
                            .textExplanation(yaoTextExplanation != null ? yaoTextExplanation : "")
                            .meaning("")
                            .stem(yao.getStem())
                            .branch(yao.getBranch())
                            .liuQin(yao.getLiuQin())
                            .isShi(yao.getIsShi() != null && yao.getIsShi() == 1)
                            .isYing(yao.getIsYing() != null && yao.getIsYing() == 1)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 从二进制字符串获取单卦名称
     */
    private String getTrigramFromBinary(String binary) {
        return TRIGRAM_BINARY_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(binary))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取卦的所有爻信息
     */
    public List<TbHexagramYao> getHexagramYaos(Integer hexagramId) {
        return hexagramMapper.findYaosByHexagramId(hexagramId);
    }
}
