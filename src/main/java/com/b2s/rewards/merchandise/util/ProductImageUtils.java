package com.b2s.rewards.merchandise.util;

/**
 * Product Image URLs manipulation utilities.
 * See: com.b2s.imageproxy.server.ImageProxyServlet
 * @author dmontoya
 * @version 1.0, 5/28/13 2:01 PM
 * @since b2r-rewardstep 6.0
 */
public final class ProductImageUtils {

    private ProductImageUtils() {}

    public static String resizeImage(String imageUrl, String size, boolean squared) {
        String baseURL = imageUrl.substring(0, imageUrl.lastIndexOf("."));
        String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
        return baseURL + "_" + size + (squared ? "s" : "") + extension;
    }

    public static String resizeImage(String imageUrl, String size) {
        return resizeImage(imageUrl, size, false);
    }

    public static String squareImage(String imageUrl) {
        return resizeImage(imageUrl, "", true);
    }
}
