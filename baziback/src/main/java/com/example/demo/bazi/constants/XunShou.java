package com.example.demo.bazi.constants;

/**
 * 旬首常量类
 * 旬首是指每个甲子旬的开头，共有6个旬首：甲子、甲戌、甲申、甲午、甲辰、甲寅
 */
public class XunShou {
    
    /**
     * 根据干支获取旬首
     * @param ganZhi 干支，如"甲子"、"乙丑"等
     * @return 旬首，如"甲子"、"甲戌"等
     */
    public static String getXunShou(String ganZhi) {
        if (ganZhi == null || ganZhi.length() != 2) {
            return "";
        }
        
        String tianGan = ganZhi.substring(0, 1);
        String diZhi = ganZhi.substring(1, 2);
        
        TianGan gan = TianGan.fromName(tianGan);
        DiZhi zhi = DiZhi.fromName(diZhi);
        
        // 计算该干支在60甲子中的位置
        int ganIndex = gan.getIndex();
        int zhiIndex = zhi.getIndex();
        
        // 计算距离最近的甲子旬首
        // 甲子旬：甲子(1,1)到癸酉(10,10)
        // 甲戌旬：甲戌(1,11)到癸未(10,8)
        // 甲申旬：甲申(1,9)到癸巳(10,6)
        // 甲午旬：甲午(1,7)到癸卯(10,4)
        // 甲辰旬：甲辰(1,5)到癸丑(10,2)
        // 甲寅旬：甲寅(1,3)到癸亥(10,12)
        
        // 计算干支差值
        int diff = (ganIndex - zhiIndex + 12) % 12;
        
        // 根据差值确定旬首地支
        int xunShouZhiIndex;
        if (diff == 0) {
            // 正好是甲子旬
            xunShouZhiIndex = zhiIndex;
        } else {
            // 向前找到旬首
            xunShouZhiIndex = (zhiIndex - diff + 12) % 12;
            if (xunShouZhiIndex == 0) xunShouZhiIndex = 12;
        }
        
        // 确定旬首天干（都是甲）
        TianGan xunShouGan = TianGan.fromIndex(1); // 甲
        DiZhi xunShouZhi = DiZhi.fromIndex(xunShouZhiIndex);
        
        return xunShouGan.getName() + xunShouZhi.getName();
    }
}
