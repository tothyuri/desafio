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
	private Map<Long,List<Object>> thCPF= new TreeMap<>();
	private List<Object> cpfVoto= new ArrayList<>();
	private Gson gson = new Gson();
	
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
		Optional<Pauta> pauta = pautaRepository.findById(id_pauta);
		if(!th.containsKey(id_pauta)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Sessão em para votação! Resultado da votação: Sim("+pauta.get().getNm_votosim()+") Não("+pauta.get().getNm_votonao()+").");
		}
		if(!th.get(id_pauta).isAlive()) {
			pauta.get().setTp_aberta(false);
			pautaRepository.save(pauta.get());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão finalizada! Resultado da votação: Sim("+pauta.get().getNm_votosim()+") Não("+pauta.get().getNm_votonao()+").");
		}
		Map<?,?> respValidaCPF = null;
		String validaID = urlConnection.postJson("https://user-info.herokuapp.com/users/"+resp.get("cpf_associado"));
		if(validaID.equals("")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "ID CPF inválido");
		}else {
			respValidaCPF = gson.fromJson(validaID, Map.class);
		}
		if(respValidaCPF != null && respValidaCPF.get("status").equals("ABLE_TO_VOTE") && (thCPF.get(id_pauta) != null ? !thCPF.get(id_pauta).contains(resp.get("cpf_associado")): true)) {
			String voto = (String) resp.get("voto");
			cpfVoto.add((String)resp.get("cpf_associado"));
			thCPF.put(id_pauta, cpfVoto);
			if(voto.equalsIgnoreCase("sim")) {
				pauta.get().setNm_votosim(pauta.get().getNm_votosim()+1);
				pautaRepository.save(pauta.get());
			}else if(voto.equalsIgnoreCase("nao")) {
				pauta.get().setNm_votonao(pauta.get().getNm_votonao()+1);
				pautaRepository.save(pauta.get());
			}
			else
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Voto não reconhecido!");
		}else {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "ID CPF não permitido votar!");
		}
	}
}
