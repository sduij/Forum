package com.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResetVO {
    @Email
    private String email;

    @Length(min = 6, max = 6)
    private String code;

    @Length(min = 5, max = 20)
    private String password;
}