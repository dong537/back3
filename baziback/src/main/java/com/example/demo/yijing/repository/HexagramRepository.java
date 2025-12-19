package com.example.demo.yijing.repository;

import com.example.demo.yijing.model.Hexagram;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class HexagramRepository {

    private static final List<Hexagram> HEXAGRAMS = build64Hexagrams();

    public List<Hexagram> findAll() {
        return Collections.unmodifiableList(HEXAGRAMS);
    }

    public Optional<Hexagram> findById(Integer id) {
        return HEXAGRAMS.stream()
                .filter(h -> h.getId().equals(id))
                .findFirst();
    }

    public Optional<Hexagram> findByBinary(String binary) {
        return HEXAGRAMS.stream()
                .filter(h -> h.getBinary().equals(binary))
                .findFirst();
    }

    public Optional<Hexagram> findByName(String name) {
        return HEXAGRAMS.stream()
                .filter(h -> h.getName().equals(name) || h.getChinese().equals(name))
                .findFirst();
    }

    private static List<Hexagram> build64Hexagrams() {
        List<Hexagram> hexagrams = new ArrayList<>(64);

        hexagrams.add(Hexagram.builder()
                .id(1).name("Qian").chinese("乾").pinyin("qián")
                .binary("111111").upper("乾").lower("乾").symbol("☰☰")
                .judgment("元亨利贞")
                .image("天行健，君子以自强不息")
                .meaning("纯阳之卦，象征刚健、创造、领导。大吉大利，但需坚守正道。")
                .keywords(Arrays.asList("刚健", "创造", "领导", "自强"))
                .element("金").season("秋").direction("西北")
                .applications(Map.of(
                        "事业", "大展宏图，积极进取",
                        "财运", "财源广进，投资有利",
                        "感情", "主动出击，把握机会",
                        "健康", "精力充沛，注意不要过劳"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：潜龙勿用").meaning("时机未到，韬光养晦").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：见龙在田，利见大人").meaning("崭露头角，寻求贵人").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：君子终日乾乾，夕惕若厉，无咎").meaning("勤勉谨慎，避免危险").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：或跃在渊，无咎").meaning("把握时机，审慎前进").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：飞龙在天，利见大人").meaning("登峰造极，大展宏图").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：亢龙有悔").meaning("物极必反，适可而止").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(2).name("Kun").chinese("坤").pinyin("kūn")
                .binary("000000").upper("坤").lower("坤").symbol("☷☷")
                .judgment("元亨，利牝马之贞")
                .image("地势坤，君子以厚德载物")
                .meaning("纯阴之卦，象征柔顺、包容、承载。顺应天时，厚德载物。")
                .keywords(Arrays.asList("柔顺", "包容", "承载", "厚德"))
                .element("土").season("夏末").direction("西南")
                .applications(Map.of(
                        "事业", "顺势而为，稳扎稳打",
                        "财运", "守成为主，积少成多",
                        "感情", "温柔体贴，包容对方",
                        "健康", "注意脾胃，调养为主"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：履霜，坚冰至").meaning("见微知著，防患未然").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：直方大，不习无不利").meaning("正直宽厚，自然吉利").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：含章可贞，或从王事，无成有终").meaning("含蓄内敛，辅佐他人").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：括囊，无咎无誉").meaning("谨言慎行，明哲保身").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：黄裳，元吉").meaning("谦逊美德，大吉大利").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：龙战于野，其血玄黄").meaning("阴阳相争，两败俱伤").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(3).name("Zhun").chinese("屯").pinyin("zhūn")
                .binary("010001").upper("坎").lower("震").symbol("☵☳")
                .judgment("元亨利贞，勿用有攸往，利建侯")
                .image("云雷屯，君子以经纶")
                .meaning("初始艰难之卦，万事开头难。宜守不宜进，建立基础。")
                .keywords(Arrays.asList("初始", "艰难", "积累", "守成"))
                .element("水").season("冬").direction("北")
                .applications(Map.of(
                        "事业", "创业维艰，打好基础",
                        "财运", "初期困难，不宜冒进",
                        "感情", "感情初萌，需要培养",
                        "健康", "注意保养，预防为主"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：磐桓，利居贞，利建侯").meaning("徘徊不前，宜守正道").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：屯如邅如，乘马班如，匪寇婚媾").meaning("困难重重，终成眷属").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：即鹿无虞，惟入于林中，君子几不如舍").meaning("盲目追求，不如放弃").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：乘马班如，求婚媾，往吉，无不利").meaning("主动求助，前往吉利").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：屯其膏，小贞吉，大贞凶").meaning("小事可为，大事不宜").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：乘马班如，泣血涟如").meaning("进退两难，悲伤哭泣").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(4).name("Meng").chinese("蒙").pinyin("méng")
                .binary("100010").upper("艮").lower("坎").symbol("☶☵")
                .judgment("亨，匪我求童蒙，童蒙求我")
                .image("山下出泉，蒙，君子以果行育德")
                .meaning("启蒙教育之卦，需要虚心学习，诚心求教。")
                .keywords(Arrays.asList("启蒙", "学习", "教育", "求知"))
                .element("水").season("冬春").direction("东北")
                .applications(Map.of(
                        "事业", "虚心学习，积累经验",
                        "财运", "理财需学，不宜投机",
                        "感情", "真诚相待，培养感情",
                        "健康", "注意养生，学习保健"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：发蒙，利用刑人，用说桎梏").meaning("启发蒙昧，立规矩").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：包蒙，吉，纳妇吉，子克家").meaning("包容教导，吉利").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：勿用取女，见金夫，不有躬，无攸利").meaning("不要娶这样的女子").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：困蒙，吝").meaning("困于蒙昧，羞愧").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：童蒙，吉").meaning("如童子般求学，吉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：击蒙，不利为寇，利御寇").meaning("打击蒙昧，防御为宜").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(5).name("Xu").chinese("需").pinyin("xū")
                .binary("010111").upper("坎").lower("乾").symbol("☵☰")
                .judgment("有孚，光亨，贞吉，利涉大川")
                .image("云上于天，需，君子以饮食宴乐")
                .meaning("等待时机之卦，需要耐心等待，不可急躁。")
                .keywords(Arrays.asList("等待", "耐心", "时机", "积蓄"))
                .element("水").season("冬").direction("北")
                .applications(Map.of(
                        "事业", "等待时机，养精蓄锐",
                        "财运", "守住资本，等待机会",
                        "感情", "耐心等待，水到渠成",
                        "健康", "调养身体，不可急躁"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：需于郊，利用恒，无咎").meaning("在郊外等待，恒心无咎").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：需于沙，小有言，终吉").meaning("在沙滩等待，稍有非议，终吉").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：需于泥，致寇至").meaning("在泥泞中等待，招致危险").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：需于血，出自穴").meaning("在血泊中等待，脱离险境").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：需于酒食，贞吉").meaning("饮食宴乐中等待，吉").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：入于穴，有不速之客三人来，敬之终吉").meaning("进入洞穴，有客来访，敬之则吉").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(6).name("Song").chinese("讼").pinyin("sòng")
                .binary("111010").upper("乾").lower("坎").symbol("☰☵")
                .judgment("有孚窒惕，中吉，终凶，利见大人，不利涉大川")
                .image("天与水违行，讼，君子以作事谋始")
                .meaning("争讼之卦，有纷争诉讼。宜和解，不宜对抗。")
                .keywords(Arrays.asList("争讼", "纷争", "和解", "谨慎"))
                .element("水金").season("秋冬").direction("西北")
                .applications(Map.of(
                        "事业", "避免争执，寻求和解",
                        "财运", "财务纠纷，谨慎处理",
                        "感情", "矛盾冲突，需要沟通",
                        "健康", "心情郁闷，注意调节"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：不永所事，小有言，终吉").meaning("不长久争讼，稍有非议，终吉").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：不克讼，归而逋").meaning("打不赢官司，回家躲避").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：食旧德，贞厉，终吉").meaning("守住旧业，虽危终吉").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：不克讼，复即命，渝安贞，吉").meaning("不能胜诉，回归正道，改变态度则吉").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：讼，元吉").meaning("诉讼，大吉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：或锡之鞶带，终朝三褫之").meaning("即使得到赏赐，一天内三次被剥夺").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(7).name("Shi").chinese("师").pinyin("shī")
                .binary("000010").upper("坤").lower("坎").symbol("☷☵")
                .judgment("贞丈人吉，无咎")
                .image("地中有水，师，君子以容民畜众")
                .meaning("军队之卦，需要纪律和领导。以德服人，众望所归。")
                .keywords(Arrays.asList("军队", "纪律", "领导", "众望"))
                .element("土水").season("冬").direction("北")
                .applications(Map.of(
                        "事业", "统筹规划，团队协作",
                        "财运", "集众人之力，共创财富",
                        "感情", "需要包容，建立信任",
                        "健康", "注意肾脏，调理身体"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：师出以律，否臧凶").meaning("军队出征要有纪律，否则凶险").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：在师中，吉，无咎，王三锡命").meaning("在军中，吉利，国王三次赏赐").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：师或舆尸，凶").meaning("军队可能载尸而归，凶").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：师左次，无咎").meaning("军队撤退驻扎，无咎").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：田有禽，利执言，无咎").meaning("田中有禽兽，利于捕获，无咎").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：大君有命，开国承家，小人勿用").meaning("君王有命，分封诸侯，不用小人").build()
                ))
                .build());

        hexagrams.add(Hexagram.builder()
                .id(8).name("Bi").chinese("比").pinyin("bǐ")
                .binary("010000").upper("坎").lower("坤").symbol("☵☷")
                .judgment("吉，原筮，元永贞，无咎")
                .image("地上有水，比，先王以建万国，亲诸侯")
                .meaning("亲比之卦，团结合作，互相帮助。选择良友，建立联盟。")
                .keywords(Arrays.asList("亲近", "团结", "合作", "联盟"))
                .element("水土").season("冬").direction("北")
                .applications(Map.of(
                        "事业", "寻求合作，建立联盟",
                        "财运", "合伙经营，互利共赢",
                        "感情", "亲密无间，互相扶持",
                        "健康", "朋友支持，心情舒畅"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：有孚比之，无咎").meaning("有诚信地亲近，无咎").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：比之自内，贞吉").meaning("从内部亲近，吉").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：比之匪人").meaning("亲近不当之人").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：外比之，贞吉").meaning("对外亲近，吉").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：显比，王用三驱，失前禽，邑人不诫，吉").meaning("显明地亲近，吉").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：比之无首，凶").meaning("亲近无首领，凶").build()
                ))
                .build());

        // 第9卦 小畜
        hexagrams.add(Hexagram.builder()
                .id(9).name("Xiao Xu").chinese("小畜").pinyin("xiǎo xù")
                .binary("110111").upper("巽").lower("乾").symbol("☴☰")
                .judgment("亨，密云不雨，自我西郊")
                .image("风行天上，小畜，君子以懿文德")
                .meaning("小有积蓄之卦，力量尚小，需要继续积累。密云不雨，时机未到。")
                .keywords(Arrays.asList("积蓄", "等待", "准备", "小成"))
                .element("风").season("春").direction("东南")
                .applications(Map.of(
                        "事业", "小有成就，继续积累实力",
                        "财运", "小有积蓄，不宜大投资",
                        "感情", "感情发展缓慢，需要培养",
                        "健康", "小病小痛，注意调养"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：复自道，何其咎，吉").meaning("返回正道，有何过错，吉利").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：牵复，吉").meaning("被牵引返回，吉利").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：舆说辐，夫妻反目").meaning("车轮脱落，夫妻不和").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：有孚，血去惕出，无咎").meaning("有诚信，脱离危险，无咎").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：有孚挛如，富以其邻").meaning("有诚信相连，与邻居共富").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：既雨既处，尚德载").meaning("雨已下，德行圆满").build()
                ))
                .build());

        // 第10卦 履
        hexagrams.add(Hexagram.builder()
                .id(10).name("Lv").chinese("履").pinyin("lǚ")
                .binary("111011").upper("乾").lower("兑").symbol("☰☱")
                .judgment("履虎尾，不咥人，亨")
                .image("上天下泽，履，君子以辨上下，定民志")
                .meaning("行为谨慎之卦，如履虎尾，需要小心谨慎，但最终亨通。")
                .keywords(Arrays.asList("谨慎", "礼仪", "行为", "小心"))
                .element("金").season("秋").direction("西")
                .applications(Map.of(
                        "事业", "谨慎行事，遵守规则",
                        "财运", "稳健理财，不可冒进",
                        "感情", "礼貌相待，把握分寸",
                        "健康", "小心行动，避免意外"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：素履，往无咎").meaning("朴素而行，前往无咎").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：履道坦坦，幽人贞吉").meaning("道路平坦，隐居者吉").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：眇能视，跛能履，履虎尾，咥人，凶").meaning("眼瞎能看，腿瘸能走，踩虎尾，被咬，凶").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：履虎尾，愬愬，终吉").meaning("踩虎尾，恐惧，终吉").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：夬履，贞厉").meaning("果断行事，守正则危").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：视履考祥，其旋元吉").meaning("审视行为，回顾吉祥").build()
                ))
                .build());

        // 第11卦 泰
        hexagrams.add(Hexagram.builder()
                .id(11).name("Tai").chinese("泰").pinyin("tài")
                .binary("000111").upper("坤").lower("乾").symbol("☷☰")
                .judgment("小往大来，吉亨")
                .image("天地交，泰，后以财成天地之道，辅相天地之宜")
                .meaning("通泰之卦，天地交泰，万物亨通。阴阳和合，吉祥如意。")
                .keywords(Arrays.asList("通泰", "和谐", "顺利", "吉祥"))
                .element("土").season("春").direction("中")
                .applications(Map.of(
                        "事业", "事业顺利，大展宏图",
                        "财运", "财运亨通，投资有利",
                        "感情", "感情和谐，婚姻美满",
                        "健康", "身体健康，精力充沛"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：拔茅茹，以其汇，征吉").meaning("拔茅草连根，志同道合，前进吉利").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：包荒，用冯河，不遐遗，朋亡，得尚于中行").meaning("包容荒野，徒步过河，不遗弃远方，失去朋友，得中道").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：无平不陂，无往不复").meaning("没有平坦不变陡峭，没有去而不返").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：翩翩，不富以其邻，不戒以孚").meaning("翩翩而来，不以财富，以诚信相待").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：帝乙归妹，以祉元吉").meaning("帝乙嫁女，得福大吉").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：城复于隍，勿用师，自邑告命，贞吝").meaning("城墙倒塌，不用军队，自己宣告，守正则羞").build()
                ))
                .build());

        // 第12卦 否
        hexagrams.add(Hexagram.builder()
                .id(12).name("Pi").chinese("否").pinyin("pǐ")
                .binary("111000").upper("乾").lower("坤").symbol("☰☷")
                .judgment("否之匪人，不利君子贞，大往小来")
                .image("天地不交，否，君子以俭德辟难，不可荣以禄")
                .meaning("闭塞不通之卦，天地不交，阴阳不和。君子宜守，小人得志。")
                .keywords(Arrays.asList("闭塞", "不通", "困难", "守正"))
                .element("金土").season("秋冬").direction("西北")
                .applications(Map.of(
                        "事业", "事业受阻，宜守不宜进",
                        "财运", "财运不佳，避免投资",
                        "感情", "感情不顺，需要沟通",
                        "健康", "身体欠佳，注意调养"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：拔茅茹，以其汇，贞吉亨").meaning("拔茅草连根，守正吉利亨通").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：包承，小人吉，大人否亨").meaning("包容承受，小人吉，大人否而亨").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：包羞").meaning("包藏羞耻").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：有命无咎，畴离祉").meaning("有天命无咎，同类得福").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：休否，大人吉，其亡其亡，系于苞桑").meaning("停止闭塞，大人吉，危险啊危险，系于桑树").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：倾否，先否后喜").meaning("倾覆闭塞，先否后喜").build()
                ))
                .build());

        // 第13卦 同人
        hexagrams.add(Hexagram.builder()
                .id(13).name("Tong Ren").chinese("同人").pinyin("tóng rén")
                .binary("111101").upper("乾").lower("离").symbol("☰☲")
                .judgment("同人于野，亨，利涉大川，利君子贞")
                .image("天与火，同人，君子以类族辨物")
                .meaning("与人和同之卦，团结合作，志同道合。在野外会合，亨通顺利。")
                .keywords(Arrays.asList("团结", "合作", "和同", "志同道合"))
                .element("火").season("夏").direction("南")
                .applications(Map.of(
                        "事业", "团队合作，共创大业",
                        "财运", "合伙经营，互利共赢",
                        "感情", "志趣相投，感情深厚",
                        "健康", "心情愉悦，身心健康"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：同人于门，无咎").meaning("在门口与人和同，无咎").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：同人于宗，吝").meaning("在宗族内和同，羞愧").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：伏戎于莽，升其高陵，三岁不兴").meaning("埋伏军队在草丛，登上高陵，三年不起").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：乘其墉，弗克攻，吉").meaning("登上城墙，不能攻克，吉").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：同人，先号咷而后笑，大师克相遇").meaning("和同，先哭后笑，大军克敌相遇").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：同人于郊，无悔").meaning("在郊外和同，无悔").build()
                ))
                .build());

        // 第14卦 大有
        hexagrams.add(Hexagram.builder()
                .id(14).name("Da You").chinese("大有").pinyin("dà yǒu")
                .binary("101111").upper("离").lower("乾").symbol("☲☰")
                .judgment("元亨")
                .image("火在天上，大有，君子以遏恶扬善，顺天休命")
                .meaning("大有收获之卦，火在天上，光明普照。大获成功，丰收在望。")
                .keywords(Arrays.asList("丰收", "成功", "富有", "光明"))
                .element("火").season("夏").direction("南")
                .applications(Map.of(
                        "事业", "事业有成，大展宏图",
                        "财运", "财源广进，大有收获",
                        "感情", "感情美满，幸福圆满",
                        "健康", "身心健康，精力旺盛"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：无交害，匪咎，艰则无咎").meaning("不交往有害，非过错，艰难则无咎").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：大车以载，有攸往，无咎").meaning("大车装载，有所前往，无咎").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：公用亨于天子，小人弗克").meaning("公侯献给天子，小人不能").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：匪其彭，无咎").meaning("不是盛大，无咎").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：厥孚交如，威如，吉").meaning("诚信交往，威严，吉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：自天佑之，吉无不利").meaning("天降福佑，吉无不利").build()
                ))
                .build());

        // 第15卦 谦
        hexagrams.add(Hexagram.builder()
                .id(15).name("Qian").chinese("谦").pinyin("qiān")
                .binary("000100").upper("坤").lower("艮").symbol("☷☶")
                .judgment("亨，君子有终")
                .image("地中有山，谦，君子以裒多益寡，称物平施")
                .meaning("谦虚之卦，地中有山而不显露。谦虚谨慎，终获吉祥。")
                .keywords(Arrays.asList("谦虚", "谨慎", "低调", "有终"))
                .element("土").season("四季").direction("东北")
                .applications(Map.of(
                        "事业", "谦虚谨慎，稳步发展",
                        "财运", "低调理财，细水长流",
                        "感情", "谦逊待人，感情稳定",
                        "健康", "心态平和，身体健康"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：谦谦君子，用涉大川，吉").meaning("谦虚的君子，可涉大川，吉").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：鸣谦，贞吉").meaning("谦虚有声，守正吉").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：劳谦，君子有终，吉").meaning("劳苦谦虚，君子有终，吉").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：无不利，撝谦").meaning("无不利，发挥谦虚").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：不富以其邻，利用侵伐，无不利").meaning("不以财富，利于征伐，无不利").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：鸣谦，利用行师，征邑国").meaning("谦虚有声，利于行军，征伐邑国").build()
                ))
                .build());

        // 第16卦 豫
        hexagrams.add(Hexagram.builder()
                .id(16).name("Yu").chinese("豫").pinyin("yù")
                .binary("001000").upper("震").lower("坤").symbol("☳☷")
                .judgment("利建侯行师")
                .image("雷出地奋，豫，先王以作乐崇德，殷荐之上帝")
                .meaning("喜悦安乐之卦，雷出地面，万物欢悦。顺应天时，安乐和谐。")
                .keywords(Arrays.asList("喜悦", "安乐", "和谐", "顺应"))
                .element("雷").season("春").direction("东")
                .applications(Map.of(
                        "事业", "顺势而为，事业顺利",
                        "财运", "财运亨通，收获颇丰",
                        "感情", "感情愉悦，关系和谐",
                        "健康", "心情舒畅，身体健康"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：鸣豫，凶").meaning("鸣叫喜悦，凶").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：介于石，不终日，贞吉").meaning("坚如磐石，不终日，守正吉").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：盱豫，悔，迟有悔").meaning("仰视喜悦，悔，迟则有悔").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：由豫，大有得，勿疑，朋盍簪").meaning("由喜悦，大有得，勿疑，朋友聚集").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：贞疾，恒不死").meaning("守正有病，长久不死").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：冥豫，成有渝，无咎").meaning("昏暗喜悦，成后有变，无咎").build()
                ))
                .build());

        // 添加第17-64卦
        hexagrams.addAll(buildRemainingHexagrams());

        return hexagrams;
    }

    private static List<Hexagram> buildRemainingHexagrams() {
        List<Hexagram> hexagrams = new ArrayList<>();

        // 第17卦 随
        hexagrams.add(Hexagram.builder()
                .id(17).name("Sui").chinese("随").pinyin("suí")
                .binary("011001").upper("兑").lower("震").symbol("☱☳")
                .judgment("元亨利贞，无咎")
                .image("泽中有雷，随，君子以向晦入宴息")
                .meaning("随从之卦，顺应时势，随机应变。随和而不失原则，吉祥亨通。")
                .keywords(Arrays.asList("随从", "顺应", "灵活", "适应"))
                .element("金").season("秋").direction("西")
                .applications(Map.of(
                        "事业", "顺应形势，灵活应变",
                        "财运", "随机而动，把握时机",
                        "感情", "随缘而行，自然发展",
                        "健康", "顺应自然，劳逸结合"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：官有渝，贞吉，出门交有功").meaning("职位有变，守正吉，出门交往有功").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：系小子，失丈夫").meaning("系住小子，失去丈夫").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：系丈夫，失小子，随有求得，利居贞").meaning("系住丈夫，失去小子，随从有所求得，利于守正").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：随有获，贞凶，有孚在道，以明，何咎").meaning("随从有获，守正凶，有诚信在道，明白，何咎").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：孚于嘉，吉").meaning("诚信于善，吉").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：拘系之，乃从维之，王用亨于西山").meaning("拘系之，然后跟从维系，王在西山祭祀").build()
                ))
                .build());

        // 第18卦 蛊
        hexagrams.add(Hexagram.builder()
                .id(18).name("Gu").chinese("蛊").pinyin("gǔ")
                .binary("100011").upper("艮").lower("巽").symbol("☶☴")
                .judgment("元亨，利涉大川，先甲三日，后甲三日")
                .image("山下有风，蛊，君子以振民育德")
                .meaning("整治腐败之卦，事情败坏需要整顿。拨乱反正，革新图强。")
                .keywords(Arrays.asList("整治", "革新", "改革", "振作"))
                .element("木").season("春").direction("东南")
                .applications(Map.of(
                        "事业", "整顿改革，重新出发",
                        "财运", "清理旧账，开创新局",
                        "感情", "修复关系，重建信任",
                        "健康", "治疗旧疾，调养身体"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：干父之蛊，有子，考无咎，厉终吉").meaning("整治父辈之事，有儿子，父亲无咎，虽危终吉").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：干母之蛊，不可贞").meaning("整治母亲之事，不可过于坚持").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：干父之蛊，小有悔，无大咎").meaning("整治父辈之事，小有悔恨，无大咎").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：裕父之蛊，往见吝").meaning("宽容父辈之事，前往则羞").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：干父之蛊，用誉").meaning("整治父辈之事，得到赞誉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：不事王侯，高尚其事").meaning("不侍奉王侯，高尚其志").build()
                ))
                .build());

        // 第19卦 临
        hexagrams.add(Hexagram.builder()
                .id(19).name("Lin").chinese("临").pinyin("lín")
                .binary("000011").upper("坤").lower("兑").symbol("☷☱")
                .judgment("元亨利贞，至于八月有凶")
                .image("泽上有地，临，君子以教思无穷，容保民无疆")
                .meaning("临近之卦，阳气上升，君临天下。以德临民，教化无穷。")
                .keywords(Arrays.asList("临近", "监督", "教化", "领导"))
                .element("土").season("春").direction("西南")
                .applications(Map.of(
                        "事业", "掌握主动，积极进取",
                        "财运", "财运渐旺，把握时机",
                        "感情", "主动出击，建立关系",
                        "健康", "身体转好，注意保养"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：咸临，贞吉").meaning("感应而临，守正吉").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：咸临，吉，无不利").meaning("感应而临，吉，无不利").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：甘临，无攸利，既忧之，无咎").meaning("甜言蜜语而临，无所利，已忧虑，无咎").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：至临，无咎").meaning("亲临，无咎").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：知临，大君之宜，吉").meaning("智慧而临，大君之宜，吉").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：敦临，吉，无咎").meaning("敦厚而临，吉，无咎").build()
                ))
                .build());

        // 第20卦 观
        hexagrams.add(Hexagram.builder()
                .id(20).name("Guan").chinese("观").pinyin("guān")
                .binary("110000").upper("巽").lower("坤").symbol("☴☷")
                .judgment("盥而不荐，有孚颙若")
                .image("风行地上，观，先王以省方观民设教")
                .meaning("观察之卦，观察天下，以身作则。观风察俗，设教化民。")
                .keywords(Arrays.asList("观察", "审视", "榜样", "教化"))
                .element("风").season("秋").direction("东南")
                .applications(Map.of(
                        "事业", "观察形势，谨慎决策",
                        "财运", "观望为主，不宜冒进",
                        "感情", "观察了解，慎重选择",
                        "健康", "观察症状，及时就医"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：童观，小人无咎，君子吝").meaning("幼稚的观察，小人无咎，君子羞").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：窥观，利女贞").meaning("窥视观察，利于女子守正").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：观我生，进退").meaning("观察我的行为，进退").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：观国之光，利用宾于王").meaning("观察国家的光彩，利于作王的宾客").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：观我生，君子无咎").meaning("观察我的行为，君子无咎").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：观其生，君子无咎").meaning("观察其行为，君子无咎").build()
                ))
                .build());

        // 第21卦 噬嗑
        hexagrams.add(Hexagram.builder()
                .id(21).name("Shi He").chinese("噬嗑").pinyin("shì hé")
                .binary("101001").upper("离").lower("震").symbol("☲☳")
                .judgment("亨，利用狱")
                .image("雷电噬嗑，先王以明罚敕法")
                .meaning("咬合之卦，如咬硬物，需用刑罚。明辨是非，执法必严。")
                .keywords(Arrays.asList("咬合", "刑罚", "执法", "明辨"))
                .element("火").season("夏").direction("南")
                .applications(Map.of(
                        "事业", "排除障碍，果断决策",
                        "财运", "清除阻力，收回欠款",
                        "感情", "解决矛盾，消除隔阂",
                        "健康", "治疗疾病，清除病根"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：屦校灭趾，无咎").meaning("戴上脚镣伤脚趾，无咎").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：噬肤灭鼻，无咎").meaning("咬破皮肤伤鼻子，无咎").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：噬腊肉，遇毒，小吝，无咎").meaning("咬腊肉，遇到毒，小羞，无咎").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：噬干胏，得金矢，利艰贞，吉").meaning("咬干肉，得金箭，利于艰难守正，吉").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：噬干肉，得黄金，贞厉，无咎").meaning("咬干肉，得黄金，守正危，无咎").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：何校灭耳，凶").meaning("戴枷锁伤耳朵，凶").build()
                ))
                .build());

        // 第22卦 贲
        hexagrams.add(Hexagram.builder()
                .id(22).name("Bi").chinese("贲").pinyin("bì")
                .binary("100101").upper("艮").lower("离").symbol("☶☲")
                .judgment("亨，小利有攸往")
                .image("山下有火，贲，君子以明庶政，无敢折狱")
                .meaning("文饰之卦，文质彬彬，内外兼修。装饰得当，美而不华。")
                .keywords(Arrays.asList("文饰", "装饰", "美化", "修饰"))
                .element("火").season("夏").direction("南")
                .applications(Map.of(
                        "事业", "注重形象，内外兼修",
                        "财运", "适度装饰，不可过度",
                        "感情", "注重外表，培养内涵",
                        "健康", "注意仪表，保持健康"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：贲其趾，舍车而徒").meaning("装饰脚趾，舍车步行").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：贲其须").meaning("装饰胡须").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：贲如濡如，永贞吉").meaning("装饰如湿润，永远守正吉").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：贲如皤如，白马翰如，匪寇婚媾").meaning("装饰如白色，白马飞驰，非寇是婚").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：贲于丘园，束帛戋戋，吝，终吉").meaning("装饰丘园，束帛少少，羞，终吉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：白贲，无咎").meaning("白色装饰，无咎").build()
                ))
                .build());

        // 第23卦 剥
        hexagrams.add(Hexagram.builder()
                .id(23).name("Bo").chinese("剥").pinyin("bō")
                .binary("100000").upper("艮").lower("坤").symbol("☶☷")
                .judgment("不利有攸往")
                .image("山附于地，剥，上以厚下安宅")
                .meaning("剥落之卦，阴盛阳衰，小人得势。君子宜守，不宜妄动。")
                .keywords(Arrays.asList("剥落", "衰败", "守成", "等待"))
                .element("土").season("秋").direction("东北")
                .applications(Map.of(
                        "事业", "守成为主，不宜扩张",
                        "财运", "财运衰退，保守理财",
                        "感情", "关系疏远，需要维护",
                        "健康", "身体衰弱，注意休养"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：剥床以足，蔑贞凶").meaning("剥床从脚开始，灭守正凶").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：剥床以辨，蔑贞凶").meaning("剥床到床沿，灭守正凶").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：剥之，无咎").meaning("剥落，无咎").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：剥床以肤，凶").meaning("剥床到皮肤，凶").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：贯鱼，以宫人宠，无不利").meaning("串鱼，以宫人得宠，无不利").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：硕果不食，君子得舆，小人剥庐").meaning("大果不吃，君子得车，小人剥屋").build()
                ))
                .build());

        // 第24卦 复
        hexagrams.add(Hexagram.builder()
                .id(24).name("Fu").chinese("复").pinyin("fù")
                .binary("000001").upper("坤").lower("震").symbol("☷☳")
                .judgment("亨，出入无疾，朋来无咎，反复其道，七日来复，利有攸往")
                .image("雷在地中，复，先王以至日闭关，商旅不行，后不省方")
                .meaning("复返之卦，一阳来复，生机再现。周而复始，否极泰来。")
                .keywords(Arrays.asList("复返", "恢复", "重生", "循环"))
                .element("雷").season("冬至").direction("北")
                .applications(Map.of(
                        "事业", "重新开始，恢复发展",
                        "财运", "财运回升，逐步好转",
                        "感情", "破镜重圆，重修旧好",
                        "健康", "病情好转，逐渐康复"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：不远复，无祗悔，元吉").meaning("不远就返回，无大悔，大吉").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：休复，吉").meaning("美好的返回，吉").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：频复，厉，无咎").meaning("频繁返回，危，无咎").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：中行独复").meaning("中途独自返回").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：敦复，无悔").meaning("敦厚返回，无悔").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：迷复，凶，有灾眚，用行师，终有大败，以其国君凶，至于十年不克征").meaning("迷途返回，凶，有灾祸，用兵，终大败，国君凶，十年不能征伐").build()
                ))
                .build());

        // 添加第25-64卦
        hexagrams.addAll(buildHexagrams25to64());

        return hexagrams;
    }

    private static List<Hexagram> buildHexagrams25to64() {
        List<Hexagram> hexagrams = new ArrayList<>();

        // 第25卦 无妄
        hexagrams.add(Hexagram.builder()
                .id(25).name("Wu Wang").chinese("无妄").pinyin("wú wàng")
                .binary("111001").upper("乾").lower("震").symbol("☰☳")
                .judgment("元亨利贞，其匪正有眚，不利有攸往")
                .image("天下雷行，物与无妄，先王以茂对时育万物")
                .meaning("无妄之卦，顺应天道，不可妄为。真诚无欺，自然而然。")
                .keywords(Arrays.asList("真诚", "自然", "无欺", "天道"))
                .element("雷").season("春").direction("东")
                .applications(Map.of(
                        "事业", "顺其自然，不可妄动",
                        "财运", "诚信经营，不可投机",
                        "感情", "真诚相待，自然发展",
                        "健康", "顺应自然，保持真我"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：无妄，往吉").meaning("无妄而行，前往吉利").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：不耕获，不菑畲，则利有攸往").meaning("不耕而获，不开荒而种，则利前往").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：无妄之灾，或系之牛，行人之得，邑人之灾").meaning("无妄之灾，牛被系，行人得利，邑人受灾").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：可贞，无咎").meaning("可守正，无咎").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：无妄之疾，勿药有喜").meaning("无妄之病，不用药有喜").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：无妄，行有眚，无攸利").meaning("无妄而行，有灾，无所利").build()
                ))
                .build());

        // 第26卦 大畜
        hexagrams.add(Hexagram.builder()
                .id(26).name("Da Xu").chinese("大畜").pinyin("dà xù")
                .binary("100111").upper("艮").lower("乾").symbol("☶☰")
                .judgment("利贞，不家食吉，利涉大川")
                .image("天在山中，大畜，君子以多识前言往行，以畜其德")
                .meaning("大畜之卦，积蓄力量，厚积薄发。储备充足，可成大事。")
                .keywords(Arrays.asList("积蓄", "储备", "厚积", "薄发"))
                .element("土").season("四季").direction("东北")
                .applications(Map.of(
                        "事业", "积累经验，等待时机",
                        "财运", "储蓄为主，稳健增长",
                        "感情", "培养感情，积累信任",
                        "健康", "调养身体，积蓄精力"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：有厉，利已").meaning("有危险，利于停止").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：舆说辐").meaning("车轮脱落").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：良马逐，利艰贞，曰闲舆卫，利有攸往").meaning("良马追逐，利于艰难守正，练习车马，利前往").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：童牛之牿，元吉").meaning("小牛戴枷，大吉").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：豮豕之牙，吉").meaning("阉猪之牙，吉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：何天之衢，亨").meaning("通达天衢，亨通").build()
                ))
                .build());

        // 第27卦 颐
        hexagrams.add(Hexagram.builder()
                .id(27).name("Yi").chinese("颐").pinyin("yí")
                .binary("100001").upper("艮").lower("震").symbol("☶☳")
                .judgment("贞吉，观颐，自求口实")
                .image("山下有雷，颐，君子以慎言语，节饮食")
                .meaning("颐养之卦，养生养德，修身养性。慎言节食，颐养天年。")
                .keywords(Arrays.asList("颐养", "养生", "修身", "节制"))
                .element("土").season("春").direction("东北")
                .applications(Map.of(
                        "事业", "修身养性，稳步发展",
                        "财运", "节制开支，稳健理财",
                        "感情", "相互滋养，和谐相处",
                        "健康", "注重养生，饮食有节"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：舍尔灵龟，观我朵颐，凶").meaning("舍弃灵龟，看我大吃，凶").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：颠颐，拂经，于丘颐，征凶").meaning("颠倒颐养，违背常理，在山丘颐养，前进凶").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：拂颐，贞凶，十年勿用，无攸利").meaning("违背颐养，守正凶，十年不用，无所利").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：颠颐，吉，虎视眈眈，其欲逐逐，无咎").meaning("颠倒颐养，吉，虎视眈眈，欲望追逐，无咎").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：拂经，居贞吉，不可涉大川").meaning("违背常理，居守正吉，不可涉大川").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：由颐，厉吉，利涉大川").meaning("由颐养，危吉，利涉大川").build()
                ))
                .build());

        // 第28卦 大过
        hexagrams.add(Hexagram.builder()
                .id(28).name("Da Guo").chinese("大过").pinyin("dà guò")
                .binary("011110").upper("兑").lower("巽").symbol("☱☴")
                .judgment("栋桡，利有攸往，亨")
                .image("泽灭木，大过，君子以独立不惧，遯世无闷")
                .meaning("大过之卦，过度之象，负担过重。需要果断，非常之时行非常之事。")
                .keywords(Arrays.asList("过度", "超越", "果断", "非常"))
                .element("金木").season("秋春").direction("西东")
                .applications(Map.of(
                        "事业", "非常时期，果断决策",
                        "财运", "风险较大，谨慎投资",
                        "感情", "压力过大，需要调整",
                        "健康", "负担过重，注意休息"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：藉用白茅，无咎").meaning("用白茅垫底，无咎").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：枯杨生稊，老夫得其女妻，无不利").meaning("枯杨生新芽，老夫得少妻，无不利").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：栋桡，凶").meaning("栋梁弯曲，凶").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：栋隆，吉，有它吝").meaning("栋梁高隆，吉，有其他羞愧").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：枯杨生华，老妇得其士夫，无咎无誉").meaning("枯杨开花，老妇得壮夫，无咎无誉").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：过涉灭顶，凶，无咎").meaning("过度涉水淹没头顶，凶，无咎").build()
                ))
                .build());

        // 第29卦 坎
        hexagrams.add(Hexagram.builder()
                .id(29).name("Kan").chinese("坎").pinyin("kǎn")
                .binary("010010").upper("坎").lower("坎").symbol("☵☵")
                .judgment("习坎，有孚，维心亨，行有尚")
                .image("水洊至，习坎，君子以常德行，习教事")
                .meaning("重坎之卦，险中有险，困难重重。保持诚信，坚持不懈可脱险。")
                .keywords(Arrays.asList("险难", "困境", "坚持", "诚信"))
                .element("水").season("冬").direction("北")
                .applications(Map.of(
                        "事业", "困难重重，坚持不懈",
                        "财运", "财运受阻，谨慎理财",
                        "感情", "感情波折，需要坚守",
                        "健康", "注意肾脏，小心水患"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yin").text("初六：习坎，入于坎窞，凶").meaning("重重险难，陷入坎窞，凶").build(),
                        Hexagram.Line.builder().position(2).type("yang").text("九二：坎有险，求小得").meaning("坎中有险，求小有得").build(),
                        Hexagram.Line.builder().position(3).type("yin").text("六三：来之坎坎，险且枕，入于坎窞，勿用").meaning("来到坎坎，险而枕，陷入坎窞，不用").build(),
                        Hexagram.Line.builder().position(4).type("yin").text("六四：樽酒簋贰，用缶，纳约自牖，终无咎").meaning("一樽酒二簋，用瓦器，从窗纳约，终无咎").build(),
                        Hexagram.Line.builder().position(5).type("yang").text("九五：坎不盈，祗既平，无咎").meaning("坎不满，只求平，无咎").build(),
                        Hexagram.Line.builder().position(6).type("yin").text("上六：系用徽纆，寘于丛棘，三岁不得，凶").meaning("用绳索捆绑，置于荆棘，三年不得，凶").build()
                ))
                .build());

        // 第30卦 离
        hexagrams.add(Hexagram.builder()
                .id(30).name("Li").chinese("离").pinyin("lí")
                .binary("101101").upper("离").lower("离").symbol("☲☲")
                .judgment("利贞，亨，畜牝牛，吉")
                .image("明两作，离，大人以继明照于四方")
                .meaning("重离之卦，光明重叠，文明之象。附丽依附，柔顺而明。")
                .keywords(Arrays.asList("光明", "文明", "附丽", "柔顺"))
                .element("火").season("夏").direction("南")
                .applications(Map.of(
                        "事业", "光明正大，事业亨通",
                        "财运", "财运光明，投资有利",
                        "感情", "热情如火，感情美满",
                        "健康", "注意心脏，防火防热"
                ))
                .lines(Arrays.asList(
                        Hexagram.Line.builder().position(1).type("yang").text("初九：履错然，敬之，无咎").meaning("行走错乱，敬慎，无咎").build(),
                        Hexagram.Line.builder().position(2).type("yin").text("六二：黄离，元吉").meaning("黄色光明，大吉").build(),
                        Hexagram.Line.builder().position(3).type("yang").text("九三：日昃之离，不鼓缶而歌，则大耋之嗟，凶").meaning("日落之离，不击缶而歌，则老年之叹，凶").build(),
                        Hexagram.Line.builder().position(4).type("yang").text("九四：突如其来如，焚如，死如，弃如").meaning("突然而来，焚烧，死亡，抛弃").build(),
                        Hexagram.Line.builder().position(5).type("yin").text("六五：出涕沱若，戚嗟若，吉").meaning("泪流如注，悲叹，吉").build(),
                        Hexagram.Line.builder().position(6).type("yang").text("上九：王用出征，有嘉折首，获匪其丑，无咎").meaning("王用出征，嘉奖斩首，俘获非其类，无咎").build()
                ))
                .build());

        // 继续添加第31-40卦...
        hexagrams.addAll(buildHexagrams31to40());
        // 继续添加第41-50卦...
        hexagrams.addAll(buildHexagrams41to50());
        // 继续添加第51-64卦...
        hexagrams.addAll(buildHexagrams51to64());

        return hexagrams;
    }

    private static List<Hexagram> buildHexagrams31to40() {
        List<Hexagram> hexagrams = new ArrayList<>();
        
        // 第31-40卦的简化版本，包含基本信息
        String[] names = {"咸", "恒", "遯", "大壮", "晋", "明夷", "家人", "睽", "蹇", "解"};
        String[] binaries = {"011100", "001110", "111100", "001111", "101011", "011010", "101011", "110101", "010100", "001010"};
        String[] meanings = {
            "感应之卦，男女相感，婚姻美满。感而遂通，无心之感最真诚。",
            "恒久之卦，持之以恒，长久不变。坚守正道，恒心可成大事。",
            "退避之卦，君子退隐，远离小人。识时务者为俊杰，退而求进。",
            "壮盛之卦，阳气强盛，勇往直前。刚强有力，但需守正不可妄动。",
            "晋升之卦，光明上进，前途光明。积极进取，事业蒸蒸日上。",
            "光明受伤之卦，明珠蒙尘，暂时困难。守正待时，终见光明。",
            "家庭之卦，齐家治国，和睦相处。家和万事兴，内外有别。",
            "乖离之卦，意见不合，暂时分离。求同存异，和而不同。",
            "艰难之卦，前路艰险，进退两难。停止前进，等待时机。",
            "解除之卦，困难解除，雨过天晴。化险为夷，转危为安。"
        };

        for (int i = 0; i < 10; i++) {
            int id = 31 + i;
            hexagrams.add(Hexagram.builder()
                    .id(id)
                    .name("Hexagram" + id)
                    .chinese(names[i])
                    .pinyin("")
                    .binary(binaries[i])
                    .upper("待补充")
                    .lower("待补充")
                    .symbol("☰☰")
                    .judgment("待补充")
                    .image("待补充")
                    .meaning(meanings[i])
                    .keywords(Arrays.asList("待补充"))
                    .element("待补充")
                    .season("待补充")
                    .direction("待补充")
                    .applications(Map.of(
                            "事业", "根据卦象指导事业发展",
                            "财运", "根据卦象指导财运规划",
                            "感情", "根据卦象指导感情处理",
                            "健康", "根据卦象注意健康养生"
                    ))
                    .lines(new ArrayList<>())
                    .build());
        }

        return hexagrams;
    }

    private static List<Hexagram> buildHexagrams41to50() {
        List<Hexagram> hexagrams = new ArrayList<>();
        
        String[] names = {"损", "益", "夬", "姤", "萃", "升", "困", "井", "革", "鼎"};
        String[] binaries = {"100011", "110001", "111110", "111110", "011000", "000110", "011010", "010110", "011101", "101110"};
        String[] meanings = {
            "损减之卦，减损下益上，节制欲望。损己利人，舍小取大。",
            "增益之卦，损上益下，利益众生。助人为乐，利人利己。",
            "决断之卦，果断决策，刚决柔。当机立断，除旧布新。",
            "遘遇之卦，不期而遇，偶然相逢。防微杜渐，谨慎交往。",
            "聚集之卦，众人聚集，团结一致。凝聚力量，共创大业。",
            "上升之卦，步步高升，前途光明。积极进取，顺势而上。",
            "困境之卦，身处困境，进退维谷。坚守正道，等待转机。",
            "水井之卦，取之不尽，用之不竭。修德养性，泽被万民。",
            "变革之卦，除旧布新，改革创新。顺应时势，革故鼎新。",
            "鼎新之卦，稳固基业，承前启后。巩固成果，继往开来。"
        };

        for (int i = 0; i < 10; i++) {
            int id = 41 + i;
            hexagrams.add(Hexagram.builder()
                    .id(id)
                    .name("Hexagram" + id)
                    .chinese(names[i])
                    .pinyin("")
                    .binary(binaries[i])
                    .upper("待补充")
                    .lower("待补充")
                    .symbol("☰☰")
                    .judgment("待补充")
                    .image("待补充")
                    .meaning(meanings[i])
                    .keywords(Arrays.asList("待补充"))
                    .element("待补充")
                    .season("待补充")
                    .direction("待补充")
                    .applications(Map.of(
                            "事业", "根据卦象指导事业发展",
                            "财运", "根据卦象指导财运规划",
                            "感情", "根据卦象指导感情处理",
                            "健康", "根据卦象注意健康养生"
                    ))
                    .lines(new ArrayList<>())
                    .build());
        }

        return hexagrams;
    }

    private static List<Hexagram> buildHexagrams51to64() {
        List<Hexagram> hexagrams = new ArrayList<>();
        
        String[] names = {"震", "艮", "渐", "归妹", "丰", "旅", "巽", "兑", "涣", "节", "中孚", "小过", "既济", "未济"};
        String[] binaries = {"001001", "100100", "110100", "001011", "101011", "011100", "110110", "011011", "110010", "010011", "110011", "001100", "010101", "101010"};
        String[] meanings = {
            "震动之卦，雷声震动，惊恐之象。临危不乱，反省修身。",
            "止静之卦，山之静止，适可而止。知止而后有定，静以修身。",
            "渐进之卦，循序渐进，稳步前行。欲速则不达，渐进则成。",
            "归妹之卦，少女出嫁，归宿之象。顺从天命，各得其所。",
            "丰盛之卦，丰收盛大，光明照耀。盛极而衰，居安思危。",
            "旅途之卦，行旅在外，寄人篱下。谨言慎行，随遇而安。",
            "巽顺之卦，谦逊柔顺，随风而动。顺势而为，灵活应变。",
            "喜悦之卦，和悦欢乐，口舌之象。和颜悦色，以诚待人。",
            "涣散之卦，离散分离，涣然冰释。化解矛盾，重新聚合。",
            "节制之卦，节约有度，适可而止。过犹不及，中庸之道。",
            "诚信之卦，诚实守信，中心诚信。以诚待人，信誉第一。",
            "小过之卦，小有过失，谨小慎微。小心谨慎，避免大错。",
            "既济之卦，事已成功，功德圆满。居安思危，防止倒退。",
            "未济之卦，事未成功，前途未卜。继续努力，终将成功。"
        };

        for (int i = 0; i < 14; i++) {
            int id = 51 + i;
            hexagrams.add(Hexagram.builder()
                    .id(id)
                    .name("Hexagram" + id)
                    .chinese(names[i])
                    .pinyin("")
                    .binary(binaries[i])
                    .upper("待补充")
                    .lower("待补充")
                    .symbol("☰☰")
                    .judgment("待补充")
                    .image("待补充")
                    .meaning(meanings[i])
                    .keywords(Arrays.asList("待补充"))
                    .element("待补充")
                    .season("待补充")
                    .direction("待补充")
                    .applications(Map.of(
                            "事业", "根据卦象指导事业发展",
                            "财运", "根据卦象指导财运规划",
                            "感情", "根据卦象指导感情处理",
                            "健康", "根据卦象注意健康养生"
                    ))
                    .lines(new ArrayList<>())
                    .build());
        }

        return hexagrams;
    }
}
