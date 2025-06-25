package com.clinica.model;

public interface UserInterface {

    boolean login(String username, String password);
    void register(UserDTO data);
}
