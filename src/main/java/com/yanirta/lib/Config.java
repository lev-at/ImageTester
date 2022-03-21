package com.yanirta.lib;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.ProxySettings;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.Region;
import com.applitools.eyes.fluent.BatchClose;
import com.applitools.eyes.fluent.EnabledBatchClose;
import com.twelvemonkeys.util.IgnoreCaseMap;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.IntStream;

public class Config {
    public RectangleSize viewport;
    public String appName = "ImageTester";
    public float DocumentConversionDPI = 250;
    public boolean splitSteps = false;
    public String pages = null;
    public String pdfPass = null;
    public boolean includePageNumbers = false;
    public Logger logger = new Logger();
    public EyesUtilitiesConfig eyesUtilsConf;
    public BatchInfo flatBatch = null;
    public String forcedName = null;
    public String sequenceName = null;
    public boolean notifyOnComplete = false;
    public String apiKey;
    public String serverUrl;
    public ProxySettings proxy_settings = null;
    public String matchWidth = null;
    public String matchHeight = null;
    public boolean legacyFileOrder = false;
    public boolean dontCloseBatches = false;
    public String batchMapperPath = null;
    public Region[] ignoreRegions = null;
    public Region[] layoutRegions = null;
    public Region[] contentRegions = null;

    private final HashSet<String> batchesIdListForBatchClose = new HashSet<>();



    public void setViewport(String viewport) {
        if (viewport == null) return;
        String[] dims = viewport.split("x");
        if (dims.length != 2)
            throw new RuntimeException("invalid viewport-size, make sure the call is -vs <width>x<height>");
        this.viewport = new RectangleSize(
                Integer.parseInt(dims[0]),
                Integer.parseInt(dims[1]));
    }

    public void setProxy(String[] proxy) {
        if (proxy != null && proxy.length > 0)
            if (proxy.length == 1) {
                logger.reportDebug("Using proxy %s \n", proxy[0]);
                proxy_settings = new ProxySettings(proxy[0]);
            } else if (proxy.length == 3) {
                logger.reportDebug("Using proxy %s with user %s and pass %s \n", proxy[0], proxy[1], proxy[2]);
                proxy_settings = new ProxySettings(proxy[0], proxy[1], proxy[2]);
            } else
                throw new RuntimeException("Proxy setting are invalid");
    }

    public void setMatchSize(String size) {
        if (size == null)
            return;
        String[] dims = size.split("x");
        matchWidth = dims[0];
        if (dims.length > 1)
            matchHeight = dims[1];
    }

    //set batch related info
    public void setBatchInfo(String flatBatchArg, boolean notifyOnComplete) {
        this.notifyOnComplete = notifyOnComplete;
        //set batch- take flat batch if described- get environment variables values unless overwritten
        String batchNameToAdd = System.getenv("JOB_NAME");
        String batchIdToAdd = System.getenv("APPLITOOLS_BATCH_ID");

        //set flat batch- config.notify complete must be before this set
        if (StringUtils.isNoneBlank(flatBatchArg)) {
            String[] batch_parts = flatBatchArg.split("<>");
            //check if batch id was specified
            batchNameToAdd = batch_parts[0];
            batchIdToAdd = batch_parts.length > 1 ? batch_parts[1] : null;
        }

        //if flat batch name is not empty initialize flat batch
        if (StringUtils.isNoneBlank(batchNameToAdd)) {
            flatBatch = new BatchInfo(batchNameToAdd);
            //if flat batch id is not empty set batch id
            if (StringUtils.isNoneBlank(batchIdToAdd))
                flatBatch.setId(batchIdToAdd);
        }
    }

    //add batch id to list
    public void addBatchIdToCloseList(String batchId) {
        batchesIdListForBatchClose.add(batchId);
    }

    //close batches
    public void closeBatches() {
        if (notifyOnComplete) {
            BatchClose batchClose = new BatchClose();
            batchClose.setApiKey(apiKey);
            if (serverUrl != null)
                batchClose.setUrl(serverUrl);
            if (proxy_settings != null)
                batchClose.setProxy(proxy_settings);
            EnabledBatchClose enabledBatchClose = batchClose.setBatchId(new ArrayList<>(batchesIdListForBatchClose));
            if (!dontCloseBatches) enabledBatchClose.close();
        }
    }

    public void setIgnoreRegions(String ignoreRegionsOption) {
        if (ignoreRegionsOption != null) {
            try {
                String[] regionStrings = ignoreRegionsOption.split("\\|");
                Region[] ignoreRegionsArr = new Region[regionStrings.length];
                for(int regionIndex = 0; regionIndex < regionStrings.length; regionIndex++) {
                    String[] regionParameters = regionStrings[regionIndex].split(",");
                    ignoreRegionsArr[regionIndex] =
                            new Region(
                                    Integer.parseInt(regionParameters[0]),
                                    Integer.parseInt(regionParameters[1]),
                                    Integer.parseInt(regionParameters[2]),
                                    Integer.parseInt(regionParameters[3])
                            );
                }
                this.ignoreRegions = ignoreRegionsArr;
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.printMessage("Error parsing parameters for ignore regions. " +
                        "Please ensure that the layout regions are in the format x,y,width,height|x,y,width,height...");
            }
        }
    }

    public void setContentRegions(String contentRegionsOption) {
        if (contentRegionsOption != null) {
            try{
                String[] regionStrings = contentRegionsOption.split("\\|");
                Region[] ignoreRegionsArr = new Region[regionStrings.length];
                String[] regionParameters;
                for(int regionIndex = 0; regionIndex < regionStrings.length; regionIndex++) {
                    regionParameters = regionStrings[regionIndex].split(",");
                    ignoreRegionsArr[regionIndex] =
                            new Region(
                                    Integer.parseInt(regionParameters[0]),
                                    Integer.parseInt(regionParameters[1]),
                                    Integer.parseInt(regionParameters[2]),
                                    Integer.parseInt(regionParameters[3])
                            );
                }
                this.contentRegions = ignoreRegionsArr;
            } catch(ArrayIndexOutOfBoundsException e) {
                logger.printMessage("Error parsing parameters for content regions. " +
                        "Please ensure that the layout regions are in the format x,y,width,height|x,y,width,height...");
            }

        }
    }

    public void setLayoutRegions(String layoutRegionsOption) {
        if (layoutRegionsOption != null) {
            try {
                String[] regionStrings = layoutRegionsOption.split("\\|");
                Region[] layoutRegionsArr = new Region[regionStrings.length];
                for(int regionIndex = 0; regionIndex < regionStrings.length; regionIndex++) {
                    String[] regionParameters = regionStrings[regionIndex].split(",");
                    layoutRegionsArr[regionIndex] =
                            new Region(
                                    Integer.parseInt(regionParameters[0]),
                                    Integer.parseInt(regionParameters[1]),
                                    Integer.parseInt(regionParameters[2]),
                                    Integer.parseInt(regionParameters[3])
                            );
                }
                this.layoutRegions = layoutRegionsArr;
            } catch(ArrayIndexOutOfBoundsException e) {
                logger.printMessage("Error parsing parameters for layout regions. " +
                        "Please ensure that the layout regions are in the format x,y,width,height|x,y,width,height...");
            }
        }
    }
}
