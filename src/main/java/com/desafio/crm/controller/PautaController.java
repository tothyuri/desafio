package com.desafio.crm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.crm.model.Pauta;
import com.desafio.crm.repository.PautaRepository;

@RestController
@RequestMapping("/pauta")
public class PautaController {
	
	@Autowired
	private PautaRepository pautaRepository;
	
	@GetMapping
	public List<Pauta> listar() {
		return pautaRepository.findAll();
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Pauta adicionar(@RequestBody Pauta pauta) {
		pauta.setTp_aberta(true);
		return pautaRepository.save(pauta);
	}
	
	@DeleteMapping(value = "/pauta/{id}")
	public List<Pauta> excluir(@RequestParam Long id) {
		pautaRepository.deleteById(id);
		return pautaRepository.findAll();
	}
	
	@DeleteMapping()
	public List<Pauta> excluir(@RequestBody Pauta pauta) {
		pautaRepository.delete(pauta);
		return pautaRepository.findAll();
	}

}
