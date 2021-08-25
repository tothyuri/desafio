package com.desafio.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desafio.crm.model.Pauta;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long>{

}
