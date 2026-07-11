package com.cavus.delivery_food.common.utils;

import java.util.Locale;

/// ! Utils sınıfından nesne üretmeyi engellemek için final yaptık
public final class NormalizeStringUtils {

     private NormalizeStringUtils() {
    }

    public static String normalizeString(String value) {
    if (value == null) {
        return null;
    }

    return value.trim().toLowerCase(Locale.ROOT);
}
    
}
