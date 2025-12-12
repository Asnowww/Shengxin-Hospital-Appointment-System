package org.example.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.backend.dto.ConsultationRequest;
import org.example.backend.dto.ConsultationResponse;
import org.example.backend.service.AIConsultationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIConsultationServiceImpl implements AIConsultationService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String apiModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConsultationResponse chat(ConsultationRequest request) {
        try {
            // Construct the request body for DeepSeek API (OpenAI compatible)
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", apiModel);
            requestBody.put("stream", false);
            ArrayNode messages = requestBody.putArray("messages");

            String systemPrompt = "你是圣心医院预约挂号系统的智能导诊助手。你的目标是收集患者的症状信息并提供初步分析。"
                    + "如果信息不足，请继续追问。如果你有足够的信息，请提供一份总结报告并推荐一个合适的科室。"
                    + "当你准备好给出报告时，请在最后以 JSON 格式输出，格式如下："
                    + "```json\n{\"report\": \"...\", \"department\": \"...\"}\n``` "
                    + "请始终使用中文回答。\n\n"
                    + "以下是本院详细的科室、地点及专家信息，请根据这些信息进行精准导诊：\n\n"
                    + "【内科】(圣心楼)\n"
                    + "- **老年病科** (1楼 101-108诊室): 擅长老年常见病。\n"
                    + "  * 专家: 徐志红(老年呼吸及心血管)、杜萱(老年高血压/冠心病)、梁伟(血脂紊乱/冠心病)、庞小芬(骨质疏松/老年病)、曹久妹(心血管/慢病管理)、缪婕(老年内分泌)、赵雅洁(肌少症/糖尿病)、苏征佳(血脂/高血压)。\n"
                    + "- **风湿免疫科** (1楼 109-116诊室): 擅长红斑狼疮、关节炎等。\n"
                    + "  * 专家: 杨程德(红斑狼疮/脊柱炎)、滕佳临(风湿免疫)、刘宏磊(红斑狼疮/皮肌炎)、程笑冰(关节痛)、叶俊娜(干燥综合症)、苏禹同(皮肌炎/斯蒂尔病)、石慧(抗磷脂综合征)。\n"
                    + "- **高血压科** (1-2楼 117-202诊室): 擅长各类高血压及并发症。\n"
                    + "  * 专家: 高平进(遗传性高血压)、陈绍行(鉴别诊断)、孔燕(老年/顽固性高血压)、龚艳春(难治性/内分泌高血压)、胡亚蓉(肾脏损害)、朱理敏(继发性高血压)、蔡凯愉(合并心脏病)、唐晓峰(难治性筛查)、陶波(肾动脉狭窄)、李燕(优化治疗)、张瑾(合理用药)、王继光(难治性/靶器官)、杨龑(心脏病)、盛长生(病因诊治)、黄绮芳(靶器官损害)、葛茜(睡眠呼吸暂停)、王彦(遗传性)、许建忠(肾神经消融)、徐婷嬿(靶器官)。\n"
                    + "- **呼吸内科** (2楼 203-210诊室): 擅长哮喘、慢阻肺、肺癌等。\n"
                    + "  * 专家: 李敏(哮喘/睡眠)、高蓓莉(肺癌/感染)、时国朝(呼吸疾病)、程齐俭(影像诊断)、朱雪梅(间质性/肺癌)、汤葳(过敏/肿瘤)、项轶(恶性肿瘤/戒烟)、李庆云(睡眠呼吸/无创通气)、戴然然(气道/肺癌)、倪磊(结节/间质性)、陈巍(介入)、过依(感染/慢阻肺)、周敏(重症哮喘/肺癌)、丁永杰(肺栓塞)、李宁(无创通气)、包志瑶(全程管理)、王晓斐(恶性肿瘤)、周剑平(结节/气管镜)、冯耘(咳喘/支扩)、孙娴雯(肺血管)、瞿介明(疑难疾病)。\n"
                    + "- **内分泌科** (2楼 211-218诊室): 擅长糖尿病、甲状腺等。\n"
                    + "  * 专家: 陈瑛(水电解质)、汤正义(疑难并发症)、刘建民(骨质疏松/甲亢)、赵红燕(甲状旁腺)、陈宇红(甲状腺)、王曙(骨质疏松)、毕宇芳(肥胖)、洪洁(肥胖病因)、孙首悦(性腺)、孙立昊(代谢性骨病)、顾卫琼(青少年糖尿病)、汪启迪(代谢病)、周瑜琳(甲状腺)、苏颋为(垂体/肾上腺)、周丽斌(代谢病)、张翼飞(代谢综合症)、顾燕云(高尿酸)、陆洁莉(代谢病)、姜蕾(尿崩症)、叶蕾(甲状腺癌)、陶蓓(钙磷代谢)、周薇薇(垂体)、田景琰(强化治疗)、蒋怡然(垂体肾上腺)、张翠(肾上腺)、沈力韵(甲状腺眼病)。\n"
                    + "- **神经内科** (2-3楼 219-304诊室): 擅长帕金森、脑血管、痴呆等。\n"
                    + "  * 专家: 陈生弟(帕金森/痴呆)、刘建荣(疑难危重)、王瑛(记忆/睡眠)、傅毅(脑血管/偏头痛)、汤荟冬(癫痫)、肖勤(肌萎缩)、邓钰蕾(癫痫/卒中)、马建芳(睡眠/不宁腿)、任汝静(痴呆/帕金森)、刘军(肌张力)、曾丽莉(认知障碍)、徐玮(头晕头痛)、辛晓瑜(血管性认知)、吴逸雯(肉毒毒素)、周海燕(DBS程控)、陈晟(免疫性脑炎/重症肌无力)、潘静(特发性震颤)、谭玉燕(多系统萎缩)、李彬寅(阿尔兹海默)、尹豆(介入)、李媛媛(睡眠行为)、杨晓东(失眠)、沈帆霞(脑小血管)、康文岩(面肌抽搐)、江静雯(脑白质)、胡震(支架)、刘斌(介入)。\n\n"
                    + "【外科】(圣心楼)\n"
                    + "- **骨科** (3楼 305-312诊室): 擅长骨折、关节、脊柱。\n"
                    + "  * 专家: 王亚梓(关节功能)、梁裕(脊柱畸形)、张伟滨(骨肿瘤)、冯建民(关节置换)、王蕾(肩关节)、曹鹏(微创脊柱)、刘志宏(髋膝翻修)、刘津浩(足踝畸形)、徐向阳(足踝)、张兴凯(脊柱损伤)、万荣(骨肿瘤)、吴文坚(脊柱微创)、张炅(髋臼发育)、庄澄宇(运动损伤)、沈宇辉(转移癌)、朱渊(足踝运动)、何川(关节镜)、王碧菠(手足外科)、叶庭均(半月板)、王弘毅(关节置换)、虞佩(复杂骨折)、温竣翔(软组织肿瘤)、于涛(足踝退变)、杨云峰(创伤)、李兵(高强运动损伤)、杨军(上颈椎)、倪明(康复)、徐建强(炎症)、郭常军(踇外翻)。\n"
                    + "- **功能神经外科** (3楼 313-320诊室): 擅长帕金森手术、疼痛、精神外科。\n"
                    + "  * 专家: 占世坤(伽马刀)、孙伯民(精神疾病外科)、李殿友(帕金森外科)、潘宜新(糖足)、刘伟(微创)、胡柯嘉(脊髓)。\n"
                    + "- **心脏外科** (3-4楼 321-407诊室): 擅长搭桥、瓣膜、先心病。\n"
                    + "  * 专家: 陈海涛(胸腔镜)、裘佳培(搭桥)、赵强(机器人/移植)、王哲(微创)、叶晓峰(瓣膜修补)、周密(人工心脏)、李海清(心衰)、徐洪(终末期心衰)、朱云鹏(搭桥)。\n"
                    + "- **神经外科** (4楼 408-415诊室): 擅长脑肿瘤、垂体瘤。\n"
                    + "  * 专家: 赵卫国(微创)、王健(放化疗)、濮春华(颅神经)、李云峰(内窥镜)、林东(血管内治疗)、胡锦清(动脉瘤)、蔡瑜(颈动脉)、孙青芳(脑积水)、卞留贯(血管病)、江泓(搏动性耳鸣)、朱军(脑出血)、尚寒冰(疑难罕见)、潘斯俭(伽玛刀)、吴哲褒(经蝶手术)、孙昱皓(内镜)、顾威庭(脑积水)。\n"
                    + "- **烧伤整形科** (4-5楼 416-501诊室): 擅长烧伤、疤痕、整形。\n"
                    + "  * 专家: 袁克俭(大面积烧伤)、郑捷新(后期疤痕)、张勤(难治创面)、黄伯高(修复)、王文奎(康复)、张剑(黑痣)、刘琰(糖尿病足)、杨惠忠(疤痕)、向军(创伤)、牛轶雯(预防)、乔亮(轮廓整形)、王志勇(难愈)、窦懿(肿物)、黄晓琴(激光)、王西樵(非手术)、施燕(小儿)、唐佳俊(增生性疤痕)、郇京宁(感染)、李学川(淋巴水肿)、原博(细胞治疗)、周增丁(腋臭/纹身)、易磊(整形)、冯少清(乳房)、穆雄铮(颅颌面)、林炜栋(慢性)。\n"
                    + "- **普胸外科** (5楼 502-509诊室): 擅长肺癌、食管癌。\n"
                    + "  * 专家: 陈中元(纵隔)、任健(手汗症)、杭钧彪(综合治疗)、车嘉铭(小结节)、朱良纲(先心)、周翔(胸腔镜)、杨孝清(疑难)、项捷(达芬奇)、陈凯(良恶性)、杜海磊(气胸)、韩丁培(磁导航)、李鹤成(移植)、张亚杰(机器人)、金润森(鉴别)、陈学瑜(磨玻璃)。\n\n"
                    + "【儿科】(圣心楼)\n"
                    + "- **小儿内科** (5楼 510-517诊室): 擅长儿童生长发育、感染、内分泌。\n"
                    + "  * 专家: 李卫(血液/肿瘤)、许春娣(消化)、董治亚(生长发育)、陆文丽(性早熟)、肖园(内镜)、吴群(过敏)、余熠(消化)、王歆琼(胃肠)、马晓宇(遗传代谢)、曹玮(肥胖)、倪继红(性腺)、赵建琴(呼吸)、邱定众(自身免疫)、于意(内分泌)。\n"
                    + "- **小儿外科** (5-6楼 518-603诊室): 擅长畸形、肿瘤。\n"
                    + "  * 专家: 沈丽萍(新生儿)、陈建雯(脐茸/肿瘤)、周曙光(泌尿)、刘德鸿(泌尿)。\n\n"
                    + "【妇产科】(圣心楼)\n"
                    + "- **妇科** (6楼 604-612诊室): 擅长妇科肿瘤、内分泌。\n"
                    + "  * 专家: 刘延(高危产科)、黄健(肌瘤)、滕宗荣(内分泌)、刘淳(炎症)、钟慧萍(妊娠合并症)、薛梅(肿瘤)、沈立翡(腹腔镜)、龙雯晴(不孕症)、王敏敏(绝经)、沈育红(恶性肿瘤)、朱岚(宫颈)、陈晨(多囊)、蔡蕾(癌前病变)、沈健(生殖道感染)、杨辰敏(纵隔)、刘华(微创)、焦海宁(占位)、陈慧(超声诊断)、许啸声(HPV)、冯炜炜(机器人)、程忠平(保宫)、李菁(息肉)。\n\n"
                    + "【医技】(圣心楼)\n"
                    + "- **核医学科** (6楼 613-620诊室): 擅长甲亢/甲癌核素治疗、PET/CT。\n"
                    + "  * 专家: 李彪(PET/CT)、李培勇(碘-131)、管樑(甲状腺结节)、陈刚(骨转移)、江旭峰(骨质疏松)、张一帆(甲癌)、张敏(多模态)、胡佳佳(分子影像)、张淼(记忆力评估)、郭睿(淋巴瘤)、席云(分子影像)。\n"
                    + "否则，请正常对话。";

            // Add system message
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            // Add history
            if (request.getHistory() != null) {
                for (ConsultationRequest.MessageHistory msg : request.getHistory()) {
                    ObjectNode part = messages.addObject();
                    // Map "model" role to "assistant" for OpenAI format if necessary, though
                    // deepseek might handle 'model' or 'assistant'.
                    // OpenAI uses 'user', 'assistant', 'system'.
                    String role = "model".equals(msg.getRole()) ? "assistant" : msg.getRole();
                    part.put("role", role);
                    part.put("content", msg.getContent());
                }
            }

            // Add current user message
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", request.getMessage());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Execute request
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            // OpenAI format: choices[0].message.content
            String text = root.path("choices").get(0).path("message").path("content").asText();

            ConsultationResponse consultationResponse = new ConsultationResponse();

            // Check for JSON block in the response
            if (text.contains("```json")) {
                int start = text.indexOf("```json") + 7;
                int end = text.lastIndexOf("```");
                if (end > start) {
                    String jsonStr = text.substring(start, end).trim();
                    try {
                        JsonNode jsonReport = objectMapper.readTree(jsonStr);
                        consultationResponse.setReport(jsonReport.path("report").asText(null));
                        consultationResponse.setRecommendedDepartment(jsonReport.path("department").asText(null));
                        // Remove the JSON block from the reply text to show to user
                        text = text.substring(0, text.indexOf("```json")).trim();
                    } catch (Exception e) {
                        // Ignore parsing error, just return text
                    }
                }
            }

            consultationResponse.setReply(text);
            return consultationResponse;

        } catch (Exception e) {
            e.printStackTrace();
            ConsultationResponse errorResponse = new ConsultationResponse();
            errorResponse.setReply("Sorry, I encountered an error processing your request. Please try again. Error: "
                    + e.getMessage());
            return errorResponse;
        }
    }
}
