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
import com.jwb.media.mapper.MediaProcessMapper;
import com.jwb.media.model.dto.QueryMediaParamsDto;
import com.jwb.media.model.dto.UploadFileParamsDto;
import com.jwb.media.model.dto.UploadFileResultDto;
import com.jwb.media.model.po.MediaFiles;
import com.jwb.media.model.po.MediaProcess;
import com.jwb.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Value("${minio.bucket.files}")
    private String bucket_files;
    @Value("${spring.servlet.multipart.max-file-size}")
    private String max_file_size;

    /**
     * 根据日期自动生成目录
     */
    private String getFileFolder() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/");
        return dateFormat.format(new Date());
    }

    /**
     * 根据md5生成文件存放路径 /b/1/b1699957c0abd88ca8c1376d7693d448/chunk/
     *
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 根据MD5和文件扩展名，生成合并后的文件所在路径 /b/1/b1699957c0abd88ca8c1376d7693d448.mp4
     *
     * @param fileMd5   文件MD5
     * @param extension 文件扩展名
     */
    @Override
    public String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
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
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
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
            // 获取源文件名的contentType
            String contentType = getContentType(objectName);
            // 如果是图片格式或者mp4格式，则设置URL属性，否则不设置
            if (contentType.contains("image") || contentType.contains("mp4")) {
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }
            // 查阅数据字典，002003表示审核通过
            mediaFiles.setAuditStatus("002003");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                JwbException.cast("保存文件信息失败");
            }
            // 如果是avi flv mkv视频，则额外添加至视频待处理表
            List<String> videoType = Arrays.asList("video/x-msvideo", "video/x-flv", "video/x-matroska");
            if (videoType.contains(contentType)) {
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles, mediaProcess);
                mediaProcess.setStatus("1"); // 未处理
                mediaProcess.setFailCount(0); // 失败次数默认为0
                int processInsert = mediaProcessMapper.insert(mediaProcess);
                if (processInsert <= 0) {
                    JwbException.cast("保存avi视频到待处理表失败");
                }
            }
        }
        return mediaFiles;
    }

    /**
     * 根据文件字节数组上传文件到MinIO
     *
     * @param bytes      文件字节数组
     * @param bucket     桶
     * @param objectName 对象名称
     */
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        String contentType = getContentType(objectName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("上传到文件系统出错:{}", e.getMessage());
            JwbException.cast("上传到文件系统出错");
        }
    }

    /**
     * 根据本地文件路径上传文件到MinIO
     *
     * @param filePath   本地文件路径
     * @param bucket     桶
     * @param objectName 对象名称
     */
    @Override
    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        String contentType = getContentType(objectName);
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("上传到文件系统出错:{}", e.getMessage());
            JwbException.cast("上传到文件系统出错");
        }
    }

    /**
     * 根据objectName获取对应的MimeType
     *
     * @param objectName 对象名称
     */
    private static String getContentType(String objectName) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认content-type为未知二进制流
        if (objectName.contains(".")) { // 判断对象名是否包含 .
            // 有 .  则划分出扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            // 根据扩展名得到content-type，如果为未知扩展名，例如 .abc之类的东西，则会返回null
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            // 如果得到了正常的content-type，则重新赋值，覆盖默认类型
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5, String fileSize) {
        // 首先检查文件大小是否超过最大限制
        int uploadFileSize = Integer.parseInt(fileSize);
        int maxFileSize = Integer.parseInt(max_file_size.substring(0, max_file_size.length() - 2)) * 1024 * 1024;
        if (uploadFileSize > maxFileSize) {
            JwbException.cast("文件大小超过限制，最大为：" + max_file_size);
        }
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

    /**
     * 上传分块
     *
     * @param fileMd5 文件MD5
     * @param chunk   分块序号
     * @param bytes   文件字节
     */
    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        // 分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        try {
            addMediaFilesToMinIO(bytes, bucket_files, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.error("上传分块文件：{}失败：{}", chunkFilePath, e.getMessage());
        }
        return RestResponse.validfail("上传文件失败", false);
    }

    /**
     * 从Minio中根据md5下载视频对应的所有分块文件
     *
     * @param fileMd5    文件的MD5
     * @param chunkTotal 总块数
     * @return 分块文件数组
     */
    private File[] downloadFileFromMinio(String fileMd5, int chunkTotal) {
        // 作为结果返回
        File[] files = new File[chunkTotal];
        // 获取分块文件目录
        String chunkFileFolder = getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            // 获取分块文件路径 1/f/1f229319d6fed3431d2f9d06193a433b/chunk/2
            String chunkFilePath = chunkFileFolder + i;
            File chunkFile = null;
            try {
                // 创建临时的分块文件
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (Exception e) {
                JwbException.cast("创建临时分块文件出错：" + e.getMessage());
            }
            // 下载分块文件
            downloadChunkFromMinio(chunkFile, bucket_files, chunkFilePath);
            // 组成结果
            files[i] = chunkFile;
        }
        return files;
    }

    /**
     * 从Minio中下载某一个分块文件到目标文件中
     *
     * @param file       目标文件
     * @param bucket     桶
     * @param objectName 桶内文件路径
     */
    public void downloadChunkFromMinio(File file, String bucket, String objectName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             InputStream inputStream = minioClient.getObject(GetObjectArgs
                     .builder()
                     .bucket(bucket)
                     .object(objectName)
                     .build())) {
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            log.error("下载分块文件：{}失败：{}", objectName, e.getMessage());
        }
    }

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     */
    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 1.合并文件
        // 下载分块文件
        File[] chunkFiles = downloadFileFromMinio(fileMd5, chunkTotal);
        // 获取源文件名
        String fileName = uploadFileParamsDto.getFilename();
        // 获取源文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        // 创建出临时文件，准备合并
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile(fileName, extension);
        } catch (IOException e) {
            JwbException.cast("创建合并临时文件出错");
        }
        try {
            // 缓冲区
            byte[] buffer = new byte[1024];
            // 写入流，向临时文件写入
            try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {
                // 遍历分块文件数组
                for (File chunkFile : chunkFiles) {
                    // 读取流，读分块文件
                    try (RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r")) {
                        int len;
                        while ((len = raf_read.read(buffer)) != -1) {
                            raf_write.write(buffer, 0, len);
                        }
                    }
                }
            } catch (Exception e) {
                JwbException.cast("合并文件过程中出错");
            }
            uploadFileParamsDto.setFileSize(mergeFile.length());

            // 2.对合并后的文件通过MD5值进行校验
            try (FileInputStream mergeInputStream = new FileInputStream(mergeFile)) {
                String mergeMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(mergeInputStream);
                // 前端传过来的md5和合并后文件的md5不等则检验失败
                if (!fileMd5.equals(mergeMd5)) {
                    log.error("合并文件校验失败，原文件md5：{}，合并后md5：{}", fileMd5, mergeMd5);
                    JwbException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过：{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                JwbException.cast("合并文件校验异常");
            }

            // 3.将本地合并好的文件，上传到minio中
            String mergeFilePath = getFilePathByMd5(fileMd5, extension);
            // 这里重载了addMediaFilesToMinIO方法
            addMediaFilesToMinIO(mergeFile.getAbsolutePath(), bucket_files, mergeFilePath);
            log.debug("合并文件上传至MinIO完成{}", mergeFile.getAbsolutePath());

            // 4.将文件信息写入数据库
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, uploadFileParamsDto, mergeFilePath, fileMd5, bucket_files);
            if (mediaFiles == null) {
                JwbException.cast("媒资文件入库出错");
            }
            log.debug("媒资文件入库完成");
            return RestResponse.success();
        } finally {
            // 5.删除临时文件
            for (File chunkFile : chunkFiles) {
                if (!chunkFile.delete()) {
                    log.error("临时分块文件删除错误：{}", chunkFile.getPath());
                }
            }
            if (!mergeFile.delete()) {
                log.error("临时合并文件删除错误：{}", mergeFile.getPath());
            }
        }
    }

    @Override
    public MediaFiles getFileById(String id) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(id);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            JwbException.cast("视频还没有转码处理");
        }
        return mediaFiles;
    }
}
