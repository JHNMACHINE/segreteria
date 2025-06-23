package com.universita.segreteria.dto;

import lombok.Data;

@Data
public class CambiaPasswordDTO {
    private String email;
    private String oldPassword;
    private String newPassword;
}
