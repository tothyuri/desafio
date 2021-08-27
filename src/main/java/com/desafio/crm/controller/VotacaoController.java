package com.desafio.crm.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.google.gson.Gson;

import helps.urlConnection;

@RestController
@RequestMapping("/votacao")
public class VotacaoController {
	
	@Autowired
	private PautaRepository pautaRepository;
	
	private Map<Long,Thread> th= new TreeMap<>();
	protected Gson gson = new Gson();
	
	@GetMapping
	public List<Pauta> listar() {
		return pautaRepository.findAll();
	}

	@PostMapping("/abrirsessao")
	@ResponseStatus(value = HttpStatus.CREATED, reason = "Sessão Aberta!")
	public void abrirSessao(@RequestBody String dados) {
		Map<?,?> resp = gson.fromJson(dados, Map.class);
		Long id_pauta = null;
		if(resp.get("id_pauta")!= null) {
			if(resp.get("id_pauta").getClass().toString().contains("String"))
				id_pauta = Long.parseLong(resp.get("id_pauta").toString());
			else
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Formato não reconhecido para o campo 'id_pauta', tente enviar como String!");
		}else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pauta não encontrado!");
		}
		int tempo = 1;
		if(resp.get("tempo")!= null) {
			if(resp.get("tempo").getClass().toString().contains("String"))
				tempo = Integer.parseInt(resp.get("tempo").toString());
			else
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Formato não reconhecido para o campo 'tempo', tente enviar como String!");
		}else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "tempo não encontrado!");
		}
		Optional<Pauta> pauta = pautaRepository.findById(id_pauta);
		if(!pauta.isEmpty()) {
			if(!pauta.get().getTp_aberta()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão finalizada!");
			}
			if(th.containsKey(id_pauta)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta em sessão!");
			}
			int timer = tempo*60;
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(timer);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			t1.start();
			th.put(id_pauta, t1);
		}else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta não cadastrada!");
		}
	}
	
	@PostMapping(value = "/votar")
	@ResponseStatus(value = HttpStatus.CREATED, reason = "Voto computado!")
	public void votar(@RequestBody String dados) {
		Map<?,?> resp = gson.fromJson(dados, Map.class);
		Long id_pauta = null;
		if(resp.get("id_pauta")!= null) {
			if(resp.get("id_pauta").getClass().toString().contains("String"))
				id_pauta = Long.parseLong(resp.get("id_pauta").toString());
			else
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Formato não reconhecido para o campo 'id_pauta', tente enviar como String!");
		}else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pauta não encontrado!");
		}
		
		if(!th.containsKey(id_pauta)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Sessão fechada para votação!");
		}
		Pauta pauta = pautaRepository.getById(id_pauta);
		if(!th.get(id_pauta).isAlive()) {
			pauta.setTp_aberta(false);
			pautaRepository.save(pauta);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão finalizada!");
		}
		Map<?,?> respValidaCPF = null;
		String validaID = urlConnection.postJson("https://user-info.herokuapp.com/users/"+resp.get("cpf_associado"));
		if(validaID.equals("")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "ID CPF inválido");
		}else {
			respValidaCPF = gson.fromJson(validaID, Map.class);
		}
		if(respValidaCPF != null && respValidaCPF.get("status").equals("ABLE_TO_VOTE")) {
			String voto = (String) resp.get("voto");
			if(voto.equalsIgnoreCase("sim")) {
				pauta.setNm_votosim(pauta.getNm_votosim()+1);
				pautaRepository.save(pauta);
			}else if(voto.equalsIgnoreCase("nao")) {
				pauta.setNm_votonao(pauta.getNm_votonao()+1);
				pautaRepository.save(pauta);
			}
			else
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Voto não reconhecido!");
		}else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "ID CPF não permitido votar!");
		}
	}
}
