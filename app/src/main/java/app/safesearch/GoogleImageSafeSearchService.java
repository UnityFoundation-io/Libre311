package app.safesearch;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Singleton
public class GoogleImageSafeSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleImageSafeSearchService.class);

    // Derived from https://cloud.google.com/vision/docs/detecting-safe-search#vision_safe_search_detection-java
    // See https://cloud.google.com/vision/docs/before-you-begin
    public boolean imageIsExplicit(String image) {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        LOG.info("in imageIsExplicit");

        ByteString imgBytes = null;
        try {
            LOG.info("parse base64");
            imgBytes = ByteString.readFrom(new ByteArrayInputStream(
                    Base64.getDecoder().decode(image.getBytes("UTF-8"))
            ));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            return true;
        }

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        LOG.info("before ImageAnnotatorClient.create");
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            LOG.info("before request");
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            LOG.info("after request");
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    LOG.error("Error: %s", res.getError().getMessage());
                    return true;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                LOG.info("getSafeSearchAnnotations");
                SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
                return annotation.getAdult() == Likelihood.LIKELY || annotation.getAdult() == Likelihood.VERY_LIKELY ||
                        annotation.getViolence() == Likelihood.LIKELY || annotation.getViolence() == Likelihood.VERY_LIKELY ||
                        annotation.getRacy() == Likelihood.LIKELY || annotation.getRacy() == Likelihood.VERY_LIKELY;
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
            return true;
        }

        return true;
    }
}