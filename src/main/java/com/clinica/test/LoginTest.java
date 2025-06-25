package com.clinica.test;
import com.clinica.model.Doctor;
import com.clinica.model.UserDTO;

public class LoginTest {

    public static void main(String[] args) {
        // Example usage of the Doctor class
        UserDTO userDTO = new UserDTO();
        userDTO.setId("123");
        userDTO.setName("Dr. Smith");
        userDTO.setPhone("555-1234");
        userDTO.setUsername("drsmith");
        userDTO.setPassword("password123");
        userDTO.setSpecialty("Cardiology");

        // Create a Doctor instance using the UserDTO
        Doctor doctor = Doctor.builder()
                .id(userDTO.getId())
                .name(userDTO.getName())
                .phone(userDTO.getPhone())
                .specialty(userDTO.getSpecialty())
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .build();

        // Simulate a login attempt
        boolean loginSuccess = doctor.login("drsmith", "password123");
        System.out.println("Login successful: " + loginSuccess);

        //Simulate a registration
    }
}
