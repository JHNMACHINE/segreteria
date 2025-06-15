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

        return EsameDTO.builder().id(esame.getId()).nome(esame.getNome()).date(esame.getData()).statoEsame(esame.getStatoEsame()).docenteId(esame.getDocente() != null ? esame.getDocente().getId() : null).build();
    }

    public static Esame fromDTO(EsameDTO dto, Docente docente) {
        if (dto == null) return null;

        return Esame.builder().id(dto.getId()).nome(dto.getNome()).data(dto.getDate()).statoEsame(dto.getStatoEsame()).docente(docente).build();
    }

    public static List<EsameDTO> convertListEsamiToDTO(List<Esame> esami) {
        if (esami == null) return Collections.emptyList();

        return esami.stream().map(EsameMapper::toDTO).collect(Collectors.toList());
    }

    public static List<Esame> convertListDTOToEsami(List<EsameDTO> dtos, Docente docente) {
        if (dtos == null) return Collections.emptyList();

        return dtos.stream().map(dto -> fromDTO(dto, docente)).collect(Collectors.toList());
    }
}
