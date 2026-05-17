package com.adidos.tryon.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TryOnAiService {

    @Value("${fitroom.api-key}")
    private String apiKey;

    private final Gson gson = new Gson();

    public String generateTryOn(String personImagePath, String garmentImagePath) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .writeTimeout(180, TimeUnit.SECONDS)
                    .build();

            File modelFile = new File(personImagePath);
            File clothFile = new File(garmentImagePath);

            if (!modelFile.exists()) {
                throw new RuntimeException("Không tìm thấy ảnh người: " + personImagePath);
            }

            if (!clothFile.exists()) {
                throw new RuntimeException("Không tìm thấy ảnh sản phẩm: " + garmentImagePath);
            }

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "model_image",
                            modelFile.getName(),
                            RequestBody.create(modelFile, MediaType.parse("image/png"))
                    )
                    .addFormDataPart(
                            "cloth_image",
                            clothFile.getName(),
                            RequestBody.create(clothFile, MediaType.parse("image/png"))
                    )
                    .addFormDataPart("cloth_type", "upper")
                    .addFormDataPart("hd_mode", "true")
                    .build();

            Request createRequest = new Request.Builder()
                    .url("https://platform.fitroom.app/api/tryon/v2/tasks")
                    .addHeader("X-API-KEY", apiKey)
                    .post(requestBody)
                    .build();

            try (Response createResponse = client.newCall(createRequest).execute()) {
                String createBody = createResponse.body() == null ? "" : createResponse.body().string();

                System.out.println("FITROOM CREATE RESPONSE:");
                System.out.println(createBody);

                if (!createResponse.isSuccessful()) {
                    throw new RuntimeException("FitRoom create task lỗi: " + createBody);
                }

                JsonObject createJson = gson.fromJson(createBody, JsonObject.class);

                if (createJson == null || !createJson.has("task_id")) {
                    throw new RuntimeException("FitRoom không trả task_id: " + createBody);
                }

                String taskId = createJson.get("task_id").getAsString();

                return pollResult(client, taskId);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String pollResult(OkHttpClient client, String taskId) throws Exception {
        for (int i = 0; i < 40; i++) {
            Thread.sleep(2000);

            Request statusRequest = new Request.Builder()
                    .url("https://platform.fitroom.app/api/tryon/v2/tasks/" + taskId)
                    .addHeader("X-API-KEY", apiKey)
                    .get()
                    .build();

            try (Response statusResponse = client.newCall(statusRequest).execute()) {
                String statusBody = statusResponse.body() == null ? "" : statusResponse.body().string();

                System.out.println("FITROOM STATUS RESPONSE:");
                System.out.println(statusBody);

                if (!statusResponse.isSuccessful()) {
                    throw new RuntimeException("FitRoom status lỗi: " + statusBody);
                }

                JsonObject statusJson = gson.fromJson(statusBody, JsonObject.class);

                String status = statusJson.has("status")
                        ? statusJson.get("status").getAsString()
                        : "";

                if ("COMPLETED".equalsIgnoreCase(status)) {
                    if (!statusJson.has("download_signed_url")) {
                        throw new RuntimeException("FitRoom completed nhưng không có download_signed_url: " + statusBody);
                    }

                    return statusJson.get("download_signed_url").getAsString();
                }

                if ("FAILED".equalsIgnoreCase(status)) {
                    String error = statusJson.has("error")
                            ? statusJson.get("error").getAsString()
                            : "FitRoom xử lý thất bại";

                    throw new RuntimeException(error);
                }
            }
        }

        throw new RuntimeException("FitRoom timeout sau 80 giây");
    }
}