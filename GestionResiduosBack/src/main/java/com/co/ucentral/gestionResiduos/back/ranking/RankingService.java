package com.co.ucentral.gestionResiduos.back.ranking;

import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;

    /**
     * Obtener el ranking de usuarios activos ordenado por puntaje acumulado (desc).
     * Solo incluye usuarios con estado VERIFICADO y rol CIUDADANO.
     *
     * @param limit Cantidad máxima de usuarios en el ranking (default 50)
     * @return Lista de RankingDTO con posición asignada
     */
    public List<RankingDTO> getRanking(int limit) {
        List<User> users = userRepository.findRankingUsers();

        List<RankingDTO> ranking = new ArrayList<>();
        int position = 1;

        for (User user : users) {
            if (position > limit) break;

            ranking.add(RankingDTO.builder()
                    .position(position)
                    .names(user.getNames())
                    .lastName(user.getLastName())
                    .nickName(user.getNickName())
                    .photo(user.getPhoto())
                    .points(user.getPoints() == null ? 0 : user.getPoints())
                    .neighborhoodName(
                            user.getNeighborhoodId() != null
                                    ? user.getNeighborhoodId().getName()
                                    : "Sin barrio"
                    )
                    .build());

            position++;
        }

        return ranking;
    }
}