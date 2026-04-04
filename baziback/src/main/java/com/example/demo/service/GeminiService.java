package com.example.demo.service;

import com.example.demo.dto.request.gemini.GeminiFaceAnalysisRequest;
import com.example.demo.dto.request.yijing.YijingSceneImageRequest;
import com.example.demo.dto.response.gemini.GeminiProbeResponse;
import com.example.demo.dto.response.gemini.GeminiFaceAnalysisResponse;
import com.example.demo.dto.response.gemini.GeminiFailureDetails;
import com.example.demo.dto.response.gemini.GeminiFaceResponseMapper;
import com.example.demo.dto.response.yijing.YijingSceneImageResponse;
import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final String DEFAULT_FACE_CULTURAL_PROMPT = """
            濠电姷鏁搁崑鐘诲箵椤忓棗绶ら柟绋垮閸欏繘姊婚崼鐔峰幏闁挎繂顦导鐘绘煏婢跺牆鍔氶柣蹇擄攻娣囧﹪濡惰箛鏇炲煂闂佸摜鍠撴繛鈧€规洘鍨块獮姗€骞囨担鐟板厞婵＄偑鍊栭崝鎴﹀磹閵堝纾婚柕濞炬櫆閳锋垿鏌涘┑鍡楊仼闁逞屽墰椤牓鏁冮姀銈呯闁诲繑妲掗～澶婎嚗閸曨垰绠涙い鎾跺仒閸濇姊绘担铏广€婇柛鎾寸箞瀹曟繆绠涘☉妯硷紱闂佽鍎抽悺銊﹀垔閹绢喗鈷戦柛顭戝櫘閸庢垶绻濊鐎氼厾鎹㈠☉姘ｅ亾閸偅鍋犻柍褜鍓欓澶婎潖娴犲绀嬫い鏍ㄧ☉閸擃參姊哄Ч鍥х仼闁诲繑姘ㄩ埀顒侇問閸撶喖寮婚妸銉㈡婵☆垯璀︽禒楣冩⒑娴兼瑧鍒扮€规洦鍓熷﹢渚€姊洪崗鑲┿偞闁哄懏绋戦弳鈺冪磽閸屾瑧璐伴柛鐘愁殔椤啯绂掔€ｎ亞鐣洪梺瑙勫劶濡嫮绮堢€ｎ偁浜滈柟浼存涧娴滄粌鈹戦埄鍐ㄢ枙婵﹥妞介幃婊堝煛閸屾稓褰囨俊鐐€ら崢濂告偋閹炬眹鈧礁螖閸涱喖浜滈梺纭呭亹閸嬫鑺辩紒妯圭箚闁靛牆绻掗崚浼存煕閻曚礁鐏﹂柡浣哥Ч瀵粙顢橀悢鍝勫汲婵犵數鍋為崹鍫曗€﹂崼鈶╁亾濮橆偄宓嗛柡灞剧☉椤繈顢楁径濠傚闁诲氦顫夊ú姗€銆冮崨瀛樺仼闁跨喓濮寸痪褔鎮归幁鎺戝闁崇粯娲熷缁樻媴娓氼垳鍔哥紓浣虹帛閸旀瑩鐛径鎰櫢闁绘ɑ褰冮崵鎴︽⒑閸涘﹤濮﹂柛鐘崇墱缁牆鐣濋崟顒傚幐閻庤鎼╅崰鏍箠瀹ュ棛顩查柕蹇嬪€栭埛鎴︽煠閹帒鍔氶柣蹇婃櫇缁辨帞绱掑Ο鑲╃暭缂備緡鍣崢濂告偩濠靛绀嬫い鎺嗗亾濞寸姰鍨烘穱濠囶敃閿旂粯娈ョ紓浣插亾濞撴埃鍋撶€殿噮鍋婇崺锟犲川椤斿皷鍋撻悽鍛婄厽鐟滃秹骞楀鍛棜閻犳亽鍔庣壕濂告椤掍礁绲诲┑顔煎€块弻鈩冩媴閸濄儛銈吤归悪鍛暤闁圭锕ュ鍕沪缁嬪じ澹曟繝鐢靛У绾板秹鎮″☉銏″€甸柨婵嗛娴滄粌鈹戦鑲┬ｉ柕鍥у婵＄兘鏁傞挊澶岋紦闁诲孩顔栭崰鏇犲垝濞嗗繒鏆︽俊銈呮噺閸ゅ啴鏌嶉崫鍕舵缂佹墎鏅犲濠氬磼濮橆兘鍋撶粙璇炬稑螖閸涱厾鐤囬梺褰掑亰閸擄箓宕崨瀛樺仭婵炲棗绻愰顐︽⒒閸曨偄顏柡灞炬礃瀵板嫬螣閾忛€涘寲缂傚倷璁查崑鎾愁熆閼搁潧濮堥柣鎾存礋閻擃偊宕堕妸锕€闉嶉梺闈╃秬濞咃綁鍩€椤掍緡鍟忛柛鐕佸亰瀹曠喖顢樺☉娆戜簷濠电姷鏁告慨鎾晝閵堝洠鍋撳鐓庡籍闁诡喒鈧枼鏋庨柟瀵稿Х閿涙粍绻涙潏鍓у埌闁圭⒈鍋呯粋鎺曨槼闁靛洤瀚伴、鏇㈡晲閸モ晝鏆ゆ俊鐐€ら崑鍛崲閸繍鍤曟い鏇楀亾鐎规洖銈告慨鈧柣妯哄暱閳ь剚娲熷?            闂傚倷娴囧畷鍨叏閺夋嚚娲Χ婢跺﹤绨ラ梺鍝勮閸庢椽寮€ｎ偁浜滈柡鍥殔娴滈箖鎮楀▓鍨灓闁轰礁顭烽妴浣肝旀担铏规嚌闂佹悶鍎滈崒婵堢闂傚倸鍊搁崐鎼佸磹閹间焦鍋嬮柛鏇ㄥ灠閸ㄥ倿鎮归崶顏嶆⒖閻熸瑥瀚欢鐐烘倵閿濆骸浜楁慨姗堢畱閳规垿鎮欓弶鎴犱桓缂備緡鍠氭繛鈧€殿喓鍔嶇粋鎺斺偓锝庡亞閸橆亪姊虹化鏇炲⒉闁挎艾鈹戦鐓庢殶缂佽鲸甯″畷锟犳倷瀹割喗娈虹紓鍌欐祰妞村摜鏁敓鐘茬畺闁靛繈鍊曞婵嗏攽閻樻彃顏ら柛瀣崌椤㈡岸鍩€椤掑嫬钃熸繛鎴欏灩閸愨偓闂侀潧臎閸愶絽鎮呭┑鐘绘涧閸婄懓顭囧▎鎾崇叀濠㈣埖鍔曠猾宥夋煃瑜滈崜鐔风暦濠靛柈鐔兼嚃閳哄啰鍔稿┑鐘垫暩婵敻鎳濇ィ鍐ㄧ闁绘绮悡娆撴煙娴ｅ啯鐝柡澶婄秺閺屾稓鈧綆鍋呭畷灞绢殽閻愬樊鍎忛柍璇叉捣娴狅箓骞嬮幒鎴?            闂傚倸鍊风粈渚€骞夐敍鍕殰闁圭儤鍤﹀☉妯锋瀻闁圭偓娼欓埀顒傛暬閺岋綁鏁愰崨顖滀紘缂佺偓鍎抽妶鎼佸蓟閻旂厧绠氱憸婊堝吹閻斿吋鐓冪憸婊堝礈閿曞倸鍨傞弶鍫氭櫇閻瑥顭块懜闈涘閸烆垶姊洪幐搴⑩拹闁稿孩濞婅棢闁哄洢鍨洪埛鎺戙€掑顒佹悙濠⒀冪摠缁绘稒鎷呴崘鍙夋悙缂佺姵鑹鹃埞鎴︽偐閸欏顦╅梺?            濠电姷鏁搁崑鐐哄垂閸洖绠伴柟缁㈠枛绾惧鏌熼崜褏甯涢柣鎾崇箻閺屾盯鍩勯崘鈺冾槶濡炪倧璁ｇ粻鎾诲蓟閵堝宸濆┑鐘插亞濡棛绱撴笟鍥ф灈闁挎洦浜獮鍐ㄢ枎閹存繂鐏婇梺鑽ゅ枛椤ｏ附绔熼弴銏♀拺闁告繂瀚鈺傜箾鐎涙ê鍝虹€?
            濠电姷鏁搁崑娑㈩敋椤撶喐鍙忓ù鍏兼綑绾惧潡寮堕崼顐簴濞存粏顫夌换婵囩節閸屾粌顣虹紓浣插亾闁稿瞼鍋為悡鏇熺節闂堟稑顏╅柛鏃€绮庣槐鎺撴媴閸︻厼寮ㄩ梺鍝勭焿缂嶄線骞冨▎鎾崇煑濠㈣埖蓱閿涘懘姊绘担椋庝覆闂傚嫬瀚幑銏ゅ磼濞戞瑦鐝￠梻鍌欑劍閹爼宕曢悽鏉嗗骞橀崜浣虹劶婵犮垼鍩栭崝鏍磻閿濆悿褰掓晲閸偅缍堥梺闈涙处缁诲啰鎹㈠☉銏♀拻闁哄鍨电粊顕€鎮楀▓鍨灆缂侇喗鐟╅獮鍐焺閸愨晛鍔呴梺鎸庣箓濡瑧鈧碍濞婂缁樼瑹閳ь剙顭囪铻為柡鍐ㄧ墛閸婂潡鏌ㄩ弴鐐测偓褰掑磿瀹ュ鐓熼柕蹇曞У閸熺偤鏌涢妶鍛悙妞ゎ叀娉曢幑鍕惞閻熼偊鏆ら梻浣侯焾椤戝棝骞愭繝姘闁告侗鍠氶悷瑙勩亜閺嶃劋绶遍柛鐔奉儏閳规垿鎮╅幇浣告櫛闂佸摜濮甸悧婊勭閹间礁宸濇い鏍ㄤ緱濞肩喎鈹戦悩缁樻锭妞ゆ垵妫濆畷鎴﹀Ω閳哄倻鍘繝鐢靛仜閻忔繃淇婇幐搴濈箚閻忕偛鍊搁埀顒佺箓椤繑绻濆顒€鑰垮┑掳鍊撶粈浣糕枔瀹€鍕拺闁硅偐鍋樼槐姗€鏌涢妷锝呭濞寸姰鍨藉娲川婵犲啫顦╅梺绋款儏椤︻垶顢氶敐澶婄妞ゆ梻鏅崢?            濠电姷鏁搁崑娑㈡偤閵娧冨灊鐎光偓閳ь剟骞冮鈧、鏇㈡晝閳ь剟鎮為崹顐犱簻闁瑰鍋涢婊勩亜閿曞偆妫戠紒杈ㄥ浮婵℃悂鏁冮埀顒傚緤婵犳碍鐓熼柨婵嗘缁犵偟鈧娲橀敃銏犵暦閿濆棗绶炵€光偓鐎ｎ剟妫锋繝鐢靛Х閺佹悂宕戦悙鍝勭闁告稑顭▓浠嬫煙闂傚顦︾紒鐘叉贡閹叉悂寮崼婵婃憰?            濠电姷鏁搁崑鐐哄箰閼姐倕鏋堢€广儱娲﹀畷鏌ユ煕椤愮姴鍓柣鎴ｆ绾惧吋绻涢幋鐑嗙劷缂佹劗鍋涢埞鎴︽倻閸モ晝校闂佺绻戦敃銏犵暦閹达箑绠婚悗娑櫭鎾绘⒑缂佹ê鐏︽い顓炴喘閹箖骞庨懞銉㈡嫼闂傚倸鐗婄粙鎺椝夊▎鎾寸厾閻庡湱濮电涵楣冩煃閻熸澘鏆ｇ€规洘甯￠幃娆撳矗閸屾ê鍔氶柕鍡樺笒椤繈鏁愰崨顒€顥氶梻鍌欐祰濡嫰宕导鏉戠獥闁哄秲鍔嬬换鍡涙倵濞戞瑯鐒介柣鐔风秺閺屽秷顧侀柛鎾跺枛瀹曟椽宕ㄩ弶鎴﹀敹闂佸搫娲ㄩ崐锝夊Ψ閵夊啫缍婇弫鎰板炊閸撲礁濮肩紓鍌氬€哥粔鐢稿箲閸ヮ剙钃熼柡鍥ュ灩楠炪垽鏌￠崶鈺佇ラ柣娑栧灩椤啴濡惰箛鏇炲煂闂佸鏉垮缂侇喗鐟︾换婵嬪礋閵娿儰澹曢梺鎸庣箓缁ㄧ厧霉閻戣姤鐓曢柍杞拌兌閻掓悂鏌＄仦璇插闁宠棄顦灒闁兼祴鏅涙慨浼存煟閻愬顣查柣鐔叉櫊瀵鏁嶉崟銊ヤ壕闁挎繂楠告禍婊冣攽椤旇偐校闁靛洤瀚幆鏃堝焺閸愩劍鐏庨梻浣筋嚃閸ㄦ壆鈧碍婢橀悾鐑藉Ω閿斿墽鐦堥梺鍛婃处閸撴稑螣閸℃稒鈷掗柛灞捐壘閳ь剚鎮傞弫鍐Χ婢跺﹨袝闂侀€炲苯澧扮紒杈ㄥ笒铻栭柍褜鍓熼幆灞炬媴閾忛€涚瑝濠电偞鍨崹鍦矆閸愵喗鐓冮悷娆忓閸斻倕霉濠婂啫鈷旈柟鍙夌摃缁犳稑鈽夐弽銈呬壕闁告稒娼欏敮閻熸粌娴锋禍鍛婃償閵婏箑鈧敻鏌ㄥ┑鍡楁殭濠碉紕鍏橀弻?            濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸蹈閸ヮ剦鏁囬柕蹇曞Х閸旓箑顪冮妶鍡楃伇闁稿骸顭峰畷妤€鐣濋埀顒傛閹烘惟闁靛／鍌濇婵＄偑鍊ら崑鍛崲閸繍鍤曟い鏇楀亾鐎规洘甯℃俊鍫曞幢閳轰焦娅斿┑鐘垫暩閸嬬偤宕归崼鏇炵闁告稑顭▓浠嬫煕濠靛嫬鍔ょ紒鎲嬬畱铻栭柨婵嗘噹閺嗘瑧绱掗悩鍐茬仼闁规彃鎲￠幆鏃堝閳ュ啿浼庨梻浣规偠閸庮噣寮插▎鎾村€?            濠电姷鏁搁崑娑㈩敋椤撶喐鍙忛柡澶嬪殮瑜版帗鍊诲┑顔藉姀閸嬫捇宕橀鑺ユ珳婵犮垼娉涢敃锕傛偩鏉堛劎绠鹃弶鍫濆⒔閹吋銇勯敐鍕煓鐎规洘鍨块獮妯尖偓娑櫭鎾绘⒑缂佹ê鐏︽い顓炴喘閹箖骞庨懞銉㈡嫼濠电偠灏濠勮姳婵犳碍鐓曟慨姗嗗墻閸庢棃鏌熼姘殻闁诡喚鏅划娆撳箰鎼达紕銈舵繝寰锋澘鈧呯不閹达箑鐤炬繝闈涱儏缁犳娊鏌￠崘锝呬壕闁诲孩纰嶅畝鎼佸箖瑜版帒鐐婇柕濞垮劤缁佺兘姊烘潪鎵槮闁绘牕銈稿璇测槈閵忊剝娅嗛梺鍛婄箓鐎氼剟鈥栨径鎰拺闂侇偆鍋涢懟顖炲礉椤栫偞鐓曢柡鍥ュ妼娴滄粌顭块悷鎵ⅵ婵﹥妞藉Λ鍐ㄢ槈濞嗘ɑ顥犻梻浣虹帛鐢亪姊介崟顖氱柧闁割偅娲橀崑鎰版偣閸ヮ亜鐨洪柣锝呭船閳规垿鎮╃紒妯婚敪闁诲孩鐨滈崶褏锛涢梺瑙勫劤閻°劍鍒婇幘顔解拺闁割煈鍣崕鎴炵節瑜嶇€氼喚妲愰幘璇茬＜婵﹩鍏橀崑鎾舵兜閸涱喗鍣烽梻鍌欒兌缁垶銆冮崨鏉戠婵犲﹤瀚々鐑芥煥閺囩偛鈧悂鎮為崹顐犱簻闁圭儤鍨甸鈺傛交?            闂傚倸鍊烽悞锕傚箖閸洖纾块柤纰卞墰閻瑩鐓崶銊р槈闁绘帒鐏氶妵鍕箣閿濆棛銆婇梺鍛婃煥缁夊墎妲愰幒鎾剁懝濠电姴瀚弳銈夋⒑缂佹ü绶遍柛鐘冲哺閸┾偓妞ゆ帊鑳堕埊鏇熴亜椤撶偞鎼愮悮娆愮節婵犲倻澧涢柛瀣剁秮閺岋綁骞囬妸锔芥緬闂佺顑嗛幑鍥箠閻樻椿鏁嗛柛灞剧☉閺嬶箓姊绘担鍛婃儓妞わ富鍋婂鎻掝煥閸繄顦梺鍝勬川閸犳挾绮婚崜褉鍋撻獮鍨姎妞わ富鍨崇划璇测槈閵忋垹褰勯梺鎼炲劘閸斿绂嶉姀銈嗙厸濠㈣泛顦遍惌娆撴煙椤旂虎鏀版い锕佸皺缁辨帞绱掑Ο铏逛紝闂?            濠电姷鏁搁崑娑㈩敋椤撶喐鍙忓ù鍏兼綑绾惧潡鏌＄仦璇插姎闁告垹濞€閺屾盯骞囬棃娑欑亪缂備讲鍋撶€光偓閸曨剛鍘搁悗骞垮劚閸燁偅淇婇悡搴唵闁荤喐澹嗘晶锕傛煙椤旇偐绉洪柟顔界懇閸┾剝鎷呯化鏇熷珶闂佽瀛╅鏍窗濡ゅ啠鍋撶粭娑樺枤閻掕棄鈹戦悩瀹犲缂佺媭鍨抽埀顒€鍘滈崑鎾绘煃瑜滈崜娑氬垝婵犳艾唯闁冲搫鍊婚崢閬嶆⒑閸濆嫬鏆婇柛瀣崌閹绮☉妯诲闁稿骸绉撮埞鎴﹀磼濠婂海鍔搁梺缁樺姇閿曨亜顕ｉ崼鏇為唶婵犻潧妫岄幐鍐磽娴ｆ彃浜炬繝銏ｆ硾鑹屾俊鎻掔墦閺岀喖骞嗚閿涘秶鈧稒绻冪换娑氣偓鐢殿焾鏍＄紓浣割儐閹告儳危閹版澘绠抽柟瀛樻⒐閺傗偓闂備胶纭跺褔寮插☉妯锋灁闁归棿鐒﹂埛鎴︽煕濠靛棗顏╁ù婊呭仱閺屾稑鈻庣仦鎴掑濠碉紕鍋戦崐鏍鸿箛娑樺瀭濞寸姴顑呴弸浣衡偓骞垮劚濞诧絽鈻介鍫熺參婵☆垯璀﹀Σ褰掓⒑濞嗘儳寮慨濠傛惈鐓ょ紓浣姑埢蹇涙⒑閸涘﹥鐓ユい锔炬暬瀹曟椽鍩€椤掍降浜滈柟鍝勭Х閸忓矂鏌ｉ鐑嗗剶闁哄矉缍佹俊姝岊槼闁哄棭鍓氭穱濠囶敃閵忊€虫闂佸摜濮撮敃銈堢亽閻庣懓瀚伴。锔界珶閺囩偐鏀介柣妯肩帛濞懷囨煕閻斿搫鈻堢€规洘鍨块獮姗€骞囨担鍝勫汲闂備礁鎲￠崝鎴﹀礉鐏炵煫褰掝敋閳ь剟寮婚敐澶嬪亹闂傚牊绋愬Ч妤€鈹戦敍鍕彙闁搞儜鍛Е婵＄偑鍊栫敮濠囨嚄閼稿吀绻嗛柛銉墯閻撳啰鎲稿鍫濈婵ê宕崹婵堚偓骞垮劚椤︿即宕戠€ｎ喗鐓曟繝闈涘閸旀瑦绻涘畝濠侀偗闁哄苯绉烽¨渚€鏌涢幘璺烘灈鐎规洖缍婇獮鍡氼槷闁衡偓娴犲鐓曟い鎰Т閻忣亪鏌熼銈囩М婵﹥妞藉畷顐﹀礋椤掍焦瀚抽梻浣告惈鐞氼偊宕濋幋锔惧祦闊洦绋戠粻銉︺亜閺冨洦顥夊ù鐘冲浮濮婃椽妫冨☉杈╁姼闂佸憡鏌ㄩ惌鍌炲箖瑜嶉～婵嬫嚋閻㈤潧甯?            濠电姷鏁搁崑娑㈡偤閵娧冨灊鐎广儱顦拑鐔兼煥濠靛棭妲搁柣鎺戠仛閵囧嫰骞嬮敐鍛Х闂佺绻愰張顒傛崲濞戙垹绾ч柟鎼幗妤旈梻渚€鈧偛鑻晶鍙夈亜椤愩埄妲洪柛鎺撳笩缁犳稑鈽夊▎蹇撳闂備胶绮濠氬储瑜嶉—鍐╃鐎ｎ偄鈧爼鏌ｉ幇顖涚【濞存粏顫夐妵鍕箻鐠哄搫濡虹紓?            缂傚倸鍊搁崐鎼佸磹閻戣姤鍊块柨鏇炲€归崑锟犳煏婢跺棙娅呴柛姘愁潐閵囧嫰骞樼捄鐩掋儳绱掗悩铏棃闁哄被鍔戝鏉懳旈埀顒佺妤ｅ啯鈷戦悹鍥ｂ偓铏亪闂備礁搴滅紞渚€鐛崘顔肩闁芥ê顦遍ˇ鏉款渻閵堝棗濮﹂柛瀣娣囧﹪骞庨懞銉㈡嫼闁荤喐鐟ョ€氼厾娆㈤懠顒傜＜缂備焦锚婵秹鏌曢崱妤€鈧寧淇婇幖浣哥厸闁稿本鑹炬竟鍕⒒娴ｅ憡鍟炴繛璇х畵瀹曘垽骞栨担鍛婄€悗骞垮劚椤︿即鎮″▎鎴犳／闁哄鐏濋懜瑙勵殽閻愭潙鐏撮柡灞界Х椤т線鏌涜箛鏃傘€掔紒顔肩墛缁楃喖鍩€椤掑嫬违闁告稒鎯岄弫鍐煏閸繂鈧憡绂嶆ィ鍐╃厽闁绘梻顭堥ˉ瀣煟閿濆骸寮柡灞界Х椤т線鏌涜箛鏃傛创闁诡喚鍋ら弫鍐磼濞戞ê澹勯梻浣圭湽閸ㄥ鈥﹂崼銉ョ闁割偅娲橀悡鐔兼煙鐎甸晲绱虫い蹇撴缁躲倗鎲搁悧鍫濈瑲闁绘挻娲樻穱濠囧Χ閸屾矮澹曟繝鐢靛仜閻即宕愬Δ鍐╊潟闁规崘顕х粻濠氭煠閹间焦娑фい搴㈢☉椤啴濡堕崱姗嗘⒖闂侀潧妫岄崑鎾绘⒑?            闂傚倸鍊烽懗鍫曗€﹂崼婢濈懓顫濈捄鍝勫亶閻熸粎澧楃敮鎺楁倿閸偁浜滈柟杈剧到閸旂敻鏌涜箛鎾剁伇缂佽鲸甯￠、娆撳传閸曨偒鐎烽梻?            婵犵數濮烽弫鎼佸磻閻愬搫绠伴柟闂寸缁犵姵淇婇婵勨偓鈧柡瀣墵閺屾洟宕煎┑鎰ч梺绋款儐缁诲牓寮诲☉銏犲嵆闁靛鍎遍～鈺傜節閵忋垺鍤€闁绘鎹囧濠氭晸閻樿尙鍔﹀銈嗗笒閸婄懓鐣锋径鎰叄闊洦娲橀崵鈧梺鍝勬４缁绘繂顫?
            闂傚倷绀侀幖顐λ囬鐐村亱濠电姴娲ょ粻浼存煙闂傚顦﹂柛姘愁潐閵囧嫰骞樼捄鐩掞綁鏌涢悢閿嬫儓闂囧鏌ㄥ┑鍡樺櫤閻犳劏鍓濈换婵嬪焵椤掑嫬绠绘い鏃傛櫕閸?            闂傚倸鍊风粈渚€骞栭锕€纾圭紒瀣紩濞差亶鏁囬柍璺烘惈椤︾敻鐛Ο鍏煎珰闁肩⒈鍓涢崢顒勬⒒娓氣偓濞佳囨偋閸℃あ娑樷枎閹惧啿鐎梺闈涚箞閸婃牠鎮￠弴銏㈠彄闁搞儯鍔嶉埛鎰版煕婵犲啫濮堢紒缁樼⊕瀵板嫰宕煎┑鍐ㄤ壕婵犻潧顑呴弸浣衡偓骞垮劚濞诧絽鈻介鍫熺厱闁圭偓顨呯€氼喖螣閸℃稒鈷掗柛灞捐壘閳ь剚鎮傞弫鍐晝閸屾碍鐎梺褰掓？闂勫秹鍩€椤掆偓閸熸挳寮幇鏉跨倞闁冲搫鍟伴崢鐘崇節绾版ɑ顫婇柛銊︽緲閿曘垽鏌嗗搴㈡櫇婵炲濮撮鍡涙偂濞戙垺鐓曢悘鐐插⒔閻銇勮箛鏇炴灁缂佽鲸甯楀蹇涘Ω瑜忛悿鍕旈悩闈涗沪閻㈩垽绻濋妴渚€寮崼婵堝€為梺鍐叉惈閸燁偉鈪搁梻鍌氬€风粈渚€骞夐敓鐘茬鐟滅増甯掔壕璇差熆閼搁潧濮囩紒鐘侯潐閵囧嫰骞囬崜浣稿煂濡炪倖娲濇ご鍛婄┍婵犲浂鏁嶆繝闈涙濮规鈹戦悙宸Ч婵炶尙鍠栧濠氬Ω閵夈垺顫嶅┑鈽嗗灥閸嬫劖瀵奸崶鈺冪＝濞撴艾娲ら弸鏃堟煕閺冣偓閸ㄧ敻顢氶敐澶婄濞达絽鎽滈ˇ鏉款渻閵堝棗濮х紒杈ㄦ礋閹苯螖閸涱喒鎷洪柡澶屽仧婢ф绮婃导瀛樼厵婵炶尪顔婄花鑺ヤ繆閸欏濮囬柍瑙勫灴瀹曠厧顫濋鍨棜婵犵數鍋為崹鍫曟偡椤栨埃鏋旈柡鍥ュ灪閻?00闂傚倷娴囬褏鈧稈鏅濈划娆撳箳濡炲皷鍋撻崘顔煎窛妞ゆ棃鏁弸娆撴椤愩垺澶勭紒瀣灴閹苯螖閸涱喚鍘介梺褰掑亰閸ㄥ秹骞掑Δ鈧壕?            闂傚倸鍊风粈渚€骞栭锕€纾圭紒瀣紩濞差亝鏅濋柍褜鍓熼弫鍐閵堝孩鏅┑鐘绘涧閻楀繘寮堕幖浣光拺闁告稑锕﹂埊鏇㈡煟閿濆簼閭柛鈹惧亾濡炪倖宸婚崑鎾剁磼缂佹◤顏堫敋閿濆棛顩烽悗锝庝簽閸婄偤姊洪棃娑辩叚闂傚嫬瀚埢鎾愁潨閳ь剙顫忓ú顏勭閹艰揪绲烘慨鍥╃磼閻愵剚绶茬紒澶嬫尦閺佸啴濮€閳ユ剚鍤ら梺鍝勵槹閸ㄥ綊藝椤愶附鈷戠紒顖涙礀婢у弶銇勯鐐村枠闁糕斁鍋撳銈嗗笒閸燁偉顣跨紓鍌欑椤戝懘藝閺夋鐒芥い蹇撶墕缁犮儲銇勯弮鈧崕鎶藉焵椤掑倸浠х紒杈ㄦ崌瀹曟帒鈻庨幒鎴濆腐闂備礁鎽滄慨鐢稿礉濞嗗浚鍤曢柟闂寸缁€鍐┿亜閺冨洤浜规い锕備憾濮婃椽宕崟顓涙瀱闂佸憡鎸婚悷銊╁Φ閹伴偊鏁嶉柣鎰嚟閸樺崬鈹戦悙鏉戠仸妞ゎ厼娲鎼佸礃椤忓棛锛滄繛杈剧到婢瑰﹪鎮￠懖鈹惧亾濞堝灝鏋熼柟顔煎€块悰顕€宕堕鈧粈鍫澝归敐鍕劅婵℃彃鍢查埞鎴︽倷瀹割喖娈舵繝娈垮枟閹告娊鐛繝鍌ゅ悑闁搞儺鐓堥崑銊╂⒑閸撹尙鍘涢柛瀣缁粯銈ｉ崘鈺冨幈濠电偛妫欓崝锕傛倿閼恒儯浜滈柡鍌涘閸犳﹢鏌＄仦鍓р槈闁宠姘︾粻娑㈠箻椤栨矮澹曟繛瀵稿Т椤戝懘鎮為崹顐犱簻闁瑰搫妫楁禍楣冩⒑閸濄儱鏋傞柛鏃€鍨垮畷娲焵?            """;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.base-url:http://104.197.139.51:3000/v1}")
    private String apiBaseUrl;

    @Value("${gemini.text-model:gemini-3-flash-preview}")
    private String textModel;

    @Value("${gemini.vision-model:gemini-3-flash-preview}")
    private String visionModel;

    @Value("${gemini.vision-models:}")
    private String visionModels;

    @Value("${gemini.vision-payload-formats:openai-image-url,openai-image-url-string}")
    private String visionPayloadFormats;

    @Value("${gemini.temperature:0.2}")
    private double temperature;

    @Value("${gemini.max-tokens:2000}")
    private int maxTokens;

    @Value("${gemini.max-image-bytes:5242880}")
    private long maxImageBytes;

    @Value("${gemini.image-model:}")
    private String imageModel;

    @Value("${gemini.image-models:}")
    private String imageModels;

    @Value("${scene-image.provider:openai-compatible}")
    private String sceneImageProvider;

    @Value("${scene-image.protocol:disabled}")
    private String sceneImageProtocol;

    @Value("${scene-image.api.key:}")
    private String sceneImageApiKey;

    @Value("${scene-image.api.base-url:}")
    private String sceneImageApiBaseUrl;

    @Value("${scene-image.model:}")
    private String sceneImageModel;

    @Value("${scene-image.models:}")
    private String sceneImageModels;

    @Value("${scene-image.response-format:b64_json}")
    private String sceneImageResponseFormat;

    @Value("${scene-image.size:1024x1024}")
    private String sceneImageSize;

    @Value("${scene-image.count:1}")
    private int sceneImageCount;

    @Value("${scene-image.chat-max-tokens:4096}")
    private int sceneImageChatMaxTokens;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TokenTracker tokenTracker;

    private GeminiSceneImageSupport sceneImageSupport() {
        return new GeminiSceneImageSupport(
                imageModel,
                imageModels,
                visionModel,
                visionModels,
                sceneImageModel,
                sceneImageModels,
                sceneImageProtocol,
                sceneImageResponseFormat,
                sceneImageSize,
                sceneImageCount,
                sceneImageChatMaxTokens,
                temperature
        );
    }

    private GeminiSceneImageGatewaySupport sceneImageGatewaySupport() {
        return new GeminiSceneImageGatewaySupport(
                sceneImageSupport(),
                sceneImageProvider,
                sceneImageApiKey,
                sceneImageApiBaseUrl,
                apiKey,
                apiBaseUrl
        );
    }

    private GeminiOneApiRequestSupport oneApiRequestSupport() {
        return new GeminiOneApiRequestSupport(
                apiKey,
                apiBaseUrl,
                textModel,
                visionModel,
                temperature,
                maxTokens
        );
    }

    private GeminiPromptSupport promptSupport() {
        return new GeminiPromptSupport(DEFAULT_FACE_CULTURAL_PROMPT);
    }

    private GeminiResponseParser responseParser() {
        return new GeminiResponseParser(objectMapper, sceneImageGatewaySupport().resolveSceneImageProviderName());
    }

    private GeminiFallbackSupport fallbackSupport() {
        return new GeminiFallbackSupport();
    }

    private GeminiSceneImageExecutor sceneImageExecutor() {
        return new GeminiSceneImageExecutor(
                httpClient,
                objectMapper,
                tokenTracker,
                responseParser(),
                fallbackSupport(),
                sceneImageGatewaySupport().resolveSceneImageProviderName()
        );
    }

    public YijingSceneImageResponse generateYijingSceneImage(YijingSceneImageRequest request) throws Exception {
        sceneImageGatewaySupport().validateSceneImageGenerationConfiguration();
        GeminiPromptSupport promptSupport = promptSupport();

        String prompt = promptSupport.buildYijingSceneImagePrompt(request);
        SceneImageExecutionResult executionResult = executeSceneImageGenerationRequest(prompt);
        boolean hasImage = StringUtils.hasText(executionResult.imageBase64())
                || StringUtils.hasText(executionResult.imageUrl());

        if (!hasImage) {
            log.error(
                    "Scene image request finished without image payload | provider={}, model={}, sceneCategory={}, generationMode={}, uri={}, revisedPromptLength={}, visualSummaryLength={}, displayText={}",
                    executionResult.provider(),
                    executionResult.model(),
                    promptSupport.resolveSceneCategory(request.getQuestion(), request.getInterpretation()),
                    executionResult.generationMode(),
                    executionResult.uri(),
                    executionResult.revisedPrompt() == null ? 0 : executionResult.revisedPrompt().length(),
                    executionResult.visualSummary() == null ? 0 : executionResult.visualSummary().length(),
                    abbreviate(executionResult.displayText())
            );
            throw new BusinessException(
                    "Scene image provider returned no usable image payload",
                    HttpStatus.BAD_GATEWAY
            );
        }

        YijingSceneImageResponse response = YijingSceneImageResponse.builder()
                .provider(executionResult.provider())
                .model(executionResult.model())
                .sceneCategory(promptSupport.resolveSceneCategory(request.getQuestion(), request.getInterpretation()))
                .prompt(prompt)
                .revisedPrompt(executionResult.revisedPrompt())
                .imageBase64(executionResult.imageBase64())
                .imageUrl(executionResult.imageUrl())
                .generationMode(executionResult.generationMode())
                .imageSupported(hasImage)
                .visualSummary(executionResult.visualSummary())
                .negativePrompt(executionResult.negativePrompt())
                .displayText(executionResult.displayText())
                .build();

        log.info(
                "Scene image result ready | provider={}, model={}, sceneCategory={}, generationMode={}, imageSupported={}, hasImageUrl={}, hasImageBase64={}, revisedPromptLength={}, visualSummaryLength={}, displayTextLength={}",
                response.getProvider(),
                response.getModel(),
                response.getSceneCategory(),
                response.getGenerationMode(),
                response.getImageSupported(),
                StringUtils.hasText(response.getImageUrl()),
                StringUtils.hasText(response.getImageBase64()),
                response.getRevisedPrompt() == null ? 0 : response.getRevisedPrompt().length(),
                response.getVisualSummary() == null ? 0 : response.getVisualSummary().length(),
                response.getDisplayText() == null ? 0 : response.getDisplayText().length()
        );

        return response;
    }

    public GeminiFaceAnalysisResponse analyzeFace(GeminiFaceAnalysisRequest request) throws Exception {
        oneApiRequestSupport().validateOneApiConfiguration();

        String mimeType = normalizeMimeType(request.getMimeType());
        if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            throw new BusinessException("Only JPG, PNG, and WEBP images are supported");
        }

        String imageBase64 = sanitizeBase64(request.getImageBase64());
        if (!StringUtils.hasText(imageBase64)) {
            throw new BusinessException("Image data is invalid");
        }

        long imageBytes = estimateDecodedBytes(imageBase64);
        if (imageBytes <= 0) {
            throw new BusinessException("Image data is invalid");
        }
        if (imageBytes > maxImageBytes) {
            throw new BusinessException("闂傚倸鍊烽悞锕傚箖閸洖纾块弶鍫涘妽濞呯娀鏌ら幁鎺戝姕婵炲懐濞€閺屸€愁吋閸愩劌顬嬮梺宕囩帛濮婂鍩€椤掆偓缁犲秹宕曢柆宥嗗亱闁糕剝绋戦崒銊╂煙缂併垹鏋熼柛瀣ㄥ€濋弻鐔兼倻濡櫣浠搁梺鎼炲€愰崑鎾剁磽?5MB");
        }

        VisionExecutionResult executionResult = executeVisionRequest(
                imageBase64,
                mimeType,
                promptSupport().buildEnhancedPrompt(request.getPrompt()),
                maxTokens,
                "face analysis"
        );
        return GeminiFaceResponseMapper.fromMap(
                responseParser().parseResponse(executionResult.responseBody(), executionResult.model())
        );
    }

    public GeminiProbeResponse probeText(String prompt) throws Exception {
        oneApiRequestSupport().validateOneApiConfiguration();

        String effectivePrompt = StringUtils.hasText(prompt)
                ? prompt.trim()
                : "Reply with exactly OK.";

        Map<String, Object> requestBody = oneApiRequestSupport().buildTextProbeRequestBody(effectivePrompt);
        return executeProbe(textModel, requestBody, "text");
    }

    public GeminiProbeResponse probeVision(GeminiFaceAnalysisRequest request) throws Exception {
        oneApiRequestSupport().validateOneApiConfiguration();

        String mimeType = normalizeMimeType(request.getMimeType());
        if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            throw new BusinessException("Only JPG, PNG, and WEBP images are supported");
        }

        String imageBase64 = sanitizeBase64(request.getImageBase64());
        if (!StringUtils.hasText(imageBase64)) {
            throw new BusinessException("Image data is invalid");
        }

        long imageBytes = estimateDecodedBytes(imageBase64);
        if (imageBytes <= 0) {
            throw new BusinessException("Image data is invalid");
        }
        if (imageBytes > maxImageBytes) {
            throw new BusinessException("闂傚倸鍊烽悞锕傚箖閸洖纾块弶鍫涘妽濞呯娀鏌ら幁鎺戝姕婵炲懐濞€閺屸€愁吋閸愩劌顬嬮梺宕囩帛濮婂鍩€椤掆偓缁犲秹宕曢柆宥嗗亱闁糕剝绋戦崒銊╂煙缂併垹鏋熼柛瀣ㄥ€濋弻鐔兼倻濡櫣浠搁梺鎼炲€愰崑鎾剁磽?5MB");
        }

        String effectivePrompt = StringUtils.hasText(request.getPrompt())
                ? request.getPrompt().trim()
                : "Describe this image in one short sentence.";

        VisionExecutionResult executionResult = executeVisionRequest(
                imageBase64,
                mimeType,
                effectivePrompt,
                Math.min(maxTokens, 300),
                "vision probe"
        );
        String content = responseParser().parseRawResponseText(executionResult.responseBody());
        return GeminiProbeResponse.builder()
                .model(executionResult.model())
                .uri(executionResult.uri().toString())
                .content(content)
                .contentLength(content == null ? 0 : content.length())
                .build();
    }

    private VisionExecutionResult executeVisionRequest(String imageBase64,
                                                       String mimeType,
                                                       String prompt,
                                                       int tokenLimit,
                                                       String scenario) throws Exception {
        GeminiOneApiRequestSupport requestSupport = oneApiRequestSupport();
        List<String> modelsToTry = resolveVisionModelsToTry();
        List<String> payloadFormatsToTry = resolveVisionPayloadFormatsToTry();
        List<String> attemptedModels = new ArrayList<>();
        BusinessException lastBusinessException = null;
        URI requestUri = requestSupport.buildRequestUri();

        for (int modelIndex = 0; modelIndex < modelsToTry.size(); modelIndex++) {
            String candidateModel = modelsToTry.get(modelIndex);
            attemptedModels.add(candidateModel);
            for (int formatIndex = 0; formatIndex < payloadFormatsToTry.size(); formatIndex++) {
                String payloadFormat = payloadFormatsToTry.get(formatIndex);
                Map<String, Object> requestBody = requestSupport.buildVisionRequestBody(
                        imageBase64,
                        mimeType,
                        prompt,
                        tokenLimit,
                        candidateModel,
                        payloadFormat
                );
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(requestUri)
                        .header("Content-Type", "application/json")
                        .headers(requestSupport.buildAuthorizationHeaders())
                        .timeout(Duration.ofSeconds(100))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                        .build();

                log.info("Calling Gemini {} via One-API | model={}, uri={}, payloadFormat={}, modelAttempt={}/{}, formatAttempt={}/{}",
                        scenario,
                        candidateModel,
                        requestUri,
                        payloadFormat,
                        modelIndex + 1,
                        modelsToTry.size(),
                        formatIndex + 1,
                        payloadFormatsToTry.size());

                LocalDateTime visionCallStart = LocalDateTime.now();
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    tokenTracker.trackFromResponse(response.body(), candidateModel, "gemini-" + scenario, visionCallStart);
                    return new VisionExecutionResult(candidateModel, requestUri, response.body());
                }

                String responseBody = response.body();
                log.error("Gemini {} failed | model={}, uri={}, payloadFormat={}, status={}, body={}",
                        scenario,
                        candidateModel,
                        requestUri,
                        payloadFormat,
                        response.statusCode(),
                        abbreviate(responseBody));

                boolean hasNextFormat = formatIndex < payloadFormatsToTry.size() - 1;
                boolean hasNextModel = modelIndex < modelsToTry.size() - 1;
                lastBusinessException = new BusinessException(
                        appendAttemptedModels(
                                buildFailureMessage(response.statusCode(), responseBody, candidateModel, requestUri),
                                attemptedModels,
                                hasNextFormat || hasNextModel
                        ),
                        mapUpstreamFailureStatus(response.statusCode(), responseBody),
                        buildFailureDetails(attemptedModels, candidateModel, response.statusCode(), payloadFormat, requestUri)
                );

                if (hasNextFormat && shouldTryNextVisionModel(response.statusCode(), responseBody)) {
                    log.warn("Gemini vision payload format {} failed for model {}, switching to the next payload format",
                            payloadFormat, candidateModel);
                    continue;
                }

                if (hasNextModel && shouldTryNextVisionModel(response.statusCode(), responseBody)) {
                    log.warn("Gemini vision model {} failed, switching to the next configured model", candidateModel);
                    break;
                }

                throw lastBusinessException;
            }
        }

        throw lastBusinessException != null
                ? lastBusinessException
                : new BusinessException(
                        appendAttemptedModels("Gemini vision call failed", attemptedModels, false),
                        HttpStatus.BAD_GATEWAY,
                        buildFailureDetails(attemptedModels, null, null, null, requestUri)
                );
    }

    private SceneImageExecutionResult executeSceneImageGenerationRequest(String prompt) throws Exception {
        GeminiSceneImageGatewaySupport gatewaySupport = sceneImageGatewaySupport();
        String protocol = gatewaySupport.resolveSceneImageProtocol();
        URI requestUri = gatewaySupport.buildSceneImageRequestUri();
        GeminiResponseParser.SceneImageExecutionPayload payload = sceneImageExecutor().executeFirstStage(
                new GeminiSceneImageExecutor.FirstStageRequest(
                        resolveSceneImageModelsToTry(),
                        prompt,
                        protocol,
                        requestUri,
                        gatewaySupport.buildSceneImageAuthorizationHeaders(),
                        this::buildSceneImageGenerationRequestBody
                )
        );

        SceneImageExecutionResult executionResult = new SceneImageExecutionResult(
                payload.provider(),
                payload.model(),
                payload.uri(),
                payload.imageBase64(),
                payload.imageUrl(),
                payload.revisedPrompt(),
                payload.visualSummary(),
                payload.negativePrompt(),
                payload.displayText(),
                payload.generationMode()
        );

        if ("prompt_only".equals(executionResult.generationMode())) {
            log.warn(
                    "Scene image first-stage returned plan instead of image | model={}, uri={}, revisedPromptLength={}, visualSummaryLength={}, displayText={}",
                    executionResult.model(),
                    executionResult.uri(),
                    executionResult.revisedPrompt() == null ? 0 : executionResult.revisedPrompt().length(),
                    executionResult.visualSummary() == null ? 0 : executionResult.visualSummary().length(),
                    abbreviate(executionResult.displayText())
            );
            SceneImageExecutionResult secondStageResult = executeSecondStageImageGeneration(executionResult, prompt);
            if (secondStageResult != null) {
                return secondStageResult;
            }
            throw new BusinessException(
                    "Scene image planning returned text only, but second-stage image generation did not produce an image",
                    HttpStatus.BAD_GATEWAY,
                    buildFailureDetails(
                            List.of(executionResult.model()),
                            executionResult.model(),
                            200,
                            "scene-image:" + protocol + ":prompt-only",
                            requestUri
                    )
            );
        }

        return executionResult;
    }

    private SceneImageExecutionResult executeSecondStageImageGeneration(SceneImageExecutionResult planningResult,
                                                                       String originalPrompt) throws Exception {
        GeminiSceneImageGatewaySupport gatewaySupport = sceneImageGatewaySupport();
        String drawingPrompt = promptSupport().buildSecondStageDrawingPrompt(
                planningResult.revisedPrompt(),
                planningResult.negativePrompt(),
                originalPrompt
        );
        GeminiSceneImageExecutor.SecondStageExecutionResult executionResult = sceneImageExecutor().executeSecondStage(
                new GeminiSceneImageExecutor.SecondStageRequest(
                        resolveSecondStageProtocolsToTry(),
                        resolveSecondStageImageModelsToTry(),
                        drawingPrompt,
                        gatewaySupport::buildSecondStageSceneImageRequestUri,
                        gatewaySupport.buildSecondStageAuthorizationHeaders(),
                        this::buildSecondStageSceneImageRequestBody
                )
        );

        if (executionResult.imageResult() != null) {
            GeminiSceneImageExecutor.SecondStageImageResult imageResult = executionResult.imageResult();
            GeminiResponseParser.GeneratedImagePayloadData payload = imageResult.payload();
            return new SceneImageExecutionResult(
                    gatewaySupport.resolveSceneImageProviderName(),
                    imageResult.model(),
                    imageResult.uri(),
                    payload.imageBase64(),
                    payload.imageUrl(),
                    StringUtils.hasText(payload.revisedPrompt()) ? payload.revisedPrompt() : planningResult.revisedPrompt(),
                    planningResult.visualSummary(),
                    planningResult.negativePrompt(),
                    "Auto-generated via second-stage scene image endpoint",
                    "second_stage_image"
            );
        }

        log.warn(
                "Second-stage image generation exhausted all models, returning prompt-only result | attemptedModels={}, fallbackMode={}",
                String.join(",", executionResult.attemptedModels()),
                planningResult.generationMode()
        );
        if (executionResult.lastBusinessException() != null) {
            log.warn("Second-stage final failure reason: {}", executionResult.lastBusinessException().getMessage());
        }
        return null;
    }

    private GeminiProbeResponse executeProbe(String model, Map<String, Object> requestBody, String probeType) throws Exception {
        GeminiOneApiRequestSupport requestSupport = oneApiRequestSupport();
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        URI requestUri = requestSupport.buildRequestUri();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(requestUri)
                .header("Content-Type", "application/json")
                .headers(requestSupport.buildAuthorizationHeaders())
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("Calling Gemini {} probe via One-API | model={}, uri={}", probeType, model, requestUri);

        LocalDateTime probeCallStart = LocalDateTime.now();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String responseBody = response.body();
            log.error("Gemini {} probe failed | model={}, uri={}, status={}, body={}",
                    probeType, model, requestUri, response.statusCode(), abbreviate(responseBody));
            throw new BusinessException(
                    buildFailureMessage(response.statusCode(), responseBody, model, requestUri),
                    mapUpstreamFailureStatus(response.statusCode(), responseBody)
            );
        }

        tokenTracker.trackFromResponse(response.body(), model, "gemini-probe-" + probeType, probeCallStart);
        String content = responseParser().parseRawResponseText(response.body());
        return GeminiProbeResponse.builder()
                .model(model)
                .uri(requestUri.toString())
                .content(content)
                .contentLength(content == null ? 0 : content.length())
                .build();
    }

    private List<String> resolveVisionModelsToTry() {
        List<String> models = new ArrayList<>();
        appendVisionModel(models, visionModel);
        if (StringUtils.hasText(visionModels)) {
            for (String candidate : visionModels.split(",")) {
                appendVisionModel(models, candidate);
            }
        }
        if (models.isEmpty()) {
            models.add("gemini-3-flash-preview");
        }
        return models;
    }

    private List<String> resolveImageModelsToTry() {
        return sceneImageSupport().resolveImageModelsToTry();
    }

    private List<String> resolveSceneImageModelsToTry() {
        return sceneImageSupport().resolveSceneImageModelsToTry();
    }

    private List<String> resolveSecondStageImageModelsToTry() {
        return sceneImageSupport().resolveSecondStageImageModelsToTry();
    }

    private List<String> resolveSecondStageProtocolsToTry() {
        return sceneImageSupport().resolveSecondStageProtocolsToTry();
    }

    private List<String> resolveVisionPayloadFormatsToTry() {
        List<String> formats = new ArrayList<>();
        if (StringUtils.hasText(visionPayloadFormats)) {
            for (String candidate : visionPayloadFormats.split(",")) {
                appendVisionPayloadFormat(formats, candidate);
            }
        }
        if (formats.isEmpty()) {
            formats.add("openai-image-url");
        }
        return formats;
    }

    private void appendVisionModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        String lowered = normalized.toLowerCase();
        if (lowered.contains("embedding")) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private void appendVisionPayloadFormat(List<String> formats, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim().toLowerCase();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!normalized.equals("openai-image-url") && !normalized.equals("openai-image-url-string")) {
            return;
        }
        if (!formats.contains(normalized)) {
            formats.add(normalized);
        }
    }

    private boolean shouldTryNextVisionModel(int statusCode, String responseBody) {
        return fallbackSupport().shouldTryNextVisionModel(statusCode, responseBody);
    }

    private boolean shouldTryNextSceneImageModel(int statusCode, String responseBody) {
        return fallbackSupport().shouldTryNextSceneImageModel(statusCode, responseBody);
    }

    private String appendAttemptedModels(String message, List<String> attemptedModels, boolean hasNextModel) {
        return fallbackSupport().appendAttemptedModels(message, attemptedModels, hasNextModel);
    }

    private GeminiFailureDetails buildFailureDetails(List<String> attemptedModels,
                                                     String lastModel,
                                                     Integer lastStatus,
                                                     String lastPayloadFormat,
                                                     URI requestUri) {
        return fallbackSupport().buildFailureDetails(
                attemptedModels == null ? List.of() : List.copyOf(attemptedModels),
                lastModel,
                lastStatus,
                lastPayloadFormat,
                requestUri
        );
    }

    private Map<String, Object> buildSceneImageGenerationRequestBody(String modelName, String prompt) {
        return sceneImageSupport().buildSceneImageGenerationRequestBody(modelName, prompt);
    }

    private Map<String, Object> buildSceneImageImagesRequestBody(String modelName, String prompt) {
        return sceneImageSupport().buildSceneImageImagesRequestBody(modelName, prompt);
    }

    private Map<String, Object> buildSceneImageChatRequestBody(String modelName, String prompt) {
        return sceneImageSupport().buildSceneImageChatRequestBody(modelName, prompt);
    }

    private Map<String, Object> buildSecondStageSceneImageRequestBody(String modelName, String prompt, String protocol) {
        return sceneImageSupport().buildSecondStageSceneImageRequestBody(modelName, prompt, protocol);
    }

    private String buildFailureMessage(int statusCode, String responseBody, String targetModel, URI requestUri) {
        return fallbackSupport().buildFailureMessage(statusCode, responseBody, targetModel, requestUri);
    }

    private String buildSecondStageSceneImageFailureMessage(int statusCode,
                                                            String responseBody,
                                                            String targetModel,
                                                            URI requestUri,
                                                            String protocol) {
        return fallbackSupport().buildSecondStageSceneImageFailureMessage(
                statusCode,
                responseBody,
                targetModel,
                requestUri,
                protocol
        );
    }

    private String buildSceneImageFailureMessage(int statusCode, String responseBody, String targetModel, URI requestUri) {
        return fallbackSupport().buildSceneImageFailureMessage(statusCode, responseBody, targetModel, requestUri);
    }
    private HttpStatus mapUpstreamFailureStatus(int statusCode, String responseBody) {
        return fallbackSupport().mapUpstreamFailureStatus(statusCode, responseBody);
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase();
    }

    private boolean hasMoreSecondStageCandidates(List<String> protocols,
                                                 List<String> models,
                                                 String currentProtocol,
                                                 String currentModel) {
        return fallbackSupport().hasMoreSecondStageCandidates(protocols, models, currentProtocol, currentModel);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sanitizeBase64(String rawBase64) {
        if (!StringUtils.hasText(rawBase64)) {
            return "";
        }
        String value = rawBase64.trim();
        int commaIndex = value.indexOf(',');
        if (value.startsWith("data:") && commaIndex >= 0) {
            value = value.substring(commaIndex + 1);
        }
        return value.replaceAll("\\s+", "");
    }

    private long estimateDecodedBytes(String base64) {
        int length = base64.length();
        if (length == 0) {
            return 0;
        }
        int padding = 0;
        if (base64.endsWith("==")) {
            padding = 2;
        } else if (base64.endsWith("=")) {
            padding = 1;
        }
        return (length * 3L) / 4L - padding;
    }

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }

    private record VisionExecutionResult(String model, URI uri, String responseBody) {
    }

    private record SceneImageExecutionResult(String provider,
                                             String model,
                                             URI uri,
                                             String imageBase64,
                                             String imageUrl,
                                             String revisedPrompt,
                                             String visualSummary,
                                             String negativePrompt,
                                             String displayText,
                                             String generationMode) {
    }
}
