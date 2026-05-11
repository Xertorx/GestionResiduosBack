package com.co.ucentral.gestionResiduos.back.Geography.District;

import com.co.ucentral.gestionResiduos.back.Geography.City.City;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "districts")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "districtid")
    public int districtId;
    @Column(name = "name")
    public String name;
    @Column(name = "code")
    public String code;
    @ManyToOne
    @JoinColumn(name = "cityid", nullable = false)
    public City cityId;
}

//