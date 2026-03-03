package com.yzq.zxing;

import android.dream.DreamApnInfo;
import android.telephony.Rlog;

import java.util.ArrayList;
import java.util.List;

public class ApnQrCodec {
    private static final String TAG = "ApnQrCodec";
    // 分隔符定义（避免和APN字段内容冲突）
    private static final String FIELD_SEPARATOR = "§";       // 单条APN内字段分隔
    private static final String APN_SEPARATOR = "¶¶¶";        // 多条APN之间分隔
    private static final String NULL_PLACEHOLDER = "_NULL_"; // 空值标记

    /**
     * 打包多个APN数据为二维码字符串（轻量化）
     * @param apnList APN数据列表
     * @return 可用于生成二维码的字符串，失败返回null
     */
    public static String packApnListToQrString(List<DreamApnInfo> apnList) {
        if (apnList == null || apnList.isEmpty()) {
            Rlog.e(TAG, "APN list is empty");
            return null;
        }

        StringBuilder qrContent = new StringBuilder();
        for (int i = 0; i < apnList.size(); i++) {
            DreamApnInfo apnInfo = apnList.get(i);
            // 按toString的字段顺序打包，空值替换为NULL_PLACEHOLDER
            String apnStr = wrapNull(apnInfo.name) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.apn) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.proxy) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.port) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.mmsproxy) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.mmsport) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.user) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.server) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.password) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.mmsc) + FIELD_SEPARATOR
                    + apnInfo.author_type + FIELD_SEPARATOR // int类型，无需空值处理
                    + wrapNull(apnInfo.protocol) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.roaming_protocol) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.type) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.mcc) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.mnc) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.ppp_number) + FIELD_SEPARATOR
                    + apnInfo.bearer_bitmask + FIELD_SEPARATOR // int类型
                    + apnInfo.bearer + FIELD_SEPARATOR // int类型
                    + wrapNull(apnInfo.mvno_type) + FIELD_SEPARATOR
                    + wrapNull(apnInfo.mvno_match_data);

            // 拼接多条APN，最后一条不加分隔符
            qrContent.append(apnStr);
            if (i != apnList.size() - 1) {
                qrContent.append(APN_SEPARATOR);
            }
        }

        // 校验长度（适配二维码容量，M纠错+40版本最大2300字符）
        if (qrContent.length() > 2000) {
            Rlog.w(TAG, "APN data too long for QR Code: " + qrContent.length() + " chars");
            // 也可抛出异常或截断，根据需求调整
        }

        return qrContent.toString();
    }

    /**
     * 从二维码字符串解包为APN数据列表
     * @param qrString 二维码解析后的字符串
     * @return APN数据列表，失败返回空列表
     */
    public static List<DreamApnInfo> unpackQrStringToApnList(String qrString) {
        List<DreamApnInfo> apnList = new ArrayList<>();
        if (qrString == null || qrString.isEmpty()) {
            Rlog.e(TAG, "QR string is empty");
            return apnList;
        }

        // 拆分多条APN
        String[] apnStrArray = qrString.split(APN_SEPARATOR);
        for (String apnStr : apnStrArray) {
            if (apnStr.isEmpty()) {
                continue;
            }

            // 拆分单条APN的字段（按打包顺序）
            String[] fields = apnStr.split(FIELD_SEPARATOR);
            // 校验字段数（必须和打包的字段数一致）
            if (fields.length != 21) { // 总共21个字段（参考toString的字段数）
                Rlog.e(TAG, "APN field count error: " + fields.length + ", expect 21");
                continue;
            }

            // 还原DreamApnInfo对象
            DreamApnInfo apnInfo = new DreamApnInfo();
            apnInfo.name = unwrapNull(fields[0]);
            apnInfo.apn = unwrapNull(fields[1]);
            apnInfo.proxy = unwrapNull(fields[2]);
            apnInfo.port = unwrapNull(fields[3]);
            apnInfo.mmsproxy = unwrapNull(fields[4]);
            apnInfo.mmsport = unwrapNull(fields[5]);
            apnInfo.user = unwrapNull(fields[6]);
            apnInfo.server = unwrapNull(fields[7]);
            apnInfo.password = unwrapNull(fields[8]);
            apnInfo.mmsc = unwrapNull(fields[9]);
            // 还原int类型字段（容错：解析失败设为0）
            apnInfo.author_type = parseIntSafely(fields[10], 0);
            apnInfo.protocol = unwrapNull(fields[11]);
            apnInfo.roaming_protocol = unwrapNull(fields[12]);
            apnInfo.type = unwrapNull(fields[13]);
            apnInfo.mcc = unwrapNull(fields[14]);
            apnInfo.mnc = unwrapNull(fields[15]);
            apnInfo.ppp_number = unwrapNull(fields[16]);
            apnInfo.bearer_bitmask = parseIntSafely(fields[17], 0);
            apnInfo.bearer = parseIntSafely(fields[18], 0);
            apnInfo.mvno_type = unwrapNull(fields[19]);
            apnInfo.mvno_match_data = unwrapNull(fields[20]);

            apnList.add(apnInfo);
        }

        return apnList;
    }

    // 辅助：空值替换为占位符
    private static String wrapNull(String str) {
        return str == null || str.isEmpty() ? NULL_PLACEHOLDER : str;
    }

    // 辅助：占位符还原为空值
    private static String unwrapNull(String str) {
        return NULL_PLACEHOLDER.equals(str) ? "" : str;
    }

    // 辅助：安全解析int（避免NumberFormatException）
    private static int parseIntSafely(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Rlog.w(TAG, "Parse int failed: " + str + ", use default: " + defaultValue);
            return defaultValue;
        }
    }

}
