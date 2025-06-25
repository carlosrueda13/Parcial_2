package com.clinica.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class Admin extends  User implements UserInterface{
    String username;
    String password;

    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    @Override
    public void register(UserDTO data) {
        this.id = data.getId();
        this.name = data.getName();
        this.phone = data.getPhone();
        this.username = data.getUsername();
        this.password = data.getPassword();
    }
}
