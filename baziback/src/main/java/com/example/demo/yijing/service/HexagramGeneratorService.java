package com.example.demo.yijing.service;

import com.example.demo.entity.TbHexagramYao;
import com.example.demo.yijing.model.Hexagram;
import com.example.demo.yijing.model.HexagramResult;
import com.example.demo.yijing.repository.HexagramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class HexagramGeneratorService {

    private final HexagramRepository hexagramRepository;

    public HexagramResult generateHexagram(String question, String method, String seed) {
        log.info("生成卦象 - 问题: {}, 方法: {}, 种子: {}", question, method, seed);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        HexagramResult result;
        switch (method.toLowerCase()) {
            case "time":
                result = generateByTime(question, timestamp);
                break;
            case "random":
                result = generateByRandom(question, timestamp);
                break;
            case "number":
                result = generateByNumber(question, timestamp, seed);
                break;
            case "coin":
                result = generateByCoin(question, timestamp);
                break;
            case "plum_blossom":
                result = generateByPlumBlossom(question, timestamp, seed);
                break;
            default:
                result = generateByRandom(question, timestamp);
        }

        return result;
    }

    private HexagramResult generateByTime(String question, String timestamp) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();

        int upperNum = (year + month + day) % 8;
        if (upperNum == 0) upperNum = 8;

        int lowerNum = (year + month + day + hour) % 8;
        if (lowerNum == 0) lowerNum = 8;

        int changingLinePos = (year + month + day + hour + minute) % 6;
        if (changingLinePos == 0) changingLinePos = 6;

        return buildHexagramResult(question, timestamp, "time", upperNum, lowerNum, changingLinePos);
    }

    private HexagramResult generateByRandom(String question, String timestamp) {
        // 使用ThreadLocalRandom确保每次调用都生成不同的随机数
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int upperNum = random.nextInt(1, 9);  // 1-8
        int lowerNum = random.nextInt(1, 9);  // 1-8
        int changingLinePos = random.nextInt(1, 7);  // 1-6
        
        log.debug("随机起卦: 上卦数字={}, 下卦数字={}, 动爻位置={}", upperNum, lowerNum, changingLinePos);
        return buildHexagramResult(question, timestamp, "random", upperNum, lowerNum, changingLinePos);
    }

    private HexagramResult generateByNumber(String question, String timestamp, String seed) {
        if (seed == null || seed.isEmpty()) {
            return generateByRandom(question, timestamp);
        }

        try {
            int seedNum = Integer.parseInt(seed);
            int upperNum = (seedNum / 10) % 8;
            if (upperNum == 0) upperNum = 8;
            
            int lowerNum = seedNum % 8;
            if (lowerNum == 0) lowerNum = 8;
            
            int changingLinePos = seedNum % 6;
            if (changingLinePos == 0) changingLinePos = 6;

            return buildHexagramResult(question, timestamp, "number", upperNum, lowerNum, changingLinePos);
        } catch (NumberFormatException e) {
            log.warn("无效的数字种子: {}, 使用随机方式", seed);
            return generateByRandom(question, timestamp);
        }
    }

    private HexagramResult generateByCoin(String question, String timestamp) {
        List<Integer> lines = new ArrayList<>();
        List<Integer> changingLines = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 1; i <= 6; i++) {
            int coin1 = random.nextInt(2);
            int coin2 = random.nextInt(2);
            int coin3 = random.nextInt(2);
            int sum = coin1 + coin2 + coin3;

            if (sum == 0) {
                lines.add(0);
                changingLines.add(i);
            } else if (sum == 3) {
                lines.add(1);
                changingLines.add(i);
            } else if (sum == 1) {
                lines.add(0);
            } else {
                lines.add(1);
            }
        }

        String binary = linesToBinary(lines);
        Hexagram original = hexagramRepository.findByBinary(binary)
                .orElseGet(() -> {
                    log.warn("硬币起卦未找到二进制 {} 对应的卦象", binary);
                    // 尝试从二进制解析上下卦
                    if (binary != null && binary.length() == 6) {
                        String upperBinary = binary.substring(0, 3);
                        String lowerBinary = binary.substring(3, 6);
                        String upperGua = getTrigramName(upperBinary);
                        String lowerGua = getTrigramName(lowerBinary);
                        if (upperGua != null && lowerGua != null) {
                            return hexagramRepository.findByUpperAndLower(upperGua, lowerGua)
                                    .orElseGet(() -> {
                                        log.error("硬币起卦通过上下卦名称也找不到卦象: {}上{}下", upperGua, lowerGua);
                                        return hexagramRepository.findById(1).orElseThrow(() -> 
                                                new RuntimeException("数据库中没有卦象数据"));
                                    });
                        }
                    }
                    return hexagramRepository.findById(1).orElseThrow(() -> 
                            new RuntimeException("数据库中没有卦象数据"));
                });

        Hexagram changed = null;
        if (!changingLines.isEmpty()) {
            List<Integer> changedLines = new ArrayList<>(lines);
            for (int pos : changingLines) {
                changedLines.set(pos - 1, 1 - changedLines.get(pos - 1));
            }
            String changedBinary = linesToBinary(changedLines);
            changed = hexagramRepository.findByBinary(changedBinary)
                    .orElseGet(() -> {
                        // 如果二进制查找失败，尝试通过上下卦名称查找
                        if (changedBinary != null && changedBinary.length() == 6) {
                            String upperBinary = changedBinary.substring(0, 3);
                            String lowerBinary = changedBinary.substring(3, 6);
                            String upperGua = getTrigramName(upperBinary);
                            String lowerGua = getTrigramName(lowerBinary);
                            if (upperGua != null && lowerGua != null) {
                                return hexagramRepository.findByUpperAndLower(upperGua, lowerGua).orElse(null);
                            }
                        }
                        return null;
                    });
        }

        // 获取本卦和变卦的爻信息
        List<TbHexagramYao> originalYaos = original != null ? 
                hexagramRepository.getHexagramYaos(original.getId()) : Collections.emptyList();
        List<TbHexagramYao> changedYaos = changed != null ? 
                hexagramRepository.getHexagramYaos(changed.getId()) : Collections.emptyList();
        
        return HexagramResult.builder()
                .timestamp(timestamp)
                .method("coin")
                .question(question)
                .original(original)
                .changingLines(changingLines.isEmpty() ? null : changingLines)
                .changed(changed)
                .originalYaos(originalYaos)
                .changedYaos(changedYaos)
                .interpretationHint(generateHint(original, changed, changingLines))
                .build();
    }

    private HexagramResult generateByPlumBlossom(String question, String timestamp, String seed) {
        LocalDateTime now = LocalDateTime.now();
        // 梅花易数时间起卦法：年+月+日为上卦，年+月+日+时为下卦
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        
        int baseNum = year + month + day;
        if (seed != null && !seed.isEmpty()) {
            try {
                baseNum += Integer.parseInt(seed);
            } catch (NumberFormatException ignored) {
            }
        }

        // 上卦 = (年 + 月 + 日) ÷ 8，取余数
        int upperNum = baseNum % 8;
        if (upperNum == 0) upperNum = 8;
        
        // 下卦 = (年 + 月 + 日 + 时) ÷ 8，取余数
        int lowerNum = (baseNum + hour) % 8;
        if (lowerNum == 0) lowerNum = 8;
        
        // 动爻 = (年 + 月 + 日 + 时) ÷ 6，取余数
        int changingLinePos = (baseNum + hour) % 6;
        if (changingLinePos == 0) changingLinePos = 6;

        return buildHexagramResult(question, timestamp, "plum_blossom", upperNum, lowerNum, changingLinePos);
    }

    private HexagramResult buildHexagramResult(String question, String timestamp, String method, 
                                               int upperNum, int lowerNum, int changingLinePos) {
        String upperTrigram = getTrigramBinary(upperNum);
        String lowerTrigram = getTrigramBinary(lowerNum);
        // 六爻二进制通常按 1-6 爻（从下到上）拼接；下卦为初爻-三爻，上卦为四爻-六爻
        String originalBinary = lowerTrigram + upperTrigram;

        Hexagram original = hexagramRepository.findByBinary(originalBinary)
                .orElseGet(() -> {
                    log.warn("未找到二进制 {} 对应的卦象，上卦: {}, 下卦: {}, upperNum: {}, lowerNum: {}", 
                            originalBinary, upperTrigram, lowerTrigram, upperNum, lowerNum);
                    // 尝试直接通过上下卦名称查找
                    String upperGua = getTrigramName(upperTrigram);
                    String lowerGua = getTrigramName(lowerTrigram);
                    if (upperGua != null && lowerGua != null) {
                        return hexagramRepository.findByUpperAndLower(upperGua, lowerGua)
                                .orElseGet(() -> {
                                    log.error("通过上下卦名称也找不到卦象: {}上{}下", upperGua, lowerGua);
                                    return hexagramRepository.findById(1).orElseThrow(() -> 
                                            new RuntimeException("数据库中没有卦象数据"));
                                });
                    }
                    return hexagramRepository.findById(1).orElseThrow(() -> 
                            new RuntimeException("数据库中没有卦象数据"));
                });

        List<Integer> changingLines = Collections.singletonList(changingLinePos);

        List<Integer> lines = binaryToLines(originalBinary);
        lines.set(changingLinePos - 1, 1 - lines.get(changingLinePos - 1));
        String changedBinary = linesToBinary(lines);

        Hexagram changed = hexagramRepository.findByBinary(changedBinary).orElse(null);

        // 获取本卦和变卦的爻信息
        List<TbHexagramYao> originalYaos = original != null ? 
                hexagramRepository.getHexagramYaos(original.getId()) : Collections.emptyList();
        List<TbHexagramYao> changedYaos = changed != null ? 
                hexagramRepository.getHexagramYaos(changed.getId()) : Collections.emptyList();
        
        return HexagramResult.builder()
                .timestamp(timestamp)
                .method(method)
                .question(question)
                .original(original)
                .changingLines(changingLines)
                .changed(changed)
                .originalYaos(originalYaos)
                .changedYaos(changedYaos)
                .interpretationHint(generateHint(original, changed, changingLines))
                .build();
    }

    /**
     * 先天八卦数（梅花易数常用）：乾1 兑2 离3 震4 巽5 坎6 艮7 坤8
     * 映射到单卦三爻二进制：
     * 乾111 兑011 离101 震001 巽110 坎010 艮100 坤000
     */
    private String getTrigramBinary(int num) {
        Map<Integer, String> trigramMap = Map.of(
                1, "111",  // 乾
                2, "011",  // 兑
                3, "101",  // 离
                4, "001",  // 震
                5, "110",  // 巽
                6, "010",  // 坎
                7, "100",  // 艮
                8, "000"   // 坤
        );
        return trigramMap.getOrDefault(num, "111");
    }

    /**
     * 从二进制字符串获取单卦名称
     */
    private String getTrigramName(String binary) {
        Map<String, String> binaryToName = Map.of(
                "111", "乾",
                "011", "兑",
                "101", "离",
                "001", "震",
                "110", "巽",
                "010", "坎",
                "100", "艮",
                "000", "坤"
        );
        return binaryToName.get(binary);
    }

    private List<Integer> binaryToLines(String binary) {
        List<Integer> lines = new ArrayList<>();
        for (char c : binary.toCharArray()) {
            lines.add(c == '1' ? 1 : 0);
        }
        return lines;
    }

    private String linesToBinary(List<Integer> lines) {
        StringBuilder sb = new StringBuilder();
        for (int line : lines) {
            sb.append(line);
        }
        return sb.toString();
    }

    private String generateHint(Hexagram original, Hexagram changed, List<Integer> changingLines) {
        if (changed == null || changingLines == null || changingLines.isEmpty()) {
            return "本卦为" + original.getChinese() + "，无变爻，以本卦卦辞和卦象为主进行解读。";
        }

        if (changingLines.size() == 1) {
            int linePos = changingLines.get(0);
            return String.format("本卦为%s，第%d爻发动，变卦为%s。以本卦第%d爻爻辞和变卦卦象综合解读。",
                    original.getChinese(), linePos, changed.getChinese(), linePos);
        }

        return String.format("本卦为%s，有%d个变爻，变卦为%s。以本卦和变卦综合解读。",
                original.getChinese(), changingLines.size(), changed.getChinese());
    }

    public List<Hexagram> getAllHexagrams() {
        return hexagramRepository.findAll();
    }

    public Optional<Hexagram> getHexagramById(Integer id) {
        return hexagramRepository.findById(id);
    }

    public Optional<Hexagram> getHexagramByName(String name) {
        return hexagramRepository.findByName(name);
    }
}
