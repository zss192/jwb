package com.jwb.checkcode.service;

import com.jwb.checkcode.model.CheckCodeParamsDto;
import com.jwb.checkcode.model.CheckCodeResultDto;

/**
 * @description 验证码接口
 */
public interface CheckCodeService {


    /**
     * @param checkCodeParamsDto 生成验证码参数
     * @return com.jwb.checkcode.model.CheckCodeResultDto 验证码结果
     * @description 生成验证码
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     * @param key
     * @param code
     * @return boolean
     * @description 校验验证码
     */
    public boolean verify(String key, String code);


    /**
     * @description 验证码生成器
     */
    public interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @return 验证码
         */
        String generate(int length);


    }

    /**
     * @description key生成器
     */
    public interface KeyGenerator {

        /**
         * key生成
         *
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * @description 验证码存储
     */
    public interface CheckCodeStore {

        /**
         * @param key    key
         * @param value  value
         * @param expire 过期时间,单位秒
         * @return void
         * @description 向缓存设置key
         */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
