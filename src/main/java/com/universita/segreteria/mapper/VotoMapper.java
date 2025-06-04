package com.universita.segreteria.mapper;

import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.model.Voto;
import org.springframework.stereotype.Component;

@Component
public final class VotoMapper {
    public static VotoDTO convertiInDTO(Voto voto) {
        return VotoDTO.builder().id(voto.getId()).studenteId(voto.getStudente().getId()).studenteMatricola(voto.getStudente().getMatricola()).studenteNome(voto.getStudente().getNome()).studenteCognome(voto.getStudente().getCognome()).esameId(voto.getEsame().getId()).esameNome(voto.getEsame().getNome()).voto(voto.getVoto()).stato(voto.getStato()).build();
    }

}
