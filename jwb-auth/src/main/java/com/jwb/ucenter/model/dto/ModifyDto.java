package com.jwb.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyDto {
    String id;

    String cellphone;

    String email;

    String username;

    String nickname;

    String userpic;

    String sex;

    String source;

    LocalDate birthday;
}