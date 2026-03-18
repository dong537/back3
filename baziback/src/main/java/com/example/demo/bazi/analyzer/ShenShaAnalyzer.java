package com.example.demo.bazi.analyzer;

import com.example.demo.bazi.constants.*;
import com.example.demo.bazi.model.*;
import java.util.*;

/**
 * 神煞分析器
 */
public class ShenShaAnalyzer {
    
    /**
     * 分析四柱神煞
     */
    public Map<String, List<String>> analyze(BaZiChart chart) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        
        String riGan = chart.getRiZhuTianGan();
        String yueZhi = chart.getYueZhu().getDiZhi();
        String nianZhi = chart.getNianZhu().getDiZhi();
        
        // 分析年柱神煞
        result.put("年", analyzePillarShenSha(chart.getNianZhu(), riGan, yueZhi, nianZhi, chart));
        
        // 分析月柱神煞
        result.put("月", analyzePillarShenSha(chart.getYueZhu(), riGan, yueZhi, nianZhi, chart));
        
        // 分析日柱神煞
        result.put("日", analyzePillarShenSha(chart.getRiZhu(), riGan, yueZhi, nianZhi, chart));
        
        // 分析时柱神煞
        result.put("时", analyzePillarShenSha(chart.getShiZhu(), riGan, yueZhi, nianZhi, chart));
        
        return result;
    }

    private List<String> analyzePillarShenSha(Pillar pillar, String riGan, String yueZhi, 
                                               String nianZhi, BaZiChart chart) {
        List<String> shenShaList = new ArrayList<>();
        String targetGan = pillar.getTianGan();
        String targetZhi = pillar.getDiZhi();
        String ganZhi = pillar.getGanZhi();
        
        // 天乙贵人
        if (ShenSha.getTianYiGuiRen(riGan).contains(targetZhi)) {
            shenShaList.add("天乙贵人");
        }
        
        // 文昌贵人
        String wenChang = ShenSha.getWenChangGuiRen(riGan);
        if (wenChang != null && wenChang.equals(targetZhi)) {
            shenShaList.add("文昌贵人");
        }
        
        // 驿马
        String yiMa = ShenSha.getYiMa(nianZhi);
        if (yiMa != null && yiMa.equals(targetZhi)) {
            shenShaList.add("驿马");
        }
        
        // 桃花
        String taoHua = ShenSha.getTaoHua(nianZhi);
        if (taoHua != null && taoHua.equals(targetZhi)) {
            shenShaList.add("桃花");
        }
        
        // 华盖
        String huaGai = ShenSha.getHuaGai(nianZhi);
        if (huaGai != null && huaGai.equals(targetZhi)) {
            shenShaList.add("华盖");
        }
        
        // 将星
        String jiangXing = ShenSha.getJiangXing(nianZhi);
        if (jiangXing != null && jiangXing.equals(targetZhi)) {
            shenShaList.add("将星");
        }
        
        // 天德贵人
        String tianDe = ShenSha.getTianDeGuiRen(yueZhi);
        if (tianDe != null && tianDe.equals(targetGan)) {
            shenShaList.add("天德贵人");
        }
        
        // 月德贵人
        String yueDe = ShenSha.getYueDeGuiRen(yueZhi);
        if (yueDe != null && yueDe.equals(targetGan)) {
            shenShaList.add("月德贵人");
        }
        
        // 天德合
        String tianDeHe = ShenSha.getTianDeHe(yueZhi);
        if (tianDeHe != null && tianDeHe.equals(targetGan)) {
            shenShaList.add("天德合");
        }
        
        // 月德合
        String yueDeHe = ShenSha.getYueDeHe(yueZhi);
        if (yueDeHe != null && yueDeHe.equals(targetGan)) {
            shenShaList.add("月德合");
        }
        
        // 羊刃
        String yangRen = ShenSha.getYangRen(riGan);
        if (yangRen != null && yangRen.equals(targetZhi)) {
            shenShaList.add("羊刃");
        }
        
        // 飞刃
        String feiRen = ShenSha.getFeiRen(riGan);
        if (feiRen != null && feiRen.equals(targetZhi)) {
            shenShaList.add("飞刃");
        }
        
        // 金舆
        String jinYu = ShenSha.getJinYu(riGan);
        if (jinYu != null && jinYu.equals(targetZhi)) {
            shenShaList.add("金舆");
        }
        
        // 福星贵人
        String fuXing = ShenSha.getFuXingGuiRen(riGan);
        if (fuXing != null && fuXing.equals(targetZhi)) {
            shenShaList.add("福星贵人");
        }
        
        // 天官贵人
        String tianGuan = ShenSha.getTianGuanGuiRen(riGan);
        if (tianGuan != null && tianGuan.equals(targetZhi)) {
            shenShaList.add("天官贵人");
        }
        
        // 德秀贵人
        if (ShenSha.isDeXiuGuiRen(yueZhi, targetGan, targetZhi)) {
            shenShaList.add("德秀贵人");
        }
        
        // 天罗地网
        if (ShenSha.isTianLuoDiWang(targetZhi)) {
            shenShaList.add("天罗地网");
        }
        
        // 十恶大败
        if (ShenSha.isShiEDaBai(ganZhi)) {
            shenShaList.add("十恶大败");
        }
        
        // 十灵
        if (ShenSha.isShiLing(ganZhi)) {
            shenShaList.add("十灵");
        }
        
        // 流霞
        String liuXia = ShenSha.getLiuXia(riGan);
        if (liuXia != null && liuXia.equals(targetZhi)) {
            shenShaList.add("流霞");
        }
        
        // 童子煞
        if (ShenSha.isTongZiSha(yueZhi, chart.getRiZhu().getDiZhi(), chart.getShiZhu().getDiZhi())) {
            if (pillar.getName().equals("日") || pillar.getName().equals("时")) {
                shenShaList.add("童子煞");
            }
        }
        
        return shenShaList;
    }
}
