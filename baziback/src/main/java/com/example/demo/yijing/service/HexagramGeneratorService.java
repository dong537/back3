package com.example.demo.yijing.service;

import com.example.demo.yijing.model.Hexagram;
import com.example.demo.yijing.model.HexagramResult;
import com.example.demo.yijing.repository.HexagramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HexagramGeneratorService {

    private final HexagramRepository hexagramRepository;
    private static final Random RANDOM = new Random();

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
        int upperNum = RANDOM.nextInt(8) + 1;
        int lowerNum = RANDOM.nextInt(8) + 1;
        int changingLinePos = RANDOM.nextInt(6) + 1;

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

        for (int i = 1; i <= 6; i++) {
            int coin1 = RANDOM.nextInt(2);
            int coin2 = RANDOM.nextInt(2);
            int coin3 = RANDOM.nextInt(2);
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
                .orElse(hexagramRepository.findById(1).get());

        Hexagram changed = null;
        if (!changingLines.isEmpty()) {
            List<Integer> changedLines = new ArrayList<>(lines);
            for (int pos : changingLines) {
                changedLines.set(pos - 1, 1 - changedLines.get(pos - 1));
            }
            String changedBinary = linesToBinary(changedLines);
            changed = hexagramRepository.findByBinary(changedBinary).orElse(null);
        }

        return HexagramResult.builder()
                .timestamp(timestamp)
                .method("coin")
                .question(question)
                .original(original)
                .changingLines(changingLines.isEmpty() ? null : changingLines)
                .changed(changed)
                .interpretationHint(generateHint(original, changed, changingLines))
                .build();
    }

    private HexagramResult generateByPlumBlossom(String question, String timestamp, String seed) {
        LocalDateTime now = LocalDateTime.now();
        int baseNum = now.getYear() + now.getMonthValue() + now.getDayOfMonth() + now.getHour();
        
        if (seed != null && !seed.isEmpty()) {
            try {
                baseNum += Integer.parseInt(seed);
            } catch (NumberFormatException ignored) {
            }
        }

        int upperNum = (baseNum / 10) % 8;
        if (upperNum == 0) upperNum = 8;
        
        int lowerNum = baseNum % 8;
        if (lowerNum == 0) lowerNum = 8;
        
        int changingLinePos = (baseNum + now.getMinute()) % 6;
        if (changingLinePos == 0) changingLinePos = 6;

        return buildHexagramResult(question, timestamp, "plum_blossom", upperNum, lowerNum, changingLinePos);
    }

    private HexagramResult buildHexagramResult(String question, String timestamp, String method, 
                                               int upperNum, int lowerNum, int changingLinePos) {
        String upperTrigram = getTrigramBinary(upperNum);
        String lowerTrigram = getTrigramBinary(lowerNum);
        String originalBinary = lowerTrigram + upperTrigram;

        Hexagram original = hexagramRepository.findByBinary(originalBinary)
                .orElse(hexagramRepository.findById(1).get());

        List<Integer> changingLines = Collections.singletonList(changingLinePos);

        List<Integer> lines = binaryToLines(originalBinary);
        lines.set(changingLinePos - 1, 1 - lines.get(changingLinePos - 1));
        String changedBinary = linesToBinary(lines);

        Hexagram changed = hexagramRepository.findByBinary(changedBinary).orElse(null);

        return HexagramResult.builder()
                .timestamp(timestamp)
                .method(method)
                .question(question)
                .original(original)
                .changingLines(changingLines)
                .changed(changed)
                .interpretationHint(generateHint(original, changed, changingLines))
                .build();
    }

    private String getTrigramBinary(int num) {
        Map<Integer, String> trigramMap = Map.of(
                1, "111",
                2, "000",
                3, "001",
                4, "100",
                5, "010",
                6, "101",
                7, "110",
                8, "011"
        );
        return trigramMap.getOrDefault(num, "111");
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
