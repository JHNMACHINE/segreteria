package com.universita.segreteria.mapper;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.model.Docente;
import com.universita.segreteria.model.Esame;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class EsameMapper {
    public static EsameDTO toDTO(Esame esame) {
        if (esame == null) return null;

        return EsameDTO.builder().id(esame.getId()).nome(esame.getNome()).date(esame.getData()).statoEsame(esame.getStatoEsame()).docenteId(esame.getDocente() != null ? esame.getDocente().getId() : null).aula(esame.getAula()).build();
    }

    public static List<EsameDTO> convertListEsamiToDTO(List<Esame> esami) {
        if (esami == null) return Collections.emptyList();

        return esami.stream().map(EsameMapper::toDTO).collect(Collectors.toList());
    }
}
