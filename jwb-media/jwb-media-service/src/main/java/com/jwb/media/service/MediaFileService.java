package com.jwb.media.service;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.base.model.RestResponse;
import com.jwb.media.model.dto.QueryMediaParamsDto;
import com.jwb.media.model.dto.UploadFileParamsDto;
import com.jwb.media.model.dto.UploadFileResultDto;
import com.jwb.media.model.po.MediaFiles;

import java.io.File;

/**
 * @author zss
 * @version 1.0
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.jwb.base.model.PageResult<com.jwb.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author zss
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件信息
     * @param bytes               文件字节数组
     * @param folder              桶下边的子目录
     * @param objectName          对象名称
     * @return com.jwb.media.model.dto.UploadFileResultDto
     * @description 上传文件的通用接口
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

    /**
     * 将文件信息添加到文件表
     * 将事务方法提取到接口，用代理类调用解决事务失效问题
     *
     * @param companyId           机构id
     * @param uploadFileParamsDto 上传文件的信息
     * @param objectName          对象名称
     * @param fileMD5             文件md5码
     * @param bucket              桶
     */
    MediaFiles addMediaFilesToDB(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String fileMD5, String bucket);

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     */
    RestResponse<Boolean> checkFile(String fileMd5, String fileSize);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的MD5
     * @param chunkIndex 分块序号
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     *
     * @param fileMd5 文件MD5
     * @param chunk   分块序号
     * @param bytes   文件字节
     */
    RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes);

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     */
    RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    MediaFiles getFileById(String mediaId);

    void downloadChunkFromMinio(File file, String bucket, String objectName);

    String getFilePathByMd5(String fileMd5, String extension);

    void addMediaFilesToMinIO(String filePath, String bucket, String objectName);

    void deleteFile(String mediaId);
}
