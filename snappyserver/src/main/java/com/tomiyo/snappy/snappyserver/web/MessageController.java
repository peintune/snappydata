package com.tomiyo.snappy.snappyserver.web;

import com.alibaba.fastjson.JSON;
import com.tomiyo.snappy.snappyserver.controller.Controller;
import com.tomiyo.snappy.snappyserver.message.ContentMessage;
import com.tomiyo.snappy.snappyserver.message.MessageManager;
import com.tomiyo.snappy.snappyserver.message.URLMessage;
import com.tomiyo.snappy.snappyserver.message.URLQueueManager;
import com.tomiyo.snappy.snappyserver.response.JsonResult;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;


/**
 * @description: 设备api web
 * @author: He Kun
 * @create: 2018-05-17 14:18
 **/
@RestController
@RequestMapping(value = "message")
public class MessageController {
    org.apache.log4j.Logger logger = Logger.getLogger(MessageController.class);


    @GetMapping("/fetchOneMessage")
    public JsonResult fetchOneMessage() {
        URLMessage urlMessage = URLQueueManager.fetchOne();
        if (null != urlMessage) {
            return JsonResult.success(urlMessage);
        } else {
            String message = "no data";
            return JsonResult.success(message);
        }
    }

    @PostMapping("/postOneMessage")
    public JsonResult postOneMessage(
            @RequestBody String json) {

        ContentMessage contentMessage = null;
        String message = "fail";
        try {
            contentMessage = JSON.parseObject(json, ContentMessage.class);
            message = "success";
            MessageManager.addOneMessage(contentMessage);
        } catch (Exception e) {
            logger.error("failed to convert content message json to ContentMessage object", e);
        }
        return JsonResult.success(message);

    }

    @GetMapping("/addSnappier")
    public JsonResult addSnappier(
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "filerex", required = false) String filerex) {
        boolean isSuccess = Controller.getInstance().addSnappier(folder, filerex);
        if (isSuccess) {
            return JsonResult.success("success");
        } else {
            return JsonResult.success("failed");
        }
    }

    @PostMapping("/initialSnappier")
    public JsonResult initialSnappier(
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "filerex", required = false) String filerex) {
        boolean isSuccess = Controller.getInstance().initialLoadSnappier(folder, filerex);
        if (isSuccess) {
            return JsonResult.success("success");
        } else {
            return JsonResult.success("failed");
        }
    }

    @GetMapping("startSnappier")
    public JsonResult startSnappier(@RequestParam(value = "folder", required = false) String folder,
                                    @RequestParam(value = "filerex", required = false) String filerex) {
        boolean isSuccess = Controller.getInstance().startSnappier(folder, filerex);
        if (isSuccess) {
            return JsonResult.success("success");
        } else {
            return JsonResult.success("failed");
        }
    }

    @GetMapping("stopSnappier")
    public JsonResult stopSnappier(
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "filerex", required = false) String filerex) {
        boolean isSuccess = Controller.getInstance().stopSnappier(folder, filerex);
        if (isSuccess) {
            return JsonResult.success("success");
        } else {
            return JsonResult.success("failed");
        }
    }

}
