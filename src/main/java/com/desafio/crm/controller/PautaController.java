package com.desafio.crm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	@ResponseStatus(value = HttpStatus.CREATED, reason = "Criado com sucesso!")
	public Pauta adicionar(@RequestBody Pauta pauta) {
		ExampleMatcher modelMatcher = ExampleMatcher.matching()
				  .withIgnorePaths("id_pauta") 
				  .withMatcher("ds_pauta",GenericPropertyMatchers.ignoreCase());
		Pauta probe = new Pauta();
		probe.setDs_pauta(pauta.getDs_pauta());
		Example<Pauta> example = Example.of(probe, modelMatcher);
		
		if(!pautaRepository.exists(example)) {
			pauta.setTp_aberta(true);
			return pautaRepository.save(pauta);
		}else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Descrição da pauta(ds_pauta) ambíguo!");
		}
	}
	
	@DeleteMapping(value = "/pauta/{id}")
	@ResponseStatus(value = HttpStatus.OK, reason = "Excluido com sucesso!")
	public List<Pauta> excluir(@RequestParam Long id) {
		pautaRepository.deleteById(id);
		return pautaRepository.findAll();
	}
	
	@DeleteMapping()
	@ResponseStatus(value = HttpStatus.OK, reason = "Excluido com sucesso!")
	public List<Pauta> excluir(@RequestBody Pauta pauta) {
		pautaRepository.delete(pauta);
		return pautaRepository.findAll();
	}

}
