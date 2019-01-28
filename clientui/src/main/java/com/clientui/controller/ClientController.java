package com.clientui.controller;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.clientui.beans.CommandeBean;
import com.clientui.beans.PaiementBean;
import com.clientui.beans.ProductBean;
import com.clientui.proxies.MicroserviceCommandeProxy;
import com.clientui.proxies.MicroservicePaiementProxy;
import com.clientui.proxies.MicroserviceProduitsProxy;

@Controller
public class ClientController {
	
	@Autowired
	private MicroserviceProduitsProxy ProduitsProxy;
	
	@Autowired
	private MicroserviceCommandeProxy CommandesProxy;
	
	@Autowired
	private MicroservicePaiementProxy PaiementProxy;
	
	/* Etape 1 Liste des produits */
	@RequestMapping("/")
	public String accueil(Model model) {
		List<ProductBean> produits = ProduitsProxy.listeDesProduits();
		model.addAttribute("produits", produits);
		
		return "Accueil";
	}
	
	/* Etape 2 Détail d'un produit */
	@RequestMapping("/details-produit/{id}")
	public String ficheProduit(@PathVariable int id,  Model model){
		ProductBean produit = ProduitsProxy.recupererUnProduit(id);
		model.addAttribute("produit", produit);
		return"FicheProduit";
	}
	
	/* Etape 3 et 4 Commande d'un produit */
	@RequestMapping("/details-produit/commander-produit/{id}")
	public String passerCommande(@PathVariable int id, Model model) {
		
		// Création de la commande
		CommandeBean commande = new CommandeBean();
		commande.setProductId(id);
		commande.setQuantite(1);
		commande.setDateCommande(new Date());
		
		// Création de la commande auprès du microservice de commande
		CommandeBean commandeAjoutee = CommandesProxy.ajouterCommande(commande);
		
		// La commande ne contient qu'un seul produit dont on récupère le prix sinon il faudrait récupérer l'ensemble des prix
		ProductBean produit = ProduitsProxy.recupererUnProduit(id);
		double montant = produit.getPrix();
		
		// Envoie des données à la vue de Paiement de la commande
		model.addAttribute("commande", commandeAjoutee);
		model.addAttribute("montant", montant);
		
		return "Paiement";
	}
	
	/* Etape 5 Commande d'un produit */
	@RequestMapping("/payer-commande/{id}")
	public String payerCommande(@PathVariable int id, Model model) {
		
		// Création du paiement
		PaiementBean paiementTodo = new PaiementBean();
		paiementTodo.setIdCommande(id);
		// Récupération du prix du produit à payer
		ProductBean produit = ProduitsProxy.recupererUnProduit(id);
		double montant = produit.getPrix();
		// Arrondis et passage en integer...
		paiementTodo.setMontant((int)Math.round(montant));
		// Création du numéro de carte
		paiementTodo.setNumeroCarte(numcarte());
		
		// Demande de paiement
		ResponseEntity<PaiementBean> paiement = PaiementProxy.payerUneCommande(paiementTodo);
		Boolean paiementAccepte = false;
		if(paiement.getStatusCode() == HttpStatus.CREATED) {
			paiementAccepte = true;
		}
		model.addAttribute("paiementOk", paiementAccepte);
		
		return "confirmation";
	}

	//Génére une serie de 16 chiffres au hasard pour simuler vaguement une CB
    private Long numcarte() {

        return ThreadLocalRandom.current().nextLong(1000000000000000L,9000000000000000L );
    }
}
