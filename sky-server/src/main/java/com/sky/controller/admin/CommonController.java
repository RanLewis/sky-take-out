package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @Description: 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * @Description: 图片上传
     * @Param: [file]
     * @return: com.sky.result.Result
     */
    @PostMapping("/upload")
    @ApiOperation(value = "图片上传")
    public Result<String> upload(MultipartFile file) {
        log.info("图片上传");
        try {
            String uuid = UUID.randomUUID().toString();
            uuid = uuid.replace("-", "");
            String fileName = file.getOriginalFilename();
            String[] split = fileName.split("\\.");
            String objectName = uuid + "." + split[split.length - 1];
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("图片上传失败", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
