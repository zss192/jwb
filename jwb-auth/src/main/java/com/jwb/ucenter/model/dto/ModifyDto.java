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

    String name;

    String userpic;

    String sex;

    LocalDate birthday;
}