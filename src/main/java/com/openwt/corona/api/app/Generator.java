package com.openwt.corona.api.app;

import com.openwt.corona.api.generator.FlowFile;
import com.openwt.corona.api.generator.Translations;
import com.openwt.corona.api.generator.model.Answer;
import com.openwt.corona.api.generator.model.CantonalRecommendation;
import com.openwt.corona.api.generator.model.Question;
import com.openwt.corona.api.generator.model.Recommendation;
import com.openwt.corona.api.merger.Merger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Generator
{
    // Public WebTranslate.it keys
    public static String WTI_COV_CHECK_KEY       = "proj_pub_7hLFxjXF9Vzd4xlGh4IvOw";
    public static String WTI_SWISS_COV_GUIDELINES_KEY = "proj_pub_7JIdYXx-Xq1DQol30gGiCA";

    public static String[] SCREENING_TREE_NAMES = {
        "main"
    };

    private Translations getWtiCovCheckTranslation(String wtiPath) throws Exception {
        this.initWtiKey(wtiPath, WTI_COV_CHECK_KEY);
        return new Translations(wtiPath);
    }

    private Translations getWtiSwissCovGuidelinesTranslation(String wtiPath) throws Exception {
        this.initWtiKey(wtiPath, WTI_SWISS_COV_GUIDELINES_KEY);
        return new Translations(wtiPath);
    }

    private void loadCantonalRecommendations(String wtiPath) throws Exception {
        Translations translationsCovCheck = getWtiCovCheckTranslation(wtiPath);

        Translations translationsSwissCovGuidelines = getWtiSwissCovGuidelinesTranslation(wtiPath);

        // Loaded from WTI Covid check project
        List<CantonalRecommendation> cantons = translationsCovCheck.cantonalRecommendations();

        // Loaded from WTI SwissCovid guidelines project
        List<Question> questions = translationsSwissCovGuidelines.questions();
        List<Answer> answers = translationsSwissCovGuidelines.answers();
        List<Recommendation> recommendations = translationsSwissCovGuidelines.recommendations();
        List<String> cantonalRecommendationFor = translationsSwissCovGuidelines.getCantonalRecommendationFor();

        for (String treeName: Generator.SCREENING_TREE_NAMES) {
            FlowFile flowFile = new FlowFile(questions, answers, recommendations, cantons, cantonalRecommendationFor);
            flowFile.save();

            Merger merger = new Merger();
            merger.merge(treeName);
        }
    }

    private void initWtiKey(String wtiPath, String wtiKey) throws IOException, InterruptedException {
        String[] command = {wtiPath, "init", wtiKey};
        ProcessBuilder builder = new ProcessBuilder(command);
        Process p = builder.start();
        // Must sleep 1s so that WTI has time to update its .wti file
        TimeUnit.SECONDS.sleep(1);
    }

    public void start(String wtiPath) throws Exception
    {
        loadCantonalRecommendations(wtiPath);
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length >= 1)
        {
            Generator generator = new Generator();
            generator.start(args[0]);
        }
        else
        {
            System.err.println("Usage: java -jar json-generator.jar WTI_FOLDER_PATH");
        }
    }
}