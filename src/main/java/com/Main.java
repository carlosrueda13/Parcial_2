package com;

import com.clinica.model.Admin;
import com.clinica.model.Patient;
import com.clinica.firebase.FirebaseInitializer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

public class Main {
    public static void main(String[] args) {
        Firestore db = FirebaseInitializer.getDatabase();

        try {
            // 🔍 Obtener paciente desde Firestore
            String patientId = "123456789";
            DocumentSnapshot patientDoc = db.collection("patients").document(patientId).get().get();

            if (!patientDoc.exists()) {
                System.out.println("❌ Paciente no encontrado con ID: " + patientId);
                return;
            }

            // ✅ Convertir documento a objeto Patient
            Patient patient = patientDoc.toObject(Patient.class);

            // 🧑‍⚕️ Crear instancia de Admin y consultar historia clínica
            Admin admin = new Admin();
            admin.getMedRecordDetails(patient);

        } catch (Exception e) {
            System.out.println("❌ Error al ejecutar la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
