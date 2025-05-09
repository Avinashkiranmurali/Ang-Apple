package com.b2s.rewards.apple.testmocks;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * All mock stubbing required for Product Service calls by the unit tests
 * Created by ssrinivasan on 6/1/2015.
 */
public class ProductServiceTestWireMock {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8049));

    public static int CURRENTLY_RUNNING_CLASSES = 0;
    /**
     * Add All the PSIDs and corresponding Details file in   src/test/resources directory
     */
    private static final List WIRE_DETAIL_PSIDS = Arrays.asList("30001D2187Z/A", "30001MGXA2LL/A");
    /**
     * Add All the Slugs and corresponding Products file in   src/test/resources directory
     */
    private static final List WIRE_PRODUCT_CATEGORY_SLUGS = Arrays.asList("ipad-mini-3", "ipod-shuffle");

    public static void stop() {
        LOG.info("Shutting down Product Service wiremock. - Start");
            wireMockServer.shutdown();
        LOG.info("Shutting down Product Service wiremock. - Finished");
    }
    public static void start() {
        try {
            if(wireMockServer.isRunning()){
                LOG.info("Already Running.");
                return;
            }
            LOG.info("Starting and Configuring Product Service wiremock. - Start");
            wireMockServer.start();
            configureFor("localhost", wireMockServer.port());
            stubFor(get(urlEqualTo("/categories/apple?language=ENGLISH")).willReturn(aResponse()
                .withStatus(200)
                .withBody(Resources
                    .toString(ProductServiceTestWireMock.class.getClassLoader().getResource("Categories.json"),
                        Charsets.UTF_8))));
            //Below piece of code expects ProductDetail file to be present in src/test/resources directory
            WIRE_DETAIL_PSIDS.forEach(psid -> {
                    try {
                        stubFor(get(urlEqualTo(
                            "/detail/apple?withVariations=true&withRealTimeInfo=true&language=ENGLISH&psid=" + psid +
                                "&varId=1&programId=1"))
                            .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(Resources
                                    .toString(
                                        ProductServiceTestWireMock.class.getClassLoader()
                                            .getResource("ProductDetail-" + ((String)
                                                psid).replace
                                                ("/",
                                                    "-")
                                                + "" +
                                                ".json"), Charsets
                                            .UTF_8))));
                    } catch (IOException e) {
                        LOG.error("Could not read product detail json from classpath", e);
                    }
                }
            );
            //Below piece of code expects ProductDetail file to be present in src/test/resources directory
            WIRE_PRODUCT_CATEGORY_SLUGS.forEach(slug -> {
                    try {
                        stubFor(get(urlEqualTo(
                            "/search/apple?resultLimit=2000&language=ENGLISH&categories="+slug+"&varId=1" +
                                "&programId=1"))
                            .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(Resources
                                    .toString(
                                        ProductServiceTestWireMock.class.getClassLoader()
                                            .getResource("Products-" + slug +
                                                ".json"), Charsets
                                            .UTF_8))));
                    } catch (IOException e) {
                        LOG.error("Could not read product detail json from classpath", e);
                    }
                }
            );
            LOG.info("Starting and Configuring Product Service wiremock. - Finished");
        } catch (Exception e) {
            LOG.error("Error While running product service mock. ", e);
        }
    }
}
