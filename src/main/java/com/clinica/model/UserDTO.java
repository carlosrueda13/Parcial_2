package com.clinica.model;

import com.clinica.common.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    String id;
    String name;
    String phone;
    String username;
    String password;
    UserType type;
    String specialty; // Only for doctors
    MedRecord medRecord;

}

