package com.clinica.model;
import com.google.cloud.firestore.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;
import com.google.api.core.ApiFuture;
import com.clinica.firebase.FirebaseInitializer;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class Doctor extends User implements UserInterface {
    String specialty;
    String username;
    String password;
    List<Consult> consults;

    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
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
