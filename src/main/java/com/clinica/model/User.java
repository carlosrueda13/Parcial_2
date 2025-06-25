package com.clinica.model;
import com.clinica.common.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    String id;
    String name;
    String email;
    String password;
    UserType tipo;
}
