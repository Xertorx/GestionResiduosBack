package com.co.ucentral.gestionResiduos.back.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingDTO {
    private int position;
    private String names;
    private String lastName;
    private String nickName;
    private String photo;
    private int points;
    private String neighborhoodName;
}