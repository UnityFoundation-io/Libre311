// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.safesearch;

import app.exception.Libre311BaseException;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.protobuf.ByteString;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GoogleImageSafeSearchService {

    public static class ExplicitImageDetected extends Libre311BaseException {

        public ExplicitImageDetected() {
            super("The uploaded image has a high likelihood of being explicit and was rejected.",
                HttpStatus.BAD_REQUEST);
        }
    }

    static class FailedToAnnotateImage extends Libre311BaseException {

        public FailedToAnnotateImage(String message) {
            super(message, HttpStatus.BAD_GATEWAY);
        }
    }

    /**
     * @param bytes the image data in bytes
     * @throws ExplicitImageDetected if image is suspected of being explicit
     */
    public void preventExplicitImage(byte[] bytes) {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.copyFrom(bytes);
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    throw new FailedToAnnotateImage(res.getError().getMessage());
                }

                SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
                if (likelyExplicit(annotation)) {
                    throw new ExplicitImageDetected();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean likelyExplicit(SafeSearchAnnotation annotation) {
        int maxLikelihoodNum = Likelihood.POSSIBLE.getNumber();
        return annotation.getAdult().getNumber() > maxLikelihoodNum
            || annotation.getViolence().getNumber() > maxLikelihoodNum
            || annotation.getRacy().getNumber() > maxLikelihoodNum;
    }
}
