package org.example.backend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IdCardValidator {

    // 有效省份代码（前两位）
    private static final Set<String> VALID_PROVINCE_CODES = new HashSet<>(Arrays.asList(
            "11","12","13","14","15","21","22","23",
            "31","32","33","34","35","36","37",
            "41","42","43","44","45","46",
            "50","51","52","53","54",
            "61","62","63","64","65","71","81","82"
    ));

    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) return false;

        // 前17位数字，最后一位数字或X
        if (!idCard.matches("\\d{17}[0-9Xx]")) return false;

        // 校验省份
        String provinceCode = idCard.substring(0, 2);
        if (!VALID_PROVINCE_CODES.contains(provinceCode)) return false;

        // 校验生日
        String birth = idCard.substring(6, 14);
        if (!isValidDate(birth)) return false;

        // 校验码
        return verifyCheckCode(idCard);
    }

    // 校验日期是否合法
    private static boolean isValidDate(String birth) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(birth);
            // 出生日期不能是未来
            return !date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    // 计算并验证身份证校验码
    private static boolean verifyCheckCode(String idCard) {
        char[] idChars = idCard.toUpperCase().toCharArray();
        int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] validate = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idChars[i] - '0') * weight[i];
        }
        char code = validate[sum % 11];
        return code == idChars[17];
    }

}
