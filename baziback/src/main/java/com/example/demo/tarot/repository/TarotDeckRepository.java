package com.example.demo.tarot.repository;

import com.example.demo.tarot.model.TarotCard;
import com.example.demo.tarot.model.TarotCardType;
import com.example.demo.tarot.model.TarotSuit;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TarotDeckRepository {

    private final List<TarotCard> deck = Collections.unmodifiableList(build78());

    public List<TarotCard> findAll() {
        return deck;
    }

    public Optional<TarotCard> findByName(String nameCn) {
        if (nameCn == null || nameCn.isBlank()) return Optional.empty();
        String n = nameCn.trim();
        return deck.stream().filter(c -> n.equalsIgnoreCase(c.getCardNameCn())).findFirst();
    }

    public List<TarotCard> searchByKeyword(String keywordLike, int limit) {
        if (keywordLike == null || keywordLike.isBlank()) return List.of();
        String q = keywordLike.trim();
        return deck.stream()
                .filter(c -> (c.getKeyword() != null && c.getKeyword().contains(q))
                        || (c.getCardNameCn() != null && c.getCardNameCn().contains(q))
                        || (c.getMeaningUp() != null && c.getMeaningUp().contains(q))
                        || (c.getMeaningRev() != null && c.getMeaningRev().contains(q)))
                .limit(Math.max(1, limit))
                .toList();
    }

    private static List<TarotCard> build78() {
        List<TarotCard> d = new ArrayList<>(78);

        TarotCardType M = TarotCardType.MAJOR_ARCANA;
        TarotCardType m = TarotCardType.MINOR_ARCANA;
        TarotSuit N = TarotSuit.NONE;
        TarotSuit W = TarotSuit.WANDS;
        TarotSuit C = TarotSuit.CUPS;
        TarotSuit S = TarotSuit.SWORDS;
        TarotSuit P = TarotSuit.PENTACLES;

        // 22 Major Arcana
        d.add(new TarotCard(1, "愚者", M, N, "新的开始、信任与尝试。", "冲动、缺乏准备或逃避责任。", "开始;自由;冒险"));
        d.add(new TarotCard(2, "魔术师", M, N, "资源齐备、行动力强，适合把想法落地。", "能力分散、操控或自我怀疑，建议聚焦。", "行动;创造;掌控"));
        d.add(new TarotCard(3, "女祭司", M, N, "直觉与洞察，适合观察等待信息浮现。", "信息不透明或忽视直觉，需要更坦诚沟通。", "直觉;沉静;秘密"));
        d.add(new TarotCard(4, "女皇", M, N, "丰盛、滋养与关系经营。", "过度付出或忽略自我需求，先建立边界。", "滋养;丰盛;关怀"));
        d.add(new TarotCard(5, "皇帝", M, N, "结构、规则与负责，通过规划获得稳定。", "控制欲或僵化，需更灵活的策略。", "秩序;权威;稳定"));
        d.add(new TarotCard(6, "教皇", M, N, "学习、传统与价值观的指引。", "教条或形式化，别把规则当成唯一答案。", "信念;导师;传统"));
        d.add(new TarotCard(7, "恋人", M, N, "选择、关系与价值一致。", "摇摆或价值冲突，需要更清晰的承诺。", "选择;关系;一致"));
        d.add(new TarotCard(8, "战车", M, N, "推进与胜利，掌控节奏、目标明确。", "用力过猛或方向偏离，先校准再冲刺。", "意志;推进;胜利"));
        d.add(new TarotCard(9, "力量", M, N, "温柔而坚定的自控与勇气。", "压抑或失控，建议用更温和方式管理情绪。", "勇气;耐心;自控"));
        d.add(new TarotCard(10, "隐者", M, N, "内省寻路，独立思考与沉淀。", "孤立或逃避，别把独处当作切断联系。", "内省;智慧;独处"));
        d.add(new TarotCard(11, "命运之轮", M, N, "周期变化与转机出现。", "抗拒变化或重复旧模式，需要主动调整。", "变化;转机;周期"));
        d.add(new TarotCard(12, "正义", M, N, "公平、因果与清晰决断。", "偏见或拖延，建议回到事实与原则。", "原则;平衡;决断"));
        d.add(new TarotCard(13, "倒吊人", M, N, "换视角、暂停与重新评估。", "无意义牺牲或停滞，需设定期限与边界。", "转念;等待;暂停"));
        d.add(new TarotCard(14, "死神", M, N, "结束与新生，适合断舍离与转化。", "抗拒结束或执念，会延长不适。", "转化;告别;重生"));
        d.add(new TarotCard(15, "节制", M, N, "调和、整合与节奏管理。", "失衡或过度，先把生活结构调回稳定。", "调和;节奏;整合"));
        d.add(new TarotCard(16, "恶魔", M, N, "欲望与束缚，提醒看见依赖与惯性。", "觉醒与松绑，适合建立边界与戒断。", "欲望;束缚;依赖"));
        d.add(new TarotCard(17, "高塔", M, N, "突变与破局，旧结构崩塌带来重建机会。", "延迟爆发或更大代价，尽早修补风险点。", "破局;重建;冲击"));
        d.add(new TarotCard(18, "星星", M, N, "希望、疗愈与信心重建。", "信念动摇或失望，先照顾身心再前进。", "希望;疗愈;信心"));
        d.add(new TarotCard(19, "月亮", M, N, "不确定与潜意识，适合辨别真假与情绪。", "迷雾散去、看清现实，减少猜测。", "迷雾;直觉;不安"));
        d.add(new TarotCard(20, "太阳", M, N, "清晰、喜悦与正向成果。", "短暂低迷或自信不足，别低估自己的能力。", "喜悦;清晰;成功"));
        d.add(new TarotCard(21, "审判", M, N, "复盘觉醒与进入新阶段。", "自责或拒绝成长，建议把经验转为行动。", "觉醒;复盘;呼唤"));
        d.add(new TarotCard(22, "世界", M, N, "完成、圆满与整合。", "未完成或收尾不足，需要补齐最后一环。", "完成;圆满;整合"));

        // Wands 14
        d.add(new TarotCard(23, "权杖Ace", m, W, "火种启动、动力出现。", "延迟启动或热情不足。", "启动;灵感"));
        d.add(new TarotCard(24, "权杖二", m, W, "规划视野、选择方向。", "犹豫不决或怕风险。", "规划;视野"));
        d.add(new TarotCard(25, "权杖三", m, W, "扩展合作、等待回报。", "进展慢或预期落差。", "拓展;合作"));
        d.add(new TarotCard(26, "权杖四", m, W, "阶段成果、稳定庆祝。", "不稳或团队/家庭摩擦。", "稳定;成果"));
        d.add(new TarotCard(27, "权杖五", m, W, "竞争磨合、观点碰撞。", "内耗争执、无意义对抗。", "竞争;磨合"));
        d.add(new TarotCard(28, "权杖六", m, W, "认可与胜利，好消息。", "虚荣或认可延迟。", "胜利;认可"));
        d.add(new TarotCard(29, "权杖七", m, W, "守住立场、应对压力。", "退缩或招架不住。", "防守;坚持"));
        d.add(new TarotCard(30, "权杖八", m, W, "快速推进、消息到来。", "延误或沟通失真。", "速度;进展"));
        d.add(new TarotCard(31, "权杖九", m, W, "韧性与最后防线。", "疲惫与疑心过重。", "防线;韧性"));
        d.add(new TarotCard(32, "权杖十", m, W, "负担过重，需要分工。", "崩溃或逃避责任。", "负担;分工"));
        d.add(new TarotCard(33, "权杖侍从", m, W, "热情学习、尝试新路。", "冲动或三分钟热度。", "热情;探索"));
        d.add(new TarotCard(34, "权杖骑士", m, W, "行动果断、推进力强。", "急躁鲁莽或冲突。", "冲劲;冒进"));
        d.add(new TarotCard(35, "权杖王后", m, W, "自信魅力、带动他人。", "焦躁控制或情绪外放。", "自信;魅力"));
        d.add(new TarotCard(36, "权杖国王", m, W, "领导与愿景、推动落地。", "专断或自我中心。", "领导;愿景"));

        // Cups 14
        d.add(new TarotCard(37, "圣杯Ace", m, C, "情感开启、新连接。", "情绪封闭或不敢爱。", "情感;开启"));
        d.add(new TarotCard(38, "圣杯二", m, C, "互惠关系、对等。", "不对等或误解。", "连接;互惠"));
        d.add(new TarotCard(39, "圣杯三", m, C, "支持与庆祝、友谊。", "过度社交或八卦。", "支持;庆祝"));
        d.add(new TarotCard(40, "圣杯四", m, C, "倦怠与重新审视。", "错过机会或麻木。", "倦怠;反思"));
        d.add(new TarotCard(41, "圣杯五", m, C, "失落哀伤，但仍有希望。", "走出阴影、逐步复原。", "失落;复原"));
        d.add(new TarotCard(42, "圣杯六", m, C, "回忆与纯真、旧人事。", "沉溺过去或不成长。", "回忆;纯真"));
        d.add(new TarotCard(43, "圣杯七", m, C, "幻想与多选择，需要辨别。", "看清现实、做决定。", "幻想;选择"));
        d.add(new TarotCard(44, "圣杯八", m, C, "离开不适、寻找更好。", "逃避或半途而废。", "离开;追寻"));
        d.add(new TarotCard(45, "圣杯九", m, C, "满足与心愿达成。", "空虚或过度享乐。", "满足;愿望"));
        d.add(new TarotCard(46, "圣杯十", m, C, "情感稳定、家庭圆满。", "表面和谐但沟通不足。", "圆满;家庭"));
        d.add(new TarotCard(47, "圣杯侍从", m, C, "温柔表达、新消息。", "情绪幼稚或不稳定。", "表达;温柔"));
        d.add(new TarotCard(48, "圣杯骑士", m, C, "浪漫提议、体贴。", "多情不定或口惠。", "浪漫;提议"));
        d.add(new TarotCard(49, "圣杯王后", m, C, "共情疗愈、包容。", "过度敏感或情绪化。", "共情;疗愈"));
        d.add(new TarotCard(50, "圣杯国王", m, C, "成熟情绪与稳定支持。", "压抑或情绪操控。", "成熟;稳定"));

        // Swords 14
        d.add(new TarotCard(51, "宝剑Ace", m, S, "清晰判断、真相。", "偏激或思维混乱。", "清晰;真相"));
        d.add(new TarotCard(52, "宝剑二", m, S, "僵持与需要选择。", "逃避选择、延误。", "僵持;选择"));
        d.add(new TarotCard(53, "宝剑三", m, S, "心痛分离、现实冲击。", "修复疗愈、释怀。", "心痛;修复"));
        d.add(new TarotCard(54, "宝剑四", m, S, "休息复盘、恢复。", "过劳或焦虑难眠。", "休息;恢复"));
        d.add(new TarotCard(55, "宝剑五", m, S, "争执胜负、代价。", "和解或放下输赢。", "争执;代价"));
        d.add(new TarotCard(56, "宝剑六", m, S, "过渡前行、离开困境。", "无法前进或被拖累。", "过渡;前行"));
        d.add(new TarotCard(57, "宝剑七", m, S, "策略与隐瞒、谨慎。", "曝光或信任危机。", "策略;隐瞒"));
        d.add(new TarotCard(58, "宝剑八", m, S, "受限感、可能是自我设限。", "松绑与找到出口。", "受限;松绑"));
        d.add(new TarotCard(59, "宝剑九", m, S, "焦虑与担忧。", "减轻焦虑、适合求助。", "焦虑;担忧"));
        d.add(new TarotCard(60, "宝剑十", m, S, "结束低谷、触底。", "复原与重新开始。", "结束;复原"));
        d.add(new TarotCard(61, "宝剑侍从", m, S, "学习观察、信息收集。", "口舌是非或不成熟。", "观察;信息"));
        d.add(new TarotCard(62, "宝剑骑士", m, S, "果断推进、直言。", "鲁莽冲突或伤人。", "果断;冲突"));
        d.add(new TarotCard(63, "宝剑王后", m, S, "理性边界、清楚表达。", "冷硬防御或刻薄。", "理性;边界"));
        d.add(new TarotCard(64, "宝剑国王", m, S, "权威判断、策略。", "专断或过度批判。", "权威;策略"));

        // Pentacles 14
        d.add(new TarotCard(65, "星币Ace", m, P, "新资源与机会。", "机会延迟或不落实。", "资源;机会"));
        d.add(new TarotCard(66, "星币二", m, P, "平衡收支、多任务。", "失衡与手忙脚乱。", "平衡;应变"));
        d.add(new TarotCard(67, "星币三", m, P, "协作专业与口碑。", "配合差或标准不一。", "协作;专业"));
        d.add(new TarotCard(68, "星币四", m, P, "守成保守、安全感。", "吝啬或恐惧失去。", "保守;安全"));
        d.add(new TarotCard(69, "星币五", m, P, "匮乏感，提醒求助。", "走出困境与获得支持。", "匮乏;支持"));
        d.add(new TarotCard(70, "星币六", m, P, "给予与回报、公平。", "不对等或控制资源。", "互惠;公平"));
        d.add(new TarotCard(71, "星币七", m, P, "耐心耕耘与评估。", "急躁或缺耐心。", "耐心;评估"));
        d.add(new TarotCard(72, "星币八", m, P, "精进技能与投入。", "敷衍或缺成长。", "精进;投入"));
        d.add(new TarotCard(73, "星币九", m, P, "自足成果与稳定。", "不安全感或过度依赖。", "自足;成果"));
        d.add(new TarotCard(74, "星币十", m, P, "长期财富与家族基业。", "财务/家族压力。", "长期;稳固"));
        d.add(new TarotCard(75, "星币侍从", m, P, "务实学习与新计划。", "拖延或不切实际。", "务实;学习"));
        d.add(new TarotCard(76, "星币骑士", m, P, "稳步推进、可靠。", "停滞或固执。", "稳步;可靠"));
        d.add(new TarotCard(77, "星币王后", m, P, "照顾生活、务实丰盛。", "过度操心或控制。", "务实;照顾"));
        d.add(new TarotCard(78, "星币国王", m, P, "成就管理、资源整合。", "贪婪或僵化风险。", "成就;管理"));

        return d;
    }
}
