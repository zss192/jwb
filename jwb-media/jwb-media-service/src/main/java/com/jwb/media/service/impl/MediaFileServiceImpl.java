package com.jwb.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.base.model.RestResponse;
import com.jwb.media.mapper.MediaFilesMapper;
import com.jwb.media.model.dto.QueryMediaParamsDto;
import com.jwb.media.model.dto.UploadFileParamsDto;
import com.jwb.media.model.dto.UploadFileResultDto;
import com.jwb.media.model.po.MediaFiles;
import com.jwb.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author zss
 * @version 1.0
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;
    @Autowired
    MediaFileService currentProxy; // 构建一个当前类的代理对象

    @Value("${minio.bucket.files}")
    private String bucket_files;

    /**
     * 根据日期自动生成目录
     */
    private String getFileFolder() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/");
        return dateFormat.format(new Date());
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(!StringUtils.isEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件信息
     * @param bytes               文件字节数组
     * @param folder              桶下边的子目录
     * @param objectName          对象名称
     * @return com.jwb.media.model.dto.UploadFileResultDto
     * @description 上传文件的通用接口
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        String fileMD5 = DigestUtils.md5DigestAsHex(bytes);
        if (StringUtils.isEmpty(folder)) {
            // 如果目录不存在，则自动生成一个目录
            folder = getFileFolder();
        }
        if (StringUtils.isEmpty(objectName)) {
            // 如果传的对象名为空，则设置其文件名为文件的md5 + 文件后缀名
            String filename = uploadFileParamsDto.getFilename();
            objectName = fileMD5 + filename.substring(filename.lastIndexOf("."));
        }
        objectName = folder + objectName;
        // 上传到MinIO
        addMediaFilesToMinIO(bytes, bucket_files, objectName);
        // 信息添加到数据库
        // 用当前类的代理对象调用事务方法解决 非事务方法调用同类中事务方法，事务失效的问题
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, uploadFileParamsDto, objectName, fileMD5, bucket_files);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;

    }

    /**
     * 将文件信息添加到文件表
     *
     * @param companyId           机构id
     * @param uploadFileParamsDto 上传文件的信息
     * @param objectName          对象名称
     * @param fileMD5             文件的md5码
     * @param bucket              桶
     */
    @Transactional
    public MediaFiles addMediaFilesToDB(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String fileMD5, String bucket) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMD5);
        // 若文件不存在才添加
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMD5);
            mediaFiles.setFileId(fileMD5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            // 查阅数据字典，002003表示审核通过
            mediaFiles.setAuditStatus("002003");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                JwbException.cast("保存文件信息失败");
            }
        }
        return mediaFiles;
    }

    /**
     * @param bytes      文件字节数组
     * @param bucket     桶
     * @param objectName 对象名称 24/06/25/video.mp4
     */
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认content-type为未知二进制流
        if (objectName.contains(".")) { // 判断对象名是否包含 .
            // 有 . 则划分出扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            // 根据扩展名得到content-type，如果为未知扩展名，例如 .abc之类的东西，则会返回null
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            // 如果得到了正常的content-type，则重新赋值，覆盖默认类型
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new JwbException("上传到文件系统出错");
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        // 数据库中不存在，则直接返回false 表示不存在
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }
        // 若数据库中存在，根据数据库中的文件信息，继续判断bucket中是否存在
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build());
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的MD5
     * @param chunkIndex 分块序号
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 获取分块目录 1/f/1f229319d6fed3431d2f9d06193a433b/chunk/
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 获取分块的具体路径 1/f/1f229319d6fed3431d2f9d06193a433b/chunk/2
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        try {
            // 判断分块是否存在
            InputStream inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(bucket_files)
                    .object(chunkFilePath)
                    .build());
            // 不存在返回false
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }
        return RestResponse.success();
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }
}
