package com.co.ucentral.gestionResiduos.back.Geography.neighborhood;

import com.co.ucentral.gestionResiduos.back.Geography.District.District;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "neighborhoods")
public class Neighborhood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "neighborhoodid")
    public int neighborhoodId;
    @Column(name = "name")
    public String name;
    @ManyToOne
    @JoinColumn(name = "districtid", nullable = false)
    public District districtId;

}
