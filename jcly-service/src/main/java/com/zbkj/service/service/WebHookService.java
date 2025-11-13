package com.zbkj.service.service;

import com.alibaba.fastjson.JSONObject;

public interface WebHookService {
    void handleCsjEvent(JSONObject jsonObject);
}
