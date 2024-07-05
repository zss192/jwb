package com.jwb.media.api;

import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.base.model.RestResponse;
import com.jwb.base.utils.SecurityUtil;
import com.jwb.media.model.dto.QueryMediaParamsDto;
import com.jwb.media.model.dto.UploadFileParamsDto;
import com.jwb.media.model.dto.UploadFileResultDto;
import com.jwb.media.model.po.MediaFiles;
import com.jwb.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zss
 * @version 1.0
 * @description 媒资文件管理接口
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        SecurityUtil.JwbUser user = SecurityUtil.getUser();
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);
    }

    @ApiOperation("上传文件")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName) {
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileSize(upload.getSize());
        String contentType = upload.getContentType();
        // 其他
        if (contentType != null && contentType.contains("image")) {
            // 图片
            uploadFileParamsDto.setFileType("001001");
        }
        uploadFileParamsDto.setFilename(upload.getOriginalFilename());
        uploadFileParamsDto.setContentType(contentType);
        SecurityUtil.JwbUser user = SecurityUtil.getUser();
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        try {
            return mediaFileService.uploadFile(companyId, uploadFileParamsDto, upload.getBytes(), folder, objectName);
        } catch (IOException e) {
            JwbException.cast("上传文件过程出错");
        }
        return null;
    }

    @ApiOperation(value = "预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFile = mediaFileService.getFileById(mediaId);
        return RestResponse.success(mediaFile.getUrl());
    }
}
