package com.jwb.ucenter.model.dto;

import lombok.Data;

@Data
public class ModifyPasswordDto {
    String id;
    String password;
    String newPassword;
    String confirmPassowrd;
}
