package com.universita.segreteria.mapper;

import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class VotoMapper {
    public static VotoDTO convertiInDTO(Voto voto) {
        return VotoDTO.builder().id(voto.getId()).studenteId(voto.getStudente().getId()).studenteMatricola(voto.getStudente().getMatricola())
                .studenteNome(voto.getStudente().getNome())
                .studenteCognome(voto.getStudente().getCognome()).esameId(voto.getEsame().getId())
                .esameNome(voto.getEsame().getNome()).voto(voto.getVoto()).stato(voto.getStato()).build();
    }

    public static List<VotoDTO> convertListToDTO(List<Voto> voti) {
        if (voti == null) return Collections.emptyList();

        return voti.stream().map(VotoMapper::convertiInDTO).collect(Collectors.toList());
    }

    public static Voto convertiInEntity(VotoDTO dto) {
        Voto voto = new Voto();
        voto.setId(dto.getId());
        voto.setVoto(dto.getVoto());
        voto.setStato(dto.getStato());
        return voto;
    }

}
