package com.clinica.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consult {
    String id;
    Patient patient;
    Doctor doctor;
    String date;
    MedRecord medRecord;
    String symptoms;
    String diagnosis;
    String treatment;

}
