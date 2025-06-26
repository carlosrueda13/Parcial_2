package com.clinica.model;

public interface UserInterface {

    boolean login(String username, String password);
    void updateConsult(Consult updatedConsult);
    void getMedRecordDetails(Patient patient);
    void listConsultsByPatient(String patientId);
    void listConsultsByDoctor(String doctorId);
}
