package com.applitools.imagetester.TestObjects;

import com.applitools.ICheckSettings;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.Region;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.fluent.GetSimpleRegion;
import com.applitools.eyes.images.Eyes;
import com.applitools.eyes.images.ImagesCheckSettings;
import com.applitools.eyes.images.Target;
import com.applitools.imagetester.lib.Config;
import com.applitools.imagetester.lib.Utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PdfFileTest extends DocumentTestBase {

    public PdfFileTest(File file, Config conf) {
        super(file, conf);
    }

    public TestResults run(Eyes eyes) throws Exception {

        try (PDDocument document = PDDocument.load(file(), config().pdfPass)) {
            if (pageList_ == null || pageList_.isEmpty())
                pageList_ = Utils.generateRanage(document.getNumberOfPages() + 1, 1);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (Integer page : pageList_) {
                try {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page - 1, config().DocumentConversionDPI);
                    logger().logPage(bim, name(), page);
                    if (!eyes.getIsOpen())
                        eyes.open(appName(), name(), viewport(bim));
                    eyes.check(
                            String.format("Page-%s", page),
                            new ImagesCheckSettingsFactory(bim, config(), viewport(bim)).create()
                    );
                    bim.getGraphics().dispose();
                    bim.flush();
                } catch (IOException e) {
                    logger().reportException(e, file().getAbsolutePath());
                }
            }
            return eyes.close(false);
        }
    }
}
