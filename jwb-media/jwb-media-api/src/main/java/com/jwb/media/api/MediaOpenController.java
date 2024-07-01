package com.jwb.media.api;

import com.jwb.base.exception.JwbException;
import com.jwb.base.model.RestResponse;
import com.jwb.media.model.po.MediaFiles;
import com.jwb.media.service.MediaFileService;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/open")
@Api(value = "预览媒资接口", tags = "预览媒资接口")
public class MediaOpenController {
    @Autowired
    private MediaFileService mediaFileService;

    @GetMapping("/preview/{mediaId}")
    @ApiOperation("视频预览查询接口")
    public RestResponse<String> getMediaUrl(@PathVariable String mediaId) {
        MediaFiles mediaFile = mediaFileService.getFileById(mediaId);
        if (mediaFile == null || StringUtils.isEmpty(mediaFile.getUrl())) {
            JwbException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFile.getUrl());
    }
}
