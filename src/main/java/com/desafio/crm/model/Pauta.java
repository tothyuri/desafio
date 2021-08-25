package com.desafio.crm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Pauta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_pauta;
	
	@Column(nullable = false, unique = true)
	private String ds_pauta;
	
	@Column
	private String ds_comentario;
	
	@Column
	private Boolean tp_aberta;
	
	@Column
	private int nm_votosim;
	
	@Column
	private int nm_votonao;

	public String getDs_comentario() {
		return ds_comentario;
	}

	public void setDs_comentario(String ds_comentario) {
		this.ds_comentario = ds_comentario;
	}

	public int getNm_votosim() {
		return nm_votosim;
	}

	public void setNm_votosim(int nm_votosim) {
		this.nm_votosim = nm_votosim;
	}

	public int getNm_votonao() {
		return nm_votonao;
	}

	public void setNm_votonao(int nm_votonao) {
		this.nm_votonao = nm_votonao;
	}

	public Long getId_pauta() {
		return id_pauta;
	}

	public void setId_pauta(Long id_pauta) {
		this.id_pauta = id_pauta;
	}

	public String getDs_pauta() {
		return ds_pauta;
	}

	public void setDs_pauta(String ds_pauta) {
		this.ds_pauta = ds_pauta;
	}

	public Boolean getTp_aberta() {
		return tp_aberta;
	}

	public void setTp_aberta(Boolean tp_aberta) {
		this.tp_aberta = tp_aberta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id_pauta == null) ? 0 : id_pauta.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pauta other = (Pauta) obj;
		if (id_pauta == null) {
			if (other.id_pauta != null)
				return false;
		} else if (!id_pauta.equals(other.id_pauta))
			return false;
		return true;
	}
}
