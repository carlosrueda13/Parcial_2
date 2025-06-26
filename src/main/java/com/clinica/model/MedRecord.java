package com.clinica.model;

import java.util.ArrayList;
import java.util.List;
import com.clinica.firebase.FirebaseInitializer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedRecord {
    List<Consult> consults;
    Patient patient;

    public static void manageMedRecord(Patient patient, Consult consult) {
        Firestore db = FirebaseInitializer.getDatabase();

        try {
            String patientId = patient.getId();
            DocumentReference medRecordRef = db.collection("medrecords").document(patientId);
            DocumentSnapshot snapshot = medRecordRef.get().get();

            MedRecord medRecord;

            if (!snapshot.exists()) {
                // 🆕 Crear nuevo MedRecord con la consulta actual
                medRecord = new MedRecord();
                medRecord.setPatient(patient);
                List<Consult> newList = new ArrayList<>();
                newList.add(consult);
                medRecord.setConsults(newList);

                System.out.println("📘 Nuevo MedRecord creado para paciente: " + patient.getName());
            } else {
                // 🔄 Actualizar MedRecord existente
                medRecord = snapshot.toObject(MedRecord.class);
                if (medRecord.getConsults() == null) {
                    medRecord.setConsults(new ArrayList<>());
                }
                medRecord.getConsults().add(consult);

                System.out.println("📘 MedRecord actualizado para paciente: " + patient.getName());
            }

            // 💾 Guardar en Firestore
            ApiFuture<WriteResult> result = medRecordRef.set(medRecord);
            System.out.println("💾 Guardado en: " + result.get().getUpdateTime());

        } catch (Exception e) {
            System.out.println("❌ Error en manageMedRecord: " + e.getMessage());
        }
    }

    public void updateConsultInRecord(Consult updatedConsult) {
        if (this.consults == null || this.consults.isEmpty()) {
            System.out.println("⚠️ No hay consultas registradas en el MedRecord.");
            return;
        }

        boolean updated = false;
        for (int i = 0; i < this.consults.size(); i++) {
            Consult existing = this.consults.get(i);
            if (existing.getId().equals(updatedConsult.getId())) {
                this.consults.set(i, updatedConsult);
                updated = true;
                break;
            }
        }

        if (updated) {
            System.out.println("✅ Consulta actualizada correctamente en el MedRecord.");
        } else {
            System.out.println("❌ Consulta con ID " + updatedConsult.getId() + " no encontrada en el MedRecord.");
        }
    }

}
