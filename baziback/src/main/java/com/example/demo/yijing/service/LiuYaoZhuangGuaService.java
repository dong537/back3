package com.example.demo.yijing.service;

import com.example.demo.entity.TbHexagram;
import com.example.demo.entity.TbHexagramYao;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.HexagramMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 六爻装卦计算服务
 * 负责纳甲、六亲、世应、六神、空亡等装卦计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiuYaoZhuangGuaService {

    private final HexagramMapper hexagramMapper;

    // 天干列表
    private static final List<String> TIAN_GAN = Arrays.asList("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸");
    // 地支列表
    private static final List<String> DI_ZHI = Arrays.asList("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥");
    

    /**
     * 完整装卦计算
     * @param hexagramId 卦ID
     * @param divinationDate 占卜日期
     * @return 装卦结果
     */
    public ZhuangGuaResult calculateZhuangGua(Integer hexagramId, LocalDate divinationDate) {
        // 获取卦信息
        TbHexagram hexagram = hexagramMapper.findById(hexagramId);
        if (hexagram == null) {
            throw new BusinessException("卦象不存在: " + hexagramId);
        }

        // 获取所有爻
        List<TbHexagramYao> yaos = hexagramMapper.findYaosByHexagramId(hexagramId);
        if (yaos == null || yaos.size() != 6) {
            throw new BusinessException("卦爻数据不完整");
        }

        // 按爻位排序（1-6）
        yaos.sort(Comparator.comparing(TbHexagramYao::getYaoPosition));

        // 计算日期干支
        GanZhi dateGanZhi = calculateDateGanZhi(divinationDate);
        
        // 计算月建和日辰
        String yueJian = getMonthBranch(divinationDate.getMonthValue());
        String riChen = dateGanZhi.getDiZhi();
        
        // 计算空亡
        Set<String> kongWang = calculateKongWang(dateGanZhi);
        
        // 计算六神
        List<String> liuShen = calculateLiuShen(dateGanZhi.getTianGan());
        
        // 构建装卦结果
        List<YaoZhuangGuaInfo> yaoInfos = new ArrayList<>();
        for (int i = 0; i < yaos.size(); i++) {
            TbHexagramYao yao = yaos.get(i);
            YaoZhuangGuaInfo info = new YaoZhuangGuaInfo();
            info.setYaoPosition(yao.getYaoPosition());
            info.setYaoType(yao.getYaoType());
            info.setStem(yao.getStem());
            info.setBranch(yao.getBranch());
            info.setLiuQin(yao.getLiuQin());
            info.setIsShi(yao.getIsShi() == 1);
            info.setIsYing(yao.getIsYing() == 1);
            info.setLiuShen(liuShen.get(i));
            info.setIsKongWang(kongWang.contains(yao.getBranch()));
            
            // 计算旺衰（简化版：根据月建和日辰）
            info.setWangShuai(calculateWangShuai(yao.getBranch(), yueJian, riChen, yao.getLiuQin()));
            
            yaoInfos.add(info);
        }

        ZhuangGuaResult result = new ZhuangGuaResult();
        result.setHexagramId(hexagramId);
        result.setHexagramName(hexagram.getName());
        result.setHexagramNameShort(hexagram.getNameShort());
        result.setPalaceNature(hexagram.getPalaceNature());
        result.setDescription(hexagram.getDescription());
        result.setYaos(yaoInfos);
        result.setYueJian(yueJian);
        result.setRiChen(riChen);
        result.setDateGanZhi(dateGanZhi.toString());
        result.setKongWang(new ArrayList<>(kongWang));
        
        return result;
    }

    /**
     * 计算日期干支（简化版，使用固定算法）
     */
    private GanZhi calculateDateGanZhi(LocalDate date) {
        // 使用固定算法计算干支（实际应该使用万年历）
        // 这里使用简化算法：基于1900年1月1日为甲子日
        long days = date.toEpochDay() - LocalDate.of(1900, 1, 1).toEpochDay();
        int ganIndex = (int) (days % 10);
        int zhiIndex = (int) (days % 12);
        
        // 调整到1900年1月1日为甲子
        ganIndex = (ganIndex + 0) % 10;
        zhiIndex = (zhiIndex + 0) % 12;
        
        return new GanZhi(TIAN_GAN.get(ganIndex), DI_ZHI.get(zhiIndex));
    }

    /**
     * 获取月份地支
     */
    private String getMonthBranch(int month) {
        // 正月建寅，二月建卯...
        int[] monthBranches = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1}; // 1-12月对应地支索引
        return DI_ZHI.get(monthBranches[month - 1]);
    }

    /**
     * 计算空亡
     * 根据日柱计算旬空
     */
    private Set<String> calculateKongWang(GanZhi dateGanZhi) {
        Set<String> kongWang = new HashSet<>();
        
        // 甲子旬空戌亥，甲戌旬空申酉，甲申旬空午未，甲午旬空辰巳，甲辰旬空寅卯，甲寅旬空子丑
        String tianGan = dateGanZhi.getTianGan();
        String diZhi = dateGanZhi.getDiZhi();
        
        int ganIndex = TIAN_GAN.indexOf(tianGan);
        int zhiIndex = DI_ZHI.indexOf(diZhi);
        
        // 计算旬首（甲子、甲戌、甲申、甲午、甲辰、甲寅）
        int xunShou = (ganIndex - zhiIndex + 12) % 12;
        if (xunShou < 0) xunShou += 12;
        
        // 根据旬首确定空亡地支
        int[] kongWangMap = {10, 11, 8, 9, 6, 7, 4, 5, 2, 3, 0, 1}; // 对应旬首的空亡地支索引
        int kongWangIndex1 = kongWangMap[xunShou];
        int kongWangIndex2 = (kongWangIndex1 + 1) % 12;
        
        kongWang.add(DI_ZHI.get(kongWangIndex1));
        kongWang.add(DI_ZHI.get(kongWangIndex2));
        
        return kongWang;
    }

    /**
     * 计算六神
     * 甲乙起青龙，丙丁起朱雀，戊日起勾陈，己日起腾蛇，庚辛起白虎，壬癸起玄武
     */
    private List<String> calculateLiuShen(String tianGan) {
        List<String> liuShen = Arrays.asList("青龙", "朱雀", "勾陈", "腾蛇", "白虎", "玄武");
        
        int ganIndex = TIAN_GAN.indexOf(tianGan);
        int startIndex = 0;
        
        if (ganIndex == 0 || ganIndex == 1) { // 甲乙
            startIndex = 0; // 青龙
        } else if (ganIndex == 2 || ganIndex == 3) { // 丙丁
            startIndex = 1; // 朱雀
        } else if (ganIndex == 4) { // 戊
            startIndex = 2; // 勾陈
        } else if (ganIndex == 5) { // 己
            startIndex = 3; // 腾蛇
        } else if (ganIndex == 6 || ganIndex == 7) { // 庚辛
            startIndex = 4; // 白虎
        } else if (ganIndex == 8 || ganIndex == 9) { // 壬癸
            startIndex = 5; // 玄武
        }
        
        // 从初爻开始排布（初爻对应startIndex）
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            result.add(liuShen.get((startIndex + i) % 6));
        }
        
        return result;
    }

    /**
     * 计算旺衰（简化版）
     */
    private String calculateWangShuai(String branch, String yueJian, String riChen, String liuQin) {
        // 简化判断：得月建或日辰为旺，被克为衰
        if (branch.equals(yueJian) || branch.equals(riChen)) {
            return "旺";
        }
        // 可以根据五行生克关系进一步细化
        return "平";
    }

    /**
     * 装卦结果
     */
    public static class ZhuangGuaResult {
        private Integer hexagramId;
        private String hexagramName;
        private String hexagramNameShort;
        private String palaceNature;
        private String description;
        private List<YaoZhuangGuaInfo> yaos;
        private String yueJian;
        private String riChen;
        private String dateGanZhi;
        private List<String> kongWang;

        // Getters and Setters
        public Integer getHexagramId() { return hexagramId; }
        public void setHexagramId(Integer hexagramId) { this.hexagramId = hexagramId; }
        public String getHexagramName() { return hexagramName; }
        public void setHexagramName(String hexagramName) { this.hexagramName = hexagramName; }
        public String getHexagramNameShort() { return hexagramNameShort; }
        public void setHexagramNameShort(String hexagramNameShort) { this.hexagramNameShort = hexagramNameShort; }
        public String getPalaceNature() { return palaceNature; }
        public void setPalaceNature(String palaceNature) { this.palaceNature = palaceNature; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<YaoZhuangGuaInfo> getYaos() { return yaos; }
        public void setYaos(List<YaoZhuangGuaInfo> yaos) { this.yaos = yaos; }
        public String getYueJian() { return yueJian; }
        public void setYueJian(String yueJian) { this.yueJian = yueJian; }
        public String getRiChen() { return riChen; }
        public void setRiChen(String riChen) { this.riChen = riChen; }
        public String getDateGanZhi() { return dateGanZhi; }
        public void setDateGanZhi(String dateGanZhi) { this.dateGanZhi = dateGanZhi; }
        public List<String> getKongWang() { return kongWang; }
        public void setKongWang(List<String> kongWang) { this.kongWang = kongWang; }
    }

    /**
     * 爻装卦信息
     */
    public static class YaoZhuangGuaInfo {
        private Integer yaoPosition;
        private String yaoType;
        private String stem;
        private String branch;
        private String liuQin;
        private Boolean isShi;
        private Boolean isYing;
        private String liuShen;
        private Boolean isKongWang;
        private String wangShuai;

        // Getters and Setters
        public Integer getYaoPosition() { return yaoPosition; }
        public void setYaoPosition(Integer yaoPosition) { this.yaoPosition = yaoPosition; }
        public String getYaoType() { return yaoType; }
        public void setYaoType(String yaoType) { this.yaoType = yaoType; }
        public String getStem() { return stem; }
        public void setStem(String stem) { this.stem = stem; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getLiuQin() { return liuQin; }
        public void setLiuQin(String liuQin) { this.liuQin = liuQin; }
        public Boolean getIsShi() { return isShi; }
        public void setIsShi(Boolean isShi) { this.isShi = isShi; }
        public Boolean getIsYing() { return isYing; }
        public void setIsYing(Boolean isYing) { this.isYing = isYing; }
        public String getLiuShen() { return liuShen; }
        public void setLiuShen(String liuShen) { this.liuShen = liuShen; }
        public Boolean getIsKongWang() { return isKongWang; }
        public void setIsKongWang(Boolean isKongWang) { this.isKongWang = isKongWang; }
        public String getWangShuai() { return wangShuai; }
        public void setWangShuai(String wangShuai) { this.wangShuai = wangShuai; }
    }

    /**
     * 干支类
     */
    private static class GanZhi {
        private String tianGan;
        private String diZhi;

        public GanZhi(String tianGan, String diZhi) {
            this.tianGan = tianGan;
            this.diZhi = diZhi;
        }

        public String getTianGan() { return tianGan; }
        public String getDiZhi() { return diZhi; }
        
        @Override
        public String toString() {
            return tianGan + diZhi;
        }
    }
}
