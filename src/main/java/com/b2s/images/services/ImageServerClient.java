package com.b2s.images.services;

import java.io.IOException;
import java.net.URL;

/**
 * @author akarneyenka@bridge2solutions.com
 *
 * Client component for uploading of image and CSS files to the Images Server
 */
public interface ImageServerClient {

    /**
     * Upload file to the image server
     *
     * @param payload raw data to upload
     * @param fileName name to be assigned to the created file on the image server
     * @return public URL assigned to the file on the image server, e.g. http://images.rewardstep.com/images/banner123.jpg
     * @throws IOException
     */
    URL upload(byte[] payload, String fileName) throws IOException;
}
