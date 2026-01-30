package com.ailianlian.ablecisi.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * ailianlian
 * com.ailianlian.ablecisi.utils
 * TimeAgoUtil <br>
 * * 用于将时间转换为相对于当前时间的描述，例如 "刚刚", "2分钟前", "1小时前", "昨天", "3天前", "1周前", "2月前", "3年前"
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 09:37
 */
public class TimeAgoUtil {

    /**
     * 将给定的时间转换为相对于当前时间的描述
     *
     * @param time 要转换的时间
     * @return 相对于当前时间的描述字符串
     */
    public static String toTimeAgo(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时前";
        } else {
            LocalDate date = time.toLocalDate();
            LocalDate today = now.toLocalDate();
            Period period = Period.between(date, today);
            if (period.getDays() == 1) {
                return "昨天";
            } else if (period.getDays() < 7) {
                return period.getDays() + "天前";
            } else if (period.getDays() < 30) {
                return (period.getDays() / 7) + "周前";
            } else if (period.getMonths() < 12) {
                return period.getMonths() + "月前";
            } else {
                return period.getYears() + "年前";
            }
        }
    }
}
