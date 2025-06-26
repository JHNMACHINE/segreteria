package com.universita.segreteria.repository;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudenteRepository extends JpaRepository<Studente, Long> {
    Optional<Studente> findByMatricola(String matricola);
    List<Studente> findByNomeAndCognome(String nome, String cognome);

    Optional<Studente> findByEmail(String email);
    List<Studente> findByEsamiNome(String nomeEsame);

    boolean existsByEmail(String email);

    @Query("SELECT s.matricola FROM Studente s WHERE s.matricola LIKE CONCAT(:prefix, '%')")
    List<String> findAllMatricoleByPrefix(@Param("prefix") String prefix);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM studente_esame WHERE esame_id = :esameId", nativeQuery = true)
    void removeEsameFromStudentiPrenotati(@Param("esameId") Long esameId);


}
