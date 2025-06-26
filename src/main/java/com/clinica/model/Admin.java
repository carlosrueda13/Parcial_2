package com.clinica.model;
import com.google.cloud.firestore.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import com.clinica.firebase.*;
import com.google.api.core.ApiFuture;

import java.util.ArrayList;
import java.util.List;


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

    public String crearDoctor(Doctor doctor) {
        Firestore db = FirebaseInitializer.getDatabase();
        CollectionReference doctorsRef = db.collection("doctores");

        try {
            // Verificar si ya existe el médico con ese ID (documento)
            DocumentReference docRef = doctorsRef.document(doctor.getId());
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return "❌ El doctor con ID " + doctor.getId() + " ya está registrado.";
            }

            // Crear el documento si no existe
            ApiFuture<WriteResult> result = docRef.set(doctor);
            result.get(); // Esperar que se complete

            return "✅ Doctor creado exitosamente con ID " + doctor.getId();

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al registrar el doctor: " + e.getMessage();
        }
    }

    public void createPatient(UserDTO data) {
        try {
            Firestore db = FirebaseInitializer.getDatabase();
            DocumentReference docRef = db.collection("patients").document(data.getId());

            // Verificar si ya existe
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                System.out.println("⚠️ El paciente con ID " + data.getId() + " ya existe.");
                return;
            }

            // Crear nuevo paciente desde DTO
            Patient patient = Patient.builder()
                    .id(data.getId())
                    .name(data.getName())
                    .phone(data.getPhone())
                    .medRecord(data.getMedRecord()) // si aplica
                    .build();

            // Guardar en Firestore
            ApiFuture<WriteResult> result = docRef.set(patient);
            System.out.println("✅ Paciente creado en: " + result.get().getUpdateTime());

        } catch (Exception e) {
            System.err.println("❌ Error al crear paciente: " + e.getMessage());
        }
    }

    public void createConsult(Consult consult) {
        Firestore db = FirebaseInitializer.getDatabase();
        try {
            // 🔍 Verificar que el paciente existe
            DocumentReference patientRef = db.collection("patients").document(consult.getPatient().getId());
            ApiFuture<DocumentSnapshot> future = patientRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                System.out.println("❌ El paciente con ID " + consult.getPatient().getId() + " no existe. No se puede crear la consulta.");
                return;
            }

            // 🎯 Obtener el paciente como objeto Java
            Patient patient = document.toObject(Patient.class);
            if (patient.getConsults() == null) {
                patient.setConsults(new ArrayList<>());
            }

            // ➕ Agregar la nueva consulta a la lista
            patient.getConsults().add(consult);


            // ✅ Guardar la consulta en la colección "consults"
            ApiFuture<WriteResult> consultResult = db.collection("consults")
                    .document(consult.getId())
                    .set(consult);

            // ✅ Actualizar el paciente en la base de datos
            ApiFuture<WriteResult> patientResult = db.collection("patients")
                    .document(patient.getId())
                    .set(patient);
            MedRecord.manageMedRecord(patient, consult);
            System.out.println("📌 Lista de consultas del paciente actualizada: " + patientResult.get().getUpdateTime());



            System.out.println("✅ Consulta creada con ID: " + consult.getId());
            System.out.println("📌 Lista de consultas del paciente actualizada: " + patientResult.get().getUpdateTime());

        } catch (Exception e) {
            System.out.println("❌ Error al crear la consulta y actualizar al paciente: " + e.getMessage());
        }
    }

    @Override
    public void updateConsult(Consult consult) {
        Firestore db = FirebaseInitializer.getDatabase();
        try {
            String consultId = consult.getId();

            DocumentReference consultRef = db.collection("consults").document(consultId);
            ApiFuture<DocumentSnapshot> future = consultRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                System.out.println("❌ La consulta con ID " + consultId + " no existe.");
                return;
            }

            // 🔁 Sobrescribir toda la consulta con los nuevos datos
            ApiFuture<WriteResult> result = consultRef.set(consult);
            System.out.println("✅ Consulta actualizada exitosamente en: " + result.get().getUpdateTime());

            // 📘 Actualizar la consulta en el MedRecord del paciente
            String patientId = consult.getPatient().getId();
            DocumentReference medRecordRef = db.collection("medRecords").document(patientId);
            DocumentSnapshot medRecordDoc = medRecordRef.get().get();

            if (medRecordDoc.exists()) {
                MedRecord record = medRecordDoc.toObject(MedRecord.class);
                record.updateConsultInRecord(consult);
                medRecordRef.set(record);
                System.out.println("📘 MedRecord actualizado con la nueva versión de la consulta.");
            } else {
                System.out.println("❌ No se encontró un MedRecord asociado al paciente " + patientId);
            }

        } catch (Exception e) {
            System.out.println("❌ Error al actualizar la consulta: " + e.getMessage());
        }
    }

    @Override
    public void listConsultsByPatient(String patientId) {
        Firestore db = FirebaseInitializer.getDatabase();

        try {
            // 🔍 Consultar todas las consultas donde el paciente tenga el ID solicitado
            ApiFuture<QuerySnapshot> future = db.collection("consults")
                    .whereEqualTo("patient.id", patientId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.out.println("ℹ️ No hay consultas para el paciente con ID: " + patientId);
                return;
            }

            System.out.println("📝 Consultas del paciente con ID: " + patientId);
            for (QueryDocumentSnapshot doc : documents) {
                Consult consult = doc.toObject(Consult.class);

                System.out.println("--------------------------------------------");
                System.out.println("🗓 Fecha: " + consult.getDate());
                System.out.println("👨‍⚕️ Doctor: " + consult.getDoctor().getName());
                System.out.println("🩺 Especialidad: " + consult.getDoctor().getSpecialty());
                System.out.println("🤒 Síntomas: " + consult.getSymptoms());
                System.out.println("🔬 Diagnóstico: " + consult.getDiagnosis());
                System.out.println("💊 Tratamiento: " + consult.getTreatment());
            }

        } catch (Exception e) {
            System.out.println("❌ Error al obtener consultas del paciente: " + e.getMessage());
        }
    }

    @Override
    public void getMedRecordDetails(Patient patient) {
        Firestore db = FirebaseInitializer.getDatabase();

        try {
            // 🔍 Buscar el MedRecord del paciente
            DocumentSnapshot recordDoc = db.collection("medrecords")
                    .document(patient.getId())
                    .get()
                    .get();

            if (!recordDoc.exists()) {
                System.out.println("❌ No se encontró historia clínica para el paciente con ID: " + patient.getId());
                return;
            }

            MedRecord medRecord = recordDoc.toObject(MedRecord.class);

            if (medRecord.getConsults() == null || medRecord.getConsults().isEmpty()) {
                System.out.println("📭 El paciente no tiene consultas registradas en la historia clínica.");
                return;
            }

            System.out.println("🩺 Historia clínica del paciente: " + patient.getName() + " (ID: " + patient.getId() + ")");
            System.out.println("Total de consultas: " + medRecord.getConsults().size());

            for (Consult consult : medRecord.getConsults()) {
                System.out.println("─────────────────────────────────────────────");
                System.out.println("📅 Fecha de la consulta: " + consult.getDate());
                System.out.println("👨‍⚕️ Doctor: " + (consult.getDoctor() != null ? consult.getDoctor().getName() : "Desconocido"));
                System.out.println("🔬 Especialidad: " + (consult.getDoctor() != null ? consult.getDoctor().getSpecialty() : "Desconocida"));
                System.out.println("🤕 Síntomas: " + consult.getSymptoms());
                System.out.println("🩺 Diagnóstico: " + consult.getDiagnosis());
                System.out.println("💊 Tratamiento: " + consult.getTreatment());
            }

        } catch (Exception e) {
            System.out.println("❌ Error al obtener el MedRecord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void listConsultsByDoctor(String doctorId) {
        Firestore db = FirebaseInitializer.getDatabase();

        try {
            // 🔍 Consultar todas las consultas donde el doctor tenga el ID solicitado
            ApiFuture<QuerySnapshot> future = db.collection("consults")
                    .whereEqualTo("doctor.id", doctorId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.out.println("ℹ️ No hay consultas para el doctor con ID: " + doctorId);
                return;
            }

            System.out.println("🩺 Consultas del doctor con ID: " + doctorId);
            for (QueryDocumentSnapshot doc : documents) {
                Consult consult = doc.toObject(Consult.class);

                System.out.println("--------------------------------------------");
                System.out.println("🗓 Fecha: " + consult.getDate());
                System.out.println("👤 Paciente: " + consult.getPatient().getName());
                System.out.println("🆔 ID Paciente: " + consult.getPatient().getId());
                System.out.println("🤒 Síntomas: " + consult.getSymptoms());
                System.out.println("🔬 Diagnóstico: " + consult.getDiagnosis());
                System.out.println("💊 Tratamiento: " + consult.getTreatment());
            }

        } catch (Exception e) {
            System.out.println("❌ Error al obtener consultas del doctor: " + e.getMessage());
        }
    }

}
