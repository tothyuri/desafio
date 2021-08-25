package com.desafio.crm.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.crm.model.Pauta;
import com.desafio.crm.repository.PautaRepository;
import com.google.gson.Gson;

import helps.urlConnection;

@RestController
@RequestMapping("/votacao")
public class VotacaoController {
	
	@Autowired
	private PautaRepository pautaRepository;
	
	private Thread t1=null;
	private List<Long> pautas = new ArrayList<>();
	protected Gson gson = new Gson();
	
	@GetMapping
	public List<Pauta> listar() {
		return pautaRepository.findAll();
	}

	@PostMapping("/abrirsessao")
	public String abrirSessao(@RequestBody String dados) {
		Map<String, Object> retorno = new TreeMap<>();
		Map<?,?> resp = gson.fromJson(dados, Map.class);
		Long id_pauta = null;
		if(resp.get("id_pauta")!= null) {
			if(resp.get("id_pauta").getClass().toString().contains("String"))
				id_pauta = Long.parseLong(resp.get("id_pauta").toString());
			else
				return "Formato não reconhecido para o campo 'id_pauta', tente enviar como String";
		}else {
			return "id_pauta não encontrado";
		}
		int tempo = 1;
		if(resp.get("tempo")!= null) {
			if(resp.get("tempo").getClass().toString().contains("String"))
				tempo = Integer.parseInt(resp.get("tempo").toString());
			else
				return "Formato não reconhecido para o campo 'tempo', tente enviar como String";
		}else {
			return "tempo não encontrado";
		}
		Pauta pauta = pautaRepository.getById(id_pauta);
		if(pauta.getTp_aberta() && !pautas.contains(pauta.getId_pauta())) {
			pautas.add(pauta.getId_pauta());
			int timer = tempo*60;
			t1 = new Thread(new Runnable() {
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(timer);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			t1.start();
		}else {
			return "Sessão finalizada";
		}
		gson.toJson(retorno);
		return "Sessão aberta";
	}
	
	@PostMapping(value = "/votar")
	public String votar(@RequestBody String dados) {
		Map<String, Object> retorno = new TreeMap<>();
		Map<?,?> resp = gson.fromJson(dados, Map.class);
		
		if(t1 == null) {
			return "Não há sessão em aberto";			
		}
		
		Long id_pauta = null;
		if(resp.get("id_pauta")!= null) {
			if(resp.get("id_pauta").getClass().toString().contains("String"))
				id_pauta = Long.parseLong(resp.get("id_pauta").toString());
			else
				return "Formato não reconhecido para o campo 'id_pauta', tente enviar como String";
		}else {
			return "id_pauta não encontrado";
		}
		Pauta pauta = pautaRepository.getById(id_pauta);
		if(!t1.isAlive()) {
			pauta.setTp_aberta(false);
			pautaRepository.save(pauta);
		}
		Map<?,?> respValidaCPF = null;
		String validaID = urlConnection.postJson("https://user-info.herokuapp.com/users/"+resp.get("cpf_associado"));
		if(validaID.equals("")) {
			return "ID CPF inválido";
		}else {
			respValidaCPF = gson.fromJson(validaID, Map.class);
		}
		if(respValidaCPF != null && respValidaCPF.get("status").equals("ABLE_TO_VOTE")) {
			String voto = (String) resp.get("voto");
			PautaController pautacontroller = new PautaController();
			if(voto.equalsIgnoreCase("sim")) {
				pautacontroller.votoSim(pauta);
			}else if(voto.equalsIgnoreCase("nao")) {
				pautacontroller.votoNao(pauta);
			}else {
				return "voto não reconhecido";
			}
			
		}
		return validaID;
	}
}
